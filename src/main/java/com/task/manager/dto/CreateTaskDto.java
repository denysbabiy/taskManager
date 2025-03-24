package com.task.manager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTaskDto {

    private Long assigneeId;

    @NotBlank
    @Size(max = 40, message = "Max title length is 40")
    private String title;

    private String description;
}
