package com.olehprukhnytskyi.macrotrackerintakeservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Food intake creation request")
public class IntakeRequestDto {
    @Schema(
            description = "Food product ID",
            example = "507f1f77bcf86cd799439011",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull
    private String foodId;

    @Schema(
            description = "Consumed amount in grams",
            example = "150",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minimum = "1"
    )
    @Min(1)
    @NotNull
    private int amount;

    public IntakeRequestDto(String foodId) {
        this.foodId = foodId;
    }
}
