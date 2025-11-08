package com.olehprukhnytskyi.macrotrackerintakeservice.controller;

import com.olehprukhnytskyi.macrotrackerintakeservice.dto.CacheablePage;
import com.olehprukhnytskyi.macrotrackerintakeservice.dto.IntakeRequestDto;
import com.olehprukhnytskyi.macrotrackerintakeservice.dto.IntakeResponseDto;
import com.olehprukhnytskyi.macrotrackerintakeservice.dto.PagedResponse;
import com.olehprukhnytskyi.macrotrackerintakeservice.dto.Pagination;
import com.olehprukhnytskyi.macrotrackerintakeservice.dto.UpdateIntakeRequestDto;
import com.olehprukhnytskyi.macrotrackerintakeservice.service.IntakeService;
import com.olehprukhnytskyi.macrotrackerintakeservice.service.RequestDeduplicationService;
import com.olehprukhnytskyi.macrotrackerintakeservice.util.CustomHeaders;
import com.olehprukhnytskyi.macrotrackerintakeservice.util.ProcessedEntityType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/intake")
@Tag(
        name = "Food Intake API",
        description = "Track and manage daily food consumption with nutrition calculations"
)
public class IntakeController {
    private final RequestDeduplicationService requestDeduplicationService;
    private final IntakeService intakeService;

    @Operation(
            summary = "Get intake records",
            description = """
            Retrieve paginated food intake records for a specific date.
            
            **Date formats:**
            - 'today': Current date
            - 'yyyy-MM-dd': Specific date (e.g., 2024-01-15)
            - Empty: All dates
            
            Automatically calculates nutrition values based on food amount.
            """
    )
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

        CacheablePage<IntakeResponseDto> page = intakeService
                .findByDate(parsedDate, userId, pageable);
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

    @Operation(
            summary = "Add food intake",
            description = "Record food consumption with automatic nutrition calculation"
    )
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

    @Operation(
            summary = "Update intake amount",
            description = "Update the amount of consumed food with recalculated nutrition values"
    )
    @PatchMapping("/{id}")
    public ResponseEntity<IntakeResponseDto> updateIntake(
            @RequestHeader(CustomHeaders.X_USER_ID) Long userId,
            @PathVariable Long id,
            @Valid @RequestBody UpdateIntakeRequestDto intakeRequest) {
        IntakeResponseDto updated = intakeService.update(id, intakeRequest, userId);
        return ResponseEntity.ok(updated);
    }

    @Operation(
            summary = "Delete intake record",
            description = "Remove food intake record by ID"
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(
            @PathVariable Long id,
            @RequestHeader(CustomHeaders.X_USER_ID) Long userId) {
        intakeService.deleteById(id, userId);
        return ResponseEntity.noContent().build();
    }
}
