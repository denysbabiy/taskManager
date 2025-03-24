package com.task.manager.dto;

import com.task.manager.domain.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskDto {

    private Long id;

    private Long assigneeId;

    private String title;

    private String description;

    private String timeSpent;

    private String createdAt;

    private String updatedAt;

    private TaskStatus status;
}
