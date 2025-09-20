package com.timetrak.dto.company;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyUpdateDTO {

    private String name;

    @Size(min = 2, max = 10, message = "Company code must be between 2 and 10 characters")
    private String code;

    private Double latitude;
    private Double longitude;
    @Positive
    private Double allowedRadius;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    private Boolean isActive = true;
}
