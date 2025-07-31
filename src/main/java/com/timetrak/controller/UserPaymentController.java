package com.timetrak.controller;

import com.timetrak.dto.payment.PaymentDetailsDTO;
import com.timetrak.service.auth.AuthContextService;
import com.timetrak.service.payment.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/payments")
public class UserPaymentController {
    private final PaymentService paymentService;
    private final AuthContextService authContext;

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentDetailsDTO> getPaymentWithDetails(@PathVariable Long paymentId) {
        Long companyId = authContext.getCurrentCompanyId();
        Long empId=authContext.getCurrentEmployeeId();
        PaymentDetailsDTO payment = paymentService.getPaymentWithDetails(paymentId,empId,companyId);

        return ResponseEntity.ok(payment);
    }

    @GetMapping("/all")
    public ResponseEntity<Page<PaymentDetailsDTO>> getAllPayments(Pageable pageable) {
        Long companyId = authContext.getCurrentCompanyId();
        Long employeeId = authContext.getCurrentEmployeeId();
        return ResponseEntity.ok(paymentService.getAllPaymentsForEmployee(employeeId,companyId,pageable));
    }

}
