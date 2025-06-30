package com.timetrak.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Table(name = "payment_job_breakdown")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PaymentJobBreakdown extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Column(name = "job_title", nullable = false, length = 100)
    private String jobTitle; // "Server", "Banquet Server"

    @Column(name = "hours_worked", nullable = false, precision = 10, scale = 2)
    private BigDecimal hoursWorked;

    @Column(name = "hourly_rate", nullable = false, precision = 8, scale = 2)
    private BigDecimal hourlyRate;

    @Column(name = "total_earnings", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalEarnings;

    @Column(name = "shifts_count", nullable = false)
    private Integer shiftsCount;
}