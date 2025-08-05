package com.timetrak.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for group clock operations (clock-in/clock-out multiple employees).
 * Contains both successful and failed operations for proper frontend handling.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClockResponseDTO {

    /**
     * Total number of employees processed in this group operation
     */
    private int totalProcessed;

    /**
     * Number of successful clock operations
     */
    private int successCount;

    /**
     * Number of failed clock operations
     */
    private int failureCount;

    /**
     * Timestamp when the group operation was performed
     */
    private LocalDateTime operationTime;

    /**
     * Type of operation performed (CLOCK_IN or CLOCK_OUT)
     */
    private String operationType;

    /**
     * List of successful clock operations - complete shift data
     */
    private List<ShiftResponseDTO> successful;

    /**
     * List of failed clock operations with error details
     */
    private List<ClockFailureResponse> failed;

    /**
     * Helper method to check if operation was completely successful
     */
    public boolean isCompletelySuccessful() {
        return failureCount == 0;
    }

    /**
     * Helper method to check if operation had partial failures
     */
    public boolean isMixedResult() {
        return successCount > 0 && failureCount > 0;
    }



}
