package com.timetrak.service.shift;


import com.timetrak.dto.request.ShiftRequestDTO;
import com.timetrak.dto.response.ShiftResponseDTO;
import com.timetrak.entity.Shift;
import com.timetrak.enums.ClockErrorCode;
import com.timetrak.exception.ResourceNotFoundException;
import com.timetrak.mapper.ShiftMapper;
import com.timetrak.repository.ShiftRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class ShiftPersistenceServiceImpl implements ShiftPersistenceService {
    private final ShiftRepository shiftRepository;
    private final ShiftMapper shiftMapper;
    private final ShiftPersistenceValidator validator;

    @Override
    public ShiftResponseDTO createShift(ShiftRequestDTO request) {
        validator.validateShiftRequest(request);

        Shift shift = shiftMapper.toEntity(request);
        Shift savedShift = shiftRepository.save(shift);

        log.info("Created shift {} for employee job {}", savedShift.getId(), request.getEmployeeJobId());
        return shiftMapper.toDTO(savedShift);
    }

    @Override
    public ShiftResponseDTO updateShift(Long shiftId, ShiftRequestDTO request) {
        validator.validateShiftRequest(request);

        Shift shift = getShiftById(shiftId);
        validator.validateShiftUpdatePermissions(shift);

        shiftMapper.updateShiftFromDto(request, shift);
        Shift updatedShift = shiftRepository.save(shift);

        log.info("Updated shift {} for employee job {}", shiftId, request.getEmployeeJobId());
        return shiftMapper.toDTO(updatedShift);
    }

    @Override
    public void deleteShift(Long id) {
        Objects.requireNonNull(id, "Shift ID cannot be null");

        Shift shift = getShiftById(id);
        validator.validateShiftDeletionPermissions(shift);
        shift.markAsDeleted();
        shiftRepository.save(shift);

        log.info("Soft deleted shift {}", id);
    }

    @Override
    public Shift getShiftById(Long shiftId) {
        Objects.requireNonNull(shiftId, "Shift ID cannot be null");

        return shiftRepository.findById(shiftId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(ClockErrorCode.SHIFT_NOT_FOUND.getDefaultMessage() + " with id: " + shiftId));
    }
}
