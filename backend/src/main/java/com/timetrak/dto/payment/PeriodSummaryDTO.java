package com.timetrak.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PeriodSummaryDTO {
    
    private Integer periodNumber;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private String displayLabel;  // "Dec 16 - Dec 31, 2024"
}