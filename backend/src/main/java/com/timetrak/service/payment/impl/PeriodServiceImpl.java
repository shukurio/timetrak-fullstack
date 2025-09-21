package com.timetrak.service.payment.impl;

import com.timetrak.dto.payment.Period;
import com.timetrak.dto.payment.PeriodSummaryDTO;
import com.timetrak.entity.CompanyPaymentSettings;
import com.timetrak.enums.PayFrequency;
import com.timetrak.exception.payment.PaymentSettingsConfigurationException;
import com.timetrak.mapper.PeriodMapper;
import com.timetrak.repository.CompanyPaymentSettingsRepository;
import com.timetrak.service.payment.PeriodService;
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
public class PeriodServiceImpl implements PeriodService {

    private final CompanyPaymentSettingsRepository companyPaymentSettingsRepository;
    private final PeriodMapper periodMapper;

    @Override
    public Period getCurrentPeriod(Long companyId) {
        LocalDate today = LocalDate.now();
        return getPeriodForDate(today, companyId);
    }

    @Override
    public PeriodSummaryDTO getCurrentPeriodDTO(Long companyId) {
        Period period = getCurrentPeriod(companyId);

        return periodMapper.toSummaryDTO(period);
    }

    @Override
    public Period getPeriodForDate(LocalDate date, Long companyId) {
        CompanyPaymentSettings settings = getCompanyPaymentSettings(companyId);

        if (settings.getFirstDay() == null) {
            throw new PaymentSettingsConfigurationException(
                    "Company payment settings not configured. Please set up first payment day.");
        }

        LocalDate firstDay = settings.getFirstDay();
        PayFrequency frequency = settings.getPayFrequency();

        return calculatePeriod(date, firstDay, frequency);
    }

    @Override
    public Period getPeriodByNumber(Integer periodNumber, Long companyId) {

        CompanyPaymentSettings settings = getCompanyPaymentSettings(companyId);
        LocalDate baseDate = settings.getFirstDay();
        PayFrequency frequency = settings.getPayFrequency();

        LocalDate targetDate = switch (frequency) {
            case WEEKLY -> baseDate.plusWeeks(periodNumber - 1);
            case BIWEEKLY -> baseDate.plusWeeks((periodNumber - 1) * 2L);
            case MONTHLY -> baseDate.plusMonths(periodNumber - 1);
        };

        return calculatePeriod(targetDate, baseDate, frequency);
    }

    @Override
    public List<Period> getAvailablePeriods(int numberOfPeriods, Long companyId) {
        CompanyPaymentSettings settings = getCompanyPaymentSettings(companyId);

        List<Period> periods = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = 0; i < numberOfPeriods; i++) {
            LocalDate periodDate = today.minusWeeks((long) i * getWeeksForFrequency(settings.getPayFrequency()));
            periods.add(getPeriodForDate(periodDate, companyId));
        }

        return periods;
    }

    @Override
    public List<PeriodSummaryDTO> getAvailablePaymentPeriodSummaries(int numberOfPeriods, Long companyId) {
        List<Period> periods = getAvailablePeriods(numberOfPeriods, companyId);
        return periods.stream()
                .map(periodMapper::toSummaryDTO)
                .toList();
    }

    @Override
    public PeriodSummaryDTO getMostRecentCompletedPeriodSummary(Long companyId) {
        // Get last 2 periods to get the most recent completed one
        List<Period> periods = getAvailablePeriods(2, companyId);
        
        // Return the previous period (index 1), or current if only one exists
        Period mostRecentCompleted = periods.size() > 1 ? periods.get(1) : periods.get(0);
        
        return periodMapper.toSummaryDTO(mostRecentCompleted);
    }

    // Helper method to get company payment settings
    private CompanyPaymentSettings getCompanyPaymentSettings(Long companyId) {
        return companyPaymentSettingsRepository.findByCompanyId(companyId)
                .orElseThrow(() -> new PaymentSettingsConfigurationException(
                        "Payment settings not found for company " + companyId +
                                ". Please configure payment settings first."));
    }

    private Period calculatePeriod(LocalDate targetDate, LocalDate firstDay, PayFrequency frequency) {
        return switch (frequency) {
            case WEEKLY -> calculateWeeklyPeriod(targetDate, firstDay);
            case BIWEEKLY -> calculateBiweeklyPeriod(targetDate, firstDay);
            case MONTHLY -> calculateMonthlyPeriod(targetDate, firstDay);
        };
    }

    private Period calculateWeeklyPeriod(LocalDate targetDate, LocalDate firstDay) {
        // Calculate how many days since first day
        long daysSinceFirst = ChronoUnit.DAYS.between(firstDay, targetDate);

        // Calculate which weekly period we're in (0-based)
        long periodNumber = daysSinceFirst >= 0 ? daysSinceFirst / 7 : (daysSinceFirst - 6) / 7;

        // Calculate period start and end
        LocalDate periodStart = firstDay.plusWeeks(periodNumber);
        LocalDate periodEnd = periodStart.plusDays(6); // 7 days total (0-6)

        return new Period(periodStart, periodEnd, PayFrequency.WEEKLY, (int)periodNumber + 1);
    }

    private Period calculateBiweeklyPeriod(LocalDate targetDate, LocalDate firstDay) {
        // Calculate how many days since first day
        long daysSinceFirst = ChronoUnit.DAYS.between(firstDay, targetDate);

        // Calculate which biweekly period we're in (0-based)
        long periodNumber = daysSinceFirst >= 0 ? daysSinceFirst / 14 : (daysSinceFirst - 13) / 14;

        // Calculate period start and end
        LocalDate periodStart = firstDay.plusWeeks(periodNumber * 2);
        LocalDate periodEnd = periodStart.plusDays(13); // 14 days total (0-13)

        return new Period(periodStart, periodEnd, PayFrequency.BIWEEKLY, (int)periodNumber + 1);
    }

    private Period calculateMonthlyPeriod(LocalDate targetDate, LocalDate firstDay) {
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

        return new Period(periodStart, periodEnd, PayFrequency.MONTHLY, periodNumber);
    }

    private int getWeeksForFrequency(PayFrequency frequency) {
        return switch (frequency) {
            case WEEKLY -> 1;
            case BIWEEKLY -> 2;
            case MONTHLY -> 4;
        };
    }
}