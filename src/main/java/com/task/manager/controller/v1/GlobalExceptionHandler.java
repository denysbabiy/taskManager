package com.task.manager.controller.v1;

import com.task.manager.dto.ErrorDto;
import com.task.manager.exception.TaskInProgressException;
import com.task.manager.exception.TaskNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(TaskInProgressException.class)
    public ErrorDto handleTaskInProgressException(TaskInProgressException exc) {
        return new ErrorDto(exc.getMessage());
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(TaskNotFoundException.class)
    public ErrorDto handleTaskNotFoundException(TaskNotFoundException exc) {
        return new ErrorDto(exc.getMessage());
    }
}
