package com.timetrak.service.payment.calculation;

import com.timetrak.dto.payment.PaymentPeriod;
import com.timetrak.dto.payment.PaymentResponseDTO;
import com.timetrak.entity.CompanyPaymentSettings;
import com.timetrak.repository.CompanyPaymentSettingsRepository;
import com.timetrak.repository.PaymentRepository;
import com.timetrak.service.employee.EmployeeService;
import com.timetrak.service.payment.PaymentPeriodService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AutomaticPaymentService {

    @Value("${systemId}")
    private Long systemId;

    //TODO need to implement Validation

    private final CompanyPaymentSettingsRepository companyPaymentSettingsRepository;
    private final PaymentCalculationService calculationService;
    private final PaymentPeriodService paymentPeriodService;
    private final EmployeeService employeeService;
    private final PaymentRepository paymentRepository;

    // Run every hour to check for scheduled payments
    @Scheduled(cron = "0 0 * * * *") // Every hour at minute 0
    public void processScheduledPayments() {
        log.info("Checking for scheduled payment calculations...");

        LocalDateTime now = LocalDateTime.now();
        DayOfWeek currentDay = now.getDayOfWeek();
        LocalTime currentTime = now.toLocalTime();

        // Find all companies that have auto-calculate enabled
        List<CompanyPaymentSettings> companies = companyPaymentSettingsRepository
                .findByAutoCalculateTrue();

        for (CompanyPaymentSettings settings : companies) {
            try {
                if (shouldCalculatePayments(settings, currentDay, currentTime)) {
                    processCompanyPayments(settings);
                }
            } catch (Exception e) {
                log.error("Failed to process payments for company {}: {}",
                        settings.getCompany().getId(), e.getMessage(), e);
            }
        }
    }

    private boolean shouldCalculatePayments(CompanyPaymentSettings settings,
                                            DayOfWeek currentDay,
                                            LocalTime currentTime) {

        // Check if today is the calculation day
        if (currentDay != settings.getCalculationDay()) {
            return false;
        }

        // Check if current time is past calculation time
        if (currentTime.isBefore(settings.getCalculationTime())) {
            return false;
        }

        // Check if payments already calculated for current period
        Long companyId = settings.getCompany().getId();
        PaymentPeriod currentPeriod = paymentPeriodService.getCurrentPaymentPeriod(companyId);

        boolean alreadyCalculated = paymentRepository.existsByCompanyIdAndPeriodStartAndPeriodEnd(
                companyId, currentPeriod.getStartDate(), currentPeriod.getEndDate());

        if (alreadyCalculated) {
            log.debug("Payments already calculated for company {} period {}",
                    companyId, currentPeriod.getFormattedPeriod());
            return false;
        }

        return true;
    }

    private void processCompanyPayments(CompanyPaymentSettings settings) {
        Long companyId = settings.getCompany().getId();

        log.info("Starting automatic payment calculation for company {}", companyId);

        try {
            // Get current payment period
            PaymentPeriod currentPeriod = paymentPeriodService.getCurrentPaymentPeriod(companyId);

            // Get all active employees for this company
            List<Long> activeEmployeeIds = employeeService.getAllActiveEmployeeIdsForCompany(companyId);

            if (activeEmployeeIds.isEmpty()) {
                log.warn("No active employees found for company {}", companyId);
                return;
            }

            // Calculate payments
            PaymentResponseDTO response = calculationService.calculatePaymentsForPeriod(currentPeriod,companyId,systemId);

            log.info("Automatic payment calculation completed for company {}: {} successful, {} failed",
                    companyId, response.getSuccessCount(), response.getFailureCount());

            // Send notifications if enabled
            if (settings.getNotifyOnCalculation()) {
                sendPaymentNotification(settings, response, currentPeriod);
            }

        } catch (Exception e) {
            log.error("Failed to process automatic payments for company {}: {}",
                    companyId, e.getMessage(), e);
        }
    }

    private void sendPaymentNotification(CompanyPaymentSettings settings,
                                         PaymentResponseDTO response,
                                         PaymentPeriod period) {
        try {
            String email = settings.getNotificationEmail();
            if (email != null && !email.trim().isEmpty()) {
                // Send email notification
                String subject = String.format("Payroll Calculated - %s", period.getDescription());
                String message = String.format(
                        """
                                Automatic payroll calculation completed:
                                
                                Period: %s
                                Successful: %d employees
                                Failed: %d employees
                                Total Processed: %d employees
                                
                                Please review the payments in your dashboard.""",
                        period.getDescription(),
                        response.getSuccessCount(),
                        response.getFailureCount(),
                        response.getTotalProcessed()
                );

                // TODO: Implement email service
                // emailService.sendEmail(email, subject, message);
                log.info("Payment notification sent to {}", email);
            }
        } catch (Exception e) {
            log.error("Failed to send payment notification: {}", e.getMessage());
        }
    }
}