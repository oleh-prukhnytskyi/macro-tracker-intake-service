package com.olehprukhnytskyi.macrotrackerintakeservice.dto;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateIntakeRequestDto {
    @Min(1)
    private int amount;
}
