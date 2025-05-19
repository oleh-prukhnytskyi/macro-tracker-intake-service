package com.olehprukhnytskyi.macrotrackerintakeservice.service.impl;

import com.olehprukhnytskyi.macrotrackerintakeservice.dto.FoodDto;
import com.olehprukhnytskyi.macrotrackerintakeservice.dto.IntakeRequestDto;
import com.olehprukhnytskyi.macrotrackerintakeservice.dto.IntakeResponseDto;
import com.olehprukhnytskyi.macrotrackerintakeservice.event.RequestProcessedEvent;
import com.olehprukhnytskyi.macrotrackerintakeservice.mapper.IntakeMapper;
import com.olehprukhnytskyi.macrotrackerintakeservice.model.Intake;
import com.olehprukhnytskyi.macrotrackerintakeservice.repository.IntakeRepository;
import com.olehprukhnytskyi.macrotrackerintakeservice.service.FoodClientService;
import com.olehprukhnytskyi.macrotrackerintakeservice.service.IntakeService;
import com.olehprukhnytskyi.macrotrackerintakeservice.service.RequestDeduplicationService;
import com.olehprukhnytskyi.macrotrackerintakeservice.util.ProcessedEntityType;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
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
        } catch (FeignException.NotFound ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Food not found");
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
}
