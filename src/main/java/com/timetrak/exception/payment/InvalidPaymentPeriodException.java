package com.timetrak.exception.payment;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.LocalDate;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidPaymentPeriodException extends PaymentException {
    private static final String ERROR_CODE = "INVALID_PAYMENT_PERIOD";

    public InvalidPaymentPeriodException(String message) {
        super(message, ERROR_CODE);
    }

    public InvalidPaymentPeriodException(LocalDate start, LocalDate end, String reason) {
        super(String.format("Invalid payment period from %s to %s: %s", start, end, reason), ERROR_CODE);
    }
}