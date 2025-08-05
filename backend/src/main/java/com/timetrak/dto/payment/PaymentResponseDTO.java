package com.timetrak.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for batch payment operations.
 * Contains both successful and failed payment calculations for proper frontend handling.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponseDTO {
    private int totalProcessed;
    private int successCount;
    private int failureCount;
    private LocalDateTime operationTime;
    private String operationType;
    private String paymentPeriod;
    private List<PaymentDetailsDTO> successful;
    private List<PaymentFailureResponse> failed;
    private boolean isCompletelySuccessful(){
        return failureCount == 0;
    }
    private boolean isMixedResult(){
        return successCount >0 && failureCount > 0;
    }
}