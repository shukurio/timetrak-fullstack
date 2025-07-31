package com.timetrak.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum PaymentStatus {
    CALCULATED,   // Automatically generated in system
    ISSUED,       // Employer confirms they gave paycheck
    COMPLETED,// Employee confirms they received paycheck
    VOIDED;       // Payment was cancelled before completion

    @JsonCreator
    public static PaymentStatus fromString(String value) {
        try {
            return PaymentStatus.valueOf(value.toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid payment status: " + value);
        }
    }

    }


