package com.timetrak.service.companyPaymentSettings;

import com.timetrak.dto.company.settings.CompanyPaymentSettingsRequestDTO;
import com.timetrak.dto.company.settings.CompanyPaymentSettingsResponseDTO;

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
     * Create default payment settings for a company
     */
    void createDefaultSettings(Long companyId);

    /**
     * Initialize default payment settings for a company (returns DTO)
     */
    CompanyPaymentSettingsResponseDTO initializeDefaultSettings(Long companyId);

    /**
     * Set current admin email as notification email
     */
    CompanyPaymentSettingsResponseDTO setAdminEmailAsNotification(Long companyId);
}
