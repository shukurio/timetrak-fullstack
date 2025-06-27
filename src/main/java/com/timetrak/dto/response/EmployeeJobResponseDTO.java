package com.timetrak.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class EmployeeJobResponseDTO {
    private Long employeeJobId;
    private String jobTitle;
    private BigDecimal hourlyWage;
}
