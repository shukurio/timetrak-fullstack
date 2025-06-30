package com.timetrak.entity;

import com.timetrak.enums.PayFrequency;
import com.timetrak.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;

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

    // PAYMENT METHOD SETTINGS
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod = PaymentMethod.CHECK;

    // CHECK SETTINGS
    @Column(name = "check_prefix", length = 10)
    private String checkPrefix = "CHK"; // "CHK-001", "PAY-001"

    @Column(name = "next_check_number", nullable = false)
    private Integer nextCheckNumber = 1;

    @Column(name = "check_memo", length = 100)
    private String checkMemo; // Default memo on checks (Name and period paid)

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

    // BUSINESS METHODS

    public String generateNextCheckNumber() {
        String checkNumber = checkPrefix + "-" + String.format("%03d", nextCheckNumber);
        nextCheckNumber++;
        return checkNumber;
    }

    public LocalDateTime getNextCalculationTime() {
        LocalDate today = LocalDate.now();
        LocalDate nextCalculationDate = today.with(TemporalAdjusters.next(calculationDay));
        return nextCalculationDate.atTime(calculationTime);
    }

    public String generateCheckMemo(String employeeName, String period) {
        if (checkMemo != null && !checkMemo.trim().isEmpty()) {
            return checkMemo;
        }
        return String.format("Payroll: %s (%s)", employeeName, period);

        //TODO need to fix th period logic, maybe object or retrive from payment in payment service
    }

}
