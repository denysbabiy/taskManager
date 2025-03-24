package com.task.manager.exception;

public class TaskInProgressException extends RuntimeException {
    public TaskInProgressException(String message) {
        super(message);
    }

    public TaskInProgressException(String message, Throwable cause) {
        super(message, cause);
    }
}
