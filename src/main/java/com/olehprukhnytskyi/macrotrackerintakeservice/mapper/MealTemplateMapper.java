package com.olehprukhnytskyi.macrotrackerintakeservice.mapper;

import com.olehprukhnytskyi.macrotrackerintakeservice.dto.MealTemplateItemDto;
import com.olehprukhnytskyi.macrotrackerintakeservice.dto.MealTemplateResponseDto;
import com.olehprukhnytskyi.macrotrackerintakeservice.model.MealTemplate;
import com.olehprukhnytskyi.macrotrackerintakeservice.model.MealTemplateItem;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        uses = {NutrimentsMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface MealTemplateMapper {
    MealTemplateResponseDto toDto(MealTemplate template);

    List<MealTemplateResponseDto> toDtoList(List<MealTemplate> templates);

    @Mapping(target = "nutriments", source = "nutriments")
    MealTemplateItemDto toItemDto(MealTemplateItem item);
}
