package com.ghlove.admin.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ghlove.admin.service.OpenApiUsageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/** Consumes open-api.usage (gift 서비스가 발행) purely to build admin의 오픈API 통계 ReadModel - never publishes anything back. */
@Component
@RequiredArgsConstructor
@Slf4j
public class OpenApiUsageListener {

    private final OpenApiUsageService openApiUsageService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "open-api.usage", groupId = "admin-service")
    public void onEvent(String payload) throws Exception {
        openApiUsageService.onEvent(objectMapper.readValue(payload, OpenApiUsageEvent.class));
    }
}
