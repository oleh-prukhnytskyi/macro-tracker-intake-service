package com.olehprukhnytskyi.macrotrackerintakeservice.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.olehprukhnytskyi.macrotrackerintakeservice.util.ProcessedEntityType;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@SuppressWarnings("unchecked")
class RequestDeduplicationServiceImplTest {
    private RequestDeduplicationServiceImpl service;
    private RedisTemplate<String, String> redisTemplate;
    private ValueOperations<String, String> valueOperations;

    @BeforeEach
    void setUp() {
        redisTemplate = mock(RedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        service = new RequestDeduplicationServiceImpl(redisTemplate);
    }

    @Test
    @DisplayName("buildRequestKey should format key correctly")
    void buildRequestKey_whenCalled_shouldReturnFormattedKey() {
        // Given
        ProcessedEntityType type = ProcessedEntityType.INTAKE;
        String requestId = "req123";
        Long userId = 42L;

        // When
        String key = service.buildRequestKey(type, requestId, userId);

        // Then
        assertEquals("processed:intake:42:req123", key);
    }

    @Test
    @DisplayName("isProcessed should return true when redis has key")
    void isProcessed_whenRedisHasKey_shouldReturnTrue() {
        // Given
        ProcessedEntityType type = ProcessedEntityType.INTAKE;
        String requestId = "req123";
        Long userId = 42L;
        String key = service.buildRequestKey(type, requestId, userId);

        when(redisTemplate.hasKey(key)).thenReturn(true);

        // When
        boolean result = service.isProcessed(type, requestId, userId);

        // Then
        assertTrue(result);
        verify(redisTemplate).hasKey(key);
    }

    @Test
    @DisplayName("isProcessed should return false when redis does not have key")
    void isProcessed_whenRedisDoesNotHaveKey_shouldReturnFalse() {
        // Given
        ProcessedEntityType type = ProcessedEntityType.INTAKE;
        String requestId = "req123";
        Long userId = 42L;
        String key = service.buildRequestKey(type, requestId, userId);

        when(redisTemplate.hasKey(key)).thenReturn(false);

        // When
        boolean result = service.isProcessed(type, requestId, userId);

        // Then
        assertFalse(result);
        verify(redisTemplate).hasKey(key);
    }

    @Test
    @DisplayName("markAsProcessed should set key with expiry")
    void markAsProcessed_whenCalled_shouldSetKeyWithExpiry() {
        // Given
        ProcessedEntityType type = ProcessedEntityType.INTAKE;
        String requestId = "req123";
        Long userId = 42L;
        String key = service.buildRequestKey(type, requestId, userId);

        // When
        service.markAsProcessed(type, requestId, userId);

        // Then
        verify(valueOperations).set(eq(key), eq("1"), eq(1L), eq(TimeUnit.HOURS));
    }
}
