package com.timetrak.dto.company;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CompanyResponseDTO {
    private Long id;
    private String name;
    private String code;
    private String description;
    private Boolean isActive;
}
