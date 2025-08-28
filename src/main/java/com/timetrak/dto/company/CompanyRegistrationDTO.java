package com.timetrak.dto.company;

import jakarta.validation.Valid;
import lombok.Data;

@Data
public class CompanyRegistrationDTO {

    @Valid
    private CompanyRequestDTO company;

    @Valid
    private AdminRegRequestDTO admin;
}
