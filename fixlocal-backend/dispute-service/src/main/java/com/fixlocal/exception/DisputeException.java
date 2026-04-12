package com.fixlocal.exception;

import lombok.Getter;

@Getter
public class DisputeException extends RuntimeException {

    private final ErrorCode errorCode;

    public DisputeException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public DisputeException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
