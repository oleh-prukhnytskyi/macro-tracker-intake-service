package com.olehprukhnytskyi.macrotrackerintakeservice.service.strategy;

import com.olehprukhnytskyi.macrotrackerintakeservice.model.MealTemplateItem;
import com.olehprukhnytskyi.macrotrackerintakeservice.model.Nutriments;
import com.olehprukhnytskyi.util.UnitType;

public interface NutrientCalculationStrategy {
    void calculate(Nutriments baseNutriments, int amount);

    void recalculateItem(MealTemplateItem item, int newAmount);

    UnitType getSupportedType();
}
