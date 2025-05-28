package com.olehprukhnytskyi.macrotrackerintakeservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodDto {
    private String id;
    private String code;
    private String productName;
    private String genericName;
    private String imageUrl;
    private String brands;

    @Builder.Default
    private NutrimentsDto nutriments = new NutrimentsDto();
}
