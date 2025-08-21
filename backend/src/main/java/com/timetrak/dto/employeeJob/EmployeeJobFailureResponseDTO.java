package com.timetrak.dto.employeeJob;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeJobFailureResponseDTO {
    private Long employeeId;
    private String employeeName;
    private String errorMessage;
    private String errorCode;
}
