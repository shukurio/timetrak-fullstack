package com.timetrak.dto.employee;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDashboardSummary {

    // =============== PERIOD ===============
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private String formattedPeriod;
    private Integer periodNumber;

    // =============== CURRENT PERIOD SUMMARY ===============
    @Min(value = 0, message = "Total hours cannot be negative")
    private BigDecimal currentPeriodHours;

    @DecimalMin(value = "0.0", message = "Current earnings cannot be negative")
    private BigDecimal currentPeriodEarnings;

    @Min(value = 0, message = "Current shifts cannot be negative")
    private Integer currentPeriodShifts;


    // =============== TODAY'S SUMMARY ===============
    private BigDecimal todayHours;
    private BigDecimal todayEarnings;
    private Integer todayShifts;

    // =============== PERFORMANCE METRICS ===============
    @DecimalMin(value = "0.0", message = "Average hours cannot be negative")
    private BigDecimal averageHoursPerDay;

    @DecimalMin(value = "0.0", message = "Average earnings cannot be negative")
    private BigDecimal averageEarningsPerDay;

    @DecimalMin(value = "0.0", message = "Average hourly rate cannot be negative")
    private BigDecimal averageHourlyRate;



}