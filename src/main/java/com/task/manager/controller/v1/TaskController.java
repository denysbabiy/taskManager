package com.task.manager.controller.v1;

import java.util.List;

import com.task.manager.controller.v1.api.TaskApi;
import com.task.manager.dto.CreateTaskDto;
import com.task.manager.dto.TaskDto;
import com.task.manager.dto.TaskIdDto;
import com.task.manager.dto.UpdateTaskDto;
import com.task.manager.dto.UpdateTaskStatusDto;
import com.task.manager.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tasks")
public class TaskController implements TaskApi {

    private final TaskService taskService;

    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskIdDto createTask(@RequestBody CreateTaskDto createTaskDto) {
        var taskId = taskService.createTask(createTaskDto);

        return new TaskIdDto(taskId);
    }

    @Override
    @DeleteMapping("/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@PathVariable Long taskId) {
        taskService.deleteTask(taskId);
    }

    @Override
    @PutMapping("/{taskId}/status")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateTaskStatus(@PathVariable Long taskId, @RequestBody UpdateTaskStatusDto updateTaskStatusDto) {
        taskService.updateTaskStatus(taskId, updateTaskStatusDto);
    }

    @Override
    @PatchMapping("/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateTask(@PathVariable Long taskId, @RequestBody UpdateTaskDto updateTaskDto) {
        taskService.updateTask(taskId, updateTaskDto);
    }

    @Override
    @GetMapping("/{taskId}")
    public TaskDto getTask(@PathVariable Long taskId) {
        return taskService.getTask(taskId);
    }

    @Override
    @GetMapping
    public List<TaskDto> getTasks() {
        return taskService.getTasks();
    }
}
