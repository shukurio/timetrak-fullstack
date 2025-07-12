package com.timetrak.dto.payment;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class PaymentBatchRequestDTO {
    @NotNull(message = "Employee ID is required")
    private List<Long> employeeIds;

    @NotNull(message = "Period start date is required")
    @PastOrPresent(message = "Period start date cannot be in the future")
    private LocalDate periodStart;

    @NotNull(message = "Period end date is required")
    @PastOrPresent(message = "Period end date cannot be in the future")
    private LocalDate periodEnd;

    private String notes;


    // VALIDATION HELPER METHOD
    public void validate() {
        if (periodStart != null && periodEnd != null) {
            if (periodStart.isAfter(periodEnd)) {
                throw new IllegalArgumentException("Period start date must be before or equal to end date");
            }

            if (periodStart.isEqual(periodEnd)) {
                throw new IllegalArgumentException("Period must span at least one day");
            }

            // Prevent very long periods (more than 8 weeks)
            if (periodStart.plusWeeks(8).isBefore(periodEnd)) {
                throw new IllegalArgumentException("Payment period cannot exceed 8 weeks");
            }
        }
    }

    // HELPER METHODS
    public String getFormattedPeriod() {
        if (periodStart == null || periodEnd == null) return "";
        return periodStart + " to " + periodEnd;
    }

    public long getPeriodDays() {
        if (periodStart == null || periodEnd == null) return 0;
        return java.time.temporal.ChronoUnit.DAYS.between(periodStart, periodEnd) + 1;
    }
}