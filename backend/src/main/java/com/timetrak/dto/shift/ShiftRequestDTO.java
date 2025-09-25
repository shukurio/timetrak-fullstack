package com.timetrak.dto.shift;

import com.timetrak.enums.ShiftStatus;
import jakarta.validation.constraints.NotNull;
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
  
  
  private ShiftStatus status;
}
