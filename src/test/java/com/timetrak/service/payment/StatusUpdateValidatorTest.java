package com.timetrak.service.payment;

import com.timetrak.dto.payment.status.StatusUpdateRequest;
import com.timetrak.entity.Payment;
import com.timetrak.enums.PaymentStatus;
import com.timetrak.exception.payment.InvalidPaymentRequestException;
import com.timetrak.exception.payment.InvalidPaymentStatusException;
import com.timetrak.service.payment.paymentManagement.StatusUpdateValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import static com.timetrak.constant.PaymentConstants.MAX_BATCH_SIZE;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StatusUpdateValidator Tests")
class StatusUpdateValidatorTest {

    private StatusUpdateValidator validator;
    private Payment testPayment;

    @BeforeEach
    void setUp() {
        validator = new StatusUpdateValidator();
        testPayment = new Payment();
        testPayment.setId(1L);
    }

    @Test
    @DisplayName("Should validate request with valid batch size")
    void validateRequest_ValidBatchSize() {
        // Given
        StatusUpdateRequest request = StatusUpdateRequest.builder()
                .paymentIds(List.of(1L, 2L, 3L))
                .targetStatus(PaymentStatus.ISSUED)
                .build();

        assertDoesNotThrow(() -> validator.validateRequest(request));
    }

    @Test
    @DisplayName("Should throw exception when batch size exceeds limit")
    void validateRequest_ExceedsMaxBatchSize() {
        List<Long> paymentIds = IntStream.rangeClosed(1, MAX_BATCH_SIZE + 1)
                .mapToLong(i -> (long) i)
                .boxed()
                .toList();

        StatusUpdateRequest request = StatusUpdateRequest.builder()
                .paymentIds(paymentIds)
                .targetStatus(PaymentStatus.ISSUED)
                .build();

        InvalidPaymentRequestException exception = assertThrows(InvalidPaymentRequestException.class,
                () -> validator.validateRequest(request));

        assertEquals("Cannot update more than " + MAX_BATCH_SIZE + " payments at once", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when payment is null")
    void validatePaymentConsistency_PaymentNotFound() {
        Long paymentId = 999L;
        Payment nullPayment = null;
        PaymentStatus targetStatus = PaymentStatus.ISSUED;

        InvalidPaymentRequestException exception = assertThrows(InvalidPaymentRequestException.class,
                () -> validator.validatePaymentConsistency(paymentId, nullPayment, targetStatus));

        assertEquals("Payment ID 999 not found.", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when current status is null")
    void validatePaymentConsistency_CurrentStatusNull() {
        Long paymentId = 1L;
        testPayment.setStatus(null);
        PaymentStatus targetStatus = PaymentStatus.ISSUED;

        InvalidPaymentStatusException exception = assertThrows(InvalidPaymentStatusException.class,
                () -> validator.validatePaymentConsistency(paymentId, testPayment, targetStatus));

        assertEquals("Status cannot be null.", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when target status is null")
    void validatePaymentConsistency_TargetStatusNull() {
        Long paymentId = 1L;
        testPayment.setStatus(PaymentStatus.CALCULATED);
        PaymentStatus targetStatus = null;

        InvalidPaymentStatusException exception = assertThrows(InvalidPaymentStatusException.class,
                () -> validator.validatePaymentConsistency(paymentId, testPayment, targetStatus));

        assertEquals("Status cannot be null.", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when payment already has target status")
    void validatePaymentConsistency_AlreadyTargetStatus() {
        Long paymentId = 1L;
        PaymentStatus status = PaymentStatus.ISSUED;
        testPayment.setStatus(status);

        InvalidPaymentStatusException exception = assertThrows(InvalidPaymentStatusException.class,
                () -> validator.validatePaymentConsistency(paymentId, testPayment, status));

        assertEquals("Payment ID 1 is already ISSUED", exception.getMessage());
    }

    @ParameterizedTest
    @CsvSource({
            "CALCULATED, ISSUED",
            "CALCULATED, VOIDED",
            "ISSUED, COMPLETED",
            "ISSUED, VOIDED"
    })
    @DisplayName("Should allow valid status transitions")
    void validatePaymentConsistency_ValidTransitions(PaymentStatus currentStatus, PaymentStatus targetStatus) {
        Long paymentId = 1L;
        testPayment.setStatus(currentStatus);

        assertDoesNotThrow(() -> validator.validatePaymentConsistency(paymentId, testPayment, targetStatus));
    }

    @ParameterizedTest
    @CsvSource({
            "CALCULATED, COMPLETED",  // Can't go directly from CALCULATED to COMPLETED
            "ISSUED, CALCULATED",     // Can't go backwards
            "COMPLETED, ISSUED",      // Can't change COMPLETED
            "COMPLETED, VOIDED",      // Can't change COMPLETED
            "VOIDED, ISSUED",         // Can't change VOIDED
            "VOIDED, COMPLETED",      // Can't change VOIDED
            "VOIDED, CALCULATED"      // Can't change VOIDED
    })
    @DisplayName("Should reject invalid status transitions")
    void validatePaymentConsistency_InvalidTransitions(PaymentStatus currentStatus, PaymentStatus targetStatus) {
        // Given
        Long paymentId = 1L;
        testPayment.setStatus(currentStatus);

        InvalidPaymentStatusException exception = assertThrows(InvalidPaymentStatusException.class,
                () -> validator.validatePaymentConsistency(paymentId, testPayment, targetStatus));

        String expectedMessage = "Cannot transition payment ID 1 from " + currentStatus + " to " + targetStatus;
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    @DisplayName("Should handle edge case with empty payment IDs list")
    void validateRequest_EmptyPaymentIds() {
        StatusUpdateRequest request = StatusUpdateRequest.builder()
                .paymentIds(Collections.emptyList())
                .targetStatus(PaymentStatus.ISSUED)
                .build();

        assertDoesNotThrow(() -> validator.validateRequest(request));
    }
}