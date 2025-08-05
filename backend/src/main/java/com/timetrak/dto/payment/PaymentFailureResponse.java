package com.timetrak.dto.payment;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentFailureResponse {
    private Long employeeId;
    private String period;
    private String errorMessage;
    private String errorCode;
    @JsonIgnore
    private Exception cause;
}
