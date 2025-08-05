package com.timetrak.service.payment.paymentManagement;

import com.timetrak.dto.payment.status.StatusUpdateRequest;
import com.timetrak.entity.Payment;
import com.timetrak.enums.PaymentStatus;
import com.timetrak.exception.payment.InvalidPaymentRequestException;
import com.timetrak.exception.payment.InvalidPaymentStatusException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Map;

import static com.timetrak.constant.PaymentConstants.MAX_BATCH_SIZE;
@Component
@Slf4j
public class StatusUpdateValidator {

    private static final Map<PaymentStatus, EnumSet<PaymentStatus>> VALID_TRANSITIONS = Map.of(
            PaymentStatus.CALCULATED, EnumSet.of(PaymentStatus.ISSUED, PaymentStatus.VOIDED),
            PaymentStatus.ISSUED, EnumSet.of(PaymentStatus.COMPLETED, PaymentStatus.VOIDED),
            PaymentStatus.COMPLETED, EnumSet.noneOf(PaymentStatus.class),
            PaymentStatus.VOIDED, EnumSet.noneOf(PaymentStatus.class)
    );

    public void validateRequest(StatusUpdateRequest request) {
        if (request.getPaymentIds().size() > MAX_BATCH_SIZE) {
            throw new InvalidPaymentRequestException("Cannot update more than " + MAX_BATCH_SIZE + " payments at once");
        }
        log.debug("Validated request size: {}", request.getPaymentIds().size());
    }

    public void validatePaymentConsistency(Long paymentId, Payment payment, PaymentStatus targetStatus) {
        if (payment == null) {
            throw new InvalidPaymentRequestException("Payment ID " + paymentId + " not found.");
        }

        PaymentStatus currentStatus = payment.getStatus();
        if (currentStatus == null || targetStatus == null) {
            throw new InvalidPaymentStatusException("Status cannot be null.");
        }

        if (currentStatus == targetStatus) {
            throw new InvalidPaymentStatusException("Payment ID " + paymentId + " is already " + targetStatus);
        }

        EnumSet<PaymentStatus> allowed = VALID_TRANSITIONS.get(currentStatus);
        if (allowed == null || !allowed.contains(targetStatus)) {
            throw new InvalidPaymentStatusException("Cannot transition payment ID " + paymentId + " from " + currentStatus + " to " + targetStatus);
        }
    }
}
