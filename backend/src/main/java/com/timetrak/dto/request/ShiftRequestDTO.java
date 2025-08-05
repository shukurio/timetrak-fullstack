package com.timetrak.dto.request;

import com.timetrak.enums.ShiftStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShiftRequestDTO {
  
  @NotNull(message = "Employee job ID is required")
  private Long employeeJobId;
  
  private LocalDateTime clockIn;
  private LocalDateTime clockOut;
  
  @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
  private String notes;
  
  private ShiftStatus status;
}
