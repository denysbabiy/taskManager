package com.task.manager.service.impl;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.task.manager.domain.Task;
import com.task.manager.domain.TaskStatus;
import com.task.manager.dto.CreateTaskDto;
import com.task.manager.dto.TaskDto;
import com.task.manager.dto.UpdateTaskDto;
import com.task.manager.dto.UpdateTaskStatusDto;
import com.task.manager.event.TaskCreatedEvent;
import com.task.manager.exception.TaskInProgressException;
import com.task.manager.exception.TaskNotFoundException;
import com.task.manager.mapper.TaskMapper;
import com.task.manager.repository.TaskRepository;
import com.task.manager.service.TaskBatchProcessorService;
import com.task.manager.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskBatchProcessorService taskBatchProcessorService;

    private final KafkaTemplate<String, TaskCreatedEvent> kafkaTemplate;

    private final TaskMapper taskMapper;

    private final TaskRepository taskRepository;

    @Override
    public Long createTask(CreateTaskDto createTaskDto) {
        var task = taskMapper.fromCreateTaskDto(createTaskDto);

        var createdTask = taskRepository.save(task);

        var taskCreatedEvent = new TaskCreatedEvent(createdTask.getId(), createdTask.getAssigneeId(), createdTask.getTitle());

        try {
            log.info("Sending task created event: {}", taskCreatedEvent);

            kafkaTemplate.send("task-created", String.valueOf(task.getId()), taskCreatedEvent);

            log.info("Task created event sent");
        } catch (Exception exc) {
            log.error("Failed to send task created event: ", exc);
        }

        return createdTask.getId();
    }

    @Override
    public void deleteTask(Long taskId) {
        taskRepository.deleteById(taskId);
    }

    @Override
    public void updateTaskStatus(Long taskId, UpdateTaskStatusDto updateTaskStatusDto) {
        var task = getTaskById(taskId);

        var currentStatus = task.getStatus();
        var newStatus = updateTaskStatusDto.getStatus();

        if (newStatus != currentStatus) {
            if (currentStatus == TaskStatus.IN_PROGRESS || newStatus == TaskStatus.PAUSED) {
                task.endProgress();
            } else if (newStatus == TaskStatus.IN_PROGRESS) {
                validateUpdateTaskStatusToInProgress(task);
                task.startProgress();
            }

            taskMapper.updateTaskStatus(task, updateTaskStatusDto);

            taskRepository.save(task);
        }
    }

    @Override
    public void updateTasksStatusFromInProgressToPausedInBatch(int batchSize) {
        Pageable pageable = PageRequest.of(0, batchSize);

        List<Task> tasksBatch;
        var processedTasks = 0;

        do {
            tasksBatch = taskRepository.findTasksByStatusWithLimit(TaskStatus.IN_PROGRESS, pageable);

            if (!tasksBatch.isEmpty()) {
                taskBatchProcessorService.updateTasksStatusToPaused(tasksBatch);
            }
            processedTasks += tasksBatch.size();
            log.info("Processed {} tasks", processedTasks);
        } while (!tasksBatch.isEmpty());
    }

    @Override
    public void updateTask(final Long taskId, final UpdateTaskDto updateTaskDto) {
        var task = getTaskById(taskId);
        validateUpdateTask(task, updateTaskDto);
        taskMapper.updateTask(task, updateTaskDto);

        taskRepository.save(task);
    }

    @Override
    public TaskDto getTask(final Long taskId) {
        var task = getTaskById(taskId);

        return taskMapper.fromTask(task);
    }

    @Override
    public List<TaskDto> getTasks() {
        var tasks = taskRepository.findAll();

        return tasks.stream().map(taskMapper::fromTask).toList();
    }

    private void validateUpdateTaskStatusToInProgress(final Task task) {
        var taskInProgressForCurrentUser = taskRepository.findByAssigneeIdAndStatus(task.getAssigneeId(), TaskStatus.IN_PROGRESS);

        if (taskInProgressForCurrentUser.isPresent() && !Objects.equals(taskInProgressForCurrentUser.get().getId(), task.getId())) {
            throw new TaskInProgressException("Current user already has a task in progress");
        }
    }

    private void validateUpdateTask(final Task task, final UpdateTaskDto updateTaskDto) {
        if (updateTaskDto.getAssigneeId() != null && !Objects.equals(task.getAssigneeId(), updateTaskDto.getAssigneeId())) {
            var taskInProgressForNewUser = taskRepository.findByAssigneeIdAndStatus(updateTaskDto.getAssigneeId(), TaskStatus.IN_PROGRESS);

            if (taskInProgressForNewUser.isPresent()) {
                throw new TaskInProgressException("New user already has a task in progress");
            }
        }

        if (updateTaskDto.getStatus() == TaskStatus.IN_PROGRESS) {
            validateUpdateTaskStatusToInProgress(task);
        }
    }

    private Task getTaskById(final Long taskId) {
        return taskRepository.findById(taskId).orElseThrow(() -> new TaskNotFoundException("Task not found by id: " + taskId));
    }
}
