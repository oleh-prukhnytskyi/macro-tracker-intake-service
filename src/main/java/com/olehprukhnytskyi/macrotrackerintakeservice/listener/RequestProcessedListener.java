package com.olehprukhnytskyi.macrotrackerintakeservice.listener;

import com.olehprukhnytskyi.macrotrackerintakeservice.event.RequestProcessedEvent;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class RequestProcessedListener {
    private final RedisTemplate<String, String> redisTemplate;

    @EventListener
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(RequestProcessedEvent event) {
        redisTemplate.opsForValue().set(event.getRequestKey(), "1", 1, TimeUnit.HOURS);
    }
}
