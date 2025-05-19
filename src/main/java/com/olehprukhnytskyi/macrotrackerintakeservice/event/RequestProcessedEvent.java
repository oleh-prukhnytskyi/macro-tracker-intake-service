package com.olehprukhnytskyi.macrotrackerintakeservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RequestProcessedEvent {
    private String requestKey;
}
