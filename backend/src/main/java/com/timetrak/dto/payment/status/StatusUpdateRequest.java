package com.timetrak.dto.payment.status;

import com.timetrak.enums.PaymentStatus;
import lombok.*;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.UniqueElements;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatusUpdateRequest {

    @NotEmpty(message = "Payment IDs cannot be empty")
    @UniqueElements(message = "Duplicate payment IDs are not allowed")
    private List<Long> paymentIds;

    @NotNull(message = "Payment status is required")
    private PaymentStatus targetStatus;

    private String reason; // Optional reason for the status change
}
