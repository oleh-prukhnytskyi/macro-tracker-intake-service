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
    @Column(name = "calories_per_100")
    private BigDecimal caloriesPer100 = BigDecimal.ZERO;

    @Column(name = "carbohydrates_per_100")
    private BigDecimal carbohydratesPer100 = BigDecimal.ZERO;

    @Column(name = "fat_per_100")
    private BigDecimal fatPer100 = BigDecimal.ZERO;

    @Column(name = "protein_per_100")
    private BigDecimal proteinPer100 = BigDecimal.ZERO;

    @Column(name = "calories_total")
    private BigDecimal calories = BigDecimal.ZERO;

    @Column(name = "carbohydrates_total")
    private BigDecimal carbohydrates = BigDecimal.ZERO;

    @Column(name = "fat_total")
    private BigDecimal fat = BigDecimal.ZERO;

    @Column(name = "protein_total")
    private BigDecimal protein = BigDecimal.ZERO;
}
