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

    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "Email already exists"),
    ROLE_REQUIRED(HttpStatus.BAD_REQUEST, "Role is required"),
    PASSWORD_TOO_SHORT(HttpStatus.BAD_REQUEST, "Password must be at least 6 characters"),
    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "New password and confirm password must match"),
    TRADESPERSON_DETAILS_REQUIRED(HttpStatus.BAD_REQUEST, "Occupation and working city required for tradesperson"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "User not found"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Invalid credentials"),
    ACCOUNT_BLOCKED(HttpStatus.UNAUTHORIZED, "Your account has been blocked"),
    PASSWORD_RESOLVE_FAILED(HttpStatus.BAD_REQUEST, "Unable to resolve encrypted password");

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
