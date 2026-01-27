package com.olehprukhnytskyi.macrotrackerintakeservice.service.strategy;

import com.olehprukhnytskyi.macrotrackerintakeservice.model.MealTemplateItem;
import com.olehprukhnytskyi.macrotrackerintakeservice.model.Nutriments;
import com.olehprukhnytskyi.util.UnitType;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Component;

@Component
public class GramsCalculationStrategy implements NutrientCalculationStrategy {
    @Override
    public UnitType getSupportedType() {
        return UnitType.GRAMS;
    }

    @Override
    public void calculate(Nutriments base, int amount) {
        base.setCalories(calculateValue(base.getCaloriesPer100(), amount));
        base.setCarbohydrates(calculateValue(base.getCarbohydratesPer100(), amount));
        base.setFat(calculateValue(base.getFatPer100(), amount));
        base.setProtein(calculateValue(base.getProteinPer100(), amount));
    }

    @Override
    public void recalculateItem(MealTemplateItem item, int newAmount) {
        Nutriments n = item.getNutriments();
        n.setCalories(calculateValue(n.getCaloriesPer100(), newAmount));
        n.setProtein(calculateValue(n.getProteinPer100(), newAmount));
        n.setFat(calculateValue(n.getFatPer100(), newAmount));
        n.setCarbohydrates(calculateValue(n.getCarbohydratesPer100(), newAmount));
        item.setAmount(newAmount);
    }

    private BigDecimal calculateValue(BigDecimal per100, int amount) {
        if (per100 == null) {
            return BigDecimal.ZERO;
        }
        return per100.multiply(BigDecimal.valueOf(amount))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }
}
