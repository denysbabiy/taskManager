package com.task.manager.service;

import java.util.List;

import com.task.manager.domain.Task;

public interface TaskBatchProcessorService {

    /**
     * Update tasks status to paused.
     *
     * @param tasks tasks to update
     */
    void updateTasksStatusToPaused(List<Task> tasks);
}
