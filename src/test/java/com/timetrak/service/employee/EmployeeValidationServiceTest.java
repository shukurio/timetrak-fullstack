package com.timetrak.service.employee;

import com.timetrak.dto.request.EmployeeRequestDTO;
import com.timetrak.entity.Employee;
import com.timetrak.enums.EmployeeStatus;
import com.timetrak.enums.Role;
import com.timetrak.exception.employee.DuplicateEmployeeException;
import com.timetrak.exception.employee.EmployeeValidationException;
import com.timetrak.exception.employee.InvalidEmployeeException;
import com.timetrak.repository.DepartmentRepository;
import com.timetrak.repository.EmployeeRepository;
import com.timetrak.repository.ShiftRepository;
import com.timetrak.service.DepartmentService;
import com.timetrak.service.ShiftService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Employee Validation Service Tests")
class EmployeeValidationServiceTest {

    @Mock private EmployeeRepository employeeRepository;
    @Mock private DepartmentRepository departmentRepository;
    @Mock private ShiftRepository shiftRepository;
    @InjectMocks private EmployeeValidationService validationService;

    private EmployeeRequestDTO validDto;
    private Employee employee;

    @BeforeEach
    void setUp() {
        validDto = EmployeeRequestDTO.builder()
                .firstName("John").lastName("Doe")
                .username("johndoe")
                .email("john@example.com").password("Password123!")
                .role(Role.EMPLOYEE).companyId(1L).departmentId(1L)
                .build();

        employee = Employee.builder()
                .id(1L)
                .username("johndoe")
                .email("john@example.com")
                .status(EmployeeStatus.ACTIVE).role(Role.EMPLOYEE)
                .build();
    }

    @Nested
    @DisplayName("Business Rules")
    class BusinessRules {

        @Test
        @DisplayName("Admin must have 'admin' in username")
        void adminMustHaveAdminInUsername() {
            validDto.setRole(Role.ADMIN);
            validDto.setUsername("regularuser");

            assertThrows(EmployeeValidationException.class,
                    () -> validationService.validateBusinessRules(validDto));
        }

        @Test
        @DisplayName("Non-admin cannot have 'admin' in username")
        void nonAdminCannotHaveAdminInUsername() {
            validDto.setUsername("admintest");

            assertThrows(EmployeeValidationException.class,
                    () -> validationService.validateBusinessRules(validDto));
        }

        @Test
        @DisplayName("Password cannot contain username")
        void passwordCannotContainUsername() {
            validDto.setUsername("john");
            validDto.setPassword("john123!");

            assertThrows(EmployeeValidationException.class,
                    () -> validationService.validateBusinessRules(validDto));
        }

        @Test
        @DisplayName("Valid data passes")
        void validDataPasses() {
            assertDoesNotThrow(() -> validationService.validateBusinessRules(validDto));
        }
    }

    @Nested
    @DisplayName("Uniqueness")
    class Uniqueness {

        @Test
        @DisplayName("Duplicate username rejected")
        void duplicateUsernameRejected() {
            when(employeeRepository.existsByUsername("johndoe")).thenReturn(true);

            DuplicateEmployeeException ex = assertThrows(DuplicateEmployeeException.class,
                    () -> validationService.validateUniqueness("johndoe", "john@example.com"));

            assertEquals("username", ex.getDuplicateField());
        }

        @Test
        @DisplayName("Duplicate email rejected")
        void duplicateEmailRejected() {
            when(employeeRepository.existsByUsername("johndoe")).thenReturn(false);
            when(employeeRepository.existsByEmail("john@example.com")).thenReturn(true);

            DuplicateEmployeeException ex = assertThrows(DuplicateEmployeeException.class,
                    () -> validationService.validateUniqueness("johndoe", "john@example.com"));

            assertEquals("email", ex.getDuplicateField());
        }

        @Test
        @DisplayName("Unique data accepted")
        void uniqueDataAccepted() {
            when(employeeRepository.existsByUsername(anyString())).thenReturn(false);
            when(employeeRepository.existsByEmail(anyString())).thenReturn(false);

            assertDoesNotThrow(() -> validationService.validateUniqueness("johndoe", "john@example.com"));
        }
    }

    @Nested
    @DisplayName("Status Changes")
    class StatusChanges {

        @Test
        @DisplayName("Cannot activate deleted employee")
        void cannotActivateDeletedEmployee() {
            employee.setStatus(EmployeeStatus.DELETED);

            InvalidEmployeeException ex = assertThrows(InvalidEmployeeException.class,
                    () -> validationService.validateActivation(employee));

            assertEquals("Cannot activate deleted employee", ex.getMessage());
        }

        @Test
        @DisplayName("Can activate pending employee")
        void canActivatePendingEmployee() {
            employee.setStatus(EmployeeStatus.PENDING);
            assertDoesNotThrow(() -> validationService.validateActivation(employee));
        }

        @Test
        @DisplayName("Cannot deactivate with active shifts")
        void cannotDeactivateWithActiveShifts() {
            when(shiftRepository.hasActiveShifts(1L)).thenReturn(true);

            assertThrows(InvalidEmployeeException.class,
                    () -> validationService.validateDeactivation(employee));
        }

        @Test
        @DisplayName("Can approve pending employee")
        void canApprovePendingEmployee() {
            employee.setStatus(EmployeeStatus.PENDING);
            assertDoesNotThrow(() -> validationService.validateApproval(employee));
        }

        @Test
        @DisplayName("Cannot approve non-pending employee")
        void cannotApproveNonPendingEmployee() {
            assertThrows(InvalidEmployeeException.class,
                    () -> validationService.validateApproval(employee));
        }

        @Test
        @DisplayName("Can reject pending employee")
        void canRejectPendingEmployee() {
            employee.setStatus(EmployeeStatus.PENDING);
            when(shiftRepository.hasActiveShifts(1L)).thenReturn(false);
            assertDoesNotThrow(() -> validationService.validateRejection(employee));
        }

        @Test
        @DisplayName("Can reactivate deactivated employee")
        void canReactivateDeactivatedEmployee() {
            employee.setStatus(EmployeeStatus.DEACTIVATED);
            when(shiftRepository.hasActiveShifts(1L)).thenReturn(false);
            assertDoesNotThrow(() -> validationService.validateReactivation(employee));
        }
    }

    @Nested
    @DisplayName("Work & Payment Eligibility")
    class WorkPaymentEligibility {

        @Test
        @DisplayName("Deleted employee cannot work")
        void deletedEmployeeCannotWork() {
            employee.setStatus(EmployeeStatus.DELETED);
            assertThrows(InvalidEmployeeException.class,
                    () -> validationService.validateCanWork(employee));
        }

        @Test
        @DisplayName("Active employee can work")
        void activeEmployeeCanWork() {
            assertDoesNotThrow(() -> validationService.validateCanWork(employee));
        }

        @Test
        @DisplayName("Deleted employee cannot receive payment")
        void deletedEmployeeCannotReceivePayment() {
            employee.setStatus(EmployeeStatus.DELETED);
            assertThrows(InvalidEmployeeException.class,
                    () -> validationService.validateCanReceivePayment(employee));
        }

        @Test
        @DisplayName("Active employee can receive payment")
        void activeEmployeeCanReceivePayment() {
            assertDoesNotThrow(() -> validationService.validateCanReceivePayment(employee));
        }

        @Test
        @DisplayName("Deactivated employee can receive payment")
        void deactivatedEmployeeCanReceivePayment() {
            employee.setStatus(EmployeeStatus.DEACTIVATED);
            assertDoesNotThrow(() -> validationService.validateCanReceivePayment(employee));
        }
    }

    @Nested
    @DisplayName("Registration & Updates")
    class RegistrationUpdates {

        @Test
        @DisplayName("Valid registration passes")
        void validRegistrationPasses() {
            when(employeeRepository.existsByUsername(anyString())).thenReturn(false);
            when(employeeRepository.existsByEmail(anyString())).thenReturn(false);
            when(departmentRepository.existsById(1L)).thenReturn(true);

            assertDoesNotThrow(() -> validationService.validateRegistration(validDto));
        }

        @Test
        @DisplayName("Invalid department rejected")
        void invalidDepartmentRejected() {
            when(employeeRepository.existsByUsername(anyString())).thenReturn(false);
            when(employeeRepository.existsByEmail(anyString())).thenReturn(false);
            when(departmentRepository.existsById(1L)).thenReturn(false);

            assertThrows(EmployeeValidationException.class,
                    () -> validationService.validateRegistration(validDto));
        }

        @Test
        @DisplayName("Cannot update rejected employee")
        void cannotUpdateRejectedEmployee() {
            employee.setStatus(EmployeeStatus.REJECTED);
            assertThrows(InvalidEmployeeException.class,
                    () -> validationService.validateUpdate(employee, new EmployeeRequestDTO()));
        }

        @Test
        @DisplayName("Email change validates uniqueness - success")
        void emailChangeValidatesUniqueness() {
            EmployeeRequestDTO updateDto = new EmployeeRequestDTO();
            updateDto.setEmail("new@example.com");
            when(employeeRepository.existsByEmailAndIdNot("new@example.com", 1L)).thenReturn(false); // Unique

            assertDoesNotThrow(() -> validationService.validateUpdate(employee, updateDto));
        }

        @Test
        @DisplayName("Username change validates uniqueness - success")
        void usernameChangeValidatesUniqueness() {
            EmployeeRequestDTO updateDto = new EmployeeRequestDTO();
            updateDto.setUsername("new.username");
            when(employeeRepository.existsByUsernameAndIdNot("new.username", 1L)).thenReturn(false); // Unique

            assertDoesNotThrow(() -> validationService.validateUpdate(employee, updateDto));
        }

        @Test
        @DisplayName("Duplicate email during update rejected")
        void duplicateEmailDuringUpdateRejected() {
            EmployeeRequestDTO updateDto = new EmployeeRequestDTO();
            updateDto.setEmail("taken@example.com");
            when(employeeRepository.existsByEmailAndIdNot("taken@example.com", 1L)).thenReturn(true); // Already taken

            DuplicateEmployeeException ex = assertThrows(DuplicateEmployeeException.class,
                    () -> validationService.validateUpdate(employee, updateDto));

            assertEquals("email", ex.getDuplicateField());
            assertEquals("taken@example.com", ex.getDuplicateValue());
        }

        @Test
        @DisplayName("Duplicate username during update rejected")
        void duplicateUsernameDuringUpdateRejected() {
            EmployeeRequestDTO updateDto = new EmployeeRequestDTO();
            updateDto.setUsername("taken");
            when(employeeRepository.existsByUsernameAndIdNot("taken", 1L)).thenReturn(true); // Already taken

            DuplicateEmployeeException ex = assertThrows(DuplicateEmployeeException.class,
                    () -> validationService.validateUpdate(employee, updateDto));

            assertEquals("username", ex.getDuplicateField());
            assertEquals("taken", ex.getDuplicateValue());
        }
    }


    @Nested
    @DisplayName("Input Validation")
    class InputValidation {

        @Test
        @DisplayName("Empty search query rejected")
        void emptySearchQueryRejected() {
            assertThrows(EmployeeValidationException.class,
                    () -> validationService.validateSearchQuery(""));
        }

        @Test
        @DisplayName("Valid search query accepted")
        void validSearchQueryAccepted() {
            assertDoesNotThrow(() -> validationService.validateSearchQuery("john"));
        }

        @Test
        @DisplayName("Null department ID rejected")
        void nullDepartmentIdRejected() {
            assertThrows(EmployeeValidationException.class,
                    () -> validationService.validateDepartmentExists(null));
        }

        @Test
        @DisplayName("Valid department ID accepted")
        void validDepartmentIdAccepted() {
            when(departmentRepository.existsById(1L)).thenReturn(true);
            assertDoesNotThrow(() -> validationService.validateDepartmentExists(1L));
        }
    }
}