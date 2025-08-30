package com.timetrak.entity;

import com.timetrak.enums.PayFrequency;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "company_payment_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class CompanyPaymentSettings extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false, unique = true)
    private Company company;

    // PAYROLL SCHEDULE
    @Enumerated(EnumType.STRING)
    @Column(name = "pay_frequency", nullable = false)
    private PayFrequency payFrequency = PayFrequency.BIWEEKLY;

    @Column(name="first_day")
    private LocalDate firstDay;

    @Enumerated(EnumType.STRING)
    @Column(name = "calculation_day", nullable = false)
    private DayOfWeek calculationDay = DayOfWeek.TUESDAY;

    @Column(name = "calculation_time", nullable = false)
    private LocalTime calculationTime = LocalTime.of(2, 0); // 6 PM


    // SETTINGS & PREFERENCES
    @Column(name = "auto_calculate", nullable = false)
    private Boolean autoCalculate = true;

    @Column(name = "grace_period_hours", nullable = false)
    private Integer gracePeriodHours = 72; // Hours after period to allow late clock-outs

    // NOTIFICATIONS
    @Column(name = "notify_on_calculation", nullable = false)
    private Boolean notifyOnCalculation = true;

    @Column(name = "notification_email", length = 100)
    private String notificationEmail;

}
