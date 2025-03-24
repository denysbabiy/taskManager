package com.task.manager.dto;

import com.task.manager.domain.TaskStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTaskStatusDto {

    @NotNull
    private TaskStatus status;
}
