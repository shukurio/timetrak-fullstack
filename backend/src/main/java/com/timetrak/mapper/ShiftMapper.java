package com.timetrak.mapper;

import com.timetrak.dto.request.ShiftRequestDTO;
import com.timetrak.dto.response.ShiftResponseDTO;
import com.timetrak.entity.Shift;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ShiftMapper {
    Shift toEntity(ShiftRequestDTO dto);

    default ShiftResponseDTO toDTO(Shift shift) {
        if (shift == null) {
            return null;
        }

        return ShiftResponseDTO.builder()
                .id(shift.getId())
                .clockIn(shift.getClockIn())
                .clockOut(shift.getClockOut())
                .notes(shift.getNotes())
                .status(shift.getStatus())
                .employeeId(shift.getEmployeeJob().getEmployee().getId())
                .employeeJobId(shift.getEmployeeJob().getId())
                .username(shift.getEmployeeJob().getEmployee().getUsername())
                .fullName(shift.getEmployeeJob().getEmployee().getFirstName() + " " +
                        shift.getEmployeeJob().getEmployee().getLastName())
                .jobTitle(shift.getEmployeeJob().getJob().getJobTitle())
                .hourlyWage(shift.getEmployeeJob().getHourlyWage())
                .build();
    }

    void updateShiftFromDto(ShiftRequestDTO dto, @MappingTarget Shift shift);

    List<ShiftResponseDTO> toDTOList(List<Shift> shifts);
}
