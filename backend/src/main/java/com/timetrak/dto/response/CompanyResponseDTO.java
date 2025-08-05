package com.timetrak.dto.response;

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
