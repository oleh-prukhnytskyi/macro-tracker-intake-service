package com.olehprukhnytskyi.macrotrackerintakeservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class Nutriments {
    @Builder.Default
    @Column(name = "calories_per_100", nullable = false, columnDefinition = "decimal default 0")
    private BigDecimal caloriesPer100 = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "carbohydrates_per_100", nullable = false,
            columnDefinition = "decimal default 0")
    private BigDecimal carbohydratesPer100 = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "fat_per_100", nullable = false, columnDefinition = "decimal default 0")
    private BigDecimal fatPer100 = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "protein_per_100", nullable = false, columnDefinition = "decimal default 0")
    private BigDecimal proteinPer100 = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "calories_per_piece", nullable = false, columnDefinition = "decimal default 0")
    private BigDecimal caloriesPerPiece = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "carbohydrates_per_piece", nullable = false,
            columnDefinition = "decimal default 0")
    private BigDecimal carbohydratesPerPiece = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "fat_per_piece", nullable = false, columnDefinition = "decimal default 0")
    private BigDecimal fatPerPiece = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "protein_per_piece", nullable = false, columnDefinition = "decimal default 0")
    private BigDecimal proteinPerPiece = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "calories_total", nullable = false, columnDefinition = "decimal default 0")
    private BigDecimal calories = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "carbohydrates_total", nullable = false, columnDefinition = "decimal default 0")
    private BigDecimal carbohydrates = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "fat_total", nullable = false, columnDefinition = "decimal default 0")
    private BigDecimal fat = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "protein_total", nullable = false, columnDefinition = "decimal default 0")
    private BigDecimal protein = BigDecimal.ZERO;
}
