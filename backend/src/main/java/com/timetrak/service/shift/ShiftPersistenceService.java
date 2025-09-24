package com.timetrak.service.shift;

import com.timetrak.dto.shift.ShiftRequestDTO;
import com.timetrak.dto.shift.ShiftResponseDTO;
import com.timetrak.entity.Shift;


public interface ShiftPersistenceService {

    ShiftResponseDTO createShift( ShiftRequestDTO request,Long companyId);
    ShiftResponseDTO updateShift( Long shiftId, ShiftRequestDTO request);
    void deleteShift( Long shiftId);
    Shift getShiftById( Long shiftId);

}
