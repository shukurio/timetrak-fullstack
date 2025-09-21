package com.timetrak.mapper;

import com.timetrak.dto.company.settings.CompanyPaymentSettingsRequestDTO;
import com.timetrak.dto.company.settings.CompanyPaymentSettingsResponseDTO;
import com.timetrak.entity.CompanyPaymentSettings;
import org.springframework.stereotype.Component;

@Component
public class CompanyPaymentSettingsMapper {

    public CompanyPaymentSettingsResponseDTO toResponseDTO(CompanyPaymentSettings entity) {
        if (entity == null) {
            return null;
        }

        return CompanyPaymentSettingsResponseDTO.builder()
                .id(entity.getId())
                .companyId(entity.getCompany() != null ? entity.getCompany().getId() : null)
                .payFrequency(entity.getPayFrequency())
                .firstDay(entity.getFirstDay())
                .calculationDay(entity.getCalculationDay())
                .calculationTime(entity.getCalculationTime())
                .autoCalculate(entity.getAutoCalculate())
                .gracePeriodHours(entity.getGracePeriodHours())
                .notifyOnCalculation(entity.getNotifyOnCalculation())
                .notificationEmail(entity.getNotificationEmail())
                .build();
    }

    public CompanyPaymentSettings toEntity(CompanyPaymentSettingsRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        return CompanyPaymentSettings.builder()
                .payFrequency(dto.getPayFrequency())
                .firstDay(dto.getFirstDay())
                .calculationDay(dto.getCalculationDay())
                .calculationTime(dto.getCalculationTime())
                .autoCalculate(dto.getAutoCalculate())
                .gracePeriodHours(dto.getGracePeriodHours())
                .notifyOnCalculation(dto.getNotifyOnCalculation())
                .notificationEmail(dto.getNotificationEmail())
                .build();
    }

    public void updateEntityFromDTO(CompanyPaymentSettingsRequestDTO dto, CompanyPaymentSettings entity) {
        if (dto == null || entity == null) {
            return;
        }

        if (dto.getPayFrequency() != null) {
            entity.setPayFrequency(dto.getPayFrequency());
        }
        if (dto.getFirstDay() != null) {
            entity.setFirstDay(dto.getFirstDay());
        }
        if (dto.getCalculationDay() != null) {
            entity.setCalculationDay(dto.getCalculationDay());
        }
        if (dto.getCalculationTime() != null) {
            entity.setCalculationTime(dto.getCalculationTime());
        }
        if (dto.getAutoCalculate() != null) {
            entity.setAutoCalculate(dto.getAutoCalculate());
        }
        if (dto.getGracePeriodHours() != null) {
            entity.setGracePeriodHours(dto.getGracePeriodHours());
        }
        if (dto.getNotifyOnCalculation() != null) {
            entity.setNotifyOnCalculation(dto.getNotifyOnCalculation());
        }
        if (dto.getNotificationEmail() != null) {
            entity.setNotificationEmail(dto.getNotificationEmail());
        }
    }
}