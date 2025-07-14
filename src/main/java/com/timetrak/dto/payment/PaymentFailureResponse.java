package com.timetrak.dto.payment;


import lombok.Builder;
@Builder
public class PaymentFailureResponse {
    private Long employeeId;
    private Long paymentId;
    private String username;
    private String fullName;
    private String period;
    private String errorMessage;
    private String errorCode;
    private String failureReason;


}
