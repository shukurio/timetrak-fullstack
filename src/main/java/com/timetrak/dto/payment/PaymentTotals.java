package com.timetrak.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@AllArgsConstructor
@Getter
public class PaymentTotals {
    private BigDecimal totalHours;
    private BigDecimal totalEarnings;

}
