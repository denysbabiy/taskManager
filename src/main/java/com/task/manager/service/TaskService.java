package com.task.manager.service;

import java.util.List;

import com.task.manager.domain.TaskStatus;
import com.task.manager.dto.CreateTaskDto;
import com.task.manager.dto.TaskDto;
import com.task.manager.dto.UpdateTaskDto;
import com.task.manager.dto.UpdateTaskStatusDto;

public interface TaskService {

    /**
     * Create a new task.
     *
     * @param createTaskDto task data
     * @return id of the created task
     */
    Long createTask(CreateTaskDto createTaskDto);

    /**
     * Delete a task.
     *
     * @param taskId id of the task to delete
     */
    void deleteTask(Long taskId);

    /**
     * Update task status.
     *
     * @param taskId              id of the task to update
     * @param updateTaskStatusDto new status
     */
    void updateTaskStatus(Long taskId, UpdateTaskStatusDto updateTaskStatusDto);

    /**
     * Update tasks status from in progress to paused in batch.
     *
     * @param batchSize size of the batch
     */
    void updateTasksStatusFromInProgressToPausedInBatch(int batchSize);

    /**
     * Update task data.
     *
     * @param taskId        id of the task to update
     * @param updateTaskDto new data
     */
    void updateTask(Long taskId, UpdateTaskDto updateTaskDto);

    /**
     * Get task by id.
     *
     * @param taskId id of the task
     * @return task data
     */
    TaskDto getTask(Long taskId);

    /**
     * Get all tasks.
     *
     * @return list of tasks
     */
    List<TaskDto> getTasks();
}
