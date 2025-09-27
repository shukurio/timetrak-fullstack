package com.timetrak.service.report;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.AreaBreakType;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.timetrak.dto.payment.Period;
import com.timetrak.dto.shift.ShiftResponseDTO;
import com.timetrak.service.company.CompanyService;
import com.timetrak.service.department.DepartmentService;
import com.timetrak.service.payment.PeriodService;
import com.timetrak.service.shift.ShiftService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShiftReportService {
    private final CompanyService companyService;
    private final ShiftService shiftService;
    private final PeriodService periodService;
    private final DepartmentService depService;


    public byte[] exportShifts(Integer periodNumber, Long companyId, @Nullable List<Long> departmentIds) {
        String companyName = companyService.getCompanyById(companyId).getName();
        Period period = resolvePaymentPeriod(periodNumber, companyId);

        List<Long> targetDepartments;
        String reportType;

        if (departmentIds == null || departmentIds.isEmpty()) {
            targetDepartments = depService.getAllDepartmentIdsForCompany(companyId);
            reportType = "Company-Wide";
        } else {
            targetDepartments = departmentIds;
            reportType = departmentIds.size() == 1 ? "Department" : "Multi-Department";
        }

        Map<Long, List<ShiftResponseDTO>> shiftsByDepartment = shiftService.getShiftsByDepartmentsGrouped(
                targetDepartments, companyId, period.getStartDate(), period.getEndDate());

        Map<Long, String> departmentNames = getDepartmentNames(shiftsByDepartment.keySet(),companyId);

        try {
            return generateDepartmentCategorizedPdf(shiftsByDepartment, departmentNames, period, reportType,companyName);
        } catch (Exception e) {
            log.error("Failed to generate PDF for company {} departments {}: {}",
                    companyId, targetDepartments, e.getMessage(), e);
            throw new RuntimeException("Failed to generate PDF file", e);
        }
    }

    private Period resolvePaymentPeriod(Integer periodNumber, Long companyId) {
        if (periodNumber == null || periodNumber <= 0) {
            return periodService.getCurrentPeriod(companyId);
        } else {
            return periodService.getPeriodByNumber(periodNumber, companyId);
        }
    }

    private Map<Long, String> getDepartmentNames(Set<Long> departmentIds,Long companyId) {
        return departmentIds.stream()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> {
                            try {
                                return depService.getDepartmentById(id,companyId).getName();
                            } catch (Exception e) {
                                log.warn("Could not get name for department {}: {}", id, e.getMessage());
                                return "Department " + id;
                            }
                        }
                ));
    }

    private byte[] generateDepartmentCategorizedPdf(
            Map<Long, List<ShiftResponseDTO>> shiftsByDepartment,
            Map<Long, String> departmentNames,
            Period period,
            String reportType,
            String companyName) throws Exception {

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4.rotate());

            createPdfHeader(document, companyName, reportType + " Shift Report - " + period.getShortDescription());

            boolean firstDepartment = true;
            for (Map.Entry<Long, List<ShiftResponseDTO>> entry : shiftsByDepartment.entrySet()) {
                Long deptId = entry.getKey();
                List<ShiftResponseDTO> shifts = entry.getValue();
                String deptName = departmentNames.getOrDefault(deptId, "Department " + deptId);

                if (!firstDepartment) {
                    document.add(new AreaBreak(AreaBreakType.NEXT_PAGE));
                }

                addDepartmentSectionWithJobGrouping(document, deptName, shifts);
                firstDepartment = false;
            }

            document.add(new AreaBreak(AreaBreakType.NEXT_PAGE));
            createJobBreakdownTables(document, shiftsByDepartment, departmentNames);

            document.close();
            return out.toByteArray();
        }
    }

    private void createPdfHeader(Document document, String companyName, String title) {
        Paragraph mainTitle = new Paragraph(companyName)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(24)
                .setBold()
                .setMarginBottom(10);
        document.add(mainTitle);

        Paragraph subtitle = new Paragraph(title)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(16)
                .setMarginBottom(10);
        document.add(subtitle);

        Paragraph timestamp = new Paragraph(String.format("Generated on: %s",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))))
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(14)
                .setBold()
                .setMarginBottom(30);
        document.add(timestamp);
    }

    private void addDepartmentSectionWithJobGrouping(Document document, String deptName,
                                                    List<ShiftResponseDTO> shifts) {
        // Define colors
        DeviceRgb primaryBlue = new DeviceRgb(52, 73, 94);
        DeviceRgb lightBlue = new DeviceRgb(174, 214, 241);

        Paragraph deptTitle = new Paragraph(String.format("Department: %s", deptName))
                .setBold()
                .setFontSize(18)
                .setMarginTop(20)
                .setMarginBottom(15)
                .setBackgroundColor(primaryBlue)
                .setFontColor(ColorConstants.WHITE)
                .setPadding(12)
                .setTextAlignment(TextAlignment.LEFT);
        document.add(deptTitle);

        // Group shifts by job title
        Map<String, List<ShiftResponseDTO>> shiftsByJob = shifts.stream()
                .collect(Collectors.groupingBy(
                        shift -> shift.getJobTitle() != null ? shift.getJobTitle() : "No Job Title"
                ));

        boolean firstJob = true;
        for (Map.Entry<String, List<ShiftResponseDTO>> jobEntry : shiftsByJob.entrySet()) {
            String jobTitle = jobEntry.getKey();
            List<ShiftResponseDTO> jobShifts = jobEntry.getValue();

            if (!firstJob) {
                document.add(new Paragraph(" ").setMarginTop(15));
            }

            addJobSection(document, jobTitle, jobShifts, lightBlue);
            firstJob = false;
        }

        // Department summary removed - will be shown in breakdown tables at the end
    }

    private Table createShiftTable(List<ShiftResponseDTO> shifts) {
        String[] headers = {
                "Employee Name",
                "Clock In",
                "Clock Out",
                "Total Hours",
                "Status"
        };

        Table table = new Table(headers.length);
        table.setWidth(UnitValue.createPercentValue(100));
        table.setMarginBottom(10);

        DeviceRgb headerGray = new DeviceRgb(134, 142, 150);

        for (String header : headers) {
            Cell headerCell = new Cell()
                    .add(new Paragraph(header).setBold().setFontColor(ColorConstants.WHITE))
                    .setBackgroundColor(headerGray)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setPadding(8)
                    .setFontSize(11);
            table.addHeaderCell(headerCell);
        }

        for (ShiftResponseDTO shift : shifts) {
            addShiftRowToPdf(table, shift);
        }

        return table;
    }

    private void addShiftRowToPdf(Table table, ShiftResponseDTO shift) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd HH:mm");

        table.addCell(new Cell()
                .add(new Paragraph(shift.getFullName() != null ? shift.getFullName() : "Unknown"))
                .setTextAlignment(TextAlignment.LEFT)
                .setPadding(6)
                .setFontSize(10));

        table.addCell(new Cell()
                .add(new Paragraph(shift.getClockIn() != null ?
                        shift.getClockIn().format(formatter) : "N/A"))
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(6)
                .setFontSize(10));

        String clockOutText = shift.getClockOut() != null ?
                shift.getClockOut().format(formatter) : "Active";
        table.addCell(new Cell()
                .add(new Paragraph(clockOutText))
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(6)
                .setFontSize(10));

        String hoursText = shift.getHours() != null ?
                String.format("%.2f", shift.getHours()) : "0.00";
        table.addCell(new Cell()
                .add(new Paragraph(hoursText))
                .setTextAlignment(TextAlignment.RIGHT)
                .setPadding(6)
                .setFontSize(10));

        String statusText = shift.getStatus().toString();
        Cell statusCell = new Cell()
                .add(new Paragraph(statusText))
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(6)
                .setFontSize(10);

        if ("ACTIVE".equals(statusText)) {
            statusCell.setBackgroundColor(new DeviceRgb(231, 76, 60))
                     .setFontColor(ColorConstants.WHITE);
        }

        table.addCell(statusCell);
    }

    private void addJobSection(Document document, String jobTitle, List<ShiftResponseDTO> jobShifts,
                              DeviceRgb lightBlue) {
        BigDecimal jobTotalHours = jobShifts.stream()
                .map(ShiftResponseDTO::getHours)
                .filter(Objects::nonNull)
                .map(BigDecimal::valueOf)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Paragraph jobHeader = new Paragraph(String.format("%s • %d shifts • %.2f hrs",
                jobTitle, jobShifts.size(), jobTotalHours))
                .setBold()
                .setFontSize(14)
                .setMarginTop(10)
                .setMarginBottom(8)
                .setBackgroundColor(lightBlue)
                .setPadding(8)
                .setTextAlignment(TextAlignment.LEFT);
        document.add(jobHeader);

        Table table = createShiftTable(jobShifts);
        document.add(table);
    }



    private void createJobBreakdownTables(Document document,
                                          Map<Long, List<ShiftResponseDTO>> shiftsByDepartment,
                                          Map<Long, String> departmentNames) {

        DeviceRgb darkGray = new DeviceRgb(73, 80, 87);
        DeviceRgb headerGray = new DeviceRgb(134, 142, 150);

        // Job breakdown starts on new page (added above)

        Paragraph summaryTitle = new Paragraph("Summary Report")
                .setBold()
                .setFontSize(20)
                .setMarginBottom(25)
                .setTextAlignment(TextAlignment.CENTER)
                .setBackgroundColor(darkGray)
                .setFontColor(ColorConstants.WHITE)
                .setPadding(12);
        document.add(summaryTitle);

        // Create single job breakdown table
        Table jobTable = new Table(4);
        jobTable.setWidth(UnitValue.createPercentValue(80));
        jobTable.setMarginBottom(20);

        // Headers
        String[] headers = {"Department", "Job Title", "Shifts", "Total Hours"};
        for (String header : headers) {
            Cell headerCell = new Cell()
                    .add(new Paragraph(header).setBold().setFontColor(ColorConstants.WHITE))
                    .setBackgroundColor(headerGray)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setPadding(8)
                    .setFontSize(12);
            jobTable.addHeaderCell(headerCell);
        }

        // Collect all job data
        for (Map.Entry<Long, List<ShiftResponseDTO>> entry : shiftsByDepartment.entrySet()) {
            Long deptId = entry.getKey();
            List<ShiftResponseDTO> shifts = entry.getValue();
            String deptName = departmentNames.getOrDefault(deptId, "Department " + deptId);

            // Group shifts by job for this department
            Map<String, List<ShiftResponseDTO>> jobGroups = shifts.stream()
                    .collect(Collectors.groupingBy(
                            shift -> shift.getJobTitle() != null ? shift.getJobTitle() : "No Job Title"
                    ));

            for (Map.Entry<String, List<ShiftResponseDTO>> jobGroup : jobGroups.entrySet()) {
                String jobTitle = jobGroup.getKey();
                List<ShiftResponseDTO> jobShifts = jobGroup.getValue();
                int jobShiftCount = jobShifts.size();

                BigDecimal jobHours = jobShifts.stream()
                        .map(ShiftResponseDTO::getHours)
                        .filter(Objects::nonNull)
                        .map(BigDecimal::valueOf)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                // Add job row
                jobTable.addCell(new Cell()
                        .add(new Paragraph(deptName))
                        .setTextAlignment(TextAlignment.LEFT)
                        .setPadding(8)
                        .setFontSize(11));

                jobTable.addCell(new Cell()
                        .add(new Paragraph(jobTitle))
                        .setTextAlignment(TextAlignment.LEFT)
                        .setPadding(8)
                        .setFontSize(11));

                jobTable.addCell(new Cell()
                        .add(new Paragraph(String.valueOf(jobShiftCount)))
                        .setTextAlignment(TextAlignment.CENTER)
                        .setPadding(8)
                        .setFontSize(11));

                jobTable.addCell(new Cell()
                        .add(new Paragraph(String.format("%.2f", jobHours)))
                        .setTextAlignment(TextAlignment.RIGHT)
                        .setPadding(8)
                        .setFontSize(11));
            }
        }

        // Add grand total row to the job breakdown table
        int grandTotalShifts = shiftsByDepartment.values().stream()
                .mapToInt(List::size).sum();

        BigDecimal grandTotalHours = shiftsByDepartment.values().stream()
                .flatMap(List::stream)
                .map(ShiftResponseDTO::getHours)
                .filter(Objects::nonNull)
                .map(BigDecimal::valueOf)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        DeviceRgb totalRowColor = new DeviceRgb(52, 58, 64);

        // Add grand total row
        jobTable.addCell(new Cell()
                .add(new Paragraph("GRAND TOTAL").setBold())
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(10)
                .setFontSize(12)
                .setBackgroundColor(totalRowColor)
                .setFontColor(ColorConstants.WHITE));

        jobTable.addCell(new Cell()
                .add(new Paragraph("-").setBold())
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(10)
                .setFontSize(12)
                .setBackgroundColor(totalRowColor)
                .setFontColor(ColorConstants.WHITE));

        jobTable.addCell(new Cell()
                .add(new Paragraph(String.valueOf(grandTotalShifts)).setBold())
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(10)
                .setFontSize(12)
                .setBackgroundColor(totalRowColor)
                .setFontColor(ColorConstants.WHITE));

        jobTable.addCell(new Cell()
                .add(new Paragraph(String.format("%.2f", grandTotalHours)).setBold())
                .setTextAlignment(TextAlignment.RIGHT)
                .setPadding(10)
                .setFontSize(12)
                .setBackgroundColor(totalRowColor)
                .setFontColor(ColorConstants.WHITE));

        document.add(jobTable);
    }

}