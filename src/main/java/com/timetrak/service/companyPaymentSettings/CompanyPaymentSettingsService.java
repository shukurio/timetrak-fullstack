package com.timetrak.service.companyPaymentSettings;

import com.timetrak.dto.company.settings.CompanyPaymentSettingsRequestDTO;
import com.timetrak.dto.company.settings.CompanyPaymentSettingsResponseDTO;
import com.timetrak.entity.CompanyPaymentSettings;

import java.time.LocalDate;
import java.util.Optional;

public interface CompanyPaymentSettingsService {

    /**
     * Get payment settings for a company
     */
    Optional<CompanyPaymentSettingsResponseDTO> getPaymentSettings(Long companyId);

    /**
     * Create payment settings for a company
     */
    CompanyPaymentSettingsResponseDTO createPaymentSettings(Long companyId, CompanyPaymentSettingsRequestDTO request);

    /**
     * Update payment settings for a company
     */
    CompanyPaymentSettingsResponseDTO updatePaymentSettings(Long companyId, CompanyPaymentSettingsRequestDTO request);

    /**
     * Check if payment settings exist for a company
     */
    boolean hasPaymentSettings(Long companyId);

    /**
     * Get payment settings entity (for internal service use)
     */
    Optional<CompanyPaymentSettings> getPaymentSettingsEntity(Long companyId);

    /**
     * Create default payment settings for a company
     */
    CompanyPaymentSettings createDefaultSettings(Long companyId);

    /**
     * Initialize default payment settings for a company (returns DTO)
     */
    CompanyPaymentSettingsResponseDTO initializeDefaultSettings(Long companyId);

    /**
     * Check if it's safe to calculate payments for a period (respects grace period)
     */
    boolean canCalculatePayments(Long companyId, LocalDate periodEnd);

    /**
     * Set current admin's email as notification email
     */
    CompanyPaymentSettingsResponseDTO setAdminEmailAsNotification(Long companyId);
}
