package com.olehprukhnytskyi.macrotrackerintakeservice.dto;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateIntakeRequestDto {
    @Min(1)
    private int amount;
}
