package com.timetrak.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;


import com.timetrak.enums.PaymentStatus;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponseDTO {

    // BASIC INFO
    private Long id;
    private Long employeeId;
    private String employeeName;
    private String employeeUsername;

    // PAYMENT PERIOD
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private String formattedPeriod; // "Jan 1 to Jan 15, 2024"

    // WORK & PAY SUMMARY
    private BigDecimal totalHours;
    private BigDecimal totalAmount;
    private Integer shiftsCount;
    private Integer jobsCount;

    // JOB BREAKDOWN
    private List<JobBreakdownDTO> jobBreakdowns;

    // STATUS & WORKFLOW
    private PaymentStatus status;
    private String statusDescription;

    // DATES
    private LocalDateTime calculatedAt;
    private LocalDate issuedAt;
    private LocalDate receivedAt;

    // TRACKING
    private Long calculatedBy;
    private String calculatedByName; // Admin who calculated

    // NOTES
    private String notes;

    // COMPUTED FIELDS
    private BigDecimal averageHourlyRate;
    private boolean isMultiJob;
    private boolean isReadyForCheckWriting;
    private boolean isIssued;
    private boolean isCompleted;
    private String paymentAge; // "3 days ago", "1 week ago"

    // HELPER METHODS FOR COMPUTED FIELDS

    public boolean getIsMultiJob() {
        return jobsCount != null && jobsCount > 1;
    }

    public boolean getIsReadyForCheckWriting() {
        return status == PaymentStatus.CALCULATED &&
                totalAmount != null &&
                totalAmount.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean getIsIssued() {
        return status == PaymentStatus.ISSUED || status == PaymentStatus.COMPLETED;
    }

    public boolean getIsCompleted() {
        return status == PaymentStatus.COMPLETED;
    }

    public String getStatusDescription() {
        if (status == null) return "";

        return switch (status) {
            case CALCULATED -> "Ready for check writing";
            case ISSUED -> "Check issued to employee";
            case COMPLETED -> "Payment completed";
            default -> status.name();
        };
    }
}
