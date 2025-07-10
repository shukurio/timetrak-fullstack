package com.timetrak.service;

import com.timetrak.dto.request.PaymentBatchRequestDTO;
import com.timetrak.dto.request.PaymentRequestDTO;
import com.timetrak.dto.response.PaymentResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

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
}