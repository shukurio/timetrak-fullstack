package com.timetrak.dto.company;

import com.timetrak.dto.request.EmployeeRequestDTO;
import jakarta.validation.Valid;
import lombok.Data;

@Data
public class CompanyRegistrationDTO {

    @Valid
    private CompanyRequestDTO company;

    @Valid
    private EmployeeRequestDTO admin;
}
