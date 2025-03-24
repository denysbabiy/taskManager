package com.task.manager.controller.v1.api;

import java.util.List;

import com.task.manager.dto.CreateTaskDto;
import com.task.manager.dto.TaskDto;
import com.task.manager.dto.TaskIdDto;
import com.task.manager.dto.UpdateTaskDto;
import com.task.manager.dto.UpdateTaskStatusDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Task API", description = "API to manage tasks")
public interface TaskApi {

    @Operation(summary = "Create task")
    @ApiResponse(responseCode = "201", description = "Response body")
    @ApiResponse(responseCode = "400", description = "Bad request")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    TaskIdDto createTask(@RequestBody(description = "Task data for creating a new task") @Valid CreateTaskDto createTaskDto);

    @Operation(summary = "Delete task")
    @ApiResponse(responseCode = "204", description = "Success. No content")
    @ApiResponse(responseCode = "400", description = "Bad request")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    void deleteTask(@Parameter(description = "Task id to delete") Long taskId);

    @Operation(summary = "Update task status")
    @ApiResponse(responseCode = "204", description = "Success. No content")
    @ApiResponse(responseCode = "400", description = "Bad request")
    @ApiResponse(responseCode = "404", description = "Task not found")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    void updateTaskStatus(@Parameter(description = "Task id to update status") Long taskId,
                          @RequestBody(description = "Task status data for updating task status") @Valid UpdateTaskStatusDto updateTaskStatusDto);

    @Operation(summary = "Update task")
    @ApiResponse(responseCode = "204", description = "Success. No content")
    @ApiResponse(responseCode = "400", description = "Bad request")
    @ApiResponse(responseCode = "404", description = "Task not found")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    void updateTask(@Parameter(description = "Task id to update") Long taskId,
                    @RequestBody(description = "Task data for updating task") @Valid UpdateTaskDto updateTaskDto);

    @Operation(summary = "Get task")
    @ApiResponse(responseCode = "200", description = "Response body")
    @ApiResponse(responseCode = "400", description = "Bad request")
    @ApiResponse(responseCode = "404", description = "Task not found")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    TaskDto getTask(@Parameter(description = "Task id to get") Long taskId);

    @Operation(summary = "Get all tasks")
    @ApiResponse(responseCode = "200", description = "Response body")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    List<TaskDto> getTasks();
}
