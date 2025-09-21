package com.timetrak.service.payment.calculation;

import com.timetrak.dto.payment.*;
import com.timetrak.dto.response.ShiftResponseDTO;
import com.timetrak.entity.Employee;
import com.timetrak.entity.Payment;
import com.timetrak.exception.payment.DuplicatePaymentException;
import com.timetrak.exception.payment.InvalidPaymentPeriodException;
import com.timetrak.exception.payment.PaymentException;
import com.timetrak.exception.payment.PaymentProcessingException;
import com.timetrak.mapper.PaymentMapper;
import com.timetrak.repository.PaymentRepository;
import com.timetrak.service.shift.ShiftService;
import com.timetrak.service.payment.PeriodService;
import com.timetrak.service.payment.PaymentResponseBuilder;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class PaymentCalculationServiceImpl implements PaymentCalculationService {
    private final PaymentResponseBuilder paymentResponseBuilder;
    private final PaymentCalculator paymentCalculator;
    private final ShiftService shiftService;
    private final PeriodService paymentPeriodService;
    private final PaymentMapper paymentMapper;
    private final PaymentRepository paymentRepository;
    private final PaymentCalculationValidator validator;


    @Override
    @Transactional
    public PaymentResponseDTO calculatePaymentsForPeriod(Period period, Long companyId, Long initiatorId) {
        try {
            validator.validateRequest(period,companyId);

            Map<Employee, List<ShiftResponseDTO>> shifts =
                    shiftService.getAllShiftsByDateRange(period.getStartDate(),
                            period.getEndDate(),
                            companyId);
            validator.validateShifts(shifts);

            List<Employee> employees = new ArrayList<>(shifts.keySet());
            List<Long> employeeIds = employees.stream().map(Employee::getId).toList();

            List<Long> validIds = validator.filterEmployeesWithoutDuplicates(
                    employees.stream().map(Employee::getId).toList(),
                    period,
                    companyId
            );

            shifts.entrySet().removeIf(entry -> !validIds.contains(entry.getKey().getId()));
            List<Employee> validEmployees = shifts.keySet().stream().toList();

            List<Long> duplicatePayments = employeeIds.stream()
                    .filter(id -> !validIds.contains(id))
                    .toList();

            validator.validateEmployees(validIds,validEmployees,companyId);
            validator.validateShiftsEmployeeConsistency(shifts,validEmployees);

            PaymentCalculationResult calculationResult = paymentCalculator
                    .calculateAllPaymentsForCompany(validEmployees, shifts, period,
                             initiatorId);

            List<PaymentDetailsDTO> successful = savePayments(calculationResult.getSuccessful());
            List<PaymentFailureResponse> failed = new ArrayList<>(paymentResponseBuilder.createDuplicateFailures(duplicatePayments, period));
            failed.addAll(calculationResult.getErrors());

            log.info("Payment calculation completed: {} successful, {} failed",
                    successful.size(), failed.size());

            return paymentResponseBuilder.buildResponse(successful, failed, period);

        } catch (PaymentException e) {
            log.error("Payment processing failed: {}", e.getMessage());
            throw e; //Re-throw business exceptions
        } catch (Exception e) {
            log.error("Unexpected error during payment calculation: {}", e.getMessage(), e);
            throw new PaymentProcessingException("Payment calculation failed unexpectedly: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public PaymentResponseDTO calculatePayments(PaymentRequestDTO request, Long companyId,Long initiatorId) {
        try {
            Period period = resolvePaymentPeriod(request.getPeriodNumber(),companyId);
            return calculatePaymentsForPeriod(period,companyId,initiatorId);
        } catch (InvalidPaymentPeriodException e) {
            log.error("Invalid payment period in request: {}", e.getMessage());
            throw e;
        }
    }


    private Period resolvePaymentPeriod(Integer paymentPeriodNumber, Long companyId) {
        if (paymentPeriodNumber == null || paymentPeriodNumber <= 0) {
            return paymentPeriodService.getCurrentPeriod(companyId);
        } else {
            return paymentPeriodService.getPeriodByNumber(paymentPeriodNumber,companyId);
        }
    }

    private List<PaymentDetailsDTO> savePayments(List<Payment> paymentsToSave) {
        if (paymentsToSave.isEmpty()) {
            return new ArrayList<>();
        }

        try {
            List<Payment> savedPayments = paymentRepository.saveAll(paymentsToSave);
            log.info("Successfully saved {} payments", savedPayments.size());
            return paymentMapper.toDTOList(savedPayments);
        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation while saving payments: {}", e.getMessage(), e);
            throw new DuplicatePaymentException("Payment already exists for this period");
        } catch (Exception e) {
            log.error("Failed to save payments: {}", e.getMessage(), e);
            throw new PaymentProcessingException("Failed to save payments: " + e.getMessage(), e);
        }
    }


}
