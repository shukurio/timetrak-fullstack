package com.timetrak.dto.payment;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestDTO {

    @NotEmpty(message = "At least one employee ID is required")
    private List<Long> employeeIds;

    @NotNull(message = "Period start date is required")
    @PastOrPresent(message = "Period start date cannot be in the future")
    private LocalDate periodStart;

    @NotNull(message = "Period end date is required")
    @PastOrPresent(message = "Period end date cannot be in the future")
    private LocalDate periodEnd;

    private String notes;

    // CONVENIENCE METHODS
    public boolean isSingleEmployee() {
        return employeeIds != null && employeeIds.size() == 1;
    }

    public Long getSingleEmployeeId() {
        if (!isSingleEmployee()) {
            throw new IllegalStateException("Request contains multiple employees");
        }
        return employeeIds.getFirst();
    }

    public String getFormattedPeriod() {
        if (periodStart == null || periodEnd == null) return "";
        return periodStart + " to " + periodEnd;
    }

    public long getPeriodDays() {
        if (periodStart == null || periodEnd == null) return 0;
        return java.time.temporal.ChronoUnit.DAYS.between(periodStart, periodEnd) + 1;
    }
}