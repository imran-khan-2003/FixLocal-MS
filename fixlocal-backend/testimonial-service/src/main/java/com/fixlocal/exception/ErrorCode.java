package com.fixlocal.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred"),
    EXTERNAL_SERVICE_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "Downstream service unavailable"),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "Invalid input provided"),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "Bad request"),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "Resource not found"),
    CONFLICT(HttpStatus.CONFLICT, "Conflict occurred"),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "HTTP method not allowed"),
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Unsupported media type"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Authentication required"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "You are not authorized to access this resource"),

    TESTIMONIAL_NOT_FOUND(HttpStatus.NOT_FOUND, "Testimonial not found"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "User not found"),

    TESTIMONIAL_CREATION_FORBIDDEN(HttpStatus.FORBIDDEN, "You are not authorized to create a testimonial"),
    TESTIMONIAL_UPDATE_FORBIDDEN(HttpStatus.FORBIDDEN, "You are not authorized to update this testimonial"),
    TESTIMONIAL_DELETE_FORBIDDEN(HttpStatus.FORBIDDEN, "You are not authorized to delete this testimonial"),

    TESTIMONIAL_ALREADY_EXISTS(HttpStatus.CONFLICT, "Testimonial already exists"),
    TESTIMONIAL_STATUS_INVALID(HttpStatus.CONFLICT, "Testimonial status invalid for this operation");

    private final HttpStatus httpStatus;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getMessage() {
        return message;
    }
}
