package com.olehprukhnytskyi.macrotrackerintakeservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class Nutriments {
    @Column(name = "kcal_per_100")
    private BigDecimal kcalPer100 = BigDecimal.ZERO;

    @Column(name = "carbohydrates_per_100")
    private BigDecimal carbohydratesPer100 = BigDecimal.ZERO;

    @Column(name = "fat_per_100")
    private BigDecimal fatPer100 = BigDecimal.ZERO;

    @Column(name = "proteins_per_100")
    private BigDecimal proteinsPer100 = BigDecimal.ZERO;

    @Column(name = "kcal_total")
    private BigDecimal kcal = BigDecimal.ZERO;

    @Column(name = "carbohydrates_total")
    private BigDecimal carbohydrates = BigDecimal.ZERO;

    @Column(name = "fat_total")
    private BigDecimal fat = BigDecimal.ZERO;

    @Column(name = "proteins_total")
    private BigDecimal proteins = BigDecimal.ZERO;
}
