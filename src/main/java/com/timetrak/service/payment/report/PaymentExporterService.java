package com.timetrak.service.payment.report;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.timetrak.dto.payment.PaymentPeriod;
import com.timetrak.entity.Payment;
import com.timetrak.repository.PaymentRepository;
import com.timetrak.service.payment.PaymentPeriodService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.Document;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentExporterService {
    private final PaymentRepository paymentRepository;
    private final PaymentPeriodService paymentPeriodService;

    public byte[] exportPayments(int periodNumber,Long companyId) {

        PaymentPeriod period = resolvePaymentPeriod(periodNumber,companyId);
        log.info("Exporting payments for company {} from {} to {} in PDF format",
                companyId, period.getStartDate(), period.getEndDate());


        List<Payment> payments = paymentRepository.findByCompanyIdAndDateRange(companyId, period.getStartDate(), period.getEndDate());

        if (payments.isEmpty()) {
            log.warn("No payments found for company {} in date range {} to {}", companyId, period.getStartDate(), period.getEndDate());
        }

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4.rotate());

            //title
            createPdfHeader(document, period.getShortDescription(), payments.size());

            //add table
            Table table = createPaymentTable(payments);
            document.add(table);

            //add summary
            createPdfSummary(document, payments);

            document.close();
            log.info("Generated PDF file with {} payments", payments.size());
            return out.toByteArray();

        } catch (Exception e) {
            log.error("Failed to generate PDF file for company {}: {}", companyId, e.getMessage(), e);
            throw new RuntimeException("Failed to generate PDF file", e);
        }
    }

    private PaymentPeriod resolvePaymentPeriod(Integer periodNumber, Long companyId) {
        if (periodNumber == null || periodNumber <= 0) {
            log.info("No period number provided, using current payment period for company {}", companyId);
            return paymentPeriodService.getCurrentPaymentPeriod(companyId);
        } else {
            log.info("Using payment period {} for company {}", periodNumber, companyId);
            return paymentPeriodService.getPaymentPeriodByNumber(periodNumber, companyId);
        }
    }


    private void createPdfHeader(Document document,String formattedPeriod, int paymentCount) {
        //Main title
        Paragraph title = new Paragraph("Payment Report")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(24)
                .setBold()
                .setMarginBottom(10);
        document.add(title);

        //Date range
        Paragraph dateRange = new Paragraph("Period: " + formattedPeriod)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(14)
                .setMarginBottom(5);
        document.add(dateRange);

        //Payment count
        Paragraph count = new Paragraph(String.format("Total Payments: %d", paymentCount))
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(12)
                .setMarginBottom(20);
        document.add(count);
    }

    private Table createPaymentTable(List<Payment> payments) {
        //create table with 6 columns

        //cdd headers
        String[] headers = {
                "Payment ID",
                "Employee ID",
                "Employee Name",
                "Total Hours",
                "Total Earnings",
                "Status"
        };
        Table table = new Table(headers.length);
        table.setWidth(UnitValue.createPercentValue(100));

        for (String header : headers) {
            Cell headerCell = new Cell()
                    .add(new Paragraph(header).setBold())
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setPadding(8);
            table.addHeaderCell(headerCell);
        }

        //add data rows
        for (Payment payment : payments) {
            addPaymentRowToPdf(table, payment);
        }

        return table;
    }

    private void addPaymentRowToPdf(Table table, Payment payment) {
        // Payment ID
        table.addCell(new Cell().add(new Paragraph(payment.getId().toString()))
                .setTextAlignment(TextAlignment.CENTER));

        // Employee ID
        table.addCell(new Cell().add(new Paragraph(payment.getEmployee().getId().toString()))
                .setTextAlignment(TextAlignment.CENTER));

        // Employee Name
        table.addCell(new Cell().add(new Paragraph(payment.getEmployee().getFullName())));

        // Total Hours
        table.addCell(new Cell().add(new Paragraph(payment.getTotalHours().toString()))
                .setTextAlignment(TextAlignment.RIGHT));

        // Total Earnings (formatted as currency)
        table.addCell(new Cell().add(new Paragraph("$" + payment.getTotalEarnings().toString()))
                .setTextAlignment(TextAlignment.RIGHT));

        // Status
        table.addCell(new Cell().add(new Paragraph(payment.getStatus().toString()))
                .setTextAlignment(TextAlignment.CENTER));

    }

    private void createPdfSummary(Document document, List<Payment> payments) {
        // Calculate totals
        BigDecimal totalEarnings = payments.stream()
                .map(Payment::getTotalEarnings)
                .reduce(BigDecimal.ZERO, BigDecimal::add);


        // Add summary section
        document.add(new Paragraph(" ").setMarginTop(20)); // Space

        Paragraph summaryTitle = new Paragraph("Summary")
                .setBold()
                .setFontSize(16)
                .setMarginBottom(10);
        document.add(summaryTitle);

        Paragraph summaryText = new Paragraph(String.format(
                "Total Payments: %d \nTotal Earnings: $%s",
                payments.size(), totalEarnings))
                .setFontSize(12);
        document.add(summaryText);

        //add generation timestamp
        Paragraph timestamp = new Paragraph(String.format("Generated on: %s",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))))
                .setTextAlignment(TextAlignment.RIGHT)
                .setFontSize(10)
                .setMarginTop(20);
        document.add(timestamp);
    }

}
