package com.timetrak.dto.job;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobRequestDTO {

    @NotBlank(message = "Job title is required")
    private String jobTitle;

    @NotNull(message = "Hourly wage is required")
    @DecimalMin(value = "0.01", message = "Hourly wage must be positive")
    @Digits(integer = 3, fraction = 2, message = "Invalid wage precision")
    private BigDecimal hourlyWage;

}