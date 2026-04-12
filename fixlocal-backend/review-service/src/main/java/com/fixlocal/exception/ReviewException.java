package com.fixlocal.exception;

import lombok.Getter;

@Getter
public class ReviewException extends RuntimeException {

    private final ErrorCode errorCode;

    public ReviewException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public ReviewException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
