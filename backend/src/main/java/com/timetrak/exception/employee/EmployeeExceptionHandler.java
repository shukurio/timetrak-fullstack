package com.timetrak.exception.employee;

import com.timetrak.exception.employee.response.DuplicateEmployeeErrorResponse;
import com.timetrak.exception.employee.response.EmployeeErrorResponse;
import com.timetrak.exception.employee.response.InvalidEmployeeErrorResponse;
import com.timetrak.exception.employee.response.ValidationErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class EmployeeExceptionHandler {

    /**
     * Handle Employee Not Found Exception
     */
    @ExceptionHandler(EmployeeNotFoundException.class)
    public ResponseEntity<EmployeeErrorResponse> handleEmployeeNotFound(
            EmployeeNotFoundException ex, WebRequest request) {

        log.warn("Employee not found: {}", ex.getMessage());

        EmployeeErrorResponse errorResponse = EmployeeErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Employee Not Found")
                .message(ex.getMessage())
                .errorCode(ex.getErrorCode())
                .employeeId(ex.getEmployeeId())
                .path(getPath(request))
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Handle Duplicate Employee Exception
     */
    @ExceptionHandler(DuplicateEmployeeException.class)
    public ResponseEntity<DuplicateEmployeeErrorResponse> handleDuplicateEmployee(
            DuplicateEmployeeException ex, WebRequest request) {

        log.warn("Duplicate employee: {} - {}: {}", ex.getErrorCode(), ex.getDuplicateField(), ex.getDuplicateValue());

        DuplicateEmployeeErrorResponse errorResponse = DuplicateEmployeeErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error("Duplicate Employee")
                .message(ex.getMessage())
                .errorCode(ex.getErrorCode())
                .employeeId(ex.getEmployeeId())
                .duplicateField(ex.getDuplicateField())
                .duplicateValue(ex.getDuplicateValue())
                .existingEmployeeId(ex.getExistingEmployeeId())
                .path(getPath(request))
                .suggestion(getSuggestionForDuplicateField(ex.getDuplicateField()))
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    /**
     * Handle Employee Validation Exception
     */
    @ExceptionHandler(EmployeeValidationException.class)
    public ResponseEntity<EmployeeErrorResponse> handleEmployeeValidation(
            EmployeeValidationException ex, WebRequest request) {

        log.warn("Employee validation failed: {}", ex.getMessage());

        EmployeeErrorResponse errorResponse = EmployeeErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Employee Validation Failed")
                .message(ex.getMessage())
                .errorCode(ex.getErrorCode())
                .employeeId(ex.getEmployeeId())
                .path(getPath(request))
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handle Invalid Employee Exception
     */
    @ExceptionHandler(InvalidEmployeeException.class)
    public ResponseEntity<InvalidEmployeeErrorResponse> handleInvalidEmployee(
            InvalidEmployeeException ex, WebRequest request) {

        log.warn("Invalid employee operation: {}", ex.getMessage());

        InvalidEmployeeErrorResponse errorResponse = InvalidEmployeeErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Invalid Employee Operation")
                .message(ex.getMessage())
                .errorCode(ex.getErrorCode())
                .employeeId(ex.getEmployeeId())
                .invalidEmployeeIds(ex.getInvalidEmployeeIds())
                .validationFailureReason(ex.getValidationFailureReason())
                .path(getPath(request))
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handle Bean Validation Errors (from @Valid annotations)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex, WebRequest request) {

        log.warn("Validation failed for employee request: {} errors", ex.getBindingResult().getErrorCount());

        Map<String, String> fieldErrors = new HashMap<>();
        List<String> globalErrors = ex.getBindingResult().getGlobalErrors()
                .stream()
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.toList());

        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }

        ValidationErrorResponse errorResponse = ValidationErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failed")
                .message("Employee data validation failed")
                .errorCode("VALIDATION_FAILED")
                .fieldErrors(fieldErrors)
                .globalErrors(globalErrors)
                .errorCount(fieldErrors.size() + globalErrors.size())
                .path(getPath(request))
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handle any other Employee Exception (fallback)
     */
    @ExceptionHandler(EmployeeException.class)
    public ResponseEntity<EmployeeErrorResponse> handleGenericEmployeeException(
            EmployeeException ex, WebRequest request) {

        log.error("Unhandled employee exception: {}", ex.getMessage(), ex);

        EmployeeErrorResponse errorResponse = EmployeeErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Employee Operation Failed")
                .message(ex.getMessage())
                .errorCode(ex.getErrorCode())
                .employeeId(ex.getEmployeeId())
                .path(getPath(request))
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    // Helper methods
    private String getPath(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }

    private String getSuggestionForDuplicateField(String field) {
        return switch (field.toLowerCase()) {
            case "username" -> "Try a different username or add numbers/characters to make it unique";
            case "email" -> "Use a different email address or check if you already have an account";
            case "phone number", "phonenumber" -> "Verify the phone number or use a different one";
            default -> "Please use a different value for " + field;
        };
    }
}