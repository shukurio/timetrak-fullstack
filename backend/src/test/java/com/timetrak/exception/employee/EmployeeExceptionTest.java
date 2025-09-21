package com.timetrak.exception.employee;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Employee Exception Tests")
class EmployeeExceptionTest {

    @Test
    @DisplayName("EmployeeNotFoundException should contain employee ID")
    void employeeNotFoundException_WithEmployeeId_ShouldContainId() {

        EmployeeNotFoundException exception = new EmployeeNotFoundException(123L);
        assertEquals("EMPLOYEE_NOT_FOUND", exception.getErrorCode());
        assertEquals(123L, exception.getEmployeeId());
        assertTrue(exception.getMessage().contains("123"));
    }

    @Test
    @DisplayName("DuplicateEmployeeException should contain field information")
    void duplicateEmployeeException_WithFieldInfo_ShouldContainDetails() {
        DuplicateEmployeeException exception = new DuplicateEmployeeException("username", "johndoe");

        assertEquals("DUPLICATE_EMPLOYEE", exception.getErrorCode());
        assertEquals("username", exception.getDuplicateField());
        assertEquals("johndoe", exception.getDuplicateValue());
        assertTrue(exception.getMessage().contains("username"));
        assertTrue(exception.getMessage().contains("johndoe"));
    }

    @Test
    @DisplayName("EmployeeValidationException should contain validation message")
    void employeeValidationException_WithMessage_ShouldContainMessage() {
        EmployeeValidationException exception = new EmployeeValidationException("Invalid email format");

        assertEquals("EMPLOYEE_VALIDATION_FAILED", exception.getErrorCode());
        assertEquals("Invalid email format", exception.getMessage());
    }

    @Test
    @DisplayName("InvalidEmployeeException should contain employee ID and reason")
    void invalidEmployeeException_WithEmployeeIdAndReason_ShouldContainDetails() {
        InvalidEmployeeException exception = new InvalidEmployeeException("Cannot delete active employee", 123L);

        assertEquals("INVALID_EMPLOYEE", exception.getErrorCode());
        assertEquals(123L, exception.getEmployeeId());
        assertEquals("Cannot delete active employee", exception.getMessage());
    }


    @Test
    @DisplayName("DuplicateEmployeeException with existing employee ID should contain all details")
    void duplicateEmployeeException_WithExistingEmployeeId_ShouldContainAllDetails() {
        DuplicateEmployeeException exception = new DuplicateEmployeeException("email", "john@test.com", 456L);

        assertEquals("DUPLICATE_EMPLOYEE", exception.getErrorCode());
        assertEquals("email", exception.getDuplicateField());
        assertEquals("john@test.com", exception.getDuplicateValue());
        assertEquals(456L, exception.getExistingEmployeeId());
        assertTrue(exception.getMessage().contains("456"));
    }

    @Test
    @DisplayName("DuplicateEmployeeException factory methods should work correctly")
    void duplicateEmployeeException_FactoryMethods_ShouldCreateCorrectly() {
        DuplicateEmployeeException usernameEx = DuplicateEmployeeException.forUsername("johndoe");
        assertEquals("username", usernameEx.getDuplicateField());
        assertEquals("johndoe", usernameEx.getDuplicateValue());

        DuplicateEmployeeException emailEx = DuplicateEmployeeException.forEmail("john@test.com");
        assertEquals("email", emailEx.getDuplicateField());
    }

    @Test
    @DisplayName("InvalidEmployeeException with multiple employee IDs should contain list")
    void invalidEmployeeException_WithMultipleIds_ShouldContainList() {
        List<Long> invalidIds = Arrays.asList(1L, 2L, 3L);
        InvalidEmployeeException exception = new InvalidEmployeeException("Multiple invalid employees", invalidIds);

        assertEquals("INVALID_EMPLOYEE", exception.getErrorCode());
        assertEquals(invalidIds, exception.getInvalidEmployeeIds());
        assertTrue(exception.getMessage().contains("Multiple invalid employees"));
    }

    @Test
    @DisplayName("InvalidEmployeeException with validation reason should contain reason")
    void invalidEmployeeException_WithValidationReason_ShouldContainReason() {
        InvalidEmployeeException exception = new InvalidEmployeeException(
                "Cannot activate employee", 123L, "Employee is already active");

        assertEquals("INVALID_EMPLOYEE", exception.getErrorCode());
        assertEquals(123L, exception.getEmployeeId());
        assertEquals("Employee is already active", exception.getValidationFailureReason());
    }

    @Test
    @DisplayName("EmployeeValidationException with employee ID should contain ID")
    void employeeValidationException_WithEmployeeId_ShouldContainId() {
        EmployeeValidationException exception = new EmployeeValidationException("Invalid data", 123L);

        assertEquals("EMPLOYEE_VALIDATION_FAILED", exception.getErrorCode());
        assertEquals(123L, exception.getEmployeeId());
        assertEquals("Invalid data", exception.getMessage());
    }


}