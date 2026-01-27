package com.olehprukhnytskyi.macrotrackerintakeservice.service.strategy;

import com.olehprukhnytskyi.macrotrackerintakeservice.model.MealTemplateItem;
import com.olehprukhnytskyi.macrotrackerintakeservice.model.Nutriments;
import com.olehprukhnytskyi.util.UnitType;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class PiecesCalculationStrategy implements NutrientCalculationStrategy {
    @Override
    public UnitType getSupportedType() {
        return UnitType.PIECES;
    }

    @Override
    public void calculate(Nutriments base, int amount) {
        BigDecimal multiplier = BigDecimal.valueOf(amount);
        base.setCalories(base.getCaloriesPerPiece().multiply(multiplier));
        base.setCarbohydrates(base.getCarbohydratesPerPiece().multiply(multiplier));
        base.setFat(base.getFatPerPiece().multiply(multiplier));
        base.setProtein(base.getProteinPerPiece().multiply(multiplier));
    }

    @Override
    public void recalculateItem(MealTemplateItem item, int newAmount) {
        Nutriments n = item.getNutriments();
        BigDecimal multiplier = BigDecimal.valueOf(newAmount);
        n.setCalories(n.getCaloriesPer100().multiply(multiplier));
        n.setProtein(n.getProteinPer100().multiply(multiplier));
        n.setFat(n.getFatPer100().multiply(multiplier));
        n.setCarbohydrates(n.getCarbohydratesPer100().multiply(multiplier));
        item.setAmount(newAmount);
    }
}
