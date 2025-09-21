package com.timetrak.dto.payment.status;

import com.timetrak.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class StatusUpdateSuccess {
    private Long paymentId;
    private PaymentStatus previousStatus;
    private PaymentStatus newStatus;
    private LocalDateTime updatedAt;
    private Long updatedBy;
}
