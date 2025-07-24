package com.timetrak.entity;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.timetrak.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;


@Entity
@Table(name = "payment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Payment extends BaseEntity {

    // EMPLOYEE REFERENCE
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "payments"})
    private Employee employee;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    // PAYMENT PERIOD
    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    // WORK & PAY DETAILS
    @Column(name = "total_hours", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalHours = BigDecimal.ZERO;

    @Column(name = "total_earnings", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalEarnings = BigDecimal.ZERO;

    @Column(name = "shifts_count", nullable = false)
    private Integer shiftsCount = 0;

    // STATUS
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status = PaymentStatus.CALCULATED;

    // MANUAL CHECK PROCESS

    // TRACKING
    @Column(name = "calculated_at")
    private LocalDateTime calculatedAt;

    @Column(name = "issued_at")
    private LocalDate issuedAt;

    @Column(name = "completed_at")
    private LocalDate completedAt;

    @Column(name = "calculated_by")
    private Long calculatedBy;

    // NOTES
    @Column(name = "notes", length = 500)
    private String notes;

    public void setEmployee(Employee employee) {
        this.employee = employee;
        if (employee != null && employee.getCompany() != null) {
            this.companyId = employee.getCompany().getId();
        }
    }

    // BUSINESS METHODS


    public boolean isReadyForCheckWriting() {
        return status == PaymentStatus.CALCULATED &&
                totalEarnings.compareTo(BigDecimal.ZERO) > 0;
    }


    public boolean isCompleted() {
        return status == PaymentStatus.COMPLETED;
    }

    public String getFormattedPeriod() {
        return periodStart + " to " + periodEnd;
    }


    public void markCheckIssued(LocalDate date) {
        this.status = PaymentStatus.ISSUED;
        this.issuedAt = date;
    }

    public void markCompleted(LocalDate date) {
        this.status = PaymentStatus.COMPLETED;
        this.completedAt = date;
    }

}