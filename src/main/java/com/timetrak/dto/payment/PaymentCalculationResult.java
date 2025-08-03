package com.timetrak.dto.payment;

import com.timetrak.entity.Payment;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PaymentCalculationResult {
    private List<Payment> successful;
    private List<PaymentFailureResponse> errors;
    private final PaymentPeriod paymentPeriod;
}
