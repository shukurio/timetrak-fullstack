package com.timetrak.mapper;

import com.timetrak.dto.job.JobRequestDTO;
import com.timetrak.dto.job.JobResponseDTO;
import com.timetrak.dto.job.JobUpdateDTO;
import com.timetrak.entity.Job;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface JobMapper {

    @Mapping(target = "departmentId", source = "department.id")
    @Mapping(target = "departmentName", source = "department.name")
    JobResponseDTO toDTO(Job job);

    List<JobResponseDTO> toDTOList(List<Job> jobs);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "employeeJobs", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "jobTitle", expression = "java(requestDTO.getJobTitle().toUpperCase())")
    Job toEntity(JobRequestDTO requestDTO);


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "employeeJobs", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "jobTitle", expression = "java(requestDTO.getJobTitle() != null ? requestDTO.getJobTitle().toUpperCase() : null)")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Job updateJobFromDto(JobUpdateDTO requestDTO, @MappingTarget Job job);

}