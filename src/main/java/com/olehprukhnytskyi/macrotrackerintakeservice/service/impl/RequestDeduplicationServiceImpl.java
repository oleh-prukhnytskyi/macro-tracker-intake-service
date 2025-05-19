package com.olehprukhnytskyi.macrotrackerintakeservice.service.impl;

import com.olehprukhnytskyi.macrotrackerintakeservice.service.RequestDeduplicationService;
import com.olehprukhnytskyi.macrotrackerintakeservice.util.ProcessedEntityType;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RequestDeduplicationServiceImpl implements RequestDeduplicationService {
    private final RedisTemplate<String, String> redisTemplate;

    public String buildRequestKey(ProcessedEntityType type, String requestId, Long userId) {
        return String.format("processed:%s:%d:%s", type.name().toLowerCase(), userId, requestId);
    }

    public boolean isProcessed(ProcessedEntityType type, String requestId, Long userId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(buildRequestKey(type, requestId, userId)));
    }

    public void markAsProcessed(ProcessedEntityType type, String requestId, Long userId) {
        redisTemplate.opsForValue()
                .set(buildRequestKey(type, requestId, userId), "1", 1, TimeUnit.HOURS);
    }
}
