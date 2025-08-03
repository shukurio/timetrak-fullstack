package com.timetrak.exception.payment;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class InsufficientPaymentDataException extends PaymentException {
    private static final String ERROR_CODE = "INSUFFICIENT_PAYMENT_DATA";

    public InsufficientPaymentDataException(String message) {
        super(message, ERROR_CODE);
    }

    public InsufficientPaymentDataException(String dataType, String reason) {
        super(String.format("Insufficient %s data: %s", dataType, reason), ERROR_CODE);
    }
}