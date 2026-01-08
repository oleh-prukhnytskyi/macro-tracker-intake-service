package com.olehprukhnytskyi.macrotrackerintakeservice.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealTemplateResponseDto {
    private Long id;
    private String name;
    private List<MealTemplateItemDto> items;
}
