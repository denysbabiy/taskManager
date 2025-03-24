package com.task.manager.job;

import com.task.manager.service.TaskService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TaskPauseJobTest {

    private static final int BATCH_SIZE = 100;

    @Mock
    private TaskService taskService;

    @InjectMocks
    private TaskPauseJob testingInstance;

    @Test
    public void shouldPauseTasks() {
        testingInstance.pauseTasks();

        verify(taskService).updateTasksStatusFromInProgressToPausedInBatch(BATCH_SIZE);
    }
}