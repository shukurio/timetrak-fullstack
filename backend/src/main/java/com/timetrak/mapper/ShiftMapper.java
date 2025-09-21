package com.timetrak.mapper;

import com.timetrak.dto.request.ShiftRequestDTO;
import com.timetrak.dto.response.ShiftResponseDTO;
import com.timetrak.entity.Shift;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;
import java.util.Objects;

@Mapper(componentModel = "spring")
public interface ShiftMapper {
    Shift toEntity(ShiftRequestDTO dto);

    default ShiftResponseDTO toDTO(Shift shift) {
        if (shift == null) {
            return null;
        }

        Double hours = null;
        if (shift.getClockIn() != null && shift.getClockOut() != null) {
            long durationInSeconds = java.time.Duration.between(shift.getClockIn(), shift.getClockOut()).getSeconds();
            double rawHours = durationInSeconds / 3600.0; // convert seconds to hours
            hours = Math.round(rawHours * 100.0) / 100.0; // round to 2 decimal places
        }

        return ShiftResponseDTO.builder()
                .id(shift.getId())
                .clockIn(shift.getClockIn())
                .clockOut(shift.getClockOut())
                .status(shift.getStatus())
                .employeeId(shift.getEmployee().getId())
                .employeeJobId(shift.getEmployeeJob().getId())
                .username(shift.getEmployeeJob().getEmployee().getUsername())
                .fullName(shift.getEmployeeJob().getEmployee().getFirstName() + " " +
                        shift.getEmployeeJob().getEmployee().getLastName())
                .jobTitle(shift.getEmployeeJob().getJob().getJobTitle())
                .hourlyWage(
                        shift.getEmployeeJob() != null && shift.getEmployeeJob().getHourlyWage() != null
                                ? shift.getEmployeeJob().getHourlyWage()
                                : Objects.requireNonNull(shift.getEmployeeJob()).getJob().getHourlyWage()
                )
                .hours(hours)
                .build();
    }


    void updateShiftFromDto(ShiftRequestDTO dto, @MappingTarget Shift shift);

    List<ShiftResponseDTO> toDTOList(List<Shift> shifts);
}
