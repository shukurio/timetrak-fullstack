package com.timetrak.service.payment;

import com.timetrak.dto.payment.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PaymentResponseBuilder {

    // ========== MAIN RESPONSE BUILDER ==========

    public PaymentResponseDTO buildResponse(
            List<PaymentDetailsDTO> successful,
            List<PaymentFailureResponse> failed,
            Period period) {

        int successCount = successful.size();
        int failureCount = failed.size();
        int totalProcessed = successCount + failureCount;

        return PaymentResponseDTO.builder()
                .totalProcessed(totalProcessed)
                .successCount(successCount)
                .failureCount(failureCount)
                .operationType("Payment Calculation")
                .operationTime(LocalDateTime.now())
                .paymentPeriod(period.getFormattedPeriod())
                .successful(successful)
                .failed(failed)
                .build();
    }


    public List<PaymentFailureResponse> createDuplicateFailures(List<Long> duplicateEmployeeIds, Period period) {
        return duplicateEmployeeIds.stream()
                .map(employeeId -> PaymentFailureResponse.builder()
                        .employeeId(employeeId)
                        .period(period.getFormattedPeriod())
                        .errorMessage("Payment already exists for employee " + employeeId + " for period " + period.getFormattedPeriod())
                        .errorCode("DUPLICATE_PAYMENT")
                        .build())
                .toList();
    }

}