package com.olehprukhnytskyi.macrotrackerintakeservice.service;

import com.olehprukhnytskyi.macrotrackerintakeservice.dto.FoodDto;

public interface FoodClientService {
    FoodDto getFoodById(String foodId);
}
