package com.timetrak.controller;

import com.timetrak.dto.payment.PaymentRequestDTO;
import com.timetrak.dto.payment.PaymentResponseDTO;
import com.timetrak.service.payment.AutomaticPaymentService;
import com.timetrak.service.payment.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentService paymentService;
    private final AutomaticPaymentService automaticPaymentService;

    @PostMapping("/calculate-period")
    @PreAuthorize("hasRole('ADMIN')")
    //TODO always validate if request is valid, cant have empty request in calculate period
    public ResponseEntity<PaymentResponseDTO> calculatePaymentsForPeriod(
            @RequestBody PaymentRequestDTO request) {
        PaymentResponseDTO response = paymentService.calculatePayments(request);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/trigger-automatic")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> triggerAutomaticPayments() {
        automaticPaymentService.processScheduledPayments();
        return ResponseEntity.ok("Automatic payment processing triggered successfully");
    }
}