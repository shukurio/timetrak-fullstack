package com.timetrak.service.payment;

import com.timetrak.dto.payment.PaymentBatchRequestDTO;
import com.timetrak.dto.payment.PaymentRequestDTO;
import com.timetrak.dto.payment.JobDetailsDTO;
import com.timetrak.dto.payment.PaymentDashboardDTO;
import com.timetrak.dto.payment.PaymentResponseDTO;
import com.timetrak.dto.payment.PaymentSummaryDTO;
import com.timetrak.enums.PaymentStatus;
import com.timetrak.mapper.PaymentMapper;
import com.timetrak.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;


@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentServiceImpl  implements PaymentService{

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    @Override
    public PaymentResponseDTO calculatePayment(PaymentRequestDTO request) {
        throw new UnsupportedOperationException("This feature is not supported yet");

    }

    @Override
    public Page<PaymentResponseDTO> calculateBatchPayments(PaymentBatchRequestDTO request, Pageable pageable) {
        throw new UnsupportedOperationException("This feature is not supported yet");

    }

    @Override
    public PaymentResponseDTO getPaymentById(Long paymentId) {
        throw new UnsupportedOperationException("This feature is not supported yet");

    }

    @Override
    public PaymentResponseDTO getLastPaymentForEmployee(Long employeeId) {
        throw new UnsupportedOperationException("This feature is not supported yet");

    }

    @Override
    public Page<PaymentResponseDTO> getAllPaymentsForEmployee(Long employeeId, Pageable pageable) {
        throw new UnsupportedOperationException("This feature is not supported yet");

    }

    @Override
    public Page<PaymentResponseDTO> getAllPaymentsForCompany(Long companyId, Pageable pageable) {
        throw new UnsupportedOperationException("This feature is not supported yet");

    }

    @Override
    public Page<PaymentResponseDTO> getAllPaymentsForLastPeriod(Pageable pageable) {
        throw new UnsupportedOperationException("This feature is not supported yet");

    }

    @Override
    public PaymentResponseDTO markPaymentIssued(Long paymentId, LocalDate issuedDate) {
        throw new UnsupportedOperationException("This feature is not supported yet");

    }

    @Override
    public PaymentResponseDTO markPaymentReceived(Long paymentId, LocalDate receivedDate) {
        throw new UnsupportedOperationException("This feature is not supported yet");

    }

    @Override
    public boolean paymentExistsForPeriod(Long employeeId, LocalDate startDate, LocalDate endDate) {
        return false;
    }

    @Override
    public PaymentResponseDTO approvePayment(Long paymentId, String approvedBy) {
        throw new UnsupportedOperationException("This feature is not supported yet");

    }

    @Override
    public PaymentResponseDTO voidPayment(Long paymentId, String reason, String voidedBy) {
        throw new UnsupportedOperationException("This feature is not supported yet");

    }

    @Override
    public List<PaymentResponseDTO> bulkApprovePayments(List<Long> paymentIds, String approvedBy) {
        return List.of();
    }

    @Override
    public PaymentResponseDTO reprocessPayment(Long paymentId, String reason) {
        throw new UnsupportedOperationException("This feature is not supported yet");
    }

    @Override
    public Page<PaymentResponseDTO> searchPayments(Long companyId, LocalDate startDate, LocalDate endDate, PaymentStatus status, String employeeName, Pageable pageable) {
        throw new UnsupportedOperationException("This feature is not supported yet");
    }

    @Override
    public Page<PaymentResponseDTO> getPaymentsByStatus(Long companyId, PaymentStatus status, Pageable pageable) {
        throw new UnsupportedOperationException("This feature is not supported yet");
    }

    @Override
    public Page<PaymentResponseDTO> getPaymentsByDateRange(Long companyId, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        throw new UnsupportedOperationException("This feature is not supported yet");
    }

    @Override
    public PaymentSummaryDTO getPaymentSummary(Long companyId, LocalDate startDate, LocalDate endDate) {
        throw new UnsupportedOperationException("This feature is not supported yet");
    }

    @Override
    public PaymentDashboardDTO getPaymentDashboard(Long companyId) {
        throw new UnsupportedOperationException("This feature is not supported yet");
    }

    @Override
    public byte[] exportPayments(Long companyId, LocalDate startDate, LocalDate endDate, String format) {
        throw new UnsupportedOperationException("This feature is not supported yet");
    }

    @Override
    public Page<PaymentResponseDTO> calculateCompanyPayroll(Long companyId, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        throw new UnsupportedOperationException("This feature is not supported yet");
    }

    @Override
    public List<PaymentResponseDTO> bulkMarkPaymentsIssued(List<Long> paymentIds, LocalDate issuedDate, String issuedBy) {
        throw new UnsupportedOperationException("This feature is not supported yet");
    }

    @Override
    public PaymentResponseDTO addCheckNumber(Long paymentId, String checkNumber) {
        throw new UnsupportedOperationException("This feature is not supported yet");
    }

    @Override
    public JobDetailsDTO getPaymentJobDetails(Long paymentId) {
        throw new UnsupportedOperationException("This feature is not supported yet");
    }
}
