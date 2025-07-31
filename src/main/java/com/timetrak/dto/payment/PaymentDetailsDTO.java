package com.timetrak.dto.payment;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.timetrak.enums.PaymentStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDetailsDTO {

    // =============== CORE IDENTIFICATION ===============
    @NotNull(message = "Payment ID cannot be null")
    private Long id;

    @NotNull(message = "Employee ID cannot be null")
    private Long employeeId;

    @NotBlank(message = "Employee name is required")
    @Size(max = 100, message = "Employee name cannot exceed 100 characters")
    private String employeeName;

    private String employeeUsername;

    // =============== PAYMENT PERIOD ===============
    @NotNull(message = "Period start date is required")
    private LocalDate periodStart;

    @NotNull(message = "Period end date is required")
    private LocalDate periodEnd;

    private String formattedPeriod; // "Jan 1 - Jan 15, 2024"

    // =============== WORK & PAY SUMMARY ===============
    @NotNull(message = "Total hours is required")
    @DecimalMin(value = "0.0", message = "Total hours cannot be negative")
    @Digits(integer = 3, fraction = 2, message = "Total hours precision invalid")
    private BigDecimal totalHours;

    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.0", message = "Total amount cannot be negative")
    @Digits(integer = 5, fraction = 2, message = "Total amount precision invalid")
    private BigDecimal totalEarnings;

    @NotNull(message = "Shifts count is required")
    @Min(value = 0, message = "Shifts count cannot be negative")
    private Integer shiftsCount;

    @Min(value = 0, message = "Jobs count cannot be negative")
    private Integer jobsCount;

    // =============== JOB BREAKDOWN ===============
    @Valid
    private List<JobDetailsDTO> jobDetails;

    // =============== STATUS & WORKFLOW ===============
    @NotNull(message = "Payment status is required")
    private PaymentStatus status;

    // =============== TIMESTAMPS ===============
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime calculatedAt;
    private LocalDate issuedAt;
    private LocalDate completedAt;

    // =============== TRACKING ===============
    private Long modifiedBy;
    private String modifiedByName;

    // =============== NOTES ===============
    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;

    // =============== COMPUTED FIELDS ===============
    @DecimalMin(value = "0.0", message = "Average hourly rate cannot be negative")
    @Digits(integer = 3, fraction = 2, message = "Average hourly rate precision invalid")
    private BigDecimal averageHourlyRate;


    public String getFormattedPeriod() {
        if (periodStart == null || periodEnd == null) return "";

        java.time.format.DateTimeFormatter formatter =
                java.time.format.DateTimeFormatter.ofPattern("MMM d");

        return periodStart.format(formatter) +
                " - " +
                periodEnd.format(java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy"));
    }

}