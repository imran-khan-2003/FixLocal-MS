package com.fixlocal.exception;

import lombok.Getter;

@Getter
public class TestimonialException extends RuntimeException {

    private final ErrorCode errorCode;

    public TestimonialException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public TestimonialException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
