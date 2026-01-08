package com.olehprukhnytskyi.macrotrackerintakeservice.service;

import static com.olehprukhnytskyi.macrotrackerintakeservice.util.IntakeUtils.calculateNutriments;

import com.olehprukhnytskyi.exception.NotFoundException;
import com.olehprukhnytskyi.exception.error.IntakeErrorCode;
import com.olehprukhnytskyi.macrotrackerintakeservice.dto.FoodDto;
import com.olehprukhnytskyi.macrotrackerintakeservice.dto.IntakeResponseDto;
import com.olehprukhnytskyi.macrotrackerintakeservice.dto.MealTemplateRequestDto;
import com.olehprukhnytskyi.macrotrackerintakeservice.mapper.IntakeMapper;
import com.olehprukhnytskyi.macrotrackerintakeservice.mapper.NutrimentsMapper;
import com.olehprukhnytskyi.macrotrackerintakeservice.model.Intake;
import com.olehprukhnytskyi.macrotrackerintakeservice.model.MealTemplate;
import com.olehprukhnytskyi.macrotrackerintakeservice.model.MealTemplateItem;
import com.olehprukhnytskyi.macrotrackerintakeservice.model.Nutriments;
import com.olehprukhnytskyi.macrotrackerintakeservice.repository.jpa.IntakeRepository;
import com.olehprukhnytskyi.macrotrackerintakeservice.repository.jpa.MealTemplateRepository;
import com.olehprukhnytskyi.macrotrackerintakeservice.util.CacheConstants;
import com.olehprukhnytskyi.util.IntakePeriod;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MealService {
    private final IntakeRepository intakeRepository;
    private final MealTemplateRepository mealTemplateRepository;
    private final IntakeMapper intakeMapper;
    private final CacheManager cacheManager;
    private final NutrimentsMapper nutrimentsMapper;
    private final FoodClientService foodClientService;

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

    private List<Intake> createIntakesFromTemplateItem(List<MealTemplateItem> items, LocalDate date,
                                                       IntakePeriod period, Long userId,
                                                       String batchId) {
        List<Intake> intakes = new ArrayList<>();
        for (MealTemplateItem mealTemplateItem : items) {
            Intake intake = new Intake();
            intake.setMealGroupId(batchId);
            intake.setUserId(userId);
            intake.setFoodId(mealTemplateItem.getFoodId());
            intake.setFoodName(mealTemplateItem.getFoodName());
            intake.setDate(date);
            intake.setIntakePeriod(period != null ? period : IntakePeriod.SNACK);
            intake.setAmount(mealTemplateItem.getAmount());
            Nutriments calculatedNutriments = calculateNutriments(
                    nutrimentsMapper.toDto(mealTemplateItem.getNutriments()),
                    mealTemplateItem.getAmount()
            );
            intake.setNutriments(calculatedNutriments);
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
        Map<String, Integer> amountMap = request.getItems().stream()
                .collect(Collectors.toMap(
                        MealTemplateRequestDto.TemplateItemDto::getFoodId,
                        MealTemplateRequestDto.TemplateItemDto::getAmount,
                        (existing, replacement) -> existing
                ));
        List<MealTemplateItem> meals = new ArrayList<>();
        for (FoodDto food : foods) {
            Integer amount = amountMap.get(food.getId());
            if (amount == null) {
                continue;
            }
            MealTemplateItem mealTemplateItem = MealTemplateItem.builder()
                    .template(template)
                    .foodId(food.getId())
                    .amount(amount)
                    .nutriments(nutrimentsMapper.toModel(food.getNutriments()))
                    .foodName(food.getProductName())
                    .build();
            meals.add(mealTemplateItem);
        }
        return meals;
    }
}
