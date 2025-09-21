package com.timetrak.exception.employee;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidEmployeeException extends EmployeeException {
    private static final String ERROR_CODE = "INVALID_EMPLOYEE";

    private final List<Long> invalidEmployeeIds;
    private final String validationFailureReason;

    public InvalidEmployeeException(String message) {
        super(message, ERROR_CODE);
        this.invalidEmployeeIds = null;
        this.validationFailureReason = null;
    }

    public InvalidEmployeeException(String message, Long employeeId) {
        super(message, ERROR_CODE, employeeId);
        this.invalidEmployeeIds = null;
        this.validationFailureReason = null;
    }

    public InvalidEmployeeException(String message, List<Long> invalidEmployeeIds) {
        super(message, ERROR_CODE);
        this.invalidEmployeeIds = invalidEmployeeIds;
        this.validationFailureReason = null;
    }

    public InvalidEmployeeException(String message, Long employeeId, String validationFailureReason) {
        super(message, ERROR_CODE, employeeId);
        this.invalidEmployeeIds = null;
        this.validationFailureReason = validationFailureReason;
    }

    public InvalidEmployeeException(String message, List<Long> invalidEmployeeIds, String validationFailureReason) {
        super(message, ERROR_CODE);
        this.invalidEmployeeIds = invalidEmployeeIds;
        this.validationFailureReason = validationFailureReason;
    }

    public List<Long> getInvalidEmployeeIds() {
        return invalidEmployeeIds;
    }

    public String getValidationFailureReason() {
        return validationFailureReason;
    }
}