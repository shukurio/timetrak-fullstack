package com.timetrak.service.payment.calculation;

import com.timetrak.dto.payment.PaymentPeriod;
import com.timetrak.dto.payment.PaymentTotals;
import com.timetrak.dto.response.ShiftResponseDTO;
import com.timetrak.entity.Employee;
import com.timetrak.enums.PaymentStatus;
import com.timetrak.exception.UnauthorizedAccessException;
import com.timetrak.exception.payment.InvalidPaymentRequestException;
import com.timetrak.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.timetrak.constant.PaymentConstants.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentCalculationValidator {
    private final PaymentRepository paymentRepository;

    public void validateRequest(PaymentPeriod period, Long companyId) {
        if (period == null) {
            throw new InvalidPaymentRequestException("Payment period is required for payment processing");
        }

        if (companyId == null) {
            throw new InvalidPaymentRequestException("Company ID is required for payment processing");
        }

        if (period.getStartDate() == null || period.getEndDate() == null) {
            throw new InvalidPaymentRequestException("Payment period must have valid start and end dates");
        }

        if (companyId <= 0) {
            throw new InvalidPaymentRequestException("Company ID must be a positive number");
        }
    }

    public void validateShifts(Map<Long, List<ShiftResponseDTO>> shifts) {
        if (shifts == null) {
            throw new InvalidPaymentRequestException("Shifts map cannot be null");
        }

        if (shifts.isEmpty()) {
            log.info("No shifts found for payment period - no employees worked");
        }
    }

    public void validateEmployees(List<Long> expectedEmployeeIds, List<Employee> employees, Long companyId) {
        if (employees == null) {
            throw new InvalidPaymentRequestException("Employee data cannot be null");
        }

        if (employees.size() != expectedEmployeeIds.size()) {
            throw new InvalidPaymentRequestException(
                    String.format("Expected %d employees, but found %d",
                            expectedEmployeeIds.size(), employees.size()));
        }

        Set<Long> foundEmployeeIds = employees.stream()
                .map(Employee::getId)
                .collect(Collectors.toSet());

        List<Long> missingEmployees = expectedEmployeeIds.stream()
                .filter(id -> !foundEmployeeIds.contains(id))
                .toList();

        if (!missingEmployees.isEmpty()) {
            throw new InvalidPaymentRequestException(
                    "Employees not found: " + missingEmployees);
        }

        for (Employee employee : employees) {
            if (!companyId.equals(employee.getCompany().getId())) {
                throw new UnauthorizedAccessException(
                        "Employee " + employee.getId() + " does not belong to company " + companyId);
            }
        }
    }

    public void validateShiftsEmployeeConsistency(Map<Long, List<ShiftResponseDTO>> shifts, List<Long> employeeIds) {

        Set<Long> shiftEmployeeIds = shifts.keySet();
        Set<Long> expectedEmployeeIds = new HashSet<>(employeeIds);

        if (!shiftEmployeeIds.equals(expectedEmployeeIds)) {
            Set<Long> extraInShifts = new HashSet<>(shiftEmployeeIds);
            extraInShifts.removeAll(expectedEmployeeIds);

            Set<Long> missingInShifts = new HashSet<>(expectedEmployeeIds);
            missingInShifts.removeAll(shiftEmployeeIds);

            StringBuilder error = new StringBuilder("Data inconsistency detected:");

            if (!extraInShifts.isEmpty()) {
                error.append(" Shifts found for unexpected employees: ").append(extraInShifts);
            }

            if (!missingInShifts.isEmpty()) {
                error.append(" Expected employees missing shifts: ").append(missingInShifts);
            }

            throw new InvalidPaymentRequestException(error.toString());
        }

        log.debug("Shifts-employee consistency validated: {} employees have shift data", employeeIds.size());
    }


    public void validatePaymentEarningsAndHours(PaymentTotals totals) {
        validateEarnings(totals);
        validateHours(totals);
        validateShiftsCount(totals);
        validateCrossFieldRules(totals);

        log.debug("Payment totals validation passed: {} hours, ${}, {} shifts",
                totals.getTotalHours(), totals.getTotalEarnings(), totals.getShiftsCount());
    }

    private void validateEarnings(PaymentTotals totals) {
        BigDecimal earnings = totals.getTotalEarnings();

        if (earnings == null) {
            throw new InvalidPaymentRequestException("Earnings must not be null");
        }

        if (earnings.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidPaymentRequestException("Payment amount must not be negative");
        }

        if (earnings.compareTo(MAX_PAYMENT_AMOUNT) > 0) {
            throw new InvalidPaymentRequestException(
                    "Payment amount cannot exceed $" + MAX_PAYMENT_AMOUNT);
        }

        if (earnings.scale() > 2) {
            throw new InvalidPaymentRequestException("Earnings cannot have more than 2 decimal places");
        }
    }

    private void validateHours(PaymentTotals totals) {
        BigDecimal hours = totals.getTotalHours();

        if (hours == null) {
            throw new InvalidPaymentRequestException("Hours must not be null");
        }

        if (hours.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidPaymentRequestException("Payment hours must not be negative");
        }

        if (hours.compareTo(MAX_HOURS_PER_PERIOD) > 0) {
            throw new InvalidPaymentRequestException(
                    "Payment hours cannot exceed " + MAX_HOURS_PER_PERIOD);
        }

        if (hours.scale() > 2) {
            throw new InvalidPaymentRequestException("Hours cannot have more than 2 decimal places");
        }
    }

    private void validateShiftsCount(PaymentTotals totals) {
        Integer shiftsCount = totals.getShiftsCount();

        if (shiftsCount == null) {
            throw new InvalidPaymentRequestException("Shifts count must not be null");
        }

        if (shiftsCount < 0) {
            throw new InvalidPaymentRequestException("Shifts count cannot be negative");
        }

        if (shiftsCount > MAX_SHIFTS_PER_PERIOD) {
            throw new InvalidPaymentRequestException(
                    "Shifts count cannot exceed " + MAX_SHIFTS_PER_PERIOD + " per period");
        }
    }

    private void validateCrossFieldRules(PaymentTotals totals) {
        BigDecimal earnings = totals.getTotalEarnings();
        BigDecimal hours = totals.getTotalHours();
        Integer shiftsCount = totals.getShiftsCount();

        if (hours.compareTo(BigDecimal.ZERO) == 0 && earnings.compareTo(BigDecimal.ZERO) > 0) {
            throw new InvalidPaymentRequestException(
                    "Cannot have earnings ($" + earnings + ") with zero hours worked");
        }

        if (earnings.compareTo(BigDecimal.ZERO) == 0 && hours.compareTo(BigDecimal.ZERO) > 0) {
            log.warn("Zero earnings calculated for {} hours worked - possible unpaid time", hours);
        }

        if (shiftsCount > 0 && hours.compareTo(BigDecimal.ZERO) == 0) {
            throw new InvalidPaymentRequestException(
                    "Cannot have " + shiftsCount + " shifts with zero total hours");
        }

        if (shiftsCount > 0) {
            BigDecimal avgHoursPerShift = hours.divide(new BigDecimal(shiftsCount), 2, RoundingMode.HALF_UP);
            if (avgHoursPerShift.compareTo(BigDecimal.valueOf(MAX_SHIFT_DURATION_HOURS)) > 0) {
                throw new InvalidPaymentRequestException(
                        "Average hours per shift (" + avgHoursPerShift + ") " +
                                "exceeds maximum shift duration (" + MAX_SHIFT_DURATION_HOURS + ")");
            }
        }
    }

    public List<Long> filterEmployeesWithoutDuplicates(List<Long> employeeIds, PaymentPeriod period, Long companyId) {
        if (employeeIds.isEmpty()) return employeeIds;

        List<Long> employeesWithPayments = paymentRepository.findEmployeeIdsWithExistingPayments(
                employeeIds, period.getStartDate(), period.getEndDate(), companyId, PaymentStatus.VOIDED);

        if (!employeesWithPayments.isEmpty()) {
            log.warn("Found existing payments for {} employees in period {}: {}",
                    employeesWithPayments.size(), period.getFormattedPeriod(), employeesWithPayments);
        }

        return employeeIds.stream()
                .filter(id -> !employeesWithPayments.contains(id))
                .toList();
    }

}
