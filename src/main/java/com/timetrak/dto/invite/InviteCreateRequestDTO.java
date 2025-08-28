package com.timetrak.dto.invite;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InviteCreateRequestDTO {

    @NotNull(message = "Company ID is required")
    private Long companyId;

    private Long departmentId;

    @NotNull(message = "Max uses is required")
    @Min(value = 1, message = "Max uses must be at least 1")
    private Integer maxUses;

    @NotNull(message = "Expiry hours is required")
    @Min(value = 1, message = "Expiry hours must be at least 1")
    private Integer expiryHours;

    private String description;
}