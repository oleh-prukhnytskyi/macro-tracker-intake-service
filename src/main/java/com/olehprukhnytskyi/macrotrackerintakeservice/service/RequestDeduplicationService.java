package com.olehprukhnytskyi.macrotrackerintakeservice.service;

import com.olehprukhnytskyi.macrotrackerintakeservice.util.ProcessedEntityType;

public interface RequestDeduplicationService {
    String buildRequestKey(ProcessedEntityType type, String requestId, Long userId);

    boolean isProcessed(ProcessedEntityType type, String requestId, Long userId);

    void markAsProcessed(ProcessedEntityType type, String requestId, Long userId);
}
