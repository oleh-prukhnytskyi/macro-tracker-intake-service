package com.olehprukhnytskyi.macrotrackerintakeservice.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.olehprukhnytskyi.event.UserDeletedEvent;
import com.olehprukhnytskyi.exception.EventProcessingException;
import com.olehprukhnytskyi.exception.error.EventErrorCode;
import com.olehprukhnytskyi.macrotrackerintakeservice.service.IntakeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserEventConsumer {
    private final IntakeService intakeService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "user-deleted", groupId = "intake-service")
    public void handleUserDeleted(String message) {
        UserDeletedEvent event;
        try {
            event = objectMapper.readValue(message, UserDeletedEvent.class);
        } catch (JsonProcessingException e) {
            log.error("Invalid user-deleted event payload. Message: {}", message, e);
            throw new EventProcessingException(EventErrorCode.EVENT_DESERIALIZATION_FAILED,
                    "Failed to parse user-deleted event", e);
        }
        try {
            log.info("Processing user-deleted event for userId={}", event.getUserId());
            intakeService.deleteAllByUserId(event.getUserId());
            log.info("Successfully deleted all intakes for userId={}", event.getUserId());
        } catch (Exception e) {
            log.error("Error processing user-deleted event for userId={}", event.getUserId(), e);
            throw new EventProcessingException(EventErrorCode.KAFKA_PROCESSING_ERROR,
                    "Failed to process user-deleted event", e);
        }
    }
}
