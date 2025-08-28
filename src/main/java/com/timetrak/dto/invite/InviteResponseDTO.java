package com.timetrak.dto.invite;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InviteResponseDTO {

    private Long id;
    private String inviteCode;
    private String inviteUrl;
    private Long companyId;
    private Long departmentId;
    private Integer maxUses;
    private Integer currentUses;
    private Boolean isActive;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private String description;
    private Boolean isExpired;
    private Boolean isFullyUsed;
    private Boolean isValid;
}