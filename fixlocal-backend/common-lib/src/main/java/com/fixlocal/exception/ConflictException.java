package com.fixlocal.exception;

public class ConflictException extends BaseException {

    public ConflictException() {
        super(ErrorCode.CONFLICT);
    }

    public ConflictException(String message) {
        super(ErrorCode.CONFLICT, message);
    }
}