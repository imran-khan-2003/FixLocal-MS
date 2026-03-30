package com.fixlocal.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedAccessException extends BaseException {

    public UnauthorizedAccessException(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }
}