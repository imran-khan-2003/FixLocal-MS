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

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "User not found"),
    USER_ACCOUNT_BLOCKED(HttpStatus.FORBIDDEN, "Your account is blocked"),
    USER_ROLE_REQUIRED(HttpStatus.FORBIDDEN, "Only users can perform this action"),

    TRADESPERSON_NOT_FOUND(HttpStatus.NOT_FOUND, "Tradesperson not found"),
    TRADESPERSON_ROLE_REQUIRED(HttpStatus.FORBIDDEN, "Only tradespersons can perform this action"),
    TRADESPERSON_NOT_ELIGIBLE(HttpStatus.BAD_REQUEST, "Selected user is not a tradesperson"),
    TRADESPERSON_BLOCKED(HttpStatus.CONFLICT, "Tradesperson account is blocked"),
    TRADESPERSON_BUSY(HttpStatus.CONFLICT, "Tradesperson is currently busy"),

    BOOKING_NOT_FOUND(HttpStatus.NOT_FOUND, "Booking not found"),
    OFFER_NOT_FOUND(HttpStatus.NOT_FOUND, "Offer not found"),
    LIVE_LOCATION_NOT_FOUND(HttpStatus.NOT_FOUND, "Live location not available"),

    BOOKING_ACCESS_FORBIDDEN(HttpStatus.FORBIDDEN, "You are not authorized to act on this booking"),
    NEGOTIATION_TURN_MISMATCH(HttpStatus.CONFLICT, "It is not your turn to respond"),
    PENDING_BOOKING_EXISTS(HttpStatus.CONFLICT, "Pending booking already exists"),
    ACTIVE_BOOKING_EXISTS(HttpStatus.CONFLICT, "You already have an active booking"),
    OFFER_SELF_ACCEPT_FORBIDDEN(HttpStatus.CONFLICT, "You cannot accept your own offer"),
    BOOKING_STATE_CONFLICT(HttpStatus.BAD_REQUEST, "Action not allowed for current booking status"),
    LIVE_LOCATION_UPDATE_FORBIDDEN(HttpStatus.BAD_REQUEST, "Cannot update location for closed booking");

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
