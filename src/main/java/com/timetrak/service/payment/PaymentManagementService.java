package com.timetrak.service.payment;

import com.timetrak.dto.payment.PaymentDetailsDTO;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public interface PaymentManagementService {

    // =============== BASIC STATUS MANAGEMENT ===============

    PaymentDetailsDTO markPaymentIssued(Long paymentId, LocalDate issuedDate);
    PaymentDetailsDTO approvePayment(Long paymentId, String approvedBy);
    PaymentDetailsDTO voidPayment(Long paymentId, String reason, String voidedBy);
    List<PaymentDetailsDTO> bulkApprovePayments(List<Long> paymentIds, String approvedBy);
    PaymentDetailsDTO reprocessPayment(Long paymentId, String reason);
    byte[] exportPayments(Long companyId,
                          LocalDate startDate,
                          LocalDate endDate,
                          String format); // CSV, PDF, etc.

    List<PaymentDetailsDTO> bulkMarkPaymentsIssued(List<Long> paymentIds,
                                                   LocalDate issuedDate,
                                                   String issuedBy);

    PaymentDetailsDTO addCheckNumber(Long paymentId, String checkNumber);


}
