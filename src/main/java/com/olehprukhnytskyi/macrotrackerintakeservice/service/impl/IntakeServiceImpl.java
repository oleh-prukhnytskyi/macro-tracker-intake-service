package com.olehprukhnytskyi.macrotrackerintakeservice.service.impl;

import com.olehprukhnytskyi.macrotrackerintakeservice.dto.FoodDto;
import com.olehprukhnytskyi.macrotrackerintakeservice.dto.IntakeRequestDto;
import com.olehprukhnytskyi.macrotrackerintakeservice.dto.IntakeResponseDto;
import com.olehprukhnytskyi.macrotrackerintakeservice.dto.UpdateIntakeRequestDto;
import com.olehprukhnytskyi.macrotrackerintakeservice.event.RequestProcessedEvent;
import com.olehprukhnytskyi.macrotrackerintakeservice.exception.NotFoundException;
import com.olehprukhnytskyi.macrotrackerintakeservice.mapper.IntakeMapper;
import com.olehprukhnytskyi.macrotrackerintakeservice.model.Intake;
import com.olehprukhnytskyi.macrotrackerintakeservice.model.Nutriments;
import com.olehprukhnytskyi.macrotrackerintakeservice.repository.IntakeRepository;
import com.olehprukhnytskyi.macrotrackerintakeservice.service.FoodClientService;
import com.olehprukhnytskyi.macrotrackerintakeservice.service.IntakeService;
import com.olehprukhnytskyi.macrotrackerintakeservice.service.RequestDeduplicationService;
import com.olehprukhnytskyi.macrotrackerintakeservice.util.ProcessedEntityType;
import feign.FeignException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class IntakeServiceImpl implements IntakeService {
    private final RequestDeduplicationService requestDeduplicationService;
    private final ApplicationEventPublisher eventPublisher;
    private final IntakeRepository intakeRepository;
    private final IntakeMapper intakeMapper;
    private final FoodClientService foodClientService;

    @Override
    @Transactional
    public IntakeResponseDto save(IntakeRequestDto intakeRequest,
                                  Long userId, String requestId) {
        Intake intake = intakeMapper.toModel(intakeRequest);
        intake.setUserId(userId);
        intake.setFoodId(intakeRequest.getFoodId());

        try {
            FoodDto food = foodClientService.getFoodById(intakeRequest.getFoodId());
            intakeMapper.updateIntakeFromFoodDto(intake, food);

            int amount = intakeRequest.getAmount();
            Nutriments nutriments = new Nutriments(
                    food.getNutriments().getKcal(),
                    food.getNutriments().getCarbohydrates(),
                    food.getNutriments().getFat(),
                    food.getNutriments().getProteins(),

                    calculate(food.getNutriments().getKcal(), amount),
                    calculate(food.getNutriments().getCarbohydrates(), amount),
                    calculate(food.getNutriments().getFat(), amount),
                    calculate(food.getNutriments().getProteins(), amount)
            );
            intake.setNutriments(nutriments);
        } catch (FeignException.NotFound ex) {
            throw new NotFoundException("Food not found");
        } catch (FeignException ex) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE, "Food service is unavailable");
        }

        Intake saved = intakeRepository.save(intake);

        String requestKey = requestDeduplicationService.buildRequestKey(
                ProcessedEntityType.INTAKE, requestId, userId
        );
        eventPublisher.publishEvent(new RequestProcessedEvent(requestKey));
        return intakeMapper.toDto(saved);
    }

    @Override
    public Page<IntakeResponseDto> findByDate(LocalDate date, Long userId,
                                              Pageable pageable) {
        Page<Intake> intakes;
        if (date != null) {
            intakes = intakeRepository.findByUserIdAndDate(userId, date, pageable);
        } else {
            intakes = intakeRepository.findByUserId(userId, pageable);
        }
        return intakes.map(intakeMapper::toDto);
    }

    @Override
    @Transactional
    public IntakeResponseDto update(Long id, UpdateIntakeRequestDto intakeRequest,
                                    Long userId) {
        Intake intake = intakeRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NotFoundException("Intake not found"));
        int oldAmount = intake.getAmount();
        int newAmount = intakeRequest.getAmount();

        intakeMapper.updateFromDto(intake, intakeRequest);

        if (oldAmount != newAmount) {
            Nutriments nutriments = intake.getNutriments();
            nutriments.setKcal(calculate(nutriments.getKcalPer100(), newAmount));
            nutriments.setCarbohydrates(calculate(nutriments.getCarbohydratesPer100(), newAmount));
            nutriments.setFat(calculate(nutriments.getFatPer100(), newAmount));
            nutriments.setProteins(calculate(nutriments.getProteinsPer100(), newAmount));
        }
        Intake saved = intakeRepository.save(intake);
        return intakeMapper.toDto(saved);
    }

    @Transactional
    @Override
    public void deleteById(Long id, Long userId) {
        intakeRepository.deleteByIdAndUserId(id, userId);
    }

    private BigDecimal calculate(BigDecimal per100, int amount) {
        return per100.multiply(BigDecimal.valueOf(amount))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }
}
