package com.olehprukhnytskyi.macrotrackerintakeservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealTemplateItemDto {
    private String foodId;
    private String foodName;
    private int amount;
    private NutrimentsDto nutriments;
}
