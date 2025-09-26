package com.timetrak.repository;

import com.timetrak.entity.EmployeeInvite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeInviteRepository extends JpaRepository<EmployeeInvite, Long> {

    Optional<EmployeeInvite> findByInviteCode(String inviteCode);

    @Query("SELECT ei FROM EmployeeInvite ei WHERE ei.inviteCode = :inviteCode AND ei.isActive = true")
    Optional<EmployeeInvite> findActiveInviteByCode(@Param("inviteCode") String inviteCode);

    List<EmployeeInvite> findByCompanyIdAndIsActiveTrue(Long companyId);

    @Query("SELECT ei FROM EmployeeInvite ei WHERE ei.expiresAt < :now AND ei.isActive = true")
    List<EmployeeInvite> findExpiredInvites(@Param("now") LocalDateTime now);

    boolean existsByInviteCode(String inviteCode);
}