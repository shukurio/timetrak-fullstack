package com.timetrak.dto.company.settings;

import com.timetrak.enums.PayFrequency;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class CompanyPaymentSettingsResponseDTO {

    private Long id;
    private Long companyId;

    // Payroll schedule
    private PayFrequency payFrequency;
    private LocalDate firstDay;
    private DayOfWeek calculationDay;
    private LocalTime calculationTime;

    // Settings & Preferences
    private Boolean autoCalculate;
    private Integer gracePeriodHours;

    // Notifications
    private Boolean notifyOnCalculation;
    private String notificationEmail;
}
