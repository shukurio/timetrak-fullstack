package com.timetrak.service.payment;

import com.timetrak.dto.payment.*;
import com.timetrak.enums.PaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.time.LocalDate;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentServiceImpl  implements PaymentService{

    @Override
    public PaymentDetailsDTO getPaymentById(Long paymentId) {
        throw new UnsupportedOperationException("This feature is not supported yet");
    }

    @Override
    public Page<PaymentDetailsDTO> getAllPaymentsForEmployee(Long employeeId, Pageable pageable) {
        throw new UnsupportedOperationException("This feature is not supported yet");
    }

    @Override
    public Page<PaymentDetailsDTO> getAllPaymentsForCompany(Long companyId, Pageable pageable) {
        throw new UnsupportedOperationException("This feature is not supported yet");
    }

    @Override
    public Page<PaymentDetailsDTO> getAllPaymentsForLastPeriod(Pageable pageable) {
        throw new UnsupportedOperationException("This feature is not supported yet");
    }


    @Override
    public boolean paymentExistsForPeriod(Long employeeId, LocalDate startDate, LocalDate endDate) {
        return false;
    }


    @Override
    public Page<PaymentDetailsDTO> searchPayments(Long companyId, LocalDate startDate, LocalDate endDate, PaymentStatus status, String employeeName, Pageable pageable) {
        throw new UnsupportedOperationException("This feature is not supported yet");
    }

    @Override
    public Page<PaymentDetailsDTO> getPaymentsByStatus(Long companyId, PaymentStatus status, Pageable pageable) {
        throw new UnsupportedOperationException("This feature is not supported yet");
    }

    @Override
    public Page<PaymentDetailsDTO> getPaymentsByDateRange(Long companyId, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        throw new UnsupportedOperationException("This feature is not supported yet");
    }

    @Override
    public JobDetailsDTO getPaymentJobDetails(Long paymentId) {
        throw new UnsupportedOperationException("This feature is not supported yet");
    }

}
