package com.timetrak.mapper;

import com.timetrak.dto.payment.JobDetailsDTO;
import com.timetrak.dto.payment.PaymentDetailsDTO;
import com.timetrak.entity.Payment;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(target = "employeeId", source = "employee.id")
    @Mapping(target = "employeeName", source = "employee.fullName")
    @Mapping(target = "employeeUsername", source = "employee.username")
    @Mapping(target = "jobDetails", ignore = true)
    @Mapping(target = "jobsCount", constant = "0")
    @Mapping(target = "modifiedBy", source="payment.modifiedBy")
    @Mapping(target = "averageHourlyRate", expression = "java(calculateAverageRate(payment))")
    PaymentDetailsDTO toDTO(Payment payment);

    default Integer getJobsCount(List<JobDetailsDTO> jobDetails) {
        return jobDetails != null ? jobDetails.size() : 0;
    }

    default BigDecimal calculateAverageRate(Payment payment) {
        if (payment.getTotalHours() == null || payment.getTotalEarnings() == null) {
            return BigDecimal.ZERO;
        }

        if (payment.getTotalHours().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return payment.getTotalEarnings()
                .divide(payment.getTotalHours(), 2, RoundingMode.HALF_UP);
    }

    List<PaymentDetailsDTO> toDTOList(List<Payment> payments);
}