package com.timetrak.controller.admin;

import com.timetrak.dto.payment.PaymentPeriod;
import com.timetrak.dto.payment.PaymentPeriodSummaryDTO;
import com.timetrak.service.auth.AuthContextService;
import com.timetrak.service.payment.PeriodService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/periods")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminPaymentPeriodController {

    private final PeriodService paymentPeriodService;
    private final AuthContextService authContextService;

    @GetMapping("/current")
    public ResponseEntity<PaymentPeriod> getCurrentPaymentPeriod() {
        PaymentPeriod currentPeriod = paymentPeriodService.getCurrentPaymentPeriod(companyId());
        
        log.debug("Retrieved current payment period for company: {}", companyId());
        
        return ResponseEntity.ok(currentPeriod);
    }

    @GetMapping("/most-recent-completed")
    public ResponseEntity<PaymentPeriodSummaryDTO> getMostRecentCompletedPeriod() {
        PaymentPeriodSummaryDTO mostRecentCompleted = paymentPeriodService.getMostRecentCompletedPeriodSummary(companyId());
        
        log.debug("Retrieved most recent completed payment period for company: {}", companyId());
        
        return ResponseEntity.ok(mostRecentCompleted);
    }

    @GetMapping("/available")
    public ResponseEntity<List<PaymentPeriodSummaryDTO>> getAvailablePaymentPeriods(
            @RequestParam(defaultValue = "12") int numberOfPeriods) {

        // Limit to reasonable number
        if (numberOfPeriods > 24) {
            numberOfPeriods = 24;
        }
        
        List<PaymentPeriodSummaryDTO> summaryDTOs = paymentPeriodService.getAvailablePaymentPeriodSummaries(numberOfPeriods, companyId());
        
        log.debug("Retrieved {} payment periods for company: {}", summaryDTOs.size(), companyId());
        
        return ResponseEntity.ok(summaryDTOs);
    }

    @GetMapping("/period/{periodNumber}")
    public ResponseEntity<PaymentPeriod> getPaymentPeriodByNumber(@PathVariable Integer periodNumber) {
        PaymentPeriod period = paymentPeriodService.getPaymentPeriodByNumber(periodNumber, companyId());
        
        log.debug("Retrieved payment period {} for company: {}", periodNumber, companyId());
        
        return ResponseEntity.ok(period);
    }
    private Long companyId(){
        return authContextService.getCurrentCompanyId();
    }
}