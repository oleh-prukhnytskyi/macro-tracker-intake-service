package com.olehprukhnytskyi.macrotrackerintakeservice.service;

import com.olehprukhnytskyi.exception.BadRequestException;
import com.olehprukhnytskyi.exception.NotFoundException;
import com.olehprukhnytskyi.exception.error.CommonErrorCode;
import com.olehprukhnytskyi.exception.error.FoodErrorCode;
import com.olehprukhnytskyi.exception.error.IntakeErrorCode;
import com.olehprukhnytskyi.macrotrackerintakeservice.dto.FoodDto;
import com.olehprukhnytskyi.macrotrackerintakeservice.dto.IntakeResponseDto;
import com.olehprukhnytskyi.macrotrackerintakeservice.dto.MealTemplateRequestDto;
import com.olehprukhnytskyi.macrotrackerintakeservice.dto.MealTemplateResponseDto;
import com.olehprukhnytskyi.macrotrackerintakeservice.mapper.IntakeMapper;
import com.olehprukhnytskyi.macrotrackerintakeservice.mapper.MealTemplateMapper;
import com.olehprukhnytskyi.macrotrackerintakeservice.mapper.NutrimentsMapper;
import com.olehprukhnytskyi.macrotrackerintakeservice.model.Intake;
import com.olehprukhnytskyi.macrotrackerintakeservice.model.MealTemplate;
import com.olehprukhnytskyi.macrotrackerintakeservice.model.MealTemplateItem;
import com.olehprukhnytskyi.macrotrackerintakeservice.model.Nutriments;
import com.olehprukhnytskyi.macrotrackerintakeservice.repository.jpa.IntakeRepository;
import com.olehprukhnytskyi.macrotrackerintakeservice.repository.jpa.MealTemplateRepository;
import com.olehprukhnytskyi.macrotrackerintakeservice.service.strategy.NutrientCalculationStrategy;
import com.olehprukhnytskyi.macrotrackerintakeservice.service.strategy.NutrientStrategyFactory;
import com.olehprukhnytskyi.macrotrackerintakeservice.util.CacheConstants;
import com.olehprukhnytskyi.util.IntakePeriod;
import com.olehprukhnytskyi.util.UnitType;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MealService {
    private final NutrientStrategyFactory strategyFactory;
    private final IntakeRepository intakeRepository;
    private final MealTemplateRepository mealTemplateRepository;
    private final IntakeMapper intakeMapper;
    private final MealTemplateMapper mealTemplateMapper;
    private final CacheManager cacheManager;
    private final NutrimentsMapper nutrimentsMapper;
    private final FoodClientService foodClientService;

    @Transactional(readOnly = true)
    @Cacheable(value = CacheConstants.MEAL_TEMPLATES, key = "#userId")
    public List<MealTemplateResponseDto> getTemplates(Long userId) {
        log.info("Fetching meal templates from DB for userId={}", userId);
        List<MealTemplate> templates = mealTemplateRepository.findAllByUserId(userId);
        return mealTemplateMapper.toDtoList(templates);
    }

    @Transactional
    @CacheEvict(value = CacheConstants.MEAL_TEMPLATES, key = "#userId")
    public Long createTemplate(MealTemplateRequestDto request, Long userId) {
        log.info("Creating meal template '{}' for userId={}", request.getName(), userId);
        MealTemplate template = MealTemplate.builder()
                .userId(userId)
                .name(request.getName())
                .build();
        List<MealTemplateItem> items = createMealTemplateItems(request, template);
        template.setItems(items);
        return mealTemplateRepository.save(template).getId();
    }

    @Transactional
    @CacheEvict(value = CacheConstants.USER_INTAKES, key = "#userId + ':' + #date")
    public List<IntakeResponseDto> applyTemplate(Long templateId, LocalDate date,
                                                 IntakePeriod period, Long userId) {
        log.info("Applying template id={} for userId={} on date={}", templateId, userId, date);
        MealTemplate template = mealTemplateRepository.findByIdAndUserId(templateId, userId)
                .orElseThrow(() -> new NotFoundException(IntakeErrorCode.INTAKE_NOT_FOUND,
                        "Template not found"));
        String batchId = UUID.randomUUID().toString();
        List<Intake> newIntakes = createIntakesFromTemplateItem(
                template.getItems(), date, period, userId, batchId);
        List<Intake> savedIntakes = intakeRepository.saveAll(newIntakes);
        log.debug("Applied template '{}', created {} intake records",
                template.getName(), savedIntakes.size());
        return savedIntakes.stream().map(intakeMapper::toDto).toList();
    }

    @Transactional
    public void revertIntakeGroup(String mealGroupId, Long userId) {
        log.info("Reverting intake group {} for user {}", mealGroupId, userId);
        intakeRepository.findFirstByMealGroupIdAndUserId(mealGroupId, userId)
                .ifPresent(intake -> manualEvictUserIntakes(userId, intake.getDate()));
        intakeRepository.deleteByMealGroupIdAndUserId(mealGroupId, userId);
    }

    @Transactional
    @CacheEvict(value = CacheConstants.MEAL_TEMPLATES, key = "#userId")
    public void deleteTemplate(Long templateId, Long userId) {
        log.info("Deleting template id={} for userId={}", templateId, userId);
        MealTemplate template = mealTemplateRepository.findByIdAndUserId(templateId, userId)
                .orElseThrow(() -> new NotFoundException(IntakeErrorCode.INTAKE_NOT_FOUND,
                        "Template not found or does not belong to user"));
        mealTemplateRepository.delete(template);
    }

    @Transactional
    @CacheEvict(value = CacheConstants.MEAL_TEMPLATES, key = "#userId")
    public void updateTemplate(Long templateId, MealTemplateRequestDto request, Long userId) {
        log.info("Updating template id={} for userId={}", templateId, userId);
        MealTemplate template = mealTemplateRepository.findByIdAndUserId(templateId, userId)
                .orElseThrow(() -> new NotFoundException(IntakeErrorCode.INTAKE_NOT_FOUND,
                        "Template not found"));
        template.setName(request.getName());
        Map<String, MealTemplateRequestDto.TemplateItemDto> incomingItemsMap = request
                .getItems().stream()
                .collect(Collectors.toMap(
                        MealTemplateRequestDto.TemplateItemDto::getFoodId,
                        item -> item
                ));
        removeDeletedItems(template, incomingItemsMap);

        Set<String> idsToFetch = new HashSet<>();
        Set<String> existingIds = template.getItems().stream()
                .map(MealTemplateItem::getFoodId)
                .collect(Collectors.toSet());
        incomingItemsMap.keySet().stream()
                .filter(id -> !existingIds.contains(id))
                .forEach(idsToFetch::add);
        for (MealTemplateItem item : template.getItems()) {
            var incoming = incomingItemsMap.get(item.getFoodId());
            if (incoming != null && item.getUnitType() != incoming.getUnitType()) {
                idsToFetch.add(item.getFoodId());
            }
        }
        Map<String, FoodDto> foodMap = new HashMap<>();
        if (!idsToFetch.isEmpty()) {
            List<FoodDto> foods = foodClientService.getFoodsByIds(new ArrayList<>(idsToFetch));
            validateAllFoodsFound(new ArrayList<>(idsToFetch), foods);
            foodMap = foods.stream().collect(Collectors.toMap(FoodDto::getId, f -> f));
        }

        updateExistingItems(template, incomingItemsMap, foodMap);
        addNewItems(template, incomingItemsMap, foodMap);
        mealTemplateRepository.save(template);
    }

    private void addNewItems(MealTemplate template,
                             Map<String, MealTemplateRequestDto.TemplateItemDto> incomingItemsMap,
                             Map<String, FoodDto> foodMap) {
        Set<String> existingIds = template.getItems().stream()
                .map(MealTemplateItem::getFoodId)
                .collect(Collectors.toSet());
        List<String> newIds = incomingItemsMap.keySet().stream()
                .filter(id -> !existingIds.contains(id))
                .toList();
        for (String foodId : newIds) {
            FoodDto food = foodMap.get(foodId);
            var incomingItem = incomingItemsMap.get(food.getId());
            validateUnitSupported(food, incomingItem.getUnitType());
            MealTemplateItem newItem = createNewItem(template, food, incomingItem);
            template.getItems().add(newItem);
        }
    }

    private MealTemplateItem createNewItem(MealTemplate template, FoodDto food,
                                           MealTemplateRequestDto.TemplateItemDto incomingItem) {
        NutrientCalculationStrategy strategy = strategyFactory
                .getStrategy(incomingItem.getUnitType());
        Nutriments calculatedNutriments = nutrimentsMapper.fromFoodNutriments(food.getNutriments());
        strategy.calculate(calculatedNutriments, incomingItem.getAmount());
        return MealTemplateItem.builder()
                .template(template)
                .foodId(food.getId())
                .foodName(food.getProductName())
                .amount(incomingItem.getAmount())
                .unitType(incomingItem.getUnitType())
                .nutriments(calculatedNutriments)
                .build();
    }

    private void updateExistingItems(MealTemplate template,
                                     Map<String, MealTemplateRequestDto
                                             .TemplateItemDto> incomingItemsMap,
                                     Map<String, FoodDto> foodMap) {
        for (MealTemplateItem item : template.getItems()) {
            var incomingItem = incomingItemsMap.get(item.getFoodId());
            boolean amountChanged = item.getAmount() != incomingItem.getAmount();
            boolean unitTypeChanged = item.getUnitType() != incomingItem.getUnitType();
            if (amountChanged || unitTypeChanged) {
                if (unitTypeChanged) {
                    FoodDto food = foodMap.get(item.getFoodId());
                    validateUnitSupported(food, incomingItem.getUnitType());
                    item.setUnitType(incomingItem.getUnitType());
                }
                NutrientCalculationStrategy strategy = strategyFactory
                        .getStrategy(incomingItem.getUnitType());
                strategy.recalculateItem(item, incomingItem.getAmount());
            }
        }
    }

    private void validateUnitSupported(FoodDto food, UnitType requestedUnit) {
        if (food.getAvailableUnits() == null || !food.getAvailableUnits().contains(requestedUnit)) {
            throw new BadRequestException(CommonErrorCode.VALIDATION_ERROR,
                    String.format(
                            "Food '%s' does not support unit type %s. Available types: %s",
                            food.getProductName(),
                            requestedUnit,
                            food.getAvailableUnits()));
        }
    }

    private void validateAllFoodsFound(List<String> requestedIds, List<FoodDto> foundFoods) {
        if (foundFoods == null) {
            throw new NotFoundException(FoodErrorCode.FOOD_NOT_FOUND,
                    "Food service returned no data");
        }
        if (foundFoods.size() != requestedIds.size()) {
            Set<String> foundIds = foundFoods.stream()
                    .map(FoodDto::getId)
                    .collect(Collectors.toSet());
            List<String> missingIds = requestedIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .toList();
            log.error("Consistency error: Requested foods {} but missing {}",
                    requestedIds, missingIds);
            throw new NotFoundException(FoodErrorCode.FOOD_NOT_FOUND,
                    "Foods not found with ids: " + String.join(", ", missingIds));
        }
    }

    private void removeDeletedItems(MealTemplate template, Map<String,
            MealTemplateRequestDto.TemplateItemDto> incomingItemsMap) {
        template.getItems().removeIf(item -> !incomingItemsMap.containsKey(item.getFoodId()));
    }

    private void manualEvictUserIntakes(Long userId, LocalDate date) {
        String key = userId + ":" + date;
        try {
            Cache cache = cacheManager.getCache(CacheConstants.USER_INTAKES);
            if (cache != null) {
                cache.evict(key);
            }
        } catch (Exception e) {
            log.error("Failed to evict cache for key {}", key, e);
        }
    }

    private List<Intake> createIntakesFromTemplateItem(List<MealTemplateItem> items,
                                                       LocalDate date, IntakePeriod period,
                                                       Long userId, String batchId) {
        List<Intake> intakes = new ArrayList<>();
        for (MealTemplateItem item : items) {
            Intake intake = new Intake();
            intake.setMealGroupId(batchId);
            intake.setUserId(userId);
            intake.setFoodId(item.getFoodId());
            intake.setFoodName(item.getFoodName());
            intake.setDate(date);
            intake.setIntakePeriod(period != null ? period : IntakePeriod.SNACK);
            intake.setAmount(item.getAmount());
            intake.setNutriments(nutrimentsMapper.clone(item.getNutriments()));
            intakes.add(intake);
        }
        return intakes;
    }

    private List<MealTemplateItem> createMealTemplateItems(
            MealTemplateRequestDto request, MealTemplate template) {
        List<String> foodIds = request.getItems().stream()
                .map(MealTemplateRequestDto.TemplateItemDto::getFoodId)
                .toList();
        List<FoodDto> foods = foodClientService.getFoodsByIds(foodIds);
        Map<String, MealTemplateRequestDto.TemplateItemDto> requestItemsMap = request
                .getItems().stream()
                .collect(Collectors.toMap(
                        MealTemplateRequestDto.TemplateItemDto::getFoodId,
                        item -> item,
                        (existing, replacement) -> existing
                ));
        List<MealTemplateItem> meals = new ArrayList<>();
        for (FoodDto food : foods) {
            MealTemplateRequestDto.TemplateItemDto requestItem = requestItemsMap
                    .get(food.getId());
            if (requestItem == null) {
                continue;
            }
            int amount = requestItem.getAmount();
            UnitType unitType = requestItem.getUnitType();
            NutrientCalculationStrategy strategy = strategyFactory.getStrategy(unitType);
            Nutriments calculatedNutriments = nutrimentsMapper
                    .fromFoodNutriments(food.getNutriments());
            strategy.calculate(calculatedNutriments, amount);
            MealTemplateItem mealTemplateItem = MealTemplateItem.builder()
                    .template(template)
                    .foodId(food.getId())
                    .amount(amount)
                    .unitType(unitType)
                    .nutriments(calculatedNutriments)
                    .foodName(food.getProductName())
                    .build();
            meals.add(mealTemplateItem);
        }
        return meals;
    }
}
