package com.olehprukhnytskyi.macrotrackerintakeservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Schema(description = "Request to create a reusable meal template")
public class MealTemplateRequestDto {
    @NotBlank
    @Schema(description = "Name of the meal template", example = "Morning Porridge")
    private String name;

    @Valid
    @NotEmpty
    @Schema(description = "List of food items included in this template")
    private List<TemplateItemDto> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Individual food item within a template")
    public static class TemplateItemDto {
        @NotBlank
        @Schema(description = "ID of the food from Food Service", example = "12345678")
        private String foodId;

        @Min(1)
        @Schema(description = "Standard amount in grams for this template", example = "100")
        private int amount;
    }
}
