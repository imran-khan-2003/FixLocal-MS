package com.fixlocal.exception;

import java.time.LocalDateTime;

public class ErrorResponse {
    private final String code;
    private final String message;
    private final LocalDateTime timestamp;
    private final String path;

    public ErrorResponse(String code, String message, LocalDateTime timestamp, String path) {
        this.code = code;
        this.message = message;
        this.timestamp = timestamp;
        this.path = path;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getPath() {
        return path;
    }

    public static ErrorResponse of(ErrorCode errorCode, String path) {
        return new ErrorResponse(
                errorCode.name(),
                errorCode.getMessage(),
                LocalDateTime.now(),
                path
        );
    }

    public static ErrorResponse of(ErrorCode errorCode, String customMessage, String path) {
        return new ErrorResponse(
                errorCode.name(),
                customMessage,
                LocalDateTime.now(),
                path
        );
    }
}