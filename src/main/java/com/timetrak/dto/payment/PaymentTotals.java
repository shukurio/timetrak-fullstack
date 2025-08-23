package com.timetrak.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@AllArgsConstructor
@Getter
public class PaymentTotals {
    private final BigDecimal totalHours;
    private final BigDecimal totalEarnings;
    private final Integer shiftsCount;

}
