package com.olehprukhnytskyi.macrotrackerintakeservice.service;

import com.olehprukhnytskyi.macrotrackerintakeservice.client.FoodClient;
import com.olehprukhnytskyi.macrotrackerintakeservice.dto.FoodDto;
import feign.FeignException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FoodClientService {
    private final FoodClient foodClient;

    @Retryable(
            retryFor = FeignException.class,
            backoff = @Backoff(delay = 1000)
    )
    public FoodDto getFoodById(String foodId) {
        log.debug("Fetching food details for foodId={}", foodId);
        return foodClient.getFoodById(foodId);
    }

    @Retryable(
            retryFor = FeignException.class,
            backoff = @Backoff(delay = 1000)
    )
    public List<FoodDto> getFoodsByIds(List<String> foodIds) {
        return foodClient.getFoodsByIds(foodIds);
    }
}
