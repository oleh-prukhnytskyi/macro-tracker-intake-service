package com.olehprukhnytskyi.macrotrackerintakeservice.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.olehprukhnytskyi.macrotrackerintakeservice.event.UserDeletedEvent;
import com.olehprukhnytskyi.macrotrackerintakeservice.service.IntakeService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserEventConsumer {
    private final IntakeService intakeService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "user-deleted", groupId = "intake-service")
    public void handleUserDeleted(String message) {
        try {
            UserDeletedEvent event = objectMapper.readValue(message, UserDeletedEvent.class);
            intakeService.deleteAllByUserId(event.getUserId());
        } catch (Exception e) {
            System.err.println("Kafka error: " + e.getMessage());
        }
    }
}
