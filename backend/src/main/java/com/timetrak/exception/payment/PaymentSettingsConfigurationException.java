package com.timetrak.exception.payment;


public class PaymentSettingsConfigurationException extends PaymentException {

    private static final String ERROR_CODE = "PAYMENT_SETTINGS_NOT_CONFIGURED_PROPERLY";

    public PaymentSettingsConfigurationException(String message) {
        super(message,ERROR_CODE);
    }

    public PaymentSettingsConfigurationException(String message, Throwable cause) {
            super(message, ERROR_CODE, cause);
    }
}