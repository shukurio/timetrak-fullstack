package com.timetrak.dto.employeeJob;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeJobRequestDTO {

    @NotEmpty(message = "Employee IDs list cannot be empty")
    @Size(max = 50, message = "Cannot assign more than 50 employees at once")
    private List<Long> employeeIds;

    @NotNull(message = "Job ID is required")
    private Long jobId;

    @DecimalMin(value = "0.01", message = "Hourly wage must be positive")
    @Digits(integer = 3, fraction = 2, message = "Invalid wage precision")
    private BigDecimal hourlyWage; // Applied to ALL employees (or uses job default)
}