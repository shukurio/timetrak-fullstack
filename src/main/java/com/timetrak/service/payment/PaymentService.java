package com.timetrak.service.payment;

import com.timetrak.dto.payment.PaymentBatchRequestDTO;
import com.timetrak.dto.payment.PaymentRequestDTO;
import com.timetrak.dto.payment.JobDetailsDTO;
import com.timetrak.dto.payment.PaymentDashboardDTO;
import com.timetrak.dto.payment.PaymentResponseDTO;
import com.timetrak.dto.payment.PaymentSummaryDTO;
import com.timetrak.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface PaymentService {

    // =============== CORE PAYMENT OPERATIONS ===============

    /**
     * Calculate payment for a single employee for specified period
     */
    PaymentResponseDTO calculatePayment(PaymentRequestDTO request);

    Page<PaymentResponseDTO> calculateBatchPayments(PaymentBatchRequestDTO request, Pageable pageable);

    /**
     * Get payment by ID
     */
    PaymentResponseDTO getPaymentById(Long paymentId);

    // =============== EMPLOYEE ACCESS ===============

    /**
     * Get employee's most recent payment
     */
    PaymentResponseDTO getLastPaymentForEmployee(Long employeeId);

    /**
     * Get all payments for an employee with pagination
     */
    Page<PaymentResponseDTO> getAllPaymentsForEmployee(Long employeeId, Pageable pageable);

    // =============== ADMIN ACCESS ===============

    /**
     * Get all payments for company with pagination
     */
    Page<PaymentResponseDTO> getAllPaymentsForCompany(Long companyId, Pageable pageable);

    /**
     * Get payments for last completed pay period
     */
    Page<PaymentResponseDTO> getAllPaymentsForLastPeriod(Pageable pageable);


    // =============== BASIC STATUS MANAGEMENT ===============

    /**
     * Mark payment as check issued
     */
    PaymentResponseDTO markPaymentIssued(Long paymentId, LocalDate issuedDate);

    /**
     * Mark payment as received by employee
     */
    PaymentResponseDTO markPaymentReceived(Long paymentId, LocalDate receivedDate);

    // =============== VALIDATION ===============

    /**
     * Check if payment already exists for employee and period
     */
    boolean paymentExistsForPeriod(Long employeeId, LocalDate startDate, LocalDate endDate);

    // =============== PAYMENT WORKFLOW ===============

    /**
     * Approve pending payment (ADMIN only)
     */
    PaymentResponseDTO approvePayment(Long paymentId, String approvedBy);

    /**
     * Reject/void payment with reason (ADMIN only)
     */
    PaymentResponseDTO voidPayment(Long paymentId, String reason, String voidedBy);

    /**
     * Bulk approve multiple payments
     */
    List<PaymentResponseDTO> bulkApprovePayments(List<Long> paymentIds, String approvedBy);

    /**
     * Reprocess/recalculate payment if there were errors
     */
    PaymentResponseDTO reprocessPayment(Long paymentId, String reason);

    //TODO probably dont need it

    // =============== SEARCH & FILTERING ===============

    /**
     * Search payments by multiple criteria
     */
    Page<PaymentResponseDTO> searchPayments(Long companyId,
                                            LocalDate startDate,
                                            LocalDate endDate,
                                            PaymentStatus status,
                                            String employeeName,
                                            Pageable pageable);

    /**
     * Get payments by status for company
     */
    Page<PaymentResponseDTO> getPaymentsByStatus(Long companyId,
                                                 PaymentStatus status,
                                                 Pageable pageable);

    /**
     * Get payments by date range
     */
    Page<PaymentResponseDTO> getPaymentsByDateRange(Long companyId,
                                                    LocalDate startDate,
                                                    LocalDate endDate,
                                                    Pageable pageable);

    // =============== REPORTING ===============

    /**
     * Get payment summary for period
     */
    PaymentSummaryDTO getPaymentSummary(Long companyId,
                                        LocalDate startDate,
                                        LocalDate endDate);

    /**
     * Get payment dashboard data
     */
    PaymentDashboardDTO getPaymentDashboard(Long companyId);

    /**
     * Export payments for accounting/payroll systems
     */
    byte[] exportPayments(Long companyId,
                          LocalDate startDate,
                          LocalDate endDate,
                          String format); // CSV, PDF, etc.

    // =============== BULK OPERATIONS ===============

    /**
     * Calculate payments for entire company for period
     */
    Page<PaymentResponseDTO> calculateCompanyPayroll(Long companyId,
                                                     LocalDate startDate,
                                                     LocalDate endDate,
                                                     Pageable pageable);

    /**
     * Bulk mark payments as issued
     */
    List<PaymentResponseDTO> bulkMarkPaymentsIssued(List<Long> paymentIds,
                                                    LocalDate issuedDate,
                                                    String issuedBy);

    // =============== PAYMENT DETAILS ===============


    /**
     * Add check number to payment
     */
    PaymentResponseDTO addCheckNumber(Long paymentId, String checkNumber);

    /**
     * Get payment breakdown details (overtime, regular, bonuses, etc.)
     */
    JobDetailsDTO getPaymentJobDetails(Long paymentId);
}