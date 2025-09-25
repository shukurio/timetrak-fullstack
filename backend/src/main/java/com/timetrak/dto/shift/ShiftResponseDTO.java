package com.timetrak.dto.shift;

import com.timetrak.enums.ShiftStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShiftResponseDTO {

    private Long id;
    private LocalDateTime clockIn;
    private LocalDateTime clockOut;
    private Double hours;
    private ShiftStatus status;

    private Long employeeId;
    private Long employeeJobId;
    private String username;
    private String fullName;

    private String jobTitle;
    private BigDecimal hourlyWage;
    private BigDecimal shiftEarnings;

    public boolean isActive() {
        return status == ShiftStatus.ACTIVE;
    }


    public BigDecimal getShiftEarnings() {
        if((hourlyWage != null) && getHours() != null && status == ShiftStatus.COMPLETED) {
            BigDecimal earnings = hourlyWage.multiply(BigDecimal.valueOf(getHours()));
            return earnings.setScale(2, RoundingMode.HALF_UP);
        }
        return null;
    }
}
