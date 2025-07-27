package com.timetrak.service.payment;

import com.timetrak.dto.payment.*;
import com.timetrak.dto.payment.PaymentDetailsDTO;
import com.timetrak.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface PaymentService {

    /**
     * Get payment by ID
     */
    PaymentDetailsDTO getPaymentById(Long paymentId);

    // =============== EMPLOYEE ACCESS ===============

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

    //TODO maybe implement getAllPaymentForCompanyForPeriod, it can be used with parameters, more general


    // =============== VALIDATION ===============

    /**
     * Check if payment already exists for employee and period
     */
    boolean paymentExistsForPeriod(Long employeeId, LocalDate startDate, LocalDate endDate);

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

    // =============== PAYMENT DETAILS ===============

    /**
     * Get payment breakdown details
     */
    JobDetailsDTO getPaymentJobDetails(Long paymentId);
}