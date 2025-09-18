package com.timetrak.controller;

import com.timetrak.dto.payment.PeriodSummaryDTO;
import com.timetrak.service.auth.AuthContextService;
import com.timetrak.service.payment.PeriodService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/periods")
@RequiredArgsConstructor
@Slf4j
public class PeriodController {

    private final PeriodService paymentPeriodService;
    private final AuthContextService authContextService;

    @GetMapping("/current")
    public ResponseEntity<PeriodSummaryDTO> getCurrentPaymentPeriod() {
        PeriodSummaryDTO currentPeriod = paymentPeriodService.getCurrentPeriodDTO(companyId());
        
        log.debug("Retrieved current payment period for company: {}", companyId());
        
        return ResponseEntity.ok(currentPeriod);
    }

    @GetMapping("/most-recent-completed")
    public ResponseEntity<PeriodSummaryDTO> getMostRecentCompletedPeriod() {
        PeriodSummaryDTO mostRecentCompleted = paymentPeriodService.getMostRecentCompletedPeriodSummary(companyId());
        
        log.debug("Retrieved most recent completed payment period for company: {}", companyId());
        
        return ResponseEntity.ok(mostRecentCompleted);
    }

    @GetMapping("/available")
    public ResponseEntity<List<PeriodSummaryDTO>> getAvailablePaymentPeriods(
            @RequestParam(defaultValue = "12") int numberOfPeriods) {

        // Limit to reasonable number
        if (numberOfPeriods > 24) {
            numberOfPeriods = 24;
        }
        
        List<PeriodSummaryDTO> summaryDTOs = paymentPeriodService.getAvailablePaymentPeriodSummaries(numberOfPeriods, companyId());
        
        log.debug("Retrieved {} payment periods for company: {}", summaryDTOs.size(), companyId());
        
        return ResponseEntity.ok(summaryDTOs);
    }

    private Long companyId(){
        return authContextService.getCurrentCompanyId();
    }
}