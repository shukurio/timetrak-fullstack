package com.timetrak.exception.payment;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidPaymentRequestException extends PaymentException {
    private static final String ERROR_CODE = "INVALID_PAYMENT_REQUEST";

    public InvalidPaymentRequestException(String message) {
        super(message, ERROR_CODE);
    }

    public InvalidPaymentRequestException(String message, Throwable cause) {
        super(message, ERROR_CODE, cause);
    }
}