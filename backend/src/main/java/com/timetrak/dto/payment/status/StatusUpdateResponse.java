package com.timetrak.dto.payment.status;

import com.timetrak.enums.BatchStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class StatusUpdateResponse {
    private BatchStatus batchStatus;
    private int totalProcessed;
    private int successCount;
    private int failureCount;
    private LocalDateTime operationTime;
    private String operationType;
    private List<StatusUpdateSuccess> successful;
    private List<StatusUpdateFailure> failed;

    public BatchStatus getBatchStatus() {
        if (failureCount == 0) return BatchStatus.SUCCESS;
        if (successCount == 0) return BatchStatus.FAILURE;
        return BatchStatus.MIXED_RESULT;
    }



}
