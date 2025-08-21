package com.timetrak.dto.employeeJob;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeJobUpdateDTO {
    
    @DecimalMin(value = "0.01", message = "Hourly wage must be positive")
    @Digits(integer = 3, fraction = 2, message = "Invalid wage precision")
    private BigDecimal hourlyWage;
}
