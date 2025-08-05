package com.timetrak.service.payment;

import com.timetrak.dto.payment.JobDetailsDTO;
import com.timetrak.dto.payment.PaymentDetailsDTO;
import com.timetrak.entity.Employee;
import com.timetrak.entity.Payment;
import com.timetrak.enums.PaymentStatus;
import com.timetrak.exception.payment.PaymentNotFoundException;
import com.timetrak.mapper.PaymentMapper;
import com.timetrak.repository.PaymentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService Tests")
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private JobDetailsBuilder jobDetailsBuilder;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Test
    @DisplayName("Should get payment by ID for admin successfully")
    void getPaymentByIdForAdmin_Success() {
        Long paymentId = 1L;
        Long companyId = 1L;
        Payment payment = createTestPayment(paymentId, companyId);
        PaymentDetailsDTO expectedDTO = createTestPaymentDTO(paymentId);

        when(paymentRepository.findByIdAndCompanyId(paymentId, companyId))
                .thenReturn(Optional.of(payment));
        when(paymentMapper.toDTO(payment)).thenReturn(expectedDTO);

        PaymentDetailsDTO result = paymentService.getPaymentByIdForAdmin(paymentId, companyId);

        assertNotNull(result);
        assertEquals(expectedDTO.getId(), result.getId());
        verify(paymentRepository).findByIdAndCompanyId(paymentId, companyId);
        verify(paymentMapper).toDTO(payment);
    }

    @Test
    @DisplayName("Should throw PaymentNotFoundException when payment not found for admin")
    void getPaymentByIdForAdmin_NotFound() {
        Long paymentId = 999L;
        Long companyId = 1L;

        when(paymentRepository.findByIdAndCompanyId(paymentId, companyId))
                .thenReturn(Optional.empty());

        PaymentNotFoundException exception = assertThrows(PaymentNotFoundException.class,
                () -> paymentService.getPaymentByIdForAdmin(paymentId, companyId));

        assertEquals("Payment not found with ID: " + paymentId, exception.getMessage());
        verify(paymentRepository).findByIdAndCompanyId(paymentId, companyId);
        verifyNoInteractions(paymentMapper);
    }

    @Test
    @DisplayName("Should get payment by ID for employee successfully")
    void getPaymentById_Success() {
        Long paymentId = 1L;
        Long employeeId = 2L;
        Long companyId = 1L;
        Payment payment = createTestPayment(paymentId, companyId);
        PaymentDetailsDTO expectedDTO = createTestPaymentDTO(paymentId);

        when(paymentRepository.findByIdAndEmployeeIdAndCompanyId(paymentId, employeeId, companyId))
                .thenReturn(Optional.of(payment));
        when(paymentMapper.toDTO(payment)).thenReturn(expectedDTO);

        PaymentDetailsDTO result = paymentService.getPaymentById(paymentId, employeeId, companyId);

        assertNotNull(result);
        assertEquals(expectedDTO.getId(), result.getId());
        verify(paymentRepository).findByIdAndEmployeeIdAndCompanyId(paymentId, employeeId, companyId);
        verify(paymentMapper).toDTO(payment);
    }

    @Test
    @DisplayName("Should get payment with job details successfully")
    void getPaymentWithDetails_Success() {
        Long paymentId = 1L;
        Long employeeId = 2L;
        Long companyId = 1L;
        Payment payment = createTestPayment(paymentId, companyId);
        PaymentDetailsDTO paymentDTO = createTestPaymentDTO(paymentId);
        List<JobDetailsDTO> jobDetails = createTestJobDetails();

        when(paymentRepository.findByIdAndEmployeeIdAndCompanyId(paymentId, employeeId, companyId))
                .thenReturn(Optional.of(payment));
        when(paymentMapper.toDTO(payment)).thenReturn(paymentDTO);
        when(jobDetailsBuilder.buildJobDetailsForPayment(paymentDTO)).thenReturn(jobDetails);

        PaymentDetailsDTO result = paymentService.getPaymentWithDetails(paymentId, employeeId, companyId);

        assertNotNull(result);
        assertEquals(paymentId, result.getId());
        assertNotNull(result.getJobDetails());
        assertEquals(jobDetails.size(), result.getJobDetails().size());
        verify(jobDetailsBuilder).buildJobDetailsForPayment(paymentDTO);
    }

    @Test
    @DisplayName("Should get all payments for company with pagination")
    void getAllPaymentsForCompany_Success() {
        Long companyId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        Payment payment1 = createTestPayment(1L, companyId);
        Payment payment2 = createTestPayment(2L, companyId);
        Page<Payment> paymentsPage = new PageImpl<>(List.of(payment1, payment2));

        PaymentDetailsDTO dto1 = createTestPaymentDTO(1L);
        PaymentDetailsDTO dto2 = createTestPaymentDTO(2L);

        when(paymentRepository.findByCompanyId(companyId, pageable)).thenReturn(paymentsPage);
        when(paymentMapper.toDTO(payment1)).thenReturn(dto1);
        when(paymentMapper.toDTO(payment2)).thenReturn(dto2);

        Page<PaymentDetailsDTO> result = paymentService.getAllPaymentsForCompany(companyId, pageable);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(2, result.getTotalElements());
        verify(paymentRepository).findByCompanyId(companyId, pageable);
    }

    @Test
    @DisplayName("Should get payments by status successfully")
    void getPaymentsByStatus_Success() {
        Long companyId = 1L;
        PaymentStatus status = PaymentStatus.CALCULATED;
        Pageable pageable = PageRequest.of(0, 10);
        Payment payment = createTestPayment(1L, companyId);
        payment.setStatus(status);
        Page<Payment> paymentsPage = new PageImpl<>(List.of(payment));
        PaymentDetailsDTO dto = createTestPaymentDTO(1L);

        when(paymentRepository.findByCompanyIdAndStatus(companyId, status, pageable)).thenReturn(paymentsPage);
        when(paymentMapper.toDTO(payment)).thenReturn(dto);

        Page<PaymentDetailsDTO> result = paymentService.getPaymentsByStatus(companyId, status, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(1, result.getTotalElements());
        verify(paymentRepository).findByCompanyIdAndStatus(companyId, status, pageable);
    }

    @Test
    @DisplayName("Should get all payments for employee successfully")
    void getAllPaymentsForEmployee_Success() {
        Long employeeId = 2L;
        Long companyId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        Payment payment = createTestPayment(1L, companyId);
        Page<Payment> paymentsPage = new PageImpl<>(List.of(payment));
        PaymentDetailsDTO dto = createTestPaymentDTO(1L);

        when(paymentRepository.findByEmployeeIdAndCompanyId(employeeId, companyId, pageable)).thenReturn(paymentsPage);
        when(paymentMapper.toDTO(payment)).thenReturn(dto);

        Page<PaymentDetailsDTO> result = paymentService.getAllPaymentsForEmployee(employeeId, companyId, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(paymentRepository).findByEmployeeIdAndCompanyId(employeeId, companyId, pageable);
    }

    // Helper methods
    private Payment createTestPayment(Long id, Long companyId) {
        Payment payment = new Payment();
        payment.setId(id);
        payment.setCompanyId(companyId);
        payment.setStatus(PaymentStatus.CALCULATED);
        payment.setTotalEarnings(new BigDecimal("1000.00"));
        payment.setTotalHours(new BigDecimal("40.00"));
        payment.setPeriodStart(LocalDate.of(2025, 1, 1));
        payment.setPeriodEnd(LocalDate.of(2025, 1, 15));
        payment.setShiftsCount(5);
        payment.setModifiedBy(1L);

        Employee employee = new Employee();
        employee.setId(2L);
        employee.setFirstName("John");
        employee.setLastName("Doe");
        payment.setEmployee(employee);

        return payment;
    }

    private PaymentDetailsDTO createTestPaymentDTO(Long id) {
        PaymentDetailsDTO dto = new PaymentDetailsDTO();
        dto.setId(id);
        dto.setEmployeeId(2L);
        dto.setEmployeeName("John Doe");
        dto.setTotalEarnings(new BigDecimal("1000.00"));
        dto.setTotalHours(new BigDecimal("40.00"));
        dto.setStatus(PaymentStatus.CALCULATED);
        dto.setPeriodStart(LocalDate.of(2025, 1, 1));
        dto.setPeriodEnd(LocalDate.of(2025, 1, 15));
        return dto;
    }

    private List<JobDetailsDTO> createTestJobDetails() {
        JobDetailsDTO jobDetail = new JobDetailsDTO();
        jobDetail.setJobTitle("COOK");
        jobDetail.setTotalHours(new BigDecimal("40.00"));
        jobDetail.setHourlyRate(new BigDecimal("25.00"));
        jobDetail.setTotalEarnings(new BigDecimal("1000.00"));
        return List.of(jobDetail);
    }
}