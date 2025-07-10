package com.timetrak.exception.employee;



import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class EmployeeValidationException extends EmployeeException {
    private static final String ERROR_CODE = "EMPLOYEE_VALIDATION_FAILED";


    public EmployeeValidationException(String message) {
        super(message, ERROR_CODE);
    }


    public EmployeeValidationException(String message, Long employeeId) {
        super(message, ERROR_CODE, employeeId);
    }

    public EmployeeValidationException(String message, Throwable cause) {
        super(message, ERROR_CODE, cause);
    }
}