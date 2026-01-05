package com.olehprukhnytskyi.macrotrackerintakeservice.util;

import com.olehprukhnytskyi.macrotrackerintakeservice.dto.NutrimentsDto;
import com.olehprukhnytskyi.macrotrackerintakeservice.model.Intake;
import com.olehprukhnytskyi.macrotrackerintakeservice.model.Nutriments;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class IntakeUtils {
    public static Nutriments calculateNutriments(NutrimentsDto baseNutriments, int amount) {
        return new Nutriments(
                baseNutriments.getCalories(),
                baseNutriments.getCarbohydrates(),
                baseNutriments.getFat(),
                baseNutriments.getProtein(),
                calculate(baseNutriments.getCalories(), amount),
                calculate(baseNutriments.getCarbohydrates(), amount),
                calculate(baseNutriments.getFat(), amount),
                calculate(baseNutriments.getProtein(), amount)
        );
    }

    public static void recalculateExistingIntake(Intake intake, int newAmount) {
        Nutriments n = intake.getNutriments();
        n.setCalories(calculate(n.getCaloriesPer100(), newAmount));
        n.setCarbohydrates(calculate(n.getCarbohydratesPer100(), newAmount));
        n.setFat(calculate(n.getFatPer100(), newAmount));
        n.setProtein(calculate(n.getProteinPer100(), newAmount));
    }

    public static BigDecimal calculate(BigDecimal per100, int amount) {
        if (per100 == null) {
            return BigDecimal.ZERO;
        }
        return per100.multiply(BigDecimal.valueOf(amount))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }
}
