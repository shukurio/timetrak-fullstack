package com.timetrak.service.companyPaymentSettings.impl;

import com.timetrak.dto.company.settings.CompanyPaymentSettingsRequestDTO;
import com.timetrak.dto.company.settings.CompanyPaymentSettingsResponseDTO;
import com.timetrak.entity.Company;
import com.timetrak.entity.CompanyPaymentSettings;
import com.timetrak.enums.PayFrequency;
import com.timetrak.exception.ResourceNotFoundException;
import com.timetrak.exception.payment.PaymentSettingsConfigurationException;
import com.timetrak.mapper.CompanyPaymentSettingsMapper;
import com.timetrak.repository.CompanyPaymentSettingsRepository;
import com.timetrak.repository.CompanyRepository;
import com.timetrak.service.auth.AuthContextService;
import com.timetrak.service.companyPaymentSettings.CompanyPaymentSettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CompanyPaymentSettingsServiceImpl implements CompanyPaymentSettingsService {

    private final CompanyPaymentSettingsRepository settingsRepository;
    private final CompanyRepository companyRepository;
    private final CompanyPaymentSettingsMapper mapper;
    private final AuthContextService authContextService;

    @Override
    @Transactional(readOnly = true)
    public Optional<CompanyPaymentSettingsResponseDTO> getPaymentSettings(Long companyId) {
        log.debug("Getting payment settings for company: {}", companyId);
        
        return settingsRepository.findByCompanyId(companyId)
                .map(mapper::toResponseDTO);
    }

    @Override
    public CompanyPaymentSettingsResponseDTO createPaymentSettings(Long companyId, CompanyPaymentSettingsRequestDTO request) {
        log.debug("Creating payment settings for company: {}", companyId);

        // Validate company exists
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with ID: " + companyId));

        // Check if settings already exist
        if (settingsRepository.existsByCompanyId(companyId)) {
            throw new PaymentSettingsConfigurationException(
                    "Payment settings already exist for company: " + companyId);
        }

        // Create settings
        CompanyPaymentSettings settings = mapper.toEntity(request);
        settings.setCompany(company);

        // Set defaults if not provided
        setDefaultsIfNeeded(settings);

        CompanyPaymentSettings saved = settingsRepository.save(settings);
        log.info("Created payment settings for company: {}", companyId);

        return mapper.toResponseDTO(saved);
    }

    @Override
    public CompanyPaymentSettingsResponseDTO updatePaymentSettings(Long companyId, CompanyPaymentSettingsRequestDTO request) {
        log.debug("Updating payment settings for company: {}", companyId);

        CompanyPaymentSettings existing = settingsRepository.findByCompanyId(companyId)
                .orElseThrow(() -> new PaymentSettingsConfigurationException(
                        "Payment settings not found for company: " + companyId));

        // Update fields
        mapper.updateEntityFromDTO(request, existing);

        CompanyPaymentSettings saved = settingsRepository.save(existing);
        log.info("Updated payment settings for company: {}", companyId);

        return mapper.toResponseDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasPaymentSettings(Long companyId) {
        return settingsRepository.existsByCompanyId(companyId);
    }


    @Override
    public void createDefaultSettings(Long companyId) {
        log.debug("Creating default payment settings for company: {}", companyId);

        // Validate company exists
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with ID: " + companyId));

        // Check if settings already exist
        if (settingsRepository.existsByCompanyId(companyId)) {
            log.warn("Payment settings already exist for company: {}, returning existing", companyId);
            settingsRepository.findByCompanyId(companyId).orElseThrow(() -> new ResourceNotFoundException("Company not found with ID: " + companyId));
            return;
        }

        // Create default settings
        PayFrequency defaultFrequency = PayFrequency.BIWEEKLY;
        CompanyPaymentSettings settings = CompanyPaymentSettings.builder()
                .company(company)
                .payFrequency(defaultFrequency)
                .firstDay(LocalDate.now().with(java.time.temporal.TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)))
                .calculationDay(getDefaultCalculationDay())
                .calculationTime(LocalTime.of(2, 0)) // 2 AM
                .autoCalculate(true)
                .gracePeriodHours(getDefaultGracePeriodHours(defaultFrequency))
                .notifyOnCalculation(true)
                .notificationEmail(getDefaultNotificationEmail())
                .build();

        settingsRepository.save(settings);
        log.info("Created default payment settings for company: {}", companyId);

    }

    @Override
    public CompanyPaymentSettingsResponseDTO initializeDefaultSettings(Long companyId) {
        log.debug("Initializing default payment settings for company: {}", companyId);

        // Check if settings already exist, return them if they do
        Optional<CompanyPaymentSettingsResponseDTO> existing = getPaymentSettings(companyId);
        if (existing.isPresent()) {
            log.debug("Payment settings already exist for company: {}, returning existing", companyId);
            return existing.get();
        }

        // Create default settings
        createDefaultSettings(companyId);
        
        // Return the created settings
        return getPaymentSettings(companyId)
                .orElseThrow(() -> new PaymentSettingsConfigurationException(
                        "Failed to retrieve created default settings for company: " + companyId));
    }

    private void setDefaultsIfNeeded(CompanyPaymentSettings settings) {
        if (settings.getPayFrequency() == null) {
            settings.setPayFrequency(PayFrequency.BIWEEKLY);
        }
        if (settings.getFirstDay() == null) {
            settings.setFirstDay(LocalDate.now().with(java.time.temporal.TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)));
        }
        if (settings.getCalculationDay() == null) {
            settings.setCalculationDay(getDefaultCalculationDay());
        }
        if (settings.getCalculationTime() == null) {
            settings.setCalculationTime(LocalTime.of(2, 0));
        }
        if (settings.getAutoCalculate() == null) {
            settings.setAutoCalculate(true);
        }
        if (settings.getGracePeriodHours() == null) {
            settings.setGracePeriodHours(getDefaultGracePeriodHours(settings.getPayFrequency()));
        }
        if (settings.getNotifyOnCalculation() == null) {
            settings.setNotifyOnCalculation(true);
        }
        if (settings.getNotificationEmail() == null) {
            settings.setNotificationEmail(getDefaultNotificationEmail());
        }
    }

    /**
     * Get smart default calculation day based on pay frequency
     * Accounts for grace period to allow late clock-outs
     */
    private DayOfWeek getDefaultCalculationDay() {
        return DayOfWeek.TUESDAY;
    }

    /**
     * Get smart default grace period hours based on pay frequency
     * Allows time for employees who work across period boundaries
     */
    private Integer getDefaultGracePeriodHours(PayFrequency frequency) {
        return switch (frequency) {
            case WEEKLY -> 72;    // 3 days (weekend shifts that run into Monday)
            case BIWEEKLY -> 72;  // 3 days (common for shift workers)
            case MONTHLY -> 96;   // 4 days (month-end processing needs more buffer)
        };
    }


    /**
     * Get default notification email (current admin email)
     */
    private String getDefaultNotificationEmail() {
        try {
            return authContextService.getCurrentUserEmail();
        } catch (Exception e) {
            log.warn("Could not get current user email for notification default: {}", e.getMessage());
            return null; // Will be set to null, admin can configure later
        }
    }

    @Override
    public CompanyPaymentSettingsResponseDTO setAdminEmailAsNotification(Long companyId) {
        log.debug("Setting current admin email as notification email for company: {}", companyId);

        CompanyPaymentSettings settings = settingsRepository.findByCompanyId(companyId)
                .orElseThrow(() -> new PaymentSettingsConfigurationException(
                        "Payment settings not found for company: " + companyId));

        String adminEmail = getDefaultNotificationEmail();
        if (adminEmail == null) {
            throw new PaymentSettingsConfigurationException("Could not retrieve current admin email");
        }

        settings.setNotificationEmail(adminEmail);
        CompanyPaymentSettings saved = settingsRepository.save(settings);

        log.info("Updated notification email to admin email: {} for company: {}", adminEmail, companyId);

        return mapper.toResponseDTO(saved);
    }
}