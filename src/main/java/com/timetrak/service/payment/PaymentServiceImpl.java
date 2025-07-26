package com.timetrak.service.payment;

import com.timetrak.dto.payment.*;
import com.timetrak.dto.response.ShiftResponseDTO;
import com.timetrak.entity.Employee;
import com.timetrak.entity.Payment;
import com.timetrak.enums.PaymentStatus;
import com.timetrak.exception.payment.*;
import com.timetrak.mapper.PaymentMapper;
import com.timetrak.repository.PaymentRepository;
import com.timetrak.service.ShiftService;
import com.timetrak.service.auth.AuthContextService;
import com.timetrak.service.employee.EmployeeService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentServiceImpl  implements PaymentService{


    private final PaymentResponseBuilder paymentResponseBuilder;
    private final PaymentCalculator paymentCalculator;
    private final ShiftService shiftService;
    private final EmployeeService employeeService;
    private final PaymentPeriodService paymentPeriodService;
    private final PaymentMapper paymentMapper;
    private final PaymentRepository paymentRepository;
    private final AuthContextService authContextService;

    @Override
    @Transactional
    public PaymentResponseDTO calculatePaymentsForPeriod(PaymentPeriod paymentPeriod) {
        //validate Request
        try {
            Map<Long, List<ShiftResponseDTO>> shifts =
                    shiftService.getAllShiftsByDateRange(paymentPeriod.getStartDate(), paymentPeriod.getEndDate());

            List<Long> employeeIds = new ArrayList<>(shifts.keySet());

            List<Employee> employees = employeeService.getByIds(employeeIds);

            PaymentCalculationResult calculationResult = paymentCalculator
                    .calculateAllPaymentsForCompany(employees, shifts, paymentPeriod,
                            authContextService.getCurrentCompanyId());

            List<PaymentDetailsDTO> successful = savePayments(calculationResult.getSuccessful());

            List<PaymentFailureResponse> failed = calculationResult.getErrors();

            log.info("Payment calculation completed: {} successful, {} failed",
                    successful.size(), failed.size());

            return paymentResponseBuilder.buildResponse(successful, failed, paymentPeriod);

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
    public PaymentResponseDTO calculatePayments(PaymentRequestDTO request) {
        try {
            PaymentPeriod paymentPeriod = resolvePaymentPeriod(request.getPeriodNumber());
            return calculatePaymentsForPeriod(paymentPeriod);
        } catch (InvalidPaymentPeriodException e) {
            log.error("Invalid payment period in request: {}", e.getMessage());
            throw e;
        }
    }


    private PaymentPeriod resolvePaymentPeriod(Integer paymentPeriodNumber) {
        if (paymentPeriodNumber == null || paymentPeriodNumber <= 0) {
            return paymentPeriodService.getCurrentPaymentPeriod();
        } else {
            return paymentPeriodService.getPaymentPeriodByNumber(paymentPeriodNumber);
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




    @Override
    public PaymentDetailsDTO getPaymentById(Long paymentId) {
        throw new UnsupportedOperationException("This feature is not supported yet");

    }

    @Override
    public PaymentDetailsDTO getLastPaymentForEmployee(Long employeeId) {
        throw new UnsupportedOperationException("This feature is not supported yet");

    }

    @Override
    public Page<PaymentDetailsDTO> getAllPaymentsForEmployee(Long employeeId, Pageable pageable) {
        throw new UnsupportedOperationException("This feature is not supported yet");

    }

    @Override
    public Page<PaymentDetailsDTO> getAllPaymentsForCompany(Long companyId, Pageable pageable) {
        throw new UnsupportedOperationException("This feature is not supported yet");

    }

    @Override
    public Page<PaymentDetailsDTO> getAllPaymentsForLastPeriod(Pageable pageable) {
        throw new UnsupportedOperationException("This feature is not supported yet");

    }

    @Override
    public PaymentDetailsDTO markPaymentIssued(Long paymentId, LocalDate issuedDate) {
        throw new UnsupportedOperationException("This feature is not supported yet");

    }

    @Override
    public PaymentDetailsDTO markPaymentReceived(Long paymentId, LocalDate receivedDate) {
        throw new UnsupportedOperationException("This feature is not supported yet");

    }

    @Override
    public boolean paymentExistsForPeriod(Long employeeId, LocalDate startDate, LocalDate endDate) {
        return false;
    }

    @Override
    public PaymentDetailsDTO approvePayment(Long paymentId, String approvedBy) {
        throw new UnsupportedOperationException("This feature is not supported yet");

    }

    @Override
    public PaymentDetailsDTO voidPayment(Long paymentId, String reason, String voidedBy) {
        throw new UnsupportedOperationException("This feature is not supported yet");

    }

    @Override
    public List<PaymentDetailsDTO> bulkApprovePayments(List<Long> paymentIds, String approvedBy) {
        return List.of();
    }

    @Override
    public PaymentDetailsDTO reprocessPayment(Long paymentId, String reason) {
        throw new UnsupportedOperationException("This feature is not supported yet");
    }

    @Override
    public Page<PaymentDetailsDTO> searchPayments(Long companyId, LocalDate startDate, LocalDate endDate, PaymentStatus status, String employeeName, Pageable pageable) {
        throw new UnsupportedOperationException("This feature is not supported yet");
    }

    @Override
    public Page<PaymentDetailsDTO> getPaymentsByStatus(Long companyId, PaymentStatus status, Pageable pageable) {
        throw new UnsupportedOperationException("This feature is not supported yet");
    }

    @Override
    public Page<PaymentDetailsDTO> getPaymentsByDateRange(Long companyId, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        throw new UnsupportedOperationException("This feature is not supported yet");
    }


    @Override
    public byte[] exportPayments(Long companyId, LocalDate startDate, LocalDate endDate, String format) {
        throw new UnsupportedOperationException("This feature is not supported yet");
    }

    @Override
    public Page<PaymentDetailsDTO> calculateCompanyPayroll(Long companyId, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        throw new UnsupportedOperationException("This feature is not supported yet");
    }

    @Override
    public List<PaymentDetailsDTO> bulkMarkPaymentsIssued(List<Long> paymentIds, LocalDate issuedDate, String issuedBy) {
        throw new UnsupportedOperationException("This feature is not supported yet");
    }

    @Override
    public PaymentDetailsDTO addCheckNumber(Long paymentId, String checkNumber) {
        throw new UnsupportedOperationException("This feature is not supported yet");
    }

    @Override
    public JobDetailsDTO getPaymentJobDetails(Long paymentId) {
        throw new UnsupportedOperationException("This feature is not supported yet");
    }

}
