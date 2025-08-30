package com.timetrak.mapper;

import com.timetrak.dto.payment.PaymentPeriod;
import com.timetrak.dto.payment.PaymentPeriodSummaryDTO;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class PaymentPeriodMapper {

    public PaymentPeriodSummaryDTO toSummaryDTO(PaymentPeriod period) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d");
        String displayLabel = String.format("%s - %s, %d",
                period.getStartDate().format(formatter),
                period.getEndDate().format(formatter),
                period.getEndDate().getYear());

        return PaymentPeriodSummaryDTO.builder()
                .periodNumber(period.getPeriodNumber())
                .periodStart(period.getStartDate())
                .periodEnd(period.getEndDate())
                .displayLabel(displayLabel)
                .build();
    }
}