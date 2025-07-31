package com.timetrak.entity;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.timetrak.enums.PaymentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
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
    @NotNull
    private Long companyId;

    // PAYMENT PERIOD
    @Column(name = "period_start", nullable = false)
    @NotNull
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    @NotNull
    private LocalDate periodEnd;

    // WORK & PAY DETAILS
    @Column(name = "total_hours", nullable = false, precision = 10, scale = 2)
    @NotNull
    private BigDecimal totalHours = BigDecimal.ZERO;

    @Column(name = "total_earnings", nullable = false, precision = 12, scale = 2)
    @NotNull
    private BigDecimal totalEarnings = BigDecimal.ZERO;

    @Column(name = "shifts_count", nullable = false)
    @NotNull
    private Integer shiftsCount;

    // STATUS
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @NotNull
    private PaymentStatus status = PaymentStatus.CALCULATED;

    // MANUAL CHECK PROCESS

    // TRACKING
    @Column(name = "calculated_at")
    private LocalDateTime calculatedAt;

    @Column(name = "voided_at")
    private LocalDateTime voidedAt;

    @Column(name = "issued_at")
    private LocalDateTime issuedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "modified_by")
    @NotNull
    private Long modifiedBy;

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




    public boolean isCompleted() {
        return status == PaymentStatus.COMPLETED;
    }

    public String getFormattedPeriod() {
        return periodStart + " to " + periodEnd;
    }


    public void markCheckIssued(LocalDateTime date) {
        this.status = PaymentStatus.ISSUED;
        this.issuedAt = date;
    }

    public void markCompleted(LocalDateTime date) {
        this.status = PaymentStatus.COMPLETED;
        this.completedAt = date;
    }

}