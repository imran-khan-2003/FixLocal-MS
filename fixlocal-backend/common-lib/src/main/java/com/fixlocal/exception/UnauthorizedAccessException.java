package com.fixlocal.exception;

public class UnauthorizedAccessException extends BaseException {

    public UnauthorizedAccessException() {
        super(ErrorCode.FORBIDDEN);
    }

    public UnauthorizedAccessException(String message) {
        super(ErrorCode.FORBIDDEN, message);
    }
}