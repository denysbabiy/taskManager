package com.task.manager.service.impl;

import java.util.List;
import java.util.Optional;

import com.task.manager.domain.Task;
import com.task.manager.domain.TaskStatus;
import com.task.manager.dto.CreateTaskDto;
import com.task.manager.dto.UpdateTaskDto;
import com.task.manager.dto.UpdateTaskStatusDto;
import com.task.manager.event.TaskCreatedEvent;
import com.task.manager.exception.TaskInProgressException;
import com.task.manager.exception.TaskNotFoundException;
import com.task.manager.mapper.TaskMapper;
import com.task.manager.repository.TaskRepository;
import com.task.manager.service.TaskBatchProcessorService;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    private static final int BATCH_SIZE = 100;
    private static final Long ID_1 = 1L;
    private static final Long ID_2 = 2L;
    private static final Long ASSIGNEE_ID_1 = 123L;
    private static final Long ASSIGNEE_ID_2 = 456L;
    private static final String ID_STRING = String.valueOf(ID_1);
    private static final String TITLE = "Test Task";
    private static final String DESCRIPTION = "New Description";
    private static final String KAFKA_TOPIC_NAME = "task-created";
    private static final TaskStatus TODO_TASK_STATUS = TaskStatus.TODO;
    private static final TaskStatus PAUSED_TASK_STATUS = TaskStatus.PAUSED;
    private static final TaskStatus IN_PROGRESS_TASK_STATUS = TaskStatus.IN_PROGRESS;

    private static final String TASK_NOT_FOUND_EXCEPTION_MESSAGE = "Task not found by id: 1";
    private static final String NEW_USER_ALREADY_HAS_TASK_IN_PROGRESS  = "New user already has a task in progress";
    private static final String CURRENT_USER_ALREADY_HAS_A_TASK_IN_PROGRESS_EXCEPTION_MESSAGE = "Current user already has a task in progress";

    @Mock
    private Task task1;

    @Mock
    private Task task2;

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private TaskBatchProcessorService taskBatchProcessorService;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private KafkaTemplate<String, TaskCreatedEvent> kafkaTemplate;

    @Captor
    private ArgumentCaptor<TaskCreatedEvent> taskCreatedEventCaptor;

    @InjectMocks
    private TaskServiceImpl testingInstance;

    @Test
    public void shouldCreateTask() {
        var createTaskDto = new CreateTaskDto();
        createTaskDto.setTitle(TITLE);
        when(taskMapper.fromCreateTaskDto(createTaskDto)).thenReturn(task1);
        when(taskRepository.save(task1)).thenReturn(task1);
        when(task1.getId()).thenReturn(ID_1);
        when(task1.getAssigneeId()).thenReturn(ASSIGNEE_ID_1);
        when(task1.getTitle()).thenReturn(TITLE);

        var result = testingInstance.createTask(createTaskDto);

        verify(taskMapper).fromCreateTaskDto(createTaskDto);
        verify(taskRepository).save(task1);
        verify(task1, times(3)).getId();
        verify(task1).getAssigneeId();
        verify(task1).getTitle();
        verify(kafkaTemplate).send(eq(KAFKA_TOPIC_NAME), eq(ID_STRING), taskCreatedEventCaptor.capture());
        var taskCreatedEvent = taskCreatedEventCaptor.getValue();
        assertThat(taskCreatedEvent.getTaskId(), is(ID_1));
        assertThat(taskCreatedEvent.getAssigneeId(), is(ASSIGNEE_ID_1));
        assertThat(taskCreatedEvent.getTaskTitle(), is(TITLE));

        assertThat(result, is(ID_1));
    }

    @Test
    public void shouldDeleteTask() {
        testingInstance.deleteTask(ID_1);

        verify(taskRepository).deleteById(ID_1);
    }

    @Test
    public void shouldUpdateTaskStatus() {
        var taskOptional = Optional.of(task1);
        var updateTaskStatusDto = new UpdateTaskStatusDto();
        updateTaskStatusDto.setStatus(IN_PROGRESS_TASK_STATUS);
        when(taskRepository.findById(ID_1)).thenReturn(taskOptional);
        when(task1.getStatus()).thenReturn(TODO_TASK_STATUS);
        when(task1.getAssigneeId()).thenReturn(ASSIGNEE_ID_1);
        when(taskRepository.findByAssigneeIdAndStatus(ASSIGNEE_ID_1, IN_PROGRESS_TASK_STATUS)).thenReturn(Optional.empty());

        testingInstance.updateTaskStatus(ID_1, updateTaskStatusDto);

        verify(taskRepository).findById(ID_1);
        verify(task1).getStatus();
        verify(task1).getAssigneeId();
        verify(taskRepository).findByAssigneeIdAndStatus(ASSIGNEE_ID_1, IN_PROGRESS_TASK_STATUS);
        verify(task1).startProgress();
        verify(taskMapper).updateTaskStatus(task1, updateTaskStatusDto);
        verify(taskRepository).save(task1);
        verify(task1, never()).endProgress();
    }

    @Test
    public void shouldUpdateTaskStatusFromInProgressToPaused() {
        var taskOptional = Optional.of(task1);
        var updateTaskStatusDto = new UpdateTaskStatusDto();
        updateTaskStatusDto.setStatus(PAUSED_TASK_STATUS);
        when(taskRepository.findById(ID_1)).thenReturn(taskOptional);
        when(task1.getStatus()).thenReturn(IN_PROGRESS_TASK_STATUS);

        testingInstance.updateTaskStatus(ID_1, updateTaskStatusDto);

        verify(taskRepository).findById(ID_1);
        verify(task1).getStatus();
        verify(task1).endProgress();
        verify(taskMapper).updateTaskStatus(task1, updateTaskStatusDto);
        verify(taskRepository).save(task1);
        verify(task1, never()).startProgress();
        verify(task1, never()).getAssigneeId();
        verify(taskRepository, never()).findByAssigneeIdAndStatus(anyLong(), any(TaskStatus.class));
    }

    @Test
    public void shouldNotUpdateTaskStatusWhenNewStatusIsTheSameAsCurrentStatus() {
        var taskOptional = Optional.of(task1);
        var updateTaskStatusDto = new UpdateTaskStatusDto();
        updateTaskStatusDto.setStatus(TODO_TASK_STATUS);
        when(taskRepository.findById(ID_1)).thenReturn(taskOptional);
        when(task1.getStatus()).thenReturn(TODO_TASK_STATUS);

        testingInstance.updateTaskStatus(ID_1, updateTaskStatusDto);

        verify(taskRepository).findById(ID_1);
        verifyNoMoreInteractions(task1);
        verifyNoMoreInteractions(taskRepository);
        verifyNoInteractions(taskMapper);
    }

    @Test
    public void shouldNotUpdateTaskToInProgressWhenUserAlreadyHasATaskInProgress() {
        var taskOptional1 = Optional.of(task1);
        var taskOptional2 = Optional.of(task2);
        var updateTaskStatusDto = new UpdateTaskStatusDto();
        updateTaskStatusDto.setStatus(IN_PROGRESS_TASK_STATUS);
        when(taskRepository.findById(ID_1)).thenReturn(taskOptional1);
        when(task1.getStatus()).thenReturn(TODO_TASK_STATUS);
        when(task1.getAssigneeId()).thenReturn(ASSIGNEE_ID_1);
        when(task1.getId()).thenReturn(ID_1);
        when(task2.getId()).thenReturn(ID_2);
        when(taskRepository.findByAssigneeIdAndStatus(ASSIGNEE_ID_1, IN_PROGRESS_TASK_STATUS)).thenReturn(taskOptional2);

        final ThrowableAssert.ThrowingCallable testingMethod = () -> testingInstance.updateTaskStatus(ID_1, updateTaskStatusDto);;

        assertThatThrownBy(testingMethod)
                .isInstanceOf(TaskInProgressException.class)
                .hasMessage(CURRENT_USER_ALREADY_HAS_A_TASK_IN_PROGRESS_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotUpdateTaskStatusWhenTaskIsNotFound() {
        var taskOptional = Optional.<Task>empty();
        var updateTaskStatusDto = new UpdateTaskStatusDto();
        updateTaskStatusDto.setStatus(IN_PROGRESS_TASK_STATUS);
        when(taskRepository.findById(ID_1)).thenReturn(taskOptional);

        final ThrowableAssert.ThrowingCallable testingMethod = () -> testingInstance.updateTaskStatus(ID_1, updateTaskStatusDto);;

        assertThatThrownBy(testingMethod)
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessage(TASK_NOT_FOUND_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldUpdateTasksStatusFromInProgressToPausedInBatch() {
        var tasks = List.of(task1, task2);
        Pageable pageable = PageRequest.of(0, BATCH_SIZE);
        when(taskRepository.findTasksByStatusWithLimit(IN_PROGRESS_TASK_STATUS, pageable)).thenReturn(tasks).thenReturn(List.of());

        testingInstance.updateTasksStatusFromInProgressToPausedInBatch(BATCH_SIZE);

        verify(taskRepository, times(2)).findTasksByStatusWithLimit(IN_PROGRESS_TASK_STATUS, pageable);
        verify(taskBatchProcessorService).updateTasksStatusToPaused(tasks);
    }

    @Test
    public void shouldUpdateTask() {
        var taskOptional = Optional.of(task1);
        var updateTaskDto = new UpdateTaskDto();
        updateTaskDto.setTitle(TITLE);
        updateTaskDto.setDescription(DESCRIPTION);
        updateTaskDto.setStatus(IN_PROGRESS_TASK_STATUS);
        updateTaskDto.setAssigneeId(ASSIGNEE_ID_1);
        when(taskRepository.findById(ID_1)).thenReturn(taskOptional);
        when(task1.getAssigneeId()).thenReturn(ASSIGNEE_ID_2);
        when(taskRepository.findByAssigneeIdAndStatus(ASSIGNEE_ID_1, IN_PROGRESS_TASK_STATUS)).thenReturn(Optional.empty());

        testingInstance.updateTask(ID_1, updateTaskDto);

        verify(taskRepository).findById(ID_1);
        verify(task1, times(2)).getAssigneeId();
        verify(taskRepository).findByAssigneeIdAndStatus(ASSIGNEE_ID_1, IN_PROGRESS_TASK_STATUS);
        verify(taskMapper).updateTask(task1, updateTaskDto);
        verify(taskRepository).save(task1);
    }

    @Test
    public void shouldUpdateTaskWhenAssigneeIdIsTheSameAsCurrentAssigneeId() {
        var taskOptional = Optional.of(task1);
        var updateTaskDto = new UpdateTaskDto();
        updateTaskDto.setTitle(TITLE);
        updateTaskDto.setDescription(DESCRIPTION);
        updateTaskDto.setStatus(IN_PROGRESS_TASK_STATUS);
        updateTaskDto.setAssigneeId(ASSIGNEE_ID_1);
        when(taskRepository.findById(ID_1)).thenReturn(taskOptional);
        when(task1.getAssigneeId()).thenReturn(ASSIGNEE_ID_1);

        testingInstance.updateTask(ID_1, updateTaskDto);

        verify(taskRepository).findById(ID_1);
        verify(task1, times(2)).getAssigneeId();
        verify(taskRepository).findByAssigneeIdAndStatus(ASSIGNEE_ID_1, IN_PROGRESS_TASK_STATUS);
        verify(taskMapper).updateTask(task1, updateTaskDto);
        verify(taskRepository).save(task1);
        verifyNoMoreInteractions(taskRepository);
    }

    @Test
    public void shouldUpdateTaskWhenAssigneeIdIsTheSameAsCurrentAssigneeIdAndNewTaskStatusIsNotInProgress() {
        var taskOptional = Optional.of(task1);
        var updateTaskDto = new UpdateTaskDto();
        updateTaskDto.setTitle(TITLE);
        updateTaskDto.setDescription(DESCRIPTION);
        updateTaskDto.setStatus(TODO_TASK_STATUS);
        updateTaskDto.setAssigneeId(ASSIGNEE_ID_1);
        when(taskRepository.findById(ID_1)).thenReturn(taskOptional);
        when(task1.getAssigneeId()).thenReturn(ASSIGNEE_ID_1);

        testingInstance.updateTask(ID_1, updateTaskDto);

        verify(taskRepository).findById(ID_1);
        verify(task1).getAssigneeId();
        verify(taskMapper).updateTask(task1, updateTaskDto);
        verify(taskRepository).save(task1);
        verifyNoMoreInteractions(taskRepository);
    }

    @Test
    public void shouldNotUpdateTaskWhenNewAssigneeIdAlreadyHasATaskInProgress() {
        var taskOptional = Optional.of(task1);
        var updateTaskDto = new UpdateTaskDto();
        updateTaskDto.setTitle(TITLE);
        updateTaskDto.setDescription(DESCRIPTION);
        updateTaskDto.setStatus(IN_PROGRESS_TASK_STATUS);
        updateTaskDto.setAssigneeId(ASSIGNEE_ID_1);
        when(taskRepository.findById(ID_1)).thenReturn(taskOptional);
        when(task1.getAssigneeId()).thenReturn(ASSIGNEE_ID_2);
        when(taskRepository.findByAssigneeIdAndStatus(ASSIGNEE_ID_1, IN_PROGRESS_TASK_STATUS)).thenReturn(Optional.of(task2));

        final ThrowableAssert.ThrowingCallable testingMethod = () -> testingInstance.updateTask(ID_1, updateTaskDto);;

        assertThatThrownBy(testingMethod)
                .isInstanceOf(TaskInProgressException.class)
                .hasMessage(NEW_USER_ALREADY_HAS_TASK_IN_PROGRESS);
    }

    @Test
    public void shouldNotUpdateTaskWhenCurrentUserAlreadyHasATaskInProgress() {
        var taskOptional = Optional.of(task1);
        var updateTaskDto = new UpdateTaskDto();
        updateTaskDto.setTitle(TITLE);
        updateTaskDto.setDescription(DESCRIPTION);
        updateTaskDto.setStatus(IN_PROGRESS_TASK_STATUS);
        updateTaskDto.setAssigneeId(ASSIGNEE_ID_1);
        when(taskRepository.findById(ID_1)).thenReturn(taskOptional);
        when(task1.getId()).thenReturn(ID_1);
        when(task1.getAssigneeId()).thenReturn(ASSIGNEE_ID_1);
        when(task2.getId()).thenReturn(ID_2);
        when(taskRepository.findByAssigneeIdAndStatus(ASSIGNEE_ID_1, IN_PROGRESS_TASK_STATUS)).thenReturn(Optional.of(task2));

        final ThrowableAssert.ThrowingCallable testingMethod = () -> testingInstance.updateTask(ID_1, updateTaskDto);

        assertThatThrownBy(testingMethod)
                .isInstanceOf(TaskInProgressException.class)
                .hasMessage(CURRENT_USER_ALREADY_HAS_A_TASK_IN_PROGRESS_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotUpdateTaskWhenTaskIsNotFound() {
        var taskOptional = Optional.<Task>empty();
        var updateTaskDto = new UpdateTaskDto();
        updateTaskDto.setTitle(TITLE);
        updateTaskDto.setDescription(DESCRIPTION);
        updateTaskDto.setStatus(IN_PROGRESS_TASK_STATUS);
        updateTaskDto.setAssigneeId(ASSIGNEE_ID_1);
        when(taskRepository.findById(ID_1)).thenReturn(taskOptional);

        final ThrowableAssert.ThrowingCallable testingMethod = () -> testingInstance.updateTask(ID_1, updateTaskDto);

        assertThatThrownBy(testingMethod)
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessage(TASK_NOT_FOUND_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldGetTask() {
        var taskOptional = Optional.of(task1);
        when(taskRepository.findById(ID_1)).thenReturn(taskOptional);

        testingInstance.getTask(ID_1);

        verify(taskRepository).findById(ID_1);
        verify(taskMapper).fromTask(task1);
    }

    @Test
    public void shouldNotGetTaskWhenTaskIsNotFound() {
        var taskOptional = Optional.<Task>empty();
        when(taskRepository.findById(ID_1)).thenReturn(taskOptional);

        final ThrowableAssert.ThrowingCallable testingMethod = () -> testingInstance.getTask(ID_1);

        assertThatThrownBy(testingMethod)
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessage(TASK_NOT_FOUND_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldGetTasks() {
        when(taskRepository.findAll()).thenReturn(List.of(task1, task2));

        testingInstance.getTasks();

        verify(taskRepository).findAll();
        verify(taskMapper).fromTask(task1);
        verify(taskMapper).fromTask(task2);
    }

    @Test
    public void shouldGetTasksWhenNoTasksFound() {
        when(taskRepository.findAll()).thenReturn(List.of());

        testingInstance.getTasks();

        verify(taskRepository).findAll();
        verifyNoInteractions(taskMapper);
    }
}