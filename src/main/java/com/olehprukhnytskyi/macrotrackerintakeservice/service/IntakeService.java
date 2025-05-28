package com.olehprukhnytskyi.macrotrackerintakeservice.service;

import com.olehprukhnytskyi.macrotrackerintakeservice.dto.IntakeRequestDto;
import com.olehprukhnytskyi.macrotrackerintakeservice.dto.IntakeResponseDto;
import com.olehprukhnytskyi.macrotrackerintakeservice.dto.UpdateIntakeRequestDto;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IntakeService {
    IntakeResponseDto save(IntakeRequestDto intakeRequest, Long userId, String requestId);

    Page<IntakeResponseDto> findByDate(LocalDate date, Long userId, Pageable pageable);

    IntakeResponseDto update(Long id, UpdateIntakeRequestDto intakeRequest, Long userId);

    void deleteById(Long id, Long userId);
}
