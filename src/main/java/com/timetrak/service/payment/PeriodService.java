package com.timetrak.service.payment;

import com.timetrak.dto.payment.PaymentPeriod;
import com.timetrak.dto.payment.PaymentPeriodSummaryDTO;

import java.time.LocalDate;
import java.util.List;

public interface PeriodService {

    /**
     * Get the current payment period for a company
     */
    PaymentPeriod getCurrentPaymentPeriod(Long companyId);

    /**
     * Get payment period for a specific date
     */
    PaymentPeriod getPaymentPeriodForDate(LocalDate date, Long companyId);

    /**
     * Get payment period by period number
     */
    PaymentPeriod getPaymentPeriodByNumber(Integer periodNumber, Long companyId);

    /**
     * Get list of available payment periods (recent periods)
     */
    List<PaymentPeriod> getAvailablePaymentPeriods(int numberOfPeriods, Long companyId);

    /**
     * Get list of available payment periods as summary DTOs
     */
    List<PaymentPeriodSummaryDTO> getAvailablePaymentPeriodSummaries(int numberOfPeriods, Long companyId);

    /**
     * Get most recent completed payment period as summary DTO
     */
    PaymentPeriodSummaryDTO getMostRecentCompletedPeriodSummary(Long companyId);
}