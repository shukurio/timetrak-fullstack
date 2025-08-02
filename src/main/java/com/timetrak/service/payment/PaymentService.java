package com.timetrak.service.payment;

import com.timetrak.dto.payment.PaymentDetailsDTO;
import com.timetrak.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentService {

    /**
     * Get payment by ID
     */
    PaymentDetailsDTO getPaymentById(Long paymentId,Long employeeId, Long companyId);

    // =============== EMPLOYEE ACCESS ===============

    /**
     * Get all payments for an employee with pagination
     */
    Page<PaymentDetailsDTO> getAllPaymentsForEmployee(Long employeeId,Long companyId, Pageable pageable);

    // =============== ADMIN ACCESS ===============

    /**
     * Get all payments for company with pagination
     */
    Page<PaymentDetailsDTO> getAllPaymentsForCompany(Long companyId, Pageable pageable);


    // =============== SEARCH & FILTERING ===============

    /**
     * Get payments by status for company
     */
    Page<PaymentDetailsDTO> getPaymentsByStatus(Long companyId,
                                                PaymentStatus status,
                                                Pageable pageable);


    // =============== PAYMENT DETAILS ===============

    PaymentDetailsDTO getPaymentByIdForAdmin(Long paymentId, Long companyId);

    PaymentDetailsDTO getPaymentWithDetails(Long paymentId, Long employeeId, Long companyId);
}