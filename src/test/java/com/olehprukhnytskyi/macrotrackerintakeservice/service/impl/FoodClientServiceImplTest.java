package com.olehprukhnytskyi.macrotrackerintakeservice.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.olehprukhnytskyi.macrotrackerintakeservice.client.FoodClient;
import com.olehprukhnytskyi.macrotrackerintakeservice.dto.FoodDto;
import feign.FeignException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FoodClientServiceImplTest {
    @Mock
    private FoodClient foodClient;

    @InjectMocks
    private FoodClientServiceImpl foodClientService;

    @Test
    @DisplayName("When food exists, should return DTO")
    void getFoodById_whenFoodExists_shouldReturnDto() {
        // Given
        String foodId = "123";
        FoodDto expectedFood = new FoodDto();
        expectedFood.setId(foodId);
        expectedFood.setProductName("Pizza");

        when(foodClient.getFoodById(foodId)).thenReturn(expectedFood);

        // When
        FoodDto actualFood = foodClientService.getFoodById(foodId);

        // Then
        assertEquals(expectedFood, actualFood);
        verify(foodClient).getFoodById(foodId);
    }

    @Test
    @DisplayName("When FeignException occurs, should retry and throw exception after retries")
    void getFoodById_whenFeignException_shouldRetryAndThrow() {
        // Given
        String foodId = "123";
        when(foodClient.getFoodById(foodId)).thenThrow(FeignException.class);

        // When & Then
        assertThrows(FeignException.class, () -> foodClientService.getFoodById(foodId));
        verify(foodClient, Mockito.atLeast(1)).getFoodById(foodId);
    }
}
