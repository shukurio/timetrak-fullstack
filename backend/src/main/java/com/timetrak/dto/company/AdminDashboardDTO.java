package com.timetrak.dto.company;

import com.timetrak.dto.payment.PaymentDetailsDTO;
import com.timetrak.dto.shift.ShiftResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminDashboardDTO {
    private long activeEmployeeCount;
    private long pendingEmployeeCount;
    private long activeShiftsCount;
    private Double thisPeriodHours;
    private BigDecimal thisPeriodRevenue;
    //Period over Period  = pop
    private Double popHoursAmountChange;
    private BigDecimal popRevenueAmountChange;
    private double popHoursChange;
    private double popRevenueChange;
    List<ShiftResponseDTO> recentShifts;
    List<PaymentDetailsDTO> recentPayments;

}
