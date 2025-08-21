package com.timetrak.dto.job;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class EmployeeJobResponseDTO {
    private Long employeeJobId;
    private String jobTitle;
    private BigDecimal hourlyWage;
}
