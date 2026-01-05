package com.olehprukhnytskyi.macrotrackerintakeservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.olehprukhnytskyi.macrotrackerintakeservice.config.AbstractIntegrationTest;
import com.olehprukhnytskyi.macrotrackerintakeservice.dto.CacheablePage;
import com.olehprukhnytskyi.macrotrackerintakeservice.dto.FoodDto;
import com.olehprukhnytskyi.macrotrackerintakeservice.dto.IntakeRequestDto;
import com.olehprukhnytskyi.macrotrackerintakeservice.dto.IntakeResponseDto;
import com.olehprukhnytskyi.macrotrackerintakeservice.dto.NutrimentsDto;
import com.olehprukhnytskyi.macrotrackerintakeservice.dto.UpdateIntakeRequestDto;
import com.olehprukhnytskyi.macrotrackerintakeservice.model.Intake;
import com.olehprukhnytskyi.macrotrackerintakeservice.model.Nutriments;
import com.olehprukhnytskyi.macrotrackerintakeservice.repository.jpa.IntakeRepository;
import com.olehprukhnytskyi.util.IntakePeriod;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

class IntakeServiceCacheTest extends AbstractIntegrationTest {
    @MockitoBean
    private FoodClientService foodClientService;
    @MockitoSpyBean
    private IntakeRepository intakeRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private IntakeService intakeService;
    @Autowired
    private ObjectMapper objectMapper;

    private Long intakeId;

    @BeforeEach
    void setUp() {
        intakeRepository.deleteAll();

        FoodDto foodDto = FoodDto.builder()
                .nutriments(NutrimentsDto.builder()
                        .calories(BigDecimal.valueOf(100))
                        .carbohydrates(BigDecimal.valueOf(20))
                        .fat(BigDecimal.valueOf(10))
                        .protein(BigDecimal.valueOf(5))
                        .build())
                .build();
        when(foodClientService.getFoodById(anyString())).thenReturn(foodDto);

        Nutriments nutriments = Nutriments.builder()
                .calories(BigDecimal.valueOf(200))
                .carbohydrates(BigDecimal.valueOf(40))
                .fat(BigDecimal.valueOf(20))
                .protein(BigDecimal.valueOf(10))
                .caloriesPer100(BigDecimal.valueOf(100))
                .carbohydratesPer100(BigDecimal.valueOf(20))
                .fatPer100(BigDecimal.valueOf(10))
                .proteinPer100(BigDecimal.valueOf(5))
                .build();

        Intake intake = Intake.builder()
                .date(LocalDate.now())
                .userId(1L)
                .foodName("Rice")
                .foodId("00000000")
                .nutriments(nutriments)
                .amount(200)
                .intakePeriod(IntakePeriod.SNACK)
                .build();
        intakeId = intakeRepository.save(intake).getId();
    }

    @Test
    @DisplayName("Should clear cache")
    void save_shouldClearCache() {
        // Given
        Long userId = 1L;
        LocalDate today = LocalDate.now();

        Pageable pageable = PageRequest.of(0, 20);
        String cacheKey = "user:intakes::" + userId + ":date:"
                + today + ":page:" + pageable.getPageNumber();

        redisTemplate.opsForValue().set(cacheKey, "cached_value");
        assertThat(redisTemplate.hasKey(cacheKey)).isTrue();

        IntakeRequestDto intakeRequestDto = IntakeRequestDto.builder()
                .amount(100)
                .date(LocalDate.now())
                .intakePeriod(IntakePeriod.SNACK)
                .foodId("12345678").build();

        // When
        intakeService.save(intakeRequestDto, userId);

        // Then
        assertThat(redisTemplate.hasKey(cacheKey)).isFalse();
    }

    @Test
    @DisplayName("Should use cache on second call")
    void findByDate_shouldUseCacheOnSecondCall() {
        // Given
        Long userId = 1L;
        Pageable pageable = Pageable.ofSize(20);

        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // When
        CacheablePage<IntakeResponseDto> intakes1 = intakeService
                .findByDate(null, userId, pageable);
        verify(intakeRepository, times(1)).findByUserId(eq(userId), any());
        verify(intakeRepository, never()).findByUserIdAndDate(anyLong(), any(), any());

        Object cached1 = redisTemplate.opsForValue().get("user:intakes::user:"
                + userId + ":all:page:0");
        assertThat(cached1).isNotNull();

        CacheablePage<IntakeResponseDto> intakes2 = objectMapper
                .convertValue(cached1, new TypeReference<>() { });
        verify(intakeRepository, times(1)).findByUserId(eq(userId), any());
        verify(intakeRepository, never()).findByUserIdAndDate(anyLong(), any(), any());

        // Then
        assertThat(intakes1.getTotalElements()).isEqualTo(intakes2.getTotalElements());
    }

    @Test
    @DisplayName("Should clear cache")
    void update_shouldClearCache() {
        // Given
        Long userId = 1L;
        LocalDate today = LocalDate.now();

        Pageable pageable = PageRequest.of(0, 20);
        String cacheKey = "user:intakes::user:" + userId + ":date:" + today
                + ":page:" + pageable.getPageNumber();

        redisTemplate.opsForValue().set(cacheKey, "cached_value");
        assertThat(redisTemplate.hasKey(cacheKey)).isTrue();

        UpdateIntakeRequestDto intakeRequest = UpdateIntakeRequestDto.builder()
                .amount(200)
                .build();

        // When
        intakeService.update(intakeId, intakeRequest, userId);

        // Then
        assertThat(redisTemplate.hasKey(cacheKey)).isFalse();
    }

    @Test
    @DisplayName("Should clear cache")
    void deleteById_shouldClearCache() {
        // Given
        Long id = 1L;
        Long userId = 1L;
        LocalDate today = LocalDate.now();

        Pageable pageable = PageRequest.of(0, 20);
        String cacheKey = "user:intakes::user:" + userId + ":date:" + today
                + ":page:" + pageable.getPageNumber();

        redisTemplate.opsForValue().set(cacheKey, "cached_value");
        assertThat(redisTemplate.hasKey(cacheKey)).isTrue();

        // When
        intakeService.deleteById(id, userId);

        // Then
        assertThat(redisTemplate.hasKey(cacheKey)).isFalse();
    }

    @Test
    @DisplayName("Should clear cache")
    void deleteUserIntakesRecursively_shouldClearCache() {
        // Given
        Long userId = 1L;
        LocalDate today = LocalDate.now();

        Pageable pageable = PageRequest.of(0, 20);
        String cacheKey = "user:intakes::user:" + userId + ":date:"
                + today + ":page:" + pageable.getPageNumber();

        redisTemplate.opsForValue().set(cacheKey, "cached_value");
        assertThat(redisTemplate.hasKey(cacheKey)).isTrue();

        // When
        intakeService.deleteUserIntakesRecursively(userId);

        // Then
        assertThat(redisTemplate.hasKey(cacheKey)).isFalse();
    }
}
