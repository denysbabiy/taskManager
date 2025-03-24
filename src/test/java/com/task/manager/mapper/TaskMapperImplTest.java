package com.task.manager.mapper;

import java.time.Duration;
import java.time.Instant;

import com.task.manager.domain.Task;
import com.task.manager.domain.TaskStatus;
import com.task.manager.dto.CreateTaskDto;
import com.task.manager.dto.UpdateTaskDto;
import com.task.manager.dto.UpdateTaskStatusDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(MockitoExtension.class)
class TaskMapperImplTest {

    private static final Long ID = 1L;
    private static final Long ASSIGNEE_ID = 123L;
    private static final Long NEW_ASSIGNEE_ID = 456L;
    private static final String TITLE = "Test Task";
    private static final String NEW_TITLE = "New Title";
    private static final String DESCRIPTION = "Test Description";
    private static final String NEW_DESCRIPTION = "New Description";
    private static final String TIME_SPENT_STRING = "02:00:00";
    private static final String TIME_SPENT_ZERO = "00:00:00";
    private static final TaskStatus TODO_TASK_STATUS = TaskStatus.TODO;
    private static final TaskStatus IN_PROGRESS_TASK_STATUS = TaskStatus.IN_PROGRESS;
    private static final Duration TIME_SPENT = Duration.ofHours(2);

    @InjectMocks
    private TaskMapperImpl testingInstance;

    @Test
    void shouldMapFromCreateTaskDto() {
        var createTaskDto = new CreateTaskDto();
        createTaskDto.setTitle(TITLE);
        createTaskDto.setDescription(DESCRIPTION);
        createTaskDto.setAssigneeId(123L);

        var result = testingInstance.fromCreateTaskDto(createTaskDto);

        assertThat(result, notNullValue());
        assertThat(result.getTitle(), is(TITLE));
        assertThat(result.getDescription(), is(DESCRIPTION));
        assertThat(result.getAssigneeId(), is(ASSIGNEE_ID));
        assertThat(result.getStatus(), is(TODO_TASK_STATUS));
        assertThat(result.getId(), nullValue());
        assertThat(result.getTimeSpent(), nullValue());
        assertThat(result.getStartedAt(), nullValue());
        assertThat(result.getCreatedAt(), nullValue());
        assertThat(result.getUpdatedAt(), nullValue());
    }

    @Test
    void shouldMapFromTask() {
        var task = new Task();
        task.setId(ID);
        task.setTitle(TITLE);
        task.setDescription(DESCRIPTION);
        task.setAssigneeId(ASSIGNEE_ID);
        task.setTimeSpent(TIME_SPENT);
        task.setCreatedAt(Instant.now());
        task.setUpdatedAt(Instant.now());

        var result = testingInstance.fromTask(task);

        assertThat(result, notNullValue());
        assertThat(result.getId(), is(ID));
        assertThat(result.getTitle(), is(TITLE));
        assertThat(result.getDescription(), is(DESCRIPTION));
        assertThat(result.getAssigneeId(), is(ASSIGNEE_ID));
        assertThat(result.getTimeSpent(), is(TIME_SPENT_STRING));
    }

    @Test
    void shouldUpdateTaskStatus() {
        var task = new Task();
        task.setStatus(TODO_TASK_STATUS);
        var updateTaskStatusDto = new UpdateTaskStatusDto();
        updateTaskStatusDto.setStatus(IN_PROGRESS_TASK_STATUS);

        testingInstance.updateTaskStatus(task, updateTaskStatusDto);

        assertThat(task.getStatus(), is(IN_PROGRESS_TASK_STATUS));
    }

    @Test
    void shouldUpdateTask() {
        var task = new Task();
        task.setTitle(TITLE);
        task.setDescription(DESCRIPTION);
        task.setStatus(TODO_TASK_STATUS);
        task.setAssigneeId(ASSIGNEE_ID);
        var updateTaskDto = new UpdateTaskDto();
        updateTaskDto.setTitle(NEW_TITLE);
        updateTaskDto.setDescription(NEW_DESCRIPTION);
        updateTaskDto.setStatus(IN_PROGRESS_TASK_STATUS);
        updateTaskDto.setAssigneeId(NEW_ASSIGNEE_ID);

        testingInstance.updateTask(task, updateTaskDto);

        assertThat(task.getTitle(), is(NEW_TITLE));
        assertThat(task.getDescription(), is(NEW_DESCRIPTION));
        assertThat(task.getStatus(), is(IN_PROGRESS_TASK_STATUS));
        assertThat(task.getAssigneeId(), is(NEW_ASSIGNEE_ID));
    }

    @Test
    void shouldMapTimeSpent() {
        var task = new Task();
        task.setTimeSpent(TIME_SPENT);

        var result = testingInstance.mapTimeSpent(task);

        assertThat(result, is(TIME_SPENT_STRING));
    }

    @Test
    void shouldMapTimeSpentWhenTimeSpentIsNull() {
        var task = new Task();
        task.setTimeSpent(null);

        var result = testingInstance.mapTimeSpent(task);

        assertThat(result, is(TIME_SPENT_ZERO));
    }
}