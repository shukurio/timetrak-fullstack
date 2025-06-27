package com.timetrak.service;

import com.timetrak.dto.response.EmployeeJobInfoDTO;
import com.timetrak.entity.EmployeeJob;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface EmployeeJobService {
    //For internal use
    EmployeeJob getEmpJobById(Long id);
    List<EmployeeJobInfoDTO> getEmpJobsInfoByIds(List<Long> ids);
}
