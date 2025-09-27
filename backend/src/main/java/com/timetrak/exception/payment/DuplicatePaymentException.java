package com.timetrak.exception.payment;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.LocalDate;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicatePaymentException extends PaymentException {
  private static final String ERROR_CODE = "DUPLICATE_PAYMENT";

  private final Long employeeId;
  private final LocalDate periodStart;
  private final LocalDate periodEnd;

  public DuplicatePaymentException(String message) {
    super(message, ERROR_CODE);
    this.employeeId = null;
    this.periodStart = null;
    this.periodEnd = null;
  }


  public Long getEmployeeId() {
    return employeeId;
  }

  public LocalDate getPeriodStart() {
    return periodStart;
  }

  public LocalDate getPeriodEnd() {
    return periodEnd;
  }
}