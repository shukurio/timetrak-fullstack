package com.timetrak.exception.employee;



import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateEmployeeException extends EmployeeException {
  private static final String ERROR_CODE = "DUPLICATE_EMPLOYEE";

  private final String duplicateField;
  private final String duplicateValue;
  private final Long existingEmployeeId;


  public DuplicateEmployeeException(String duplicateField, String duplicateValue) {
    super(String.format("Employee already exists with %s: %s", duplicateField, duplicateValue), ERROR_CODE);
    this.duplicateField = duplicateField;
    this.duplicateValue = duplicateValue;
    this.existingEmployeeId = null;
  }


  public DuplicateEmployeeException(String duplicateField, String duplicateValue, Long existingEmployeeId) {
    super(String.format("Employee already exists with %s: %s (Employee ID: %d)",
            duplicateField, duplicateValue, existingEmployeeId), ERROR_CODE);
    this.duplicateField = duplicateField;
    this.duplicateValue = duplicateValue;
    this.existingEmployeeId = existingEmployeeId;
  }


  public DuplicateEmployeeException(String message, String duplicateField, String duplicateValue) {
    super(message, ERROR_CODE);
    this.duplicateField = duplicateField;
    this.duplicateValue = duplicateValue;
    this.existingEmployeeId = null;
  }


  public static DuplicateEmployeeException forUsername(String username) {
    return new DuplicateEmployeeException("username", username);
  }


  public static DuplicateEmployeeException forEmail(String email) {
    return new DuplicateEmployeeException("email", email);
  }



  public static DuplicateEmployeeException forPhoneNumber(String phoneNumber) {
    return new DuplicateEmployeeException("phone number", phoneNumber);
  }

  // Getters
  public String getDuplicateField() {
    return duplicateField;
  }

  public String getDuplicateValue() {
    return duplicateValue;
  }

  public Long getExistingEmployeeId() {
    return existingEmployeeId;
  }
}
