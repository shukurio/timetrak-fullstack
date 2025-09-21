package com.timetrak.controller.admin;

import com.timetrak.dto.company.settings.CompanyPaymentSettingsRequestDTO;
import com.timetrak.dto.company.settings.CompanyPaymentSettingsResponseDTO;
import com.timetrak.service.auth.AuthContextService;
import com.timetrak.service.companyPaymentSettings.CompanyPaymentSettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/admin/company/payment-settings")
public class CompanyPaymentSettingsController {

    private final CompanyPaymentSettingsService paymentSettingsService;
    private final AuthContextService authContextService;

    @GetMapping
    public ResponseEntity<CompanyPaymentSettingsResponseDTO> getPaymentSettings() {
        
        Optional<CompanyPaymentSettingsResponseDTO> settings = paymentSettingsService.getPaymentSettings(companyId());
        
        return settings
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CompanyPaymentSettingsResponseDTO> createPaymentSettings(
            @Valid @RequestBody CompanyPaymentSettingsRequestDTO request) {
        
        CompanyPaymentSettingsResponseDTO response = paymentSettingsService.createPaymentSettings(companyId(), request);
        
        log.info("Created payment settings for company: {}", companyId());
        
        return ResponseEntity.ok(response);
    }

    @PutMapping
    public ResponseEntity<CompanyPaymentSettingsResponseDTO> updatePaymentSettings(
            @Valid @RequestBody CompanyPaymentSettingsRequestDTO request) {

        CompanyPaymentSettingsResponseDTO response = paymentSettingsService.updatePaymentSettings(companyId(), request);
        
        log.info("Updated payment settings for company: {}", companyId());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/exists")
    public ResponseEntity<Boolean> hasPaymentSettings() {

        boolean exists = paymentSettingsService.hasPaymentSettings(companyId());
        
        return ResponseEntity.ok(exists);
    }

    @PostMapping("/initialize")
    public ResponseEntity<CompanyPaymentSettingsResponseDTO> initializeDefaultSettings() {
        CompanyPaymentSettingsResponseDTO response = paymentSettingsService.initializeDefaultSettings(companyId());
        
        log.info("Initialized default payment settings for company: {}", companyId());
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/use-admin-email")
    public ResponseEntity<CompanyPaymentSettingsResponseDTO> setAdminEmailAsNotification() {
        CompanyPaymentSettingsResponseDTO response = paymentSettingsService.setAdminEmailAsNotification(companyId());
        
        log.info("Set admin email as notification email for company: {}", companyId());
        
        return ResponseEntity.ok(response);
    }

    private Long companyId(){
        return authContextService.getCurrentCompanyId();
    }
}
