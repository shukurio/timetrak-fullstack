package com.timetrak.service.payment;

import com.timetrak.dto.payment.*;
import com.timetrak.dto.payment.PaymentDetailsDTO;
import com.timetrak.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface PaymentService {

    // =============== CORE PAYMENT OPERATIONS ===============

    /**
     * Calculate payment for a single and group of employees for specified period
     */
    PaymentResponseDTO calculatePayments(PaymentRequestDTO request);
    PaymentResponseDTO calculatePaymentsForPeriod(PaymentPeriod paymentPeriod);

    /**
     * Get payment by ID
     */
    PaymentDetailsDTO getPaymentById(Long paymentId);

    // =============== EMPLOYEE ACCESS ===============

    /**
     * Get employee's most recent payment
     */
    PaymentDetailsDTO getLastPaymentForEmployee(Long employeeId);

    /**
     * Get all payments for an employee with pagination
     */
    Page<PaymentDetailsDTO> getAllPaymentsForEmployee(Long employeeId, Pageable pageable);

    // =============== ADMIN ACCESS ===============

    /**
     * Get all payments for company with pagination
     */
    Page<PaymentDetailsDTO> getAllPaymentsForCompany(Long companyId, Pageable pageable);

    /**
     * Get payments for last completed pay period
     */
    Page<PaymentDetailsDTO> getAllPaymentsForLastPeriod(Pageable pageable);


    // =============== BASIC STATUS MANAGEMENT ===============

    /**
     * Mark payment as check issued
     */
    PaymentDetailsDTO markPaymentIssued(Long paymentId, LocalDate issuedDate);

    /**
     * Mark payment as received by employee
     */
    PaymentDetailsDTO markPaymentReceived(Long paymentId, LocalDate receivedDate);

    // =============== VALIDATION ===============

    /**
     * Check if payment already exists for employee and period
     */
    boolean paymentExistsForPeriod(Long employeeId, LocalDate startDate, LocalDate endDate);

    // =============== PAYMENT WORKFLOW ===============

    /**
     * Approve pending payment (ADMIN only)
     */
    PaymentDetailsDTO approvePayment(Long paymentId, String approvedBy);

    /**
     * Reject/void payment with reason (ADMIN only)
     */
    PaymentDetailsDTO voidPayment(Long paymentId, String reason, String voidedBy);

    /**
     * Bulk approve multiple payments
     */
    List<PaymentDetailsDTO> bulkApprovePayments(List<Long> paymentIds, String approvedBy);

    /**
     * Reprocess/recalculate payment if there were errors
     */
    PaymentDetailsDTO reprocessPayment(Long paymentId, String reason);

    //TODO probably dont need it

    // =============== SEARCH & FILTERING ===============

    /**
     * Search payments by multiple criteria
     */
    Page<PaymentDetailsDTO> searchPayments(Long companyId,
                                           LocalDate startDate,
                                           LocalDate endDate,
                                           PaymentStatus status,
                                           String employeeName,
                                           Pageable pageable);

    /**
     * Get payments by status for company
     */
    Page<PaymentDetailsDTO> getPaymentsByStatus(Long companyId,
                                                PaymentStatus status,
                                                Pageable pageable);

    /**
     * Get payments by date range
     */
    Page<PaymentDetailsDTO> getPaymentsByDateRange(Long companyId,
                                                   LocalDate startDate,
                                                   LocalDate endDate,
                                                   Pageable pageable);

    // =============== REPORTING ===============



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
    Page<PaymentDetailsDTO> calculateCompanyPayroll(Long companyId,
                                                    LocalDate startDate,
                                                    LocalDate endDate,
                                                    Pageable pageable);

    /**
     * Bulk mark payments as issued
     */
    List<PaymentDetailsDTO> bulkMarkPaymentsIssued(List<Long> paymentIds,
                                                   LocalDate issuedDate,
                                                   String issuedBy);

    // =============== PAYMENT DETAILS ===============


    /**
     * Add check number to payment
     */
    PaymentDetailsDTO addCheckNumber(Long paymentId, String checkNumber);

    /**
     * Get payment breakdown details (overtime, regular, bonuses, etc.)
     */
    JobDetailsDTO getPaymentJobDetails(Long paymentId);
}