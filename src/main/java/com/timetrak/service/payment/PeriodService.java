package com.timetrak.service.payment;

import com.timetrak.dto.payment.Period;
import com.timetrak.dto.payment.PeriodSummaryDTO;

import java.time.LocalDate;
import java.util.List;

public interface PeriodService {

    /**
     * Get the current payment period for a company
     */
    Period getCurrentPeriod(Long companyId);

    /**
     * Get the current payment period for a company
     */
    PeriodSummaryDTO getCurrentPeriodDTO(Long companyId);

    /**
     * Get payment period for a specific date
     */
    Period getPeriodForDate(LocalDate date, Long companyId);

    /**
     * Get payment period by period number
     */
    Period getPeriodByNumber(Integer periodNumber, Long companyId);

    /**
     * Get list of available payment periods (recent periods)
     */
    List<Period> getAvailablePeriods(int numberOfPeriods, Long companyId);

    /**
     * Get list of available payment periods as summary DTOs
     */
    List<PeriodSummaryDTO> getAvailablePaymentPeriodSummaries(int numberOfPeriods, Long companyId);

    /**
     * Get most recent completed payment period as summary DTO
     */
    PeriodSummaryDTO getMostRecentCompletedPeriodSummary(Long companyId);
}