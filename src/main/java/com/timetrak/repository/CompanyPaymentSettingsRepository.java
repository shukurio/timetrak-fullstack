package com.timetrak.repository;

import com.timetrak.entity.CompanyPaymentSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyPaymentSettingsRepository extends JpaRepository<CompanyPaymentSettings, Long> {
    @Query("SELECT cps FROM CompanyPaymentSettings cps JOIN FETCH cps.company WHERE cps.autoCalculate = true")
    List<CompanyPaymentSettings> findByAutoCalculateTrue();

    boolean existsByCompanyId(Long companyId);

    Optional<CompanyPaymentSettings> findByCompanyId(Long companyId);

    @Query("SELECT cps FROM CompanyPaymentSettings cps WHERE cps.company.id = :companyId")
    Optional<CompanyPaymentSettings> findByCompanyIdWithCompany(@Param("companyId") Long companyId);
}
