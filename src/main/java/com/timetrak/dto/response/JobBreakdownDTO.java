package com.timetrak.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobBreakdownDTO {
    private Long id;
    private String jobTitle;
    private BigDecimal hoursWorked;
    private BigDecimal hourlyRate;
    private BigDecimal totalEarnings;
    private Integer shiftsCount;

    // COMPUTED PERCENTAGES
    private BigDecimal percentageOfTotalHours;
    private BigDecimal percentageOfTotalPay;
}
