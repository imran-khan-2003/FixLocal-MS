package com.fixlocal.exception;

import lombok.Getter;

@Getter
public class AdminException extends RuntimeException {

    private final ErrorCode errorCode;

    public AdminException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public AdminException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}