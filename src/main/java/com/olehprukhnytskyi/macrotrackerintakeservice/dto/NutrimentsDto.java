package com.olehprukhnytskyi.macrotrackerintakeservice.dto;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.olehprukhnytskyi.macrotrackerintakeservice.util.BigDecimalJsonSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Nutrition values")
public class NutrimentsDto {
    @Schema(description = "Calories", example = "247.5", minimum = "0.0")
    @Builder.Default
    @JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
    @JsonSerialize(using = BigDecimalJsonSerializer.class)
    private BigDecimal calories = BigDecimal.ZERO;

    @Schema(description = "Fat in grams", example = "5.4", minimum = "0.0")
    @Builder.Default
    @JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
    @JsonSerialize(using = BigDecimalJsonSerializer.class)
    private BigDecimal fat = BigDecimal.ZERO;

    @Schema(description = "Protein in grams", example = "46.5", minimum = "0.0")
    @Builder.Default
    @JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
    @JsonSerialize(using = BigDecimalJsonSerializer.class)
    private BigDecimal protein = BigDecimal.ZERO;

    @Schema(description = "Carbohydrates in grams", example = "0.0", minimum = "0.0")
    @Builder.Default
    @JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
    @JsonSerialize(using = BigDecimalJsonSerializer.class)
    private BigDecimal carbohydrates = BigDecimal.ZERO;
}
