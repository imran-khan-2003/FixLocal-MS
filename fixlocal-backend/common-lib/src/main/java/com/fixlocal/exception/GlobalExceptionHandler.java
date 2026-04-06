package com.fixlocal.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private ResponseEntity<ErrorResponse> buildResponse(
            String message,
            HttpStatus status,
            String path
    ) {
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path
        );

        return new ResponseEntity<>(error, status);
    }

    // ==============================
    // 404 - Resource Not Found
    // ==============================
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request
    ) {

        log.warn("Resource not found: {}", ex.getMessage());

        return buildResponse(
                ex.getMessage(),
                HttpStatus.NOT_FOUND,
                request.getRequestURI()
        );
    }

    // ==============================
    // 403 - Custom Unauthorized
    // ==============================
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(
            UnauthorizedException ex,
            HttpServletRequest request
    ) {

        log.warn("Unauthorized access attempt: {}", ex.getMessage());

        return buildResponse(
                ex.getMessage(),
                HttpStatus.FORBIDDEN,
                request.getRequestURI()
        );
    }

    // ==============================
    // 403 - Spring Security Access Denied
    // ==============================
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request
    ) {

        log.warn("Access denied for path: {}", request.getRequestURI());

        return buildResponse(
                "You are not authorized to access this resource",
                HttpStatus.FORBIDDEN,
                request.getRequestURI()
        );
    }

    // ==============================
    // 401 - Not Authenticated
    // ==============================
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            AuthenticationException ex,
            HttpServletRequest request
    ) {

        log.warn("Authentication required for path: {}", request.getRequestURI());

        return buildResponse(
                "Authentication required",
                HttpStatus.UNAUTHORIZED,
                request.getRequestURI()
        );
    }

    // ==============================
    // 400 - Bad Request
    // ==============================
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(
            BadRequestException ex,
            HttpServletRequest request
    ) {

        log.warn("Bad request: {}", ex.getMessage());

        return buildResponse(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST,
                request.getRequestURI()
        );
    }

    // ==============================
    // 409 - Conflict
    // ==============================
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(
            ConflictException ex,
            HttpServletRequest request
    ) {

        log.warn("Conflict occurred: {}", ex.getMessage());

        return buildResponse(
                ex.getMessage(),
                HttpStatus.CONFLICT,
                request.getRequestURI()
        );
    }

    // ==============================
    // Validation Errors
    // ==============================
    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            org.springframework.web.bind.MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce((msg1, msg2) -> msg1 + ", " + msg2)
                .orElse("Validation error");

        log.warn("Validation error: {}", message);

        return buildResponse(
                message,
                HttpStatus.BAD_REQUEST,
                request.getRequestURI()
        );
    }

    // ==============================
    // 500 - Internal Server Error
    // ==============================
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(
            Exception ex,
            HttpServletRequest request
    ) {

        log.error("Unexpected server error", ex);

        return buildResponse(
                "Something went wrong. Please try again later.",
                HttpStatus.INTERNAL_SERVER_ERROR,
                request.getRequestURI()
        );
    }
}
