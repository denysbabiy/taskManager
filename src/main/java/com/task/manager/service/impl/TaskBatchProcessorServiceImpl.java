package com.task.manager.service.impl;

import java.util.List;

import com.task.manager.domain.Task;
import com.task.manager.domain.TaskStatus;
import com.task.manager.repository.TaskRepository;
import com.task.manager.service.TaskBatchProcessorService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TaskBatchProcessorServiceImpl implements TaskBatchProcessorService {

    private final TaskRepository taskRepository;

    @Transactional
    @Override
    public void updateTasksStatusToPaused(List<Task> tasks) {
        tasks.forEach(task -> {
            task.endProgress();
            task.setStatus(TaskStatus.PAUSED);
        });

        taskRepository.saveAll(tasks);
    }
}
