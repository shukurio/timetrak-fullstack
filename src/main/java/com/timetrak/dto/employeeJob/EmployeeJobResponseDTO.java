package com.timetrak.dto.employeeJob;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeJobResponseDTO {
    private Long employeeJobId;
    private Long employeeId;
    private Long companyId;
    private String employeeName;
    private Long jobId;
    private String jobTitle;
    private String departmentName;
    private BigDecimal hourlyWage;
    private BigDecimal jobDefaultWage;
    private LocalDateTime assignedAt;
    private Boolean isActive;
}
