package com.timetrak.service.payment.calculation;

import com.timetrak.dto.payment.*;
import com.timetrak.dto.response.ShiftResponseDTO;
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
                                                                   Map<Long, List<ShiftResponseDTO>> shiftsMap,
                                                                   PaymentPeriod paymentPeriod,
                                                                   Long initiatorId) {
        List<Payment> successful = new ArrayList<>();
        List<PaymentFailureResponse> errors = new ArrayList<>();

        for (Employee employee : employees) {
            try {
                List<ShiftResponseDTO> shifts = shiftsMap.getOrDefault(employee.getId(), new ArrayList<>());
                Payment payment = calculateSingleEmployeePayment(employee, shifts, paymentPeriod, initiatorId);
                successful.add(payment);
            } catch (Exception e) {
                String errorCode = (e instanceof PaymentException)
                        ? ((PaymentException) e).getErrorCode()
                        : e.getClass().getSimpleName();

                errors.add(PaymentFailureResponse.builder()
                        .employeeId(employee.getId())
                        .period(paymentPeriod.getFormattedPeriod())
                        .errorMessage(e.getMessage())
                        .errorCode(errorCode)
                        .cause(e)
                        .build());
            }
        }

        return new PaymentCalculationResult(successful, errors, paymentPeriod);
    }

    public Payment calculateSingleEmployeePayment(
            Employee employee,
            List<ShiftResponseDTO> shifts,
            PaymentPeriod period,
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
        BigDecimal totalHours = BigDecimal.ZERO;
        BigDecimal totalEarnings = BigDecimal.ZERO;

        for (ShiftResponseDTO shift : shifts) {
            BigDecimal shiftHours = shift.getTotalHours() != null ?
                    shift.getTotalHours() : BigDecimal.ZERO;
            BigDecimal shiftEarnings = shift.getShiftEarnings() != null ?
                    shift.getShiftEarnings() : BigDecimal.ZERO;

            totalHours = totalHours.add(shiftHours);
            totalEarnings = totalEarnings.add(shiftEarnings);
        }

        return new PaymentTotals(totalHours, totalEarnings, shifts.size());
    }

    }