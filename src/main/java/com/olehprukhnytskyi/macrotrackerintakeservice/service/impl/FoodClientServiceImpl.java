package com.olehprukhnytskyi.macrotrackerintakeservice.service.impl;

import com.olehprukhnytskyi.macrotrackerintakeservice.client.FoodClient;
import com.olehprukhnytskyi.macrotrackerintakeservice.dto.FoodDto;
import com.olehprukhnytskyi.macrotrackerintakeservice.service.FoodClientService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FoodClientServiceImpl implements FoodClientService {
    private final FoodClient foodClient;

    @Retryable(
            retryFor = FeignException.class,
            backoff = @Backoff(delay = 1000)
    )
    @Override
    public FoodDto getFoodById(String foodId) {
        log.debug("Fetching food details for foodId={}", foodId);
        return foodClient.getFoodById(foodId);
    }
}
