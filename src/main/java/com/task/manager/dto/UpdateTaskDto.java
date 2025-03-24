package com.task.manager.dto;

import com.task.manager.domain.TaskStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTaskDto {

    private Long assigneeId;

    @Size(max = 40, message = "Max title length is 40")
    private String title;

    private String description;

    private TaskStatus status;
}
