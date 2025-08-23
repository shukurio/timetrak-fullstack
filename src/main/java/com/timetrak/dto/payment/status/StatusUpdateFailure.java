package com.timetrak.dto.payment.status;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class StatusUpdateFailure {
    private Long paymentId;
    private String errorMessage;
    private String errorCode;
    private String currentStatus;
    
    @JsonIgnore
    private Exception cause;
}
