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

    BOOKING_NOT_FOUND(HttpStatus.NOT_FOUND, "Booking not found"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "User not found"),
    MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "Message not found"),
    CONVERSATION_NOT_FOUND(HttpStatus.NOT_FOUND, "Conversation not found"),
    ATTACHMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "Attachment not found"),
    ATTACHMENT_METADATA_MISSING(HttpStatus.NOT_FOUND, "Attachment metadata missing"),
    ATTACHMENT_FILE_MISSING(HttpStatus.NOT_FOUND, "Attachment file missing"),

    ATTACHMENT_TOO_LARGE(HttpStatus.BAD_REQUEST, "Attachment exceeds size limits"),
    ATTACHMENT_STORAGE_FAILED(HttpStatus.BAD_REQUEST, "Unable to store attachment"),

    NOT_PART_OF_BOOKING(HttpStatus.FORBIDDEN, "Not part of this booking conversation"),
    ATTACHMENT_ACCESS_FORBIDDEN(HttpStatus.FORBIDDEN, "Not authorized to download this attachment");

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
