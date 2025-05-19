package com.olehprukhnytskyi.macrotrackerintakeservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IntakeRequestDto {
    @NotNull
    private String foodId;

    @Min(1)
    @NotNull
    private int amount;

    public IntakeRequestDto(String foodId) {
        this.foodId = foodId;
    }
}
