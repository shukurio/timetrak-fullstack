package com.timetrak.dto.company;

import com.timetrak.dto.response.EmployeeResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CompanyRegistrationResponseDTO {
    private CompanyResponseDTO company;
    private EmployeeResponseDTO admin;
}
