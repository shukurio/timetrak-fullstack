package com.timetrak.service.payment.calculation;

import com.timetrak.dto.payment.*;
import com.timetrak.dto.shift.ShiftResponseDTO;
import com.timetrak.entity.Employee;
import com.timetrak.entity.Payment;
import com.timetrak.enums.PaymentStatus;
import com.timetrak.exception.payment.PaymentException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentCalculator {
    private final PaymentCalculationValidator validator;



    public PaymentCalculationResult calculateAllPaymentsForCompany(List<Employee> employees,
                                                                   Map<Employee, List<ShiftResponseDTO>> shiftsMap,
                                                                   Period period,
                                                                   Long initiatorId) {
        List<Payment> successful = new ArrayList<>();
        List<PaymentFailureResponse> errors = new ArrayList<>();

        for (Employee employee : employees) {
            try {
                List<ShiftResponseDTO> shifts = shiftsMap.getOrDefault(employee, new ArrayList<>());
                Payment payment = calculateSingleEmployeePayment(employee, shifts, period, initiatorId);
                successful.add(payment);
            } catch (Exception e) {
                String errorCode = (e instanceof PaymentException)
                        ? ((PaymentException) e).getErrorCode()
                        : e.getClass().getSimpleName();

                errors.add(PaymentFailureResponse.builder()
                        .employeeId(employee.getId())
                        .period(period.getFormattedPeriod())
                        .errorMessage(e.getMessage())
                        .errorCode(errorCode)
                        .cause(e)
                        .build());
            }
        }

        return new PaymentCalculationResult(successful, errors, period);
    }

    public Payment calculateSingleEmployeePayment(
            Employee employee,
            List<ShiftResponseDTO> shifts,
            Period period,
            Long initiatorId) {

        log.debug("Calculating payment for employee {} in period {}",
                employee.getId(), period.getFormattedPeriod());

        PaymentTotals totals = calculateShiftTotals(shifts);
        validator.validatePaymentEarningsAndHours(totals);

        Payment payment = Payment.builder()
                .employee(employee)
                .companyId(employee.getCompany().getId())
                .periodStart(period.getStartDate())
                .periodEnd(period.getEndDate())
                .periodNumber(period.getPeriodNumber())
                .totalHours(totals.getTotalHours())
                .totalEarnings(totals.getTotalEarnings())
                .shiftsCount(totals.getShiftsCount())
                .status(PaymentStatus.CALCULATED)
                .modifiedBy(initiatorId)
                .calculatedAt(LocalDateTime.now())
                .build();

        log.debug("Calculated payment for employee {}: {} hours, ${}",
                employee.getId(), totals.getTotalHours(), totals.getTotalEarnings());

        return payment;
    }


    public PaymentTotals calculateShiftTotals(List<ShiftResponseDTO> shifts) {
        Double totalHours = 0.0;
        BigDecimal totalEarnings = BigDecimal.ZERO;

        for (ShiftResponseDTO shift : shifts) {
            Double shiftHours = shift.getHours() != null ? shift.getHours() : 0.0;
            BigDecimal shiftEarnings = shift.getShiftEarnings() != null ?
                    shift.getShiftEarnings() : BigDecimal.ZERO;

            totalHours += shiftHours;
            totalEarnings = totalEarnings.add(shiftEarnings);
        }

        return new PaymentTotals(totalHours, totalEarnings, shifts.size());
    }

    }