package com.olehprukhnytskyi.macrotrackerintakeservice.controller;

import com.olehprukhnytskyi.macrotrackerintakeservice.dto.IntakeRequestDto;
import com.olehprukhnytskyi.macrotrackerintakeservice.dto.IntakeResponseDto;
import com.olehprukhnytskyi.macrotrackerintakeservice.service.IntakeService;
import com.olehprukhnytskyi.macrotrackerintakeservice.service.RequestDeduplicationService;
import com.olehprukhnytskyi.macrotrackerintakeservice.util.CustomHeaders;
import com.olehprukhnytskyi.macrotrackerintakeservice.util.ProcessedEntityType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/intake")
public class IntakeController {
    private final RequestDeduplicationService requestDeduplicationService;
    private final IntakeService intakeService;

    @PostMapping
    public ResponseEntity<IntakeResponseDto> addIntake(
            @RequestHeader(CustomHeaders.X_USER_ID) Long userId,
            @RequestHeader(CustomHeaders.X_REQUEST_ID) String requestId,
            @Valid @RequestBody IntakeRequestDto intakeRequest
    ) {
        if (requestDeduplicationService.isProcessed(
                ProcessedEntityType.INTAKE, requestId, userId)) {
            return ResponseEntity.status(HttpStatus.OK).body(null);
        }
        IntakeResponseDto saved = intakeService.save(intakeRequest, userId, requestId);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
}
