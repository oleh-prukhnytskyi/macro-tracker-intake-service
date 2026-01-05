package com.olehprukhnytskyi.macrotrackerintakeservice.client;

import com.olehprukhnytskyi.macrotrackerintakeservice.dto.FoodDto;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "food-service", url = "${feign.food-service:http://localhost:8081}")
public interface FoodClient {
    @GetMapping("/api/foods/{foodId}")
    FoodDto getFoodById(@PathVariable String foodId);

    @PostMapping("/api/foods/batch")
    List<FoodDto> getFoodsByIds(@RequestBody List<String> foodId);
}
