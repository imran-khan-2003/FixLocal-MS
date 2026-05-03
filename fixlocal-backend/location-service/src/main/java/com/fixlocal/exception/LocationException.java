package com.fixlocal.exception;

import lombok.Getter;

@Getter
public class LocationException extends RuntimeException {

    private final ErrorCode errorCode;

    public LocationException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public LocationException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
