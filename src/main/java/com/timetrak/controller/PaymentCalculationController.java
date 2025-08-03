package com.timetrak.controller;

import com.timetrak.dto.payment.PaymentRequestDTO;
import com.timetrak.dto.payment.PaymentResponseDTO;
import com.timetrak.service.auth.AuthContextService;
import com.timetrak.service.payment.calculation.AutomaticPaymentService;
import com.timetrak.service.payment.calculation.PaymentCalculationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/payments")
public class PaymentCalculationController {
    private final PaymentCalculationService calculationService;
    private final AutomaticPaymentService automaticPaymentService;
    private final AuthContextService authContextService;

    @PostMapping("/calculate-period")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentResponseDTO> calculatePaymentsForPeriod(
            @RequestBody PaymentRequestDTO request) {
        Long companyId = authContextService.getCurrentCompanyId();
        Long initiatorId=authContextService.getCurrentEmployeeId();
        PaymentResponseDTO response = calculationService.calculatePayments(request,companyId,initiatorId);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/trigger-automatic")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> triggerAutomaticPayments() {
        automaticPaymentService.processScheduledPayments();
        return ResponseEntity.ok("Automatic payment processing triggered successfully");
    }

}