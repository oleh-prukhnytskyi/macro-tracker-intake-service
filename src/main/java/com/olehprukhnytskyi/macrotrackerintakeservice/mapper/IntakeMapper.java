package com.olehprukhnytskyi.macrotrackerintakeservice.mapper;

import com.olehprukhnytskyi.macrotrackerintakeservice.config.MapperConfig;
import com.olehprukhnytskyi.macrotrackerintakeservice.dto.FoodDto;
import com.olehprukhnytskyi.macrotrackerintakeservice.dto.IntakeRequestDto;
import com.olehprukhnytskyi.macrotrackerintakeservice.dto.IntakeResponseDto;
import com.olehprukhnytskyi.macrotrackerintakeservice.model.Intake;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;

@Mapper(config = MapperConfig.class, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface IntakeMapper {
    Intake toModel(IntakeRequestDto dto);

    IntakeResponseDto toDto(Intake model);

    @Mappings({
            @Mapping(target = "nutriments", source = "nutriments"),
            @Mapping(target = "foodName", source = "productName")
    })
    void updateIntakeFromFoodDto(@MappingTarget Intake intake, FoodDto food);
}
