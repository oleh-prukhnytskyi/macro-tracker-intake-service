package com.olehprukhnytskyi.macrotrackerintakeservice.dto;

import com.olehprukhnytskyi.util.IntakePeriod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
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

    @Schema(
            description = "Date of intake (user's local date, yyyy-MM-dd)",
            example = "2024-01-15",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull
    private LocalDate date;

    @Schema(
            description = "Meal period",
            example = "BREAKFAST",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            allowableValues = {
                    "BREAKFAST",
                    "LUNCH",
                    "DINNER",
                    "SNACK"
            }
    )
    private IntakePeriod intakePeriod;

    public IntakeRequestDto(String foodId) {
        this.foodId = foodId;
    }
}
