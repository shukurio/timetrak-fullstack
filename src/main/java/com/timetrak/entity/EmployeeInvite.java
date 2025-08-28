package com.timetrak.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "employee_invites")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class EmployeeInvite extends BaseEntity {

    @Column(name = "invite_code", nullable = false, unique = true)
    private String inviteCode;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "department_id")
    private Long departmentId;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "max_uses", nullable = false)
    private Integer maxUses = 1;

    @Column(name = "current_uses", nullable = false)
    private Integer currentUses = 0;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_by_employee_id", nullable = false)
    private Long createdByEmployeeId;

    @Column(name = "description")
    private String description;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isFullyUsed() {
        return currentUses >= maxUses;
    }

    public boolean isValid() {
        return isActive && !isExpired() && !isFullyUsed();
    }

    public boolean canBeUsed() {
        return isValid() && currentUses < maxUses;
    }

    public void incrementUse() {
        if (canBeUsed()) {
            this.currentUses++;
        }
    }
}