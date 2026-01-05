package com.olehprukhnytskyi.macrotrackerintakeservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class MealTemplateRequestDto {
    @NotBlank
    private String name;

    @Valid
    @NotEmpty
    private List<TemplateItemDto> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TemplateItemDto {
        @NotBlank
        private String foodId;

        @Min(1)
        private int amount;
    }
}
