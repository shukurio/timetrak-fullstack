package com.timetrak.service.employeeJob;

import com.timetrak.dto.employeeJob.*;

import java.math.BigDecimal;
import java.util.List;

public interface EmployeeJobManagementService {

    // Bulk operations (main admin features)
    EmployeeJobBulkResponseDTO assignJobToEmployees(EmployeeJobRequestDTO request, Long companyId);
    EmployeeJobBulkResponseDTO removeJobFromEmployees(List<Long> employeeIds, Long jobId, Long companyId);
    
    // Individual operations
    EmployeeJobResponseDTO createAssignment(Long employeeId, Long jobId, BigDecimal hourlyWage, Long companyId);
    void deleteAssignment(Long employeeJobId, Long companyId);
    EmployeeJobResponseDTO updateAssignment(Long employeeJobId, EmployeeJobUpdateDTO request, Long companyId);
}
