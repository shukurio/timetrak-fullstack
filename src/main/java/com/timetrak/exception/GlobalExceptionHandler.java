package com.timetrak.exception;

import com.timetrak.dto.response.ErrorResponse;
import com.timetrak.exception.payment.InvalidPaymentRequestException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // === 400 BAD REQUEST ===

    @ExceptionHandler(InvalidPaymentRequestException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPaymentRequest(
            InvalidPaymentRequestException ex, HttpServletRequest request) {
        log.warn("Invalid payment request: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .error("Bad Request")
                .message(ex.getMessage())
                .status(400)
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {

        String message = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));

        log.warn("Constraint violation: {}", message);

        ErrorResponse error = ErrorResponse.builder()
                .error("Validation Failed")
                .message(message)
                .status(400)
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        log.warn("Method argument not valid: {}", message);

        ErrorResponse error = ErrorResponse.builder()
                .error("Validation Failed")
                .message(message)
                .status(400)
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

        String message = String.format("Invalid value '%s' for parameter '%s'. Expected type: %s",
                ex.getValue(), ex.getName(), Objects.requireNonNull(ex.getRequiredType()).getSimpleName());

        log.warn("Type mismatch: {}", message);

        ErrorResponse error = ErrorResponse.builder()
                .error("Invalid Parameter")
                .message(message)
                .status(400)
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameter(
            MissingServletRequestParameterException ex, HttpServletRequest request) {

        String message = String.format("Required parameter '%s' is missing", ex.getParameterName());
        log.warn("Missing parameter: {}", message);

        ErrorResponse error = ErrorResponse.builder()
                .error("Missing Parameter")
                .message(message)
                .status(400)
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("Illegal argument: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .error("Invalid Argument")
                .message(ex.getMessage())
                .status(400)
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.badRequest().body(error);
    }

    // === 404 NOT FOUND ===
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {
        log.warn("Resource not found: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .error("Not Found")
                .message(ex.getMessage())
                .status(404)
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    // === 415 UNSUPPORTED FORMAT ===
    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<ErrorResponse> handleUnsupported(
            UnsupportedOperationException ex, HttpServletRequest request) {
        log.warn("Unsupported operation: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .error("Unsupported Operation")
                .message(ex.getMessage())
                .status(415)
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(error);
    }

    // === 500 INTERNAL ERROR ===
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralError(
            Exception ex, HttpServletRequest request) {
        log.error("Internal server error: {}", ex.getMessage(), ex);

        ErrorResponse error = ErrorResponse.builder()
                .error("Internal Server Error")
                .message("Something went wrong. Please try again later.")
                .status(500)
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.internalServerError().body(error);
    }
}