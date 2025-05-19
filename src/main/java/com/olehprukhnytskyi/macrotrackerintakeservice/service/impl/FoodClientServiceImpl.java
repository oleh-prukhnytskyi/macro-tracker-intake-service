package com.olehprukhnytskyi.macrotrackerintakeservice.service.impl;

import com.olehprukhnytskyi.macrotrackerintakeservice.client.FoodClient;
import com.olehprukhnytskyi.macrotrackerintakeservice.dto.FoodDto;
import com.olehprukhnytskyi.macrotrackerintakeservice.service.FoodClientService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

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
        return foodClient.getFoodById(foodId);
    }
}
