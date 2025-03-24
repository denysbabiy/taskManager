package com.task.manager.job;

import com.task.manager.domain.TaskStatus;
import com.task.manager.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class TaskPauseJob {

    private final TaskService taskService;

    @Scheduled(cron = "0 0 0 * * *")
    public void pauseTasks() {
        try {
            log.info("Starting task pause job");

            taskService.updateTasksStatusFromInProgressToPausedInBatch(100);

            log.info("Task pause job finished successfully");
        } catch (Exception exc) {
            log.error("Task pause job failed: ", exc);
        }
    }
}
