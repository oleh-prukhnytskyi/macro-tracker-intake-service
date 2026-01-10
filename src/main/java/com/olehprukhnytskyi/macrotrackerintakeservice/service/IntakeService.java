package com.olehprukhnytskyi.macrotrackerintakeservice.service;

import static com.olehprukhnytskyi.macrotrackerintakeservice.util.IntakeUtils.calculateNutriments;
import static com.olehprukhnytskyi.macrotrackerintakeservice.util.IntakeUtils.recalculateExistingIntake;

import com.olehprukhnytskyi.event.UserDeletedEvent;
import com.olehprukhnytskyi.exception.ExternalServiceException;
import com.olehprukhnytskyi.exception.NotFoundException;
import com.olehprukhnytskyi.exception.error.CommonErrorCode;
import com.olehprukhnytskyi.exception.error.FoodErrorCode;
import com.olehprukhnytskyi.exception.error.IntakeErrorCode;
import com.olehprukhnytskyi.macrotrackerintakeservice.dto.FoodDto;
import com.olehprukhnytskyi.macrotrackerintakeservice.dto.IntakeRequestDto;
import com.olehprukhnytskyi.macrotrackerintakeservice.dto.IntakeResponseDto;
import com.olehprukhnytskyi.macrotrackerintakeservice.dto.UpdateIntakeRequestDto;
import com.olehprukhnytskyi.macrotrackerintakeservice.mapper.IntakeMapper;
import com.olehprukhnytskyi.macrotrackerintakeservice.model.Intake;
import com.olehprukhnytskyi.macrotrackerintakeservice.producer.UserEventProducer;
import com.olehprukhnytskyi.macrotrackerintakeservice.repository.jpa.IntakeRepository;
import com.olehprukhnytskyi.macrotrackerintakeservice.util.CacheConstants;
import feign.FeignException;
import java.time.LocalDate;
import java.util.List;
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
public class IntakeService {
    private static final int DELETE_BATCH_SIZE = 1000;
    private final IntakeRepository intakeRepository;
    private final CacheManager cacheManager;
    private final IntakeMapper intakeMapper;
    private final FoodClientService foodClientService;
    private final UserEventProducer userEventProducer;

    @CacheEvict(value = CacheConstants.USER_INTAKES, key = "#userId + ':' + #intakeRequest.date")
    @Transactional
    public IntakeResponseDto save(IntakeRequestDto intakeRequest, Long userId) {
        log.info("Saving intake for userId={}", userId);
        try {
            FoodDto food = foodClientService.getFoodById(intakeRequest.getFoodId());
            Intake intake = intakeMapper.toModel(intakeRequest);
            intake.setUserId(userId);
            intake.setFoodId(intakeRequest.getFoodId());
            intakeMapper.updateIntakeFromFoodDto(intake, food);
            intake.setNutriments(calculateNutriments(food.getNutriments(),
                    intakeRequest.getAmount()));
            Intake saved = intakeRepository.save(intake);
            log.debug("Intake saved successfully for userId={} intakeId={}", userId, saved.getId());
            return intakeMapper.toDto(saved);
        } catch (FeignException.NotFound ex) {
            log.warn("Food not found for foodId={} userId={}", intakeRequest.getFoodId(), userId);
            throw new NotFoundException(FoodErrorCode.FOOD_NOT_FOUND, "Food not found");
        } catch (FeignException ex) {
            log.error("Food service unavailable while saving intake for userId={} foodId={}",
                    userId, intakeRequest.getFoodId());
            throw new ExternalServiceException(CommonErrorCode.UPSTREAM_SERVICE_UNAVAILABLE,
                    "Food service is unavailable");
        }
    }

    @Cacheable(value = CacheConstants.USER_INTAKES, key = "#userId + ':' + #date")
    public List<IntakeResponseDto> findByDate(LocalDate date, Long userId) {
        log.debug("Fetching intake list for userId={} date={}", userId, date);
        List<Intake> intakes = (date != null)
                ? intakeRepository.findByUserIdAndDate(userId, date)
                : intakeRepository.findByUserId(userId);
        log.debug("Fetched {} intake records for userId={}", intakes.size(), userId);
        return intakes.stream()
                .map(intakeMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public IntakeResponseDto update(Long id, UpdateIntakeRequestDto intakeRequest,
                                    Long userId) {
        log.info("Updating intake id={} for userId={}", id, userId);
        Intake intake = intakeRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NotFoundException(IntakeErrorCode.INTAKE_NOT_FOUND,
                        "Intake not found"));
        manualEvict(userId, intake.getDate());
        int oldAmount = intake.getAmount();
        int newAmount = intakeRequest.getAmount();
        intakeMapper.updateFromDto(intake, intakeRequest);
        if (oldAmount != newAmount) {
            recalculateExistingIntake(intake, newAmount);
        }
        Intake saved = intakeRepository.save(intake);
        log.debug("Intake updated successfully id={} userId={}", id, userId);
        return intakeMapper.toDto(saved);
    }

    @Transactional
    public void deleteById(Long id, Long userId) {
        log.info("Deleting intake id={} for userId={}", id, userId);
        intakeRepository.findByIdAndUserId(id, userId).ifPresent(intake -> {
            manualEvict(userId, intake.getDate());
            intakeRepository.delete(intake);
        });
    }

    @Transactional
    public void deleteUserIntakesRecursively(Long userId) {
        log.info("Processing batch deletion for user: {}", userId);
        int deletedCount = intakeRepository.deleteBatchByUserId(userId, DELETE_BATCH_SIZE);
        log.info("Deleted {} intake records for user {}", deletedCount, userId);
        if (deletedCount >= DELETE_BATCH_SIZE) {
            log.info("User {} still has data. Republishing event to continue deletion.",
                    userId);
            userEventProducer.sendUserDeletedEvent(new UserDeletedEvent(userId));
        } else {
            log.info("Data cleanup completed for user {}", userId);
        }
    }

    private void manualEvict(Long userId, LocalDate date) {
        String key = userId + ":" + date;
        try {
            Cache cache = cacheManager.getCache(CacheConstants.USER_INTAKES);
            if (cache != null) {
                cache.evict(key);
            }
        } catch (Exception e) {
            log.error("Failed to evict cache", e);
        }
    }
}
