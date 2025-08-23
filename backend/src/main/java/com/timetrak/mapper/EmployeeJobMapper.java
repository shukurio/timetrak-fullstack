package com.timetrak.mapper;

import com.timetrak.dto.employeeJob.EmployeeJobResponseDTO;
import com.timetrak.dto.employeeJob.EmployeeJobUpdateDTO;
import com.timetrak.entity.EmployeeJob;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EmployeeJobMapper {
    
    @Mapping(target = "employeeJobId", source = "id")
    @Mapping(target = "employeeId", source = "employee.id")
    @Mapping(target = "employeeName", expression = "java(employeeJob.getEmployee().getFullName())")
    @Mapping(target = "jobId", source = "job.id")
    @Mapping(target = "jobTitle", source = "job.jobTitle")
    @Mapping(target = "companyId", source = "employee.company.id")  // ← ADD THIS
    @Mapping(target = "departmentName", source = "job.department.name")
    @Mapping(target = "hourlyWage", expression = "java(employeeJob.getEffectiveHourlyWage())")
    @Mapping(target = "jobDefaultWage", source = "job.hourlyWage")
    @Mapping(target = "assignedAt", source = "createdAt")
    @Mapping(target = "isActive", expression = "java(!employeeJob.isDeleted())")
    EmployeeJobResponseDTO toDTO(EmployeeJob employeeJob);
    
    List<EmployeeJobResponseDTO> toDTOList(List<EmployeeJob> employeeJobs);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "job", ignore = true)
    @Mapping(target = "shifts", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEmployeeJobFromDto(EmployeeJobUpdateDTO updateDTO, @MappingTarget EmployeeJob employeeJob);
}
