package com.timetrak.service.payment;

import com.timetrak.dto.payment.status.StatusUpdateFailure;
import com.timetrak.dto.payment.status.StatusUpdateRequest;
import com.timetrak.dto.payment.status.StatusUpdateResponse;
import com.timetrak.dto.payment.status.StatusUpdateSuccess;
import com.timetrak.entity.Employee;
import com.timetrak.entity.Payment;
import com.timetrak.enums.PaymentStatus;
import com.timetrak.exception.payment.InvalidPaymentRequestException;
import com.timetrak.exception.payment.InvalidPaymentStatusException;
import com.timetrak.repository.PaymentRepository;
import com.timetrak.service.payment.paymentManagement.PaymentManagementServiceImpl;
import com.timetrak.service.payment.paymentManagement.StatusUpdateValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentManagementService Tests")
class PaymentManagementServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private StatusUpdateValidator validator;

    @InjectMocks
    private PaymentManagementServiceImpl paymentManagementService;

    private StatusUpdateRequest validRequest;
    private Payment payment1;
    private Payment payment2;
    private Long companyId;
    private Long modifierId;

    @BeforeEach
    void setUp() {
        companyId = 1L;
        modifierId = 5L;

        validRequest = StatusUpdateRequest.builder()
                .paymentIds(List.of(1L,2L))
                .targetStatus(PaymentStatus.ISSUED)
                .reason("Test update")
                .build();

        payment1 = createTestPayment(1L);
        payment2 = createTestPayment(2L);
    }

    @Test
    @DisplayName("Should successfully update payment statuses in batch")
    void updatePaymentStatus_Success() {
        List<Payment> existingPayments = List.of(payment1, payment2);

        when(paymentRepository.findAllByIdsAndCompanyId(validRequest.getPaymentIds(), companyId))
                .thenReturn(existingPayments);
        doNothing().when(validator).validateRequest(validRequest);
        doNothing().when(validator).validatePaymentConsistency(anyLong(), any(Payment.class), any(PaymentStatus.class));

        StatusUpdateResponse response = paymentManagementService.updatePaymentStatus(validRequest, companyId, modifierId);

        assertNotNull(response);
        assertEquals(2, response.getTotalProcessed());
        assertEquals(2, response.getSuccessCount());
        assertEquals(0, response.getFailureCount());
        assertEquals("BATCH_STATUS_UPDATE", response.getOperationType());
        assertNotNull(response.getSuccessful());
        assertNotNull(response.getFailed());
        assertTrue(response.getFailed().isEmpty());

        // Verify all successful updates
        assertEquals(2, response.getSuccessful().size());
        response.getSuccessful().forEach(success -> {
            assertEquals(PaymentStatus.CALCULATED, success.getPreviousStatus());
            assertEquals(PaymentStatus.ISSUED, success.getNewStatus());
        });

        verify(validator).validateRequest(validRequest);
        verify(validator, times(2)).validatePaymentConsistency(anyLong(), any(Payment.class), eq(PaymentStatus.ISSUED));
        verify(paymentRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("Should handle payment not found scenario")
    void updatePaymentStatus_PaymentNotFound() {
        // Given
        List<Long> paymentIds = List.of(1L, 999L); // 999 doesn't exist
        StatusUpdateRequest request = StatusUpdateRequest.builder()
                .paymentIds(paymentIds)
                .targetStatus(PaymentStatus.ISSUED)
                .build();

        List<Payment> existingPayments = List.of(payment1); // Only payment1 exists

        when(paymentRepository.findAllByIdsAndCompanyId(paymentIds, companyId))
                .thenReturn(existingPayments);
        doNothing().when(validator).validateRequest(request);
        doNothing().when(validator).validatePaymentConsistency(eq(1L), eq(payment1), eq(PaymentStatus.ISSUED));
        doThrow(new InvalidPaymentRequestException("Payment ID 999 not found."))
                .when(validator).validatePaymentConsistency(eq(999L), isNull(), eq(PaymentStatus.ISSUED));

        // When
        StatusUpdateResponse response = paymentManagementService.updatePaymentStatus(request, companyId, modifierId);

        // Then
        assertNotNull(response);
        assertEquals(2, response.getTotalProcessed());
        assertEquals(1, response.getSuccessCount());
        assertEquals(1, response.getFailureCount());

        // Verify failure details
        assertEquals(1, response.getFailed().size());
        StatusUpdateFailure failure = response.getFailed().getFirst();
        assertEquals(999L, failure.getPaymentId());
        assertEquals("Payment ID 999 not found.", failure.getErrorMessage());
        assertEquals("INVALID_PAYMENT_REQUEST", failure.getErrorCode());
    }

    @Test
    @DisplayName("Should handle invalid status transition")
    void updatePaymentStatus_InvalidTransition() {
        // Given
        payment1.setStatus(PaymentStatus.VOIDED); // Can't transition from VOIDED to ISSUED
        List<Payment> existingPayments = List.of(payment1);

        when(paymentRepository.findAllByIdsAndCompanyId(List.of(1L), companyId))
                .thenReturn(existingPayments);
        doNothing().when(validator).validateRequest(any());
        doThrow(new InvalidPaymentStatusException("Cannot transition payment ID 1 from VOIDED to ISSUED"))
                .when(validator).validatePaymentConsistency(eq(1L), eq(payment1), eq(PaymentStatus.ISSUED));

        StatusUpdateRequest request = StatusUpdateRequest.builder()
                .paymentIds(List.of(1L))
                .targetStatus(PaymentStatus.ISSUED)
                .build();

        // When
        StatusUpdateResponse response = paymentManagementService.updatePaymentStatus(request, companyId, modifierId);

        // Then
        assertNotNull(response);
        assertEquals(1, response.getTotalProcessed());
        assertEquals(0, response.getSuccessCount());
        assertEquals(1, response.getFailureCount());

        StatusUpdateFailure failure = response.getFailed().getFirst();
        assertEquals(1L, failure.getPaymentId());
        assertEquals("Cannot transition payment ID 1 from VOIDED to ISSUED", failure.getErrorMessage());
        assertEquals("INVALID_PAYMENT_STATUS", failure.getErrorCode());
        assertEquals("VOIDED", failure.getCurrentStatus());
    }

    @Test
    @DisplayName("Should handle mixed success and failure scenarios")
    void updatePaymentStatus_MixedResults() {
        // Given
        payment2.setStatus(PaymentStatus.VOIDED); // This will fail
        List<Payment> existingPayments =List.of(payment1, payment2);

        when(paymentRepository.findAllByIdsAndCompanyId(validRequest.getPaymentIds(), companyId))
                .thenReturn(existingPayments);
        doNothing().when(validator).validateRequest(validRequest);
        doNothing().when(validator).validatePaymentConsistency(eq(1L), eq(payment1), eq(PaymentStatus.ISSUED));
        doThrow(new InvalidPaymentStatusException("Cannot transition payment ID 2 from VOIDED to ISSUED"))
                .when(validator).validatePaymentConsistency(eq(2L), eq(payment2), eq(PaymentStatus.ISSUED));

        // When
        StatusUpdateResponse response = paymentManagementService.updatePaymentStatus(validRequest, companyId, modifierId);

        // Then
        assertNotNull(response);
        assertEquals(2, response.getTotalProcessed());
        assertEquals(1, response.getSuccessCount());
        assertEquals(1, response.getFailureCount());

        // Verify success
        assertEquals(1, response.getSuccessful().size());
        StatusUpdateSuccess success = response.getSuccessful().getFirst();
        assertEquals(1L, success.getPaymentId());
        assertEquals(PaymentStatus.CALCULATED, success.getPreviousStatus());
        assertEquals(PaymentStatus.ISSUED, success.getNewStatus());

        // Verify failure
        assertEquals(1, response.getFailed().size());
        StatusUpdateFailure failure = response.getFailed().getFirst();
        assertEquals(2L, failure.getPaymentId());
    }

    @Test
    @DisplayName("Should persist successful updates only")
    void updatePaymentStatus_PersistOnlySuccessful() {
        payment2.setStatus(PaymentStatus.VOIDED);
        List<Payment> existingPayments = List.of(payment1, payment2);

        when(paymentRepository.findAllByIdsAndCompanyId(validRequest.getPaymentIds(), companyId))
                .thenReturn(existingPayments);
        doNothing().when(validator).validateRequest(validRequest);
        doNothing().when(validator).validatePaymentConsistency(eq(1L), eq(payment1), eq(PaymentStatus.ISSUED));
        doThrow(new InvalidPaymentStatusException("Invalid transition"))
                .when(validator).validatePaymentConsistency(eq(2L), eq(payment2), eq(PaymentStatus.ISSUED));

        StatusUpdateResponse response = paymentManagementService.updatePaymentStatus(validRequest, companyId, modifierId);

        verify(paymentRepository).saveAll(argThat((List<Payment> payments) ->
                payments.size() == 1 &&
                        payments.getFirst().getId().equals(1L) &&
                        payments.getFirst().getStatus() == PaymentStatus.ISSUED
        ));

        // Verify response counts
        assertEquals(2, response.getTotalProcessed());
        assertEquals(1, response.getSuccessCount());
        assertEquals(1, response.getFailureCount());

        // Verify payment1 was updated
        assertEquals(PaymentStatus.ISSUED, payment1.getStatus());
        assertEquals(modifierId, payment1.getModifiedBy());
        assertNotNull(payment1.getIssuedAt());

        // Verify payment2 was not updated
        assertEquals(PaymentStatus.VOIDED, payment2.getStatus());
        assertNull(payment2.getIssuedAt());

        // Verify failure details in response
        assertEquals(1, response.getFailed().size());
        StatusUpdateFailure failure = response.getFailed().getFirst();
        assertEquals(2L, failure.getPaymentId());
        assertEquals("Invalid transition", failure.getErrorMessage());
    }

    private Payment createTestPayment(Long id) {
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
        employee.setId(id);
        employee.setFirstName("Test");
        employee.setLastName("Employee");
        payment.setEmployee(employee);

        return payment;
    }
}