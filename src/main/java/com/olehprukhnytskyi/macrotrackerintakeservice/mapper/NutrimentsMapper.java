package com.olehprukhnytskyi.macrotrackerintakeservice.mapper;

import com.olehprukhnytskyi.macrotrackerintakeservice.config.MapperConfig;
import com.olehprukhnytskyi.macrotrackerintakeservice.dto.NutrimentsDto;
import com.olehprukhnytskyi.macrotrackerintakeservice.model.Nutriments;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;

@Mapper(config = MapperConfig.class, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NutrimentsMapper {
    NutrimentsDto toDto(Nutriments model);

    Nutriments clone(Nutriments source);

    @Mappings({
            @Mapping(source = "calories", target = "caloriesPer100"),
            @Mapping(source = "carbohydrates", target = "carbohydratesPer100"),
            @Mapping(source = "fat", target = "fatPer100"),
            @Mapping(source = "protein", target = "proteinPer100"),

            @Mapping(target = "calories", ignore = true),
            @Mapping(target = "carbohydrates", ignore = true),
            @Mapping(target = "fat", ignore = true),
            @Mapping(target = "protein", ignore = true)
    })
    Nutriments fromFoodNutriments(NutrimentsDto nutriments);
}
