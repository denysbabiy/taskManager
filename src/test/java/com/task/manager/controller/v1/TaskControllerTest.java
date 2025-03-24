package com.task.manager.controller.v1;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.task.manager.domain.TaskStatus;
import com.task.manager.dto.CreateTaskDto;
import com.task.manager.dto.TaskDto;
import com.task.manager.dto.TaskIdDto;
import com.task.manager.dto.UpdateTaskDto;
import com.task.manager.dto.UpdateTaskStatusDto;
import com.task.manager.exception.TaskNotFoundException;
import com.task.manager.service.TaskService;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TaskController.class)
class TaskControllerTest {

    private static final Long TASK_ID = 1L;
    private static final Long ASSIGNEE_ID = 2L;
    private static final String TITLE = "Task title";
    private static final String INVALID_TITLE = "a tite must have max 40 characters and not be blank";
    private static final String DESCRIPTION = "Task description";
    private static final TaskStatus STATUS = TaskStatus.IN_PROGRESS;

    @MockitoBean
    private TaskService taskService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void shouldCreateTask() throws Exception {
        var createTaskDto = new CreateTaskDto(ASSIGNEE_ID, TITLE, DESCRIPTION);
        var requestBody = objectMapper.writeValueAsString(createTaskDto);
        var taskIdDto = new TaskIdDto(TASK_ID);
        var responseBody = objectMapper.writeValueAsString(taskIdDto);
        when(taskService.createTask(createTaskDto)).thenReturn(TASK_ID);
        final MockHttpServletRequestBuilder requestBuilder = post("/api/v1/tasks")
                .contentType(APPLICATION_JSON)
                .content(requestBody);

        mockMvc.perform(requestBuilder)
               .andExpect(status().isCreated())
               .andExpect(content().json(responseBody));

        verify(taskService).createTask(createTaskDto);
    }

    @Test
    public void shouldNotCreateTaskWhenTitleIsBlank() throws Exception {
        var createTaskDto = new CreateTaskDto(ASSIGNEE_ID, StringUtils.EMPTY, DESCRIPTION);
        var requestBody = objectMapper.writeValueAsString(createTaskDto);
        final MockHttpServletRequestBuilder requestBuilder = post("/api/v1/tasks")
                .contentType(APPLICATION_JSON)
                .content(requestBody);

        mockMvc.perform(requestBuilder)
               .andExpect(status().isBadRequest());

        verify(taskService, never()).createTask(createTaskDto);
    }

    @Test
    public void shouldDeleteTask() throws Exception {
        final var requestBuilder = delete("/api/v1/tasks/{taskId}", TASK_ID)
                .contentType(APPLICATION_JSON);

        mockMvc.perform(requestBuilder)
               .andExpect(status().isNoContent());

        verify(taskService).deleteTask(TASK_ID);
    }

    @Test
    public void shouldNotDeleteTaskWhenTaskIdHasIncorrectType() throws Exception {
        final var requestBuilder = delete("/api/v1/tasks/{taskId}", "abc")
                .contentType(APPLICATION_JSON);

        mockMvc.perform(requestBuilder)
               .andExpect(status().isBadRequest());

        verifyNoInteractions(taskService);
    }

    @Test
    public void shouldUpdateTaskStatus() throws Exception {
        var updateTaskStatusDto = new UpdateTaskStatusDto(TaskStatus.IN_PROGRESS);
        var requestBody = objectMapper.writeValueAsString(updateTaskStatusDto);
        final var requestBuilder = put("/api/v1/tasks/{taskId}/status", TASK_ID)
                .contentType(APPLICATION_JSON)
                .content(requestBody);

        mockMvc.perform(requestBuilder)
               .andExpect(status().isNoContent());

        verify(taskService).updateTaskStatus(TASK_ID, updateTaskStatusDto);
    }

    @Test
    public void shouldNotUpdateTaskStatusWhenStatusIsNull() throws Exception {
        var updateTaskStatusDto = new UpdateTaskStatusDto(null);
        var requestBody = objectMapper.writeValueAsString(updateTaskStatusDto);
        final var requestBuilder = put("/api/v1/tasks/{taskId}/status", TASK_ID)
                .contentType(APPLICATION_JSON)
                .content(requestBody);

        mockMvc.perform(requestBuilder)
               .andExpect(status().isBadRequest());

        verify(taskService, never()).updateTaskStatus(TASK_ID, updateTaskStatusDto);
    }

    @Test
    public void shouldNotUpdateTaskStatusWhenTaskWithGivenIdDoesNotExist() throws Exception {
        var exception = new TaskNotFoundException("Task not found by id: " + TASK_ID);
        var updateTaskStatusDto = new UpdateTaskStatusDto(TaskStatus.IN_PROGRESS);
        var requestBody = objectMapper.writeValueAsString(updateTaskStatusDto);
        doThrow(exception).when(taskService).updateTaskStatus(TASK_ID, updateTaskStatusDto);
        final var requestBuilder = put("/api/v1/tasks/{taskId}/status", TASK_ID)
                .contentType(APPLICATION_JSON)
                .content(requestBody);

        mockMvc.perform(requestBuilder)
               .andExpect(status().isNotFound());

        verify(taskService).updateTaskStatus(TASK_ID, updateTaskStatusDto);
    }

    @Test
    public void shouldUpdateTask() throws Exception {
        var updateTaskDto = new UpdateTaskDto(ASSIGNEE_ID, TITLE, DESCRIPTION, STATUS);
        var requestBody = objectMapper.writeValueAsString(updateTaskDto);
        final var requestBuilder = patch("/api/v1/tasks/{taskId}", TASK_ID)
                .contentType(APPLICATION_JSON)
                .content(requestBody);

        mockMvc.perform(requestBuilder)
               .andExpect(status().isNoContent());

        verify(taskService).updateTask(TASK_ID, updateTaskDto);
    }

    @Test
    public void shouldNotUpdateTaskWhenUpdateTaskDtoIsNotValid() throws Exception {
        var updateTaskDto = new UpdateTaskDto(ASSIGNEE_ID, INVALID_TITLE, DESCRIPTION, STATUS);
        var requestBody = objectMapper.writeValueAsString(updateTaskDto);
        final var requestBuilder = patch("/api/v1/tasks/{taskId}", TASK_ID)
                .contentType(APPLICATION_JSON)
                .content(requestBody);

        mockMvc.perform(requestBuilder)
               .andExpect(status().isBadRequest());

        verifyNoInteractions(taskService);
    }

    @Test
    public void shouldNotUpdateTaskWhenTaskWithGivenIdDoesNotExist() throws Exception {
        var exception = new TaskNotFoundException("Task not found by id: " + TASK_ID);
        var updateTaskDto = new UpdateTaskDto(ASSIGNEE_ID, TITLE, DESCRIPTION, STATUS);
        var requestBody = objectMapper.writeValueAsString(updateTaskDto);
        doThrow(exception).when(taskService).updateTask(TASK_ID, updateTaskDto);
        final var requestBuilder = patch("/api/v1/tasks/{taskId}", TASK_ID)
                .contentType(APPLICATION_JSON)
                .content(requestBody);

        mockMvc.perform(requestBuilder)
               .andExpect(status().isNotFound());

        verify(taskService).updateTask(TASK_ID, updateTaskDto);
    }

    @Test
    public void shouldGetTask() throws Exception {
        var taskDto = new TaskDto();
        taskDto.setId(TASK_ID);
        var responseBody = objectMapper.writeValueAsString(taskDto);
        when(taskService.getTask(TASK_ID)).thenReturn(taskDto);
        final var requestBuilder = get("/api/v1/tasks/{taskId}", TASK_ID)
                .contentType(APPLICATION_JSON);

        mockMvc.perform(requestBuilder)
               .andExpect(status().isOk())
               .andExpect(content().json(responseBody));

        verify(taskService).getTask(TASK_ID);
    }

    @Test
    public void shouldNotGetTaskWhenTaskIdHasIncorrectType() throws Exception {
        final var requestBuilder = get("/api/v1/tasks/{taskId}", "abc")
                .contentType(APPLICATION_JSON);

        mockMvc.perform(requestBuilder)
               .andExpect(status().isBadRequest());

        verifyNoInteractions(taskService);
    }

    @Test
    public void shouldNotGetTaskWhenTaskWithGivenIdDoesNotExist() throws Exception {
        var exception = new TaskNotFoundException("Task not found by id: " + TASK_ID);
        doThrow(exception).when(taskService).getTask(TASK_ID);
        final var requestBuilder = get("/api/v1/tasks/{taskId}", TASK_ID)
                .contentType(APPLICATION_JSON);

        mockMvc.perform(requestBuilder)
               .andExpect(status().isNotFound());

        verify(taskService).getTask(TASK_ID);
    }

    @Test
    public void shouldGetTasks() throws Exception {
        var taskDto = new TaskDto();
        taskDto.setId(TASK_ID);
        final List<TaskDto> taskDtoList = List.of(taskDto);
        var responseBody = objectMapper.writeValueAsString(taskDtoList);
        when(taskService.getTasks()).thenReturn(taskDtoList);
        final var requestBuilder = get("/api/v1/tasks")
                .contentType(APPLICATION_JSON);

        mockMvc.perform(requestBuilder)
               .andExpect(status().isOk())
               .andExpect(content().json(responseBody));

        verify(taskService).getTasks();
    }
}