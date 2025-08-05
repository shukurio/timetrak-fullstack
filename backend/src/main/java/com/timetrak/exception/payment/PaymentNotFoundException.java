package com.timetrak.exception.payment;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class PaymentNotFoundException extends PaymentException {
    private static final String ERROR_CODE = "PAYMENT_NOT_FOUND";

    private final Long paymentId;

    public PaymentNotFoundException(Long paymentId) {
        super(String.format("Payment not found with ID: %d", paymentId), ERROR_CODE);
        this.paymentId = paymentId;
    }

    public PaymentNotFoundException(String message) {
        super(message, ERROR_CODE);
        this.paymentId = null;
    }

    public Long getPaymentId() {
        return paymentId;
    }
}