package com.timetrak.service.payment;

import com.timetrak.dto.payment.PaymentPeriod;
import com.timetrak.entity.CompanyPaymentSettings;
import com.timetrak.enums.PayFrequency;
import com.timetrak.exception.payment.PaymentSettingsConfigurationException;
import com.timetrak.repository.CompanyPaymentSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentPeriodService {

    private final CompanyPaymentSettingsRepository companyPaymentSettingsRepository;

    public PaymentPeriod getCurrentPaymentPeriod(Long companyId) {
        LocalDate today = LocalDate.now();
        return getPaymentPeriodForDate(today, companyId);
    }

    public PaymentPeriod getPaymentPeriodForDate(LocalDate date, Long companyId) {
        CompanyPaymentSettings settings = getCompanyPaymentSettings(companyId);

        if (settings.getFirstDay() == null) {
            throw new PaymentSettingsConfigurationException(
                    "Company payment settings not configured. Please set up first payment day.");
        }

        LocalDate firstDay = settings.getFirstDay();
        PayFrequency frequency = settings.getPayFrequency();

        return calculatePaymentPeriod(date, firstDay, frequency);
    }

    public PaymentPeriod getPaymentPeriodByNumber(Integer periodNumber, Long companyId) {

        CompanyPaymentSettings settings = getCompanyPaymentSettings(companyId);
        LocalDate baseDate = settings.getFirstDay();
        PayFrequency frequency = settings.getPayFrequency();

        LocalDate targetDate = switch (frequency) {
            case WEEKLY -> baseDate.plusWeeks(periodNumber - 1);
            case BIWEEKLY -> baseDate.plusWeeks((periodNumber - 1) * 2L);
            case MONTHLY -> baseDate.plusMonths(periodNumber - 1);
        };

        return calculatePaymentPeriod(targetDate, baseDate, frequency);
    }


    public List<PaymentPeriod> getAvailablePaymentPeriods(int numberOfPeriods, Long companyId) {
        CompanyPaymentSettings settings = getCompanyPaymentSettings(companyId);

        List<PaymentPeriod> periods = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = 0; i < numberOfPeriods; i++) {
            LocalDate periodDate = today.minusWeeks((long) i * getWeeksForFrequency(settings.getPayFrequency()));
            periods.add(getPaymentPeriodForDate(periodDate, companyId));
        }

        return periods;
    }

    // Helper method to get company payment settings
    private CompanyPaymentSettings getCompanyPaymentSettings(Long companyId) {
        return companyPaymentSettingsRepository.findByCompanyId(companyId)
                .orElseThrow(() -> new PaymentSettingsConfigurationException(
                        "Payment settings not found for company " + companyId +
                                ". Please configure payment settings first."));
    }

    private PaymentPeriod calculatePaymentPeriod(LocalDate targetDate, LocalDate firstDay, PayFrequency frequency) {
        return switch (frequency) {
            case WEEKLY -> calculateWeeklyPeriod(targetDate, firstDay);
            case BIWEEKLY -> calculateBiweeklyPeriod(targetDate, firstDay);
            case MONTHLY -> calculateMonthlyPeriod(targetDate, firstDay);
        };
    }

    private PaymentPeriod calculateWeeklyPeriod(LocalDate targetDate, LocalDate firstDay) {
        // Calculate how many days since first day
        long daysSinceFirst = ChronoUnit.DAYS.between(firstDay, targetDate);

        // Calculate which weekly period we're in (0-based)
        long periodNumber = daysSinceFirst >= 0 ? daysSinceFirst / 7 : (daysSinceFirst - 6) / 7;

        // Calculate period start and end
        LocalDate periodStart = firstDay.plusWeeks(periodNumber);
        LocalDate periodEnd = periodStart.plusDays(6); // 7 days total (0-6)

        return new PaymentPeriod(periodStart, periodEnd, PayFrequency.WEEKLY, (int)periodNumber + 1);
    }

    private PaymentPeriod calculateBiweeklyPeriod(LocalDate targetDate, LocalDate firstDay) {
        // Calculate how many days since first day
        long daysSinceFirst = ChronoUnit.DAYS.between(firstDay, targetDate);

        // Calculate which biweekly period we're in (0-based)
        long periodNumber = daysSinceFirst >= 0 ? daysSinceFirst / 14 : (daysSinceFirst - 13) / 14;

        // Calculate period start and end
        LocalDate periodStart = firstDay.plusWeeks(periodNumber * 2);
        LocalDate periodEnd = periodStart.plusDays(13); // 14 days total (0-13)

        return new PaymentPeriod(periodStart, periodEnd, PayFrequency.BIWEEKLY, (int)periodNumber + 1);
    }

    private PaymentPeriod calculateMonthlyPeriod(LocalDate targetDate, LocalDate firstDay) {
        // For monthly, we need to find which month period the target date falls into
        LocalDate periodStart = firstDay;
        int periodNumber = 1;

        // Move forward month by month until we find the period containing target date
        while (periodStart.plusMonths(1).minusDays(1).isBefore(targetDate)) {
            periodStart = periodStart.plusMonths(1);
            periodNumber++;
        }

        // Move backward if we went too far
        while (periodStart.isAfter(targetDate)) {
            periodStart = periodStart.minusMonths(1);
            periodNumber--;
        }

        LocalDate periodEnd = periodStart.plusMonths(1).minusDays(1);

        return new PaymentPeriod(periodStart, periodEnd, PayFrequency.MONTHLY, periodNumber);
    }

    private int getWeeksForFrequency(PayFrequency frequency) {
        return switch (frequency) {
            case WEEKLY -> 1;
            case BIWEEKLY -> 2;
            case MONTHLY -> 4;
        };
    }
}