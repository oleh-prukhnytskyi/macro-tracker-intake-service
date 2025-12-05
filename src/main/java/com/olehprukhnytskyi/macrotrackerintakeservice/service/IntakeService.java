package com.olehprukhnytskyi.macrotrackerintakeservice.service;

import com.olehprukhnytskyi.exception.ExternalServiceException;
import com.olehprukhnytskyi.exception.NotFoundException;
import com.olehprukhnytskyi.exception.error.CommonErrorCode;
import com.olehprukhnytskyi.exception.error.FoodErrorCode;
import com.olehprukhnytskyi.exception.error.IntakeErrorCode;
import com.olehprukhnytskyi.macrotrackerintakeservice.dto.CacheablePage;
import com.olehprukhnytskyi.macrotrackerintakeservice.dto.FoodDto;
import com.olehprukhnytskyi.macrotrackerintakeservice.dto.IntakeRequestDto;
import com.olehprukhnytskyi.macrotrackerintakeservice.dto.IntakeResponseDto;
import com.olehprukhnytskyi.macrotrackerintakeservice.dto.UpdateIntakeRequestDto;
import com.olehprukhnytskyi.macrotrackerintakeservice.mapper.IntakeMapper;
import com.olehprukhnytskyi.macrotrackerintakeservice.model.Intake;
import com.olehprukhnytskyi.macrotrackerintakeservice.model.Nutriments;
import com.olehprukhnytskyi.macrotrackerintakeservice.repository.jpa.IntakeRepository;
import feign.FeignException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class IntakeService {
    private final IntakeRepository intakeRepository;
    private final IntakeMapper intakeMapper;
    private final FoodClientService foodClientService;

    @Caching(evict = {
            @CacheEvict(value = "user:intakes", key = "'user:' + #userId"),
            @CacheEvict(value = "user:intakes", key = "'user:' + #userId + ':*'", allEntries = true)
    })
    @Transactional
    public IntakeResponseDto save(IntakeRequestDto intakeRequest, Long userId) {
        log.info("Saving intake for userId={}", userId);
        Intake intake = intakeMapper.toModel(intakeRequest);
        intake.setUserId(userId);
        intake.setFoodId(intakeRequest.getFoodId());
        try {
            FoodDto food = foodClientService.getFoodById(intakeRequest.getFoodId());
            intakeMapper.updateIntakeFromFoodDto(intake, food);

            int amount = intakeRequest.getAmount();
            Nutriments nutriments = new Nutriments(
                    food.getNutriments().getCalories(),
                    food.getNutriments().getCarbohydrates(),
                    food.getNutriments().getFat(),
                    food.getNutriments().getProtein(),

                    calculate(food.getNutriments().getCalories(), amount),
                    calculate(food.getNutriments().getCarbohydrates(), amount),
                    calculate(food.getNutriments().getFat(), amount),
                    calculate(food.getNutriments().getProtein(), amount)
            );
            intake.setNutriments(nutriments);
        } catch (FeignException.NotFound ex) {
            log.warn("Food not found for foodId={} userId={}", intakeRequest.getFoodId(), userId);
            throw new NotFoundException(FoodErrorCode.FOOD_NOT_FOUND, "Food not found");
        } catch (FeignException ex) {
            log.error("Food service unavailable while saving intake for userId={} foodId={}",
                    userId, intakeRequest.getFoodId());
            throw new ExternalServiceException(CommonErrorCode.UPSTREAM_SERVICE_UNAVAILABLE,
                    "Food service is unavailable");
        }
        Intake saved = intakeRepository.save(intake);
        log.debug("Intake saved successfully for userId={} intakeId={}", userId, saved.getId());
        return intakeMapper.toDto(saved);
    }

    @Cacheable(
            value = "user:intakes",
            key = "'user:' + #userId + ( #date != null ? ':date:'"
                    + " + #date.toString() : ':all' ) + ':page:' + #pageable.pageNumber"
    )
    public CacheablePage<IntakeResponseDto> findByDate(LocalDate date, Long userId,
                                              Pageable pageable) {
        log.debug("Fetching intake list for userId={} date={}", userId, date);
        Page<Intake> intakes = (date != null)
                ? intakeRepository.findByUserIdAndDate(userId, date, pageable)
                : intakeRepository.findByUserId(userId, pageable);
        Page<IntakeResponseDto> dtoPage = intakes.map(intakeMapper::toDto);
        log.debug("Fetched {} intake records for userId={}", dtoPage.getNumberOfElements(), userId);
        return CacheablePage.fromPage(dtoPage);
    }

    @Caching(evict = {
            @CacheEvict(value = "user:intakes", key = "'user:' + #userId"),
            @CacheEvict(value = "user:intakes", key = "'user:' + #userId + ':*'", allEntries = true)
    })
    @Transactional
    public IntakeResponseDto update(Long id, UpdateIntakeRequestDto intakeRequest,
                                    Long userId) {
        log.info("Updating intake id={} for userId={}", id, userId);
        Intake intake = intakeRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NotFoundException(IntakeErrorCode.INTAKE_NOT_FOUND,
                        "Intake not found"));
        int oldAmount = intake.getAmount();
        int newAmount = intakeRequest.getAmount();

        intakeMapper.updateFromDto(intake, intakeRequest);

        if (oldAmount != newAmount) {
            Nutriments nutriments = intake.getNutriments();
            nutriments.setCalories(calculate(nutriments.getCaloriesPer100(), newAmount));
            nutriments.setCarbohydrates(calculate(nutriments.getCarbohydratesPer100(), newAmount));
            nutriments.setFat(calculate(nutriments.getFatPer100(), newAmount));
            nutriments.setProtein(calculate(nutriments.getProteinPer100(), newAmount));
        }
        Intake saved = intakeRepository.save(intake);
        log.debug("Intake updated successfully id={} userId={}", id, userId);
        return intakeMapper.toDto(saved);
    }

    @Caching(evict = {
            @CacheEvict(value = "user:intakes", key = "'user:' + #userId"),
            @CacheEvict(value = "user:intakes", key = "'user:' + #userId + ':*'", allEntries = true)
    })
    @Transactional
    public void deleteById(Long id, Long userId) {
        log.info("Deleting intake id={} for userId={}", id, userId);
        intakeRepository.deleteByIdAndUserId(id, userId);
    }

    @CacheEvict(value = "user:intakes", key = "'user:' + #userId", allEntries = true)
    @Transactional
    public void deleteAllByUserId(Long userId) {
        log.info("Deleting all intakes for userId={}", userId);
        intakeRepository.deleteAllByUserId(userId);
    }

    private BigDecimal calculate(BigDecimal per100, int amount) {
        return per100.multiply(BigDecimal.valueOf(amount))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }
}
