package com.timetrak.service.payment.calculation;

import com.timetrak.dto.payment.PaymentPeriod;
import com.timetrak.dto.payment.PaymentRequestDTO;
import com.timetrak.dto.payment.PaymentResponseDTO;

public interface PaymentCalculationService {
    PaymentResponseDTO calculatePaymentsForPeriod(PaymentPeriod paymentPeriod,Long companyId, Long initiatorId);
    PaymentResponseDTO calculatePayments(PaymentRequestDTO request,Long companyId,Long initiatorId);
}
