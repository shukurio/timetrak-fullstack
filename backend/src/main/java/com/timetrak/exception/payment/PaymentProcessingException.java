package com.timetrak.exception.payment;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class PaymentProcessingException extends PaymentException {
    private static final String ERROR_CODE = "PAYMENT_PROCESSING_ERROR";

    public PaymentProcessingException(String message) {
        super(message, ERROR_CODE);
    }

    public PaymentProcessingException(String message, Throwable cause) {
        super(message, ERROR_CODE, cause);
    }
}