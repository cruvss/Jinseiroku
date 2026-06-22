package com.cruvs.backend.response;

import java.time.LocalDateTime;

public class ApiResponse<T> {

    private LocalDateTime timestamp;
    private int statusCode;
    private boolean success;
    private String message;
    private T data;

    public ApiResponse(int statusCode, boolean success, String message, T data) {
        this.timestamp = LocalDateTime.now();
        this.statusCode = statusCode;
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public boolean isSuccess(){ return success;}

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }
}