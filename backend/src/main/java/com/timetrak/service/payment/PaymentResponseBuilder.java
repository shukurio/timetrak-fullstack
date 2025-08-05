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
            PaymentPeriod period) {

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


    // ========== GENERIC FAILURE BUILDERS ==========

    public PaymentFailureResponse createFailureResponse(Long employeeId, String errorMessage,
                                                         String errorCode, String period) {
        try {
            return PaymentFailureResponse.builder()
                    .employeeId(employeeId)
                    .period(period)
                    .errorMessage(errorMessage)
                    .errorCode(errorCode)
                    .build();
        } catch (Exception e) {
            return PaymentFailureResponse.builder()
                    .employeeId(employeeId)
                    .period(period)
                    .errorMessage(errorMessage)
                    .errorCode(errorCode)
                    .build();
        }

    }

    public List<PaymentFailureResponse> createDuplicateFailures(List<Long> duplicateEmployeeIds, PaymentPeriod period) {
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