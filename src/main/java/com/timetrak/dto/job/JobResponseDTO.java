package com.timetrak.dto.job;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class JobResponseDTO {
    private Long id;
    private String jobTitle;
    private BigDecimal hourlyWage;
    private Long departmentId;
    private String departmentName;
}
