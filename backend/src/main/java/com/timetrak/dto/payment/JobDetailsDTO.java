package com.timetrak.dto.payment;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.math.BigDecimal;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobDetailsDTO {

    @NotNull(message = "Job title is required")
    private String jobTitle;

    @NotNull(message = "Hours worked is required")
    @DecimalMin(value = "0.0", message = "Hours cannot be negative")
    @Digits(integer = 3, fraction = 2, message = "Hours precision invalid")
    private BigDecimal totalHours;

    @NotNull(message = "Hourly rate is required")
    @DecimalMin(value = "0.01", message = "Rate must be positive")
    @Digits(integer = 3, fraction = 2, message = "Rate precision invalid")
    private BigDecimal hourlyRate;

    @NotNull(message = "Total earnings is required")
    @DecimalMin(value = "0.0", message = "Earnings cannot be negative")
    @Digits(integer = 5, fraction = 2, message = "Earnings precision invalid")
    private BigDecimal totalEarnings;

    @NotNull(message = "Shifts count is required")
    @Min(value = 1, message = "Must have at least 1 shift")
    private Integer shiftsCount;

    // COMPUTED PERCENTAGES
    @DecimalMin(value = "0.0", message = "Percentage cannot be negative")
    @DecimalMax(value = "100.0", message = "Percentage cannot exceed 100%")
    @Digits(integer = 3, fraction = 2, message = "Percentage precision invalid")
    private BigDecimal percentageOfTotalHours;

    @DecimalMin(value = "0.0", message = "Percentage cannot be negative")
    @DecimalMax(value = "100.0", message = "Percentage cannot exceed 100%")
    @Digits(integer = 3, fraction = 2, message = "Percentage precision invalid")
    private BigDecimal percentageOfTotalPay;
}