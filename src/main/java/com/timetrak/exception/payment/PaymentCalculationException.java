package com.timetrak.exception.payment;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class PaymentCalculationException extends PaymentException {
    private static final String ERROR_CODE = "PAYMENT_CALCULATION_ERROR";

    public PaymentCalculationException(String message) {
        super(message, ERROR_CODE);
    }

    public PaymentCalculationException(String message, Throwable cause) {
        super(message, ERROR_CODE, cause);
    }
}