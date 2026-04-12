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

    DOWNSTREAM_USER_SERVICE_ERROR(HttpStatus.BAD_GATEWAY, "User service unavailable"),
    DOWNSTREAM_BOOKING_SERVICE_ERROR(HttpStatus.BAD_GATEWAY, "Booking service unavailable"),
    DOWNSTREAM_CHAT_SERVICE_ERROR(HttpStatus.BAD_GATEWAY, "Chat service unavailable"),
    DOWNSTREAM_RESPONSE_PARSING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to parse downstream response"),

    INVALID_ROLE_FILTER(HttpStatus.BAD_REQUEST, "Invalid role filter"),
    INVALID_PAGE_REQUEST(HttpStatus.BAD_REQUEST, "Invalid pagination request"),

    ADMIN_ACCESS_REQUIRED(HttpStatus.FORBIDDEN, "Admin privileges required");

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
