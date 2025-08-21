package com.timetrak.service.shift;

import com.timetrak.dto.request.ShiftRequestDTO;
import com.timetrak.dto.response.ShiftResponseDTO;
import com.timetrak.entity.Shift;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public interface ShiftPersistenceService {

    ShiftResponseDTO createShift(@Valid @NotNull ShiftRequestDTO request);
    ShiftResponseDTO updateShift(@NotNull Long shiftId, @Valid @NotNull ShiftRequestDTO request);
    void deleteShift(@NotNull Long shiftId);
    Shift getShiftById(@NotNull Long shiftId);

}
