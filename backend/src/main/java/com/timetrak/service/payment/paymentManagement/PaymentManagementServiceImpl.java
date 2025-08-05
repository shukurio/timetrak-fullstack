package com.timetrak.service.payment.paymentManagement;

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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PaymentManagementServiceImpl implements PaymentManagementService {

    private final PaymentRepository paymentRepository;
    private final StatusUpdateValidator validator;

    @Override
    public StatusUpdateResponse updatePaymentStatus(StatusUpdateRequest request, Long companyId, Long modifierId) {
        validator.validateRequest(request);

        List<Payment> existingPayments = paymentRepository.findAllByIdsAndCompanyId(request.getPaymentIds(), companyId);
        Map<Long, Payment> paymentMap = existingPayments.stream()
                .collect(Collectors.toMap(Payment::getId, p -> p));

        List<StatusUpdateSuccess> successes = new ArrayList<>();
        List<StatusUpdateFailure> failures = new ArrayList<>();

        for (Long paymentId : request.getPaymentIds()) {
            processSinglePayment(paymentId, paymentMap, request.getTargetStatus(), modifierId, successes, failures);
        }

        persistSuccessfulUpdates(successes, paymentMap);

        return buildResponse(request, successes, failures);
    }

    private void processSinglePayment(Long paymentId,
                                      Map<Long, Payment> paymentMap,
                                      PaymentStatus targetStatus,
                                      Long modifierId,
                                      List<StatusUpdateSuccess> successes,
                                      List<StatusUpdateFailure> failures) {
        Payment payment = paymentMap.get(paymentId);
        PaymentStatus currentStatus = payment != null ? payment.getStatus() : null;

        try {
            validator.validatePaymentConsistency(paymentId, payment, targetStatus);
            updateStatus(payment, targetStatus, modifierId);

            successes.add(new StatusUpdateSuccess(
                    paymentId, currentStatus, targetStatus, LocalDateTime.now(), modifierId
            ));
        } catch (InvalidPaymentRequestException | InvalidPaymentStatusException e) {
            failures.add(new StatusUpdateFailure(paymentId, e.getMessage(), e.getErrorCode(), statusOrUnknown(currentStatus), e));
        } catch (Exception e) {
            log.error("Unexpected error processing payment {}: {}", paymentId, e.getMessage(), e);
            failures.add(new StatusUpdateFailure(paymentId, "Unexpected error", "PROCESSING_ERROR", statusOrUnknown(currentStatus), e));
        }
    }

    private void updateStatus(Payment payment, PaymentStatus newStatus, Long modifierId) {
        payment.setStatus(newStatus);
        payment.setModifiedBy(modifierId);

        switch (newStatus) {
            case ISSUED -> payment.setIssuedAt(LocalDateTime.now());
            case COMPLETED -> payment.setCompletedAt(LocalDateTime.now());
            case VOIDED -> payment.setVoidedAt(LocalDateTime.now());
        }

        log.debug("Updated payment {} to status {}", payment.getId(), newStatus);
    }

    private void persistSuccessfulUpdates(List<StatusUpdateSuccess> successes, Map<Long, Payment> paymentMap) {
        if (successes.isEmpty()) return;

        List<Payment> toSave = successes.stream()
                .map(success -> paymentMap.get(success.getPaymentId()))
                .toList();

        paymentRepository.saveAll(toSave);
        log.info("Persisted {} payments with new statuses", toSave.size());
    }

    private StatusUpdateResponse buildResponse(StatusUpdateRequest request,
                                               List<StatusUpdateSuccess> successes,
                                               List<StatusUpdateFailure> failures) {
        return StatusUpdateResponse.builder()
                .totalProcessed(request.getPaymentIds().size())
                .successCount(successes.size())
                .failureCount(failures.size())
                .operationTime(LocalDateTime.now())
                .operationType("BATCH_STATUS_UPDATE")
                .successful(successes)
                .failed(failures)
                .build();
    }

    private String statusOrUnknown(PaymentStatus status) {
        return status != null ? status.name() : "UNKNOWN";
    }

}
