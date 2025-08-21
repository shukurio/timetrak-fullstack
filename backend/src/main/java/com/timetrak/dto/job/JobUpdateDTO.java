package com.timetrak.dto.job;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class JobUpdateDTO {
    private String jobTitle;
    private BigDecimal hourlyWage;
}