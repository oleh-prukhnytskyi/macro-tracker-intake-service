package com.olehprukhnytskyi.macrotrackerintakeservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Food intake update request")
public class UpdateIntakeRequestDto {
    @Schema(description = "Updated consumed amount in grams", example = "200", minimum = "1")
    @Min(1)
    private int amount;
}
