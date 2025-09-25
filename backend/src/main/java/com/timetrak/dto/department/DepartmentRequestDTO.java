package com.timetrak.dto.department;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentRequestDTO {

    @NotBlank
    @Size(min = 2, max = 100)
    private String name;

    @Size(max = 500)
    private String description;

    private Long companyId;
}
