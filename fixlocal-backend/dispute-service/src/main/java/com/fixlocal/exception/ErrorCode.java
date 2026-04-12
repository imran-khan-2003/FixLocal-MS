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

    DISPUTE_NOT_FOUND(HttpStatus.NOT_FOUND, "Dispute not found"),
    BOOKING_NOT_FOUND(HttpStatus.NOT_FOUND, "Booking not found"),
    REPORTER_NOT_FOUND(HttpStatus.NOT_FOUND, "Reporter not found"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "User not found"),

    DISPUTE_ACCESS_FORBIDDEN(HttpStatus.FORBIDDEN, "You are not authorized to access this dispute"),
    DISPUTE_UPDATE_FORBIDDEN(HttpStatus.FORBIDDEN, "You are not authorized to update this dispute"),
    DISPUTE_MESSAGE_FORBIDDEN(HttpStatus.FORBIDDEN, "You are not authorized to add messages to this dispute"),
    AUTHENTICATION_REQUIRED(HttpStatus.UNAUTHORIZED, "Authentication is required"),
    EXTERNAL_USER_SERVICE_ERROR(HttpStatus.BAD_GATEWAY, "Unable to fetch user information"),
    EXTERNAL_BOOKING_SERVICE_ERROR(HttpStatus.BAD_GATEWAY, "Unable to fetch booking information");

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
