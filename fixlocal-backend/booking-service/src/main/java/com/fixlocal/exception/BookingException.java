package com.fixlocal.exception;

import lombok.Getter;

@Getter
public class BookingException extends RuntimeException {

    private final ErrorCode errorCode;

    public BookingException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public BookingException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}