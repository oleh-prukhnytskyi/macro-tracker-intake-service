package com.olehprukhnytskyi.macrotrackerintakeservice.controller;

import com.olehprukhnytskyi.macrotrackerintakeservice.dto.IntakeRequestDto;
import com.olehprukhnytskyi.macrotrackerintakeservice.dto.IntakeResponseDto;
import com.olehprukhnytskyi.macrotrackerintakeservice.dto.PagedResponse;
import com.olehprukhnytskyi.macrotrackerintakeservice.dto.Pagination;
import com.olehprukhnytskyi.macrotrackerintakeservice.dto.UpdateIntakeRequestDto;
import com.olehprukhnytskyi.macrotrackerintakeservice.service.IntakeService;
import com.olehprukhnytskyi.macrotrackerintakeservice.service.RequestDeduplicationService;
import com.olehprukhnytskyi.macrotrackerintakeservice.util.CustomHeaders;
import com.olehprukhnytskyi.macrotrackerintakeservice.util.ProcessedEntityType;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/intake")
public class IntakeController {
    private final RequestDeduplicationService requestDeduplicationService;
    private final IntakeService intakeService;

    @GetMapping
    public ResponseEntity<PagedResponse<IntakeResponseDto>> findByDate(
            @RequestHeader(CustomHeaders.X_USER_ID) Long userId,
            @RequestParam(required = false) String date,
            @PageableDefault(size = 20) Pageable pageable) {
        LocalDate parsedDate = null;
        if (StringUtils.hasText(date)) {
            if (date.equalsIgnoreCase("today")) {
                parsedDate = LocalDate.now();
            } else {
                try {
                    parsedDate = LocalDate.parse(date);
                } catch (DateTimeParseException e) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Invalid date format. Use 'today' or yyyy-MM-dd");
                }
            }
        }

        Page<IntakeResponseDto> page = intakeService.findByDate(parsedDate, userId, pageable);
        PagedResponse<IntakeResponseDto> response = new PagedResponse<>(
                page.getContent(),
                new Pagination(
                        pageable.getPageNumber() * pageable.getPageSize(),
                        pageable.getPageSize(),
                        (int) page.getTotalElements()
                )
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<IntakeResponseDto> addIntake(
            @RequestHeader(CustomHeaders.X_USER_ID) Long userId,
            @RequestHeader(CustomHeaders.X_REQUEST_ID) String requestId,
            @Valid @RequestBody IntakeRequestDto intakeRequest) {
        if (requestDeduplicationService.isProcessed(
                ProcessedEntityType.INTAKE, requestId, userId)) {
            return ResponseEntity.status(HttpStatus.OK).body(null);
        }
        IntakeResponseDto saved = intakeService.save(intakeRequest, userId, requestId);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<IntakeResponseDto> updateIntake(
            @RequestHeader(CustomHeaders.X_USER_ID) Long userId,
            @PathVariable Long id,
            @Valid @RequestBody UpdateIntakeRequestDto intakeRequest) {
        IntakeResponseDto updated = intakeService.update(id, intakeRequest, userId);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(
            @PathVariable Long id,
            @RequestHeader(CustomHeaders.X_USER_ID) Long userId) {
        intakeService.deleteById(id, userId);
        return ResponseEntity.noContent().build();
    }
}
