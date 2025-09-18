package com.timetrak.service.payment.calculation;

import com.timetrak.dto.payment.Period;
import com.timetrak.dto.payment.PaymentRequestDTO;
import com.timetrak.dto.payment.PaymentResponseDTO;

public interface PaymentCalculationService {
    PaymentResponseDTO calculatePaymentsForPeriod(Period period, Long companyId, Long initiatorId);
    PaymentResponseDTO calculatePayments(PaymentRequestDTO request,Long companyId,Long initiatorId);
}
