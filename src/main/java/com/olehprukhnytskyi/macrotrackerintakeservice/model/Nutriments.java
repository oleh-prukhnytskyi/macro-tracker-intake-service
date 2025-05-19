package com.olehprukhnytskyi.macrotrackerintakeservice.model;

import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import lombok.Data;

@Data
@Embeddable
public class Nutriments {
    private BigDecimal kcal;
    private BigDecimal carbohydrates;
    private BigDecimal fat;
    private BigDecimal proteins;
}
