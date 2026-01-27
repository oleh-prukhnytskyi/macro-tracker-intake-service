package com.olehprukhnytskyi.macrotrackerintakeservice.service.strategy;

import com.olehprukhnytskyi.util.UnitType;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NutrientStrategyFactory {
    private final Map<UnitType, NutrientCalculationStrategy> strategies;

    @Autowired
    public NutrientStrategyFactory(List<NutrientCalculationStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(
                        NutrientCalculationStrategy::getSupportedType,
                        Function.identity()
                ));
    }

    public NutrientCalculationStrategy getStrategy(UnitType unitType) {
        return strategies.getOrDefault(unitType, strategies.get(UnitType.GRAMS));
    }
}
