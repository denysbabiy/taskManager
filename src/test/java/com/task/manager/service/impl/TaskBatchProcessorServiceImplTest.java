package com.task.manager.service.impl;

import java.util.List;

import com.task.manager.domain.Task;
import com.task.manager.domain.TaskStatus;
import com.task.manager.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TaskBatchProcessorServiceImplTest {

    private static final TaskStatus PAUSED_TASK_STATUS = TaskStatus.PAUSED;

    @Mock
    private Task task;

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskBatchProcessorServiceImpl testingInstance;

    @Test
    public void shouldUpdateTasksStatusToPaused() {
        List<Task> tasks = List.of(task);

        testingInstance.updateTasksStatusToPaused(tasks);

        verify(task).endProgress();
        verify(task).setStatus(PAUSED_TASK_STATUS);
        verify(taskRepository).saveAll(tasks);
    }

}