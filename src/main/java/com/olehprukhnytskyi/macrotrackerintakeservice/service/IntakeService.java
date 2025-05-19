package com.olehprukhnytskyi.macrotrackerintakeservice.service;

import com.olehprukhnytskyi.macrotrackerintakeservice.dto.IntakeRequestDto;
import com.olehprukhnytskyi.macrotrackerintakeservice.dto.IntakeResponseDto;

public interface IntakeService {
    IntakeResponseDto save(IntakeRequestDto intakeRequest, Long userId, String requestId);
}
