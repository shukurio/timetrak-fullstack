package com.timetrak.controller;

import com.timetrak.dto.payment.PaymentDetailsDTO;
import com.timetrak.dto.payment.status.StatusUpdateRequest;
import com.timetrak.dto.payment.status.StatusUpdateResponse;
import com.timetrak.enums.PaymentStatus;
import com.timetrak.service.auth.AuthContextService;
import com.timetrak.service.payment.PaymentService;
import com.timetrak.service.payment.paymentManagement.PaymentManagementService;
import com.timetrak.service.payment.report.PaymentExporterService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@Slf4j
@Validated
@RequestMapping("/api/admin/payments")
public class AdminPaymentController {
    private final PaymentService paymentService;
    private final AuthContextService authContext;
    private final PaymentManagementService manager;
    private final PaymentExporterService exporter;

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentDetailsDTO> getUserPaymentDetails(@PathVariable Long paymentId) {
        return ResponseEntity.ok(paymentService.getPaymentById(paymentId,
                authContext.getCurrentEmployeeId(),
                authContext.getCurrentCompanyId()));
    }

    @GetMapping("/all")
    public ResponseEntity<Page<PaymentDetailsDTO>> getAllPaymentsForCompany(Pageable pageable) {
        Long companyId = authContext.getCurrentCompanyId();
        return ResponseEntity.ok(paymentService.getAllPaymentsForCompany(companyId,pageable));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Page<PaymentDetailsDTO>> getPaymentsByStatus(@PathVariable PaymentStatus status, Pageable pageable) {
        Long companyId = authContext.getCurrentCompanyId();
        Page<PaymentDetailsDTO> payments = paymentService.getPaymentsByStatus(companyId,status,pageable);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/employee/{empId}/payments/{paymentId}")
    public ResponseEntity<PaymentDetailsDTO> getPaymentWithDetails(@PathVariable Long empId,@PathVariable Long paymentId) {

        PaymentDetailsDTO payment = paymentService.getPaymentWithDetails(paymentId,empId,authContext.getCurrentCompanyId());

        return ResponseEntity.ok(payment);
    }

    @PostMapping("/updateStatus")
    public ResponseEntity<StatusUpdateResponse> updatePaymentStatus(@Valid @RequestBody StatusUpdateRequest request) {
        StatusUpdateResponse response =manager.updatePaymentStatus(request,
                authContext.getCurrentCompanyId(),
                authContext.getCurrentEmployeeId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportPayments(
            @RequestParam @Min(value = 1, message = "Period number must be 1 or higher")
                                                     Integer periodNumber) {

        Long companyId = authContext.getCurrentCompanyId();
        byte[] pdfData = exporter.exportPayments(periodNumber, companyId);

        String filename = String.format("payments_period_%d.pdf", periodNumber);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/pdf"))
                .body(pdfData);
    }

}
