package com.timetrak.exception.payment;




import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class PaymentExceptionHandler {

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<Map<String, Object>> handlePaymentException(PaymentException ex) {
        log.error("Payment exception occurred: {}", ex.getMessage(), ex);

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("error", ex.getClass().getSimpleName());
        errorResponse.put("errorCode", ex.getErrorCode());
        errorResponse.put("message", ex.getMessage());

        HttpStatus status = determineHttpStatus(ex);
        return new ResponseEntity<>(errorResponse, status);
    }

    @ExceptionHandler(DuplicatePaymentException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicatePayment(DuplicatePaymentException ex) {
        log.warn("Duplicate payment attempt: {}", ex.getMessage());

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("error", "DuplicatePayment");
        errorResponse.put("errorCode", ex.getErrorCode());
        errorResponse.put("message", ex.getMessage());
        errorResponse.put("employeeId", ex.getEmployeeId());
        errorResponse.put("periodStart", ex.getPeriodStart());
        errorResponse.put("periodEnd", ex.getPeriodEnd());

        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }



    private HttpStatus determineHttpStatus(PaymentException ex) {
        if (ex instanceof InvalidPaymentRequestException ||
                ex instanceof InvalidPaymentStatusException ||
                ex instanceof InvalidPaymentPeriodException) {
            return HttpStatus.BAD_REQUEST;
        } else if (ex instanceof PaymentNotFoundException ) {
            return HttpStatus.NOT_FOUND;
        } else if (ex instanceof DuplicatePaymentException) {
            return HttpStatus.CONFLICT;
        } else if (ex instanceof PaymentCalculationException ||
                ex instanceof InsufficientPaymentDataException) {
            return HttpStatus.UNPROCESSABLE_ENTITY;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}