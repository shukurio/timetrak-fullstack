package com.timetrak.constant;

import java.math.BigDecimal;

public final class PaymentConstants {

    private PaymentConstants() {
    }

    // =============== VALIDATION LIMITS ===============
    public static final BigDecimal MAX_PAYMENT_AMOUNT = new BigDecimal("10000.00");
    public static final BigDecimal MAX_HOURS_PER_PERIOD = new BigDecimal("200.00");
    public static final int MAX_BATCH_SIZE = 500;

    // =============== BUSINESS RULES ===============
    public static final int MAX_SHIFTS_PER_PERIOD = 50;
    public static final int MAX_SHIFT_DURATION_HOURS = 24;

}