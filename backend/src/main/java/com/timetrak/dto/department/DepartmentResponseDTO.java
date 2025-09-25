package com.timetrak.dto.department;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentResponseDTO {
    private Long id;
    private String name;
    private String description;
    private Boolean isActive;
    private Long companyId;
}
