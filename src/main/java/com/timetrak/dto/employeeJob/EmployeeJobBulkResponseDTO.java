package com.timetrak.dto.employeeJob;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeJobBulkResponseDTO {
    private int totalProcessed;
    private int successCount;
    private int failureCount;
    private LocalDateTime operationTime;
    private String operationType;
    
    private List<EmployeeJobResponseDTO> successful;
    private List<EmployeeJobFailureResponseDTO> failed;
    
    public boolean isCompletelySuccessful() {
        return failureCount == 0;
    }
    
    public boolean isMixedResult() {
        return successCount > 0 && failureCount > 0;
    }
}
