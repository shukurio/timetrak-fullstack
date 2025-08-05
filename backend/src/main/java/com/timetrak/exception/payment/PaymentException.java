package com.timetrak.exception.payment;

public abstract class PaymentException extends RuntimeException {
  private final String errorCode;

  protected PaymentException(String message, String errorCode) {
    super(message);
    this.errorCode = errorCode;
  }

  protected PaymentException(String message, String errorCode, Throwable cause) {
    super(message, cause);
    this.errorCode = errorCode;
  }

  public String getErrorCode() {
    return errorCode;
  }
}