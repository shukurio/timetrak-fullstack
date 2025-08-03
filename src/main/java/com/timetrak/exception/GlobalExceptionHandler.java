package com.timetrak.exception;

import com.timetrak.dto.response.ErrorResponse;
import com.timetrak.exception.payment.*;
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
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // === 400 BAD REQUEST ===

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleEnumConversionError(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

        String message;
        if (ex.getRequiredType() != null && ex.getRequiredType().isEnum()) {
            // Get valid enum values
            Object[] enumConstants = ex.getRequiredType().getEnumConstants();
            String validValues = Arrays.stream(enumConstants)
                    .map(Object::toString)
                    .collect(Collectors.joining(", "));

            message = String.format("Invalid %s: '%s'. Valid values are: %s",
                    ex.getName(), ex.getValue(), validValues);
        } else {
            message = String.format("Invalid value '%s' for parameter '%s'",
                    ex.getValue(), ex.getName());
        }

        log.warn("Type mismatch error: {}", message);

        ErrorResponse error = ErrorResponse.builder()
                .error("Invalid Parameter")
                .message(message)
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

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(
            NoResourceFoundException ex, HttpServletRequest request) {

        // Check if this looks like an API endpoint
        String path = request.getRequestURI();
        if (path.startsWith("/api/")) {
            String message = String.format("API endpoint not found: %s", path);
            log.warn("API endpoint not found: {}", message);

            ErrorResponse error = ErrorResponse.builder()
                    .error("API Endpoint Not Found")
                    .message(message)
                    .status(404)
                    .timestamp(LocalDateTime.now())
                    .path(path)
                    .build();

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        String message = "Resource not found";
        ErrorResponse error = ErrorResponse.builder()
                .error("Not Found")
                .message(message)
                .status(404)
                .timestamp(LocalDateTime.now())
                .path(path)
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
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

    @ExceptionHandler(PaymentException.class)  // Add the parent class
    public ResponseEntity<ErrorResponse> handlePaymentException(
            PaymentException ex, HttpServletRequest request) {

        log.warn("Payment exception: {}", ex.getMessage());

        HttpStatus status = determinePaymentExceptionStatus(ex);

        ErrorResponse error = ErrorResponse.builder()
                .error("Payment Error")
                .message(ex.getMessage())
                .status(status.value())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(status).body(error);
    }

    private HttpStatus determinePaymentExceptionStatus(PaymentException ex) {
        if (ex instanceof InvalidPaymentRequestException ||
                ex instanceof InvalidPaymentStatusException ||
                ex instanceof InvalidPaymentPeriodException) {
            return HttpStatus.BAD_REQUEST;
        } else if (ex instanceof PaymentNotFoundException) {
            return HttpStatus.NOT_FOUND;
        } else if (ex instanceof DuplicatePaymentException) {
            return HttpStatus.CONFLICT;
        } else if (ex instanceof PaymentCalculationException ||
                ex instanceof InsufficientPaymentDataException) {
            return HttpStatus.UNPROCESSABLE_ENTITY;
        } else if (ex instanceof PaymentProcessingException) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        } else if (ex instanceof PaymentSettingsConfigurationException) {
            return HttpStatus.BAD_REQUEST;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    // === 500 INTERNAL ERROR ===
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralError(
            Exception ex, HttpServletRequest request) {
        log.error("Internal server error: {}", ex.getMessage(), ex);
        String message = ex.getMessage();

        ErrorResponse error = ErrorResponse.builder()
                .error("Internal Server Error")
                .message(message)
                .status(500)
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.internalServerError().body(error);
    }
}