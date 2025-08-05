package com.timetrak.exception.payment;

import com.timetrak.enums.PaymentStatus;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidPaymentStatusException extends PaymentException {
    private static final String ERROR_CODE = "INVALID_PAYMENT_STATUS";

    private final PaymentStatus currentStatus;
    private final PaymentStatus requestedStatus;

    public InvalidPaymentStatusException(PaymentStatus currentStatus, PaymentStatus requestedStatus) {
        super(String.format("Cannot transition payment from %s to %s.",
                currentStatus, requestedStatus), ERROR_CODE);
        this.currentStatus = currentStatus;
        this.requestedStatus = requestedStatus;
    }

    public InvalidPaymentStatusException(String message) {
        super(message, ERROR_CODE);
        this.currentStatus = null;
        this.requestedStatus = null;
    }

    public PaymentStatus getCurrentStatus() {
        return currentStatus;
    }

    public PaymentStatus getRequestedStatus() {
        return requestedStatus;
    }
}