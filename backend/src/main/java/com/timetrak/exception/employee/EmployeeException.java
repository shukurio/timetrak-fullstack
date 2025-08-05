package com.timetrak.exception.employee;



public abstract class EmployeeException extends RuntimeException {
    private final String errorCode;
    private final Long employeeId;

    protected EmployeeException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.employeeId = null;
    }

    protected EmployeeException(String message, String errorCode, Long employeeId) {
        super(message);
        this.errorCode = errorCode;
        this.employeeId = employeeId;
    }

    protected EmployeeException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.employeeId = null;
    }

    protected EmployeeException(String message, String errorCode, Long employeeId, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.employeeId = employeeId;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public Long getEmployeeId() {
        return employeeId;
    }
}