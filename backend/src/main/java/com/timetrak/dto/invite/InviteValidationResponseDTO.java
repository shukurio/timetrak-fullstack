package com.timetrak.dto.invite;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InviteValidationResponseDTO {

    private Boolean isValid;
    private String message;
    private Long companyId;
    private Long departmentId;
    private String companyName;
    private String departmentName;
}