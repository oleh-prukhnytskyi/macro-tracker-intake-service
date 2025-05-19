package com.olehprukhnytskyi.macrotrackerintakeservice.client;

import com.olehprukhnytskyi.macrotrackerintakeservice.dto.FoodDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "food-service", url = "${feign.goal-service:http://localhost:8081}")
public interface FoodClient {
    @GetMapping("/api/foods/{foodId}")
    FoodDto getFoodById(@PathVariable String foodId);
}
