package com.timetrak.exception.employee;

public class EmployeeNotFoundException extends EmployeeException {
    private static final String ERROR_CODE = "EMPLOYEE_NOT_FOUND";

    private final Long employeeId;

    public EmployeeNotFoundException(Long employeeId) {
        super(String.format("Employee not found with ID: %d", employeeId), ERROR_CODE);
        this.employeeId = employeeId;
    }

    public EmployeeNotFoundException(String message) {
        super(message, ERROR_CODE);
        this.employeeId = null;
    }

    public Long getEmployeeId() {
        return employeeId;
    }


}
