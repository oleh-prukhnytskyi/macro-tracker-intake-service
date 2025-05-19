package com.olehprukhnytskyi.macrotrackerintakeservice.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class NutrimentsDto {
    private BigDecimal kcal;
    private BigDecimal fat;
    private BigDecimal proteins;
    private BigDecimal carbohydrates;
}
