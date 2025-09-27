package com.timetrak.service.payment.impl;

import com.timetrak.dto.payment.*;
import com.timetrak.entity.Payment;
import com.timetrak.enums.PaymentStatus;
import com.timetrak.exception.payment.PaymentNotFoundException;
import com.timetrak.mapper.PaymentMapper;
import com.timetrak.repository.PaymentRepository;
import com.timetrak.service.payment.JobDetailsBuilder;
import com.timetrak.service.payment.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentServiceImpl  implements PaymentService{
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final JobDetailsBuilder jobDetailsBuilder;


    @Override
    public PaymentDetailsDTO getPaymentById(Long paymentId,Long employeeId, Long companyId) {
        Payment payment = paymentRepository.findByIdAndEmployeeIdAndCompanyId(paymentId, employeeId,companyId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));
        return paymentMapper.toDTO(payment);
    }

    @Override
    public PaymentDetailsDTO getPaymentByIdForAdmin(Long paymentId, Long companyId) {
        Payment payment = paymentRepository.findByIdAndCompanyId(paymentId,companyId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));
        return paymentMapper.toDTO(payment);
    }


    @Override
    public PaymentDetailsDTO getPaymentWithDetails(Long paymentId, Long employeeId, Long companyId) {
        PaymentDetailsDTO payment = getPaymentById(paymentId, employeeId, companyId);
        List<JobDetailsDTO> jobDetails = jobDetailsBuilder.buildJobDetailsForPayment(payment);
        payment.setJobDetails(jobDetails);
        return payment;
    }

    //===================Admin access only=============
    @Override
    public Page<PaymentDetailsDTO> getAllPaymentsForCompany(Long companyId, Pageable pageable) {
        Page<Payment> payments = paymentRepository.findByCompanyId(companyId,pageable);
        return payments.map(paymentMapper::toDTO);
    }

    @Override
    public Page<PaymentDetailsDTO> getPaymentsByStatus(Long companyId, PaymentStatus status, Pageable pageable) {
     Page<Payment> payments = paymentRepository.findByCompanyIdAndStatus(companyId,status,pageable);
     return payments.map(paymentMapper::toDTO);
    }

    @Override
    public Page<PaymentDetailsDTO> getPaymentsByPeriod(Long companyId, Integer periodNumber, Pageable pageable) {
        Page<Payment> payments = paymentRepository.findByCompanyIdAndPeriodNumber(companyId, periodNumber, pageable);
        return payments.map(paymentMapper::toDTO);
    }


    // For Employee Only not Admin
    @Override
    public Page<PaymentDetailsDTO> getAllPaymentsForEmployee(Long employeeId,Long companyId, Pageable pageable) {
        Page<Payment> payments =paymentRepository.findByEmployeeIdAndCompanyIdExcludingVoided(employeeId,companyId,pageable);
        Page<PaymentDetailsDTO> paymentDtos = payments.map(paymentMapper::toDTO);
        return paymentDtos.map(dto-> {
            List<JobDetailsDTO> jobDetails = jobDetailsBuilder.buildJobDetailsForPayment(dto);
            dto.setJobDetails(jobDetails);
            dto.setJobsCount(jobDetails.size());
            return dto;
        });
    }



}
