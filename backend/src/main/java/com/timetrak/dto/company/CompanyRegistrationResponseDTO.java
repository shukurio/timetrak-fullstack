package com.timetrak.dto.company;

import com.timetrak.dto.employee.EmployeeResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CompanyRegistrationResponseDTO {
    private CompanyResponseDTO company;
    private EmployeeResponseDTO admin;
}
