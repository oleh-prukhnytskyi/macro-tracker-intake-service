package com.olehprukhnytskyi.macrotrackerintakeservice.mapper;

import com.olehprukhnytskyi.macrotrackerintakeservice.config.MapperConfig;
import com.olehprukhnytskyi.macrotrackerintakeservice.dto.NutrimentsDto;
import com.olehprukhnytskyi.macrotrackerintakeservice.model.Nutriments;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(config = MapperConfig.class, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NutrimentsMapper {
    @Mapping(source = "calories", target = "caloriesPer100")
    Nutriments toModel(NutrimentsDto dto);

    @Mapping(source = "caloriesPer100", target = "calories")
    NutrimentsDto toDto(Nutriments model);
}
