package com.olehprukhnytskyi.macrotrackerintakeservice.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.olehprukhnytskyi.macrotrackerintakeservice.util.BigDecimalJsonSerializer;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class NutrimentsDto {
    @JsonSerialize(using = BigDecimalJsonSerializer.class)
    private BigDecimal kcal = BigDecimal.ZERO;

    @JsonSerialize(using = BigDecimalJsonSerializer.class)
    private BigDecimal fat = BigDecimal.ZERO;

    @JsonSerialize(using = BigDecimalJsonSerializer.class)
    private BigDecimal proteins = BigDecimal.ZERO;

    @JsonSerialize(using = BigDecimalJsonSerializer.class)
    private BigDecimal carbohydrates = BigDecimal.ZERO;
}
