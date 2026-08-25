package com.ghlove.admin.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ghlove.admin.service.StatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/** Consumes gift.lifecycle purely to build admin's local 답례품 통계 ReadModel - never publishes anything back. */
@Component
@RequiredArgsConstructor
@Slf4j
public class GiftStatsListener {

    private final StatsService statsService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "gift.lifecycle", groupId = "admin-service")
    public void onEvent(String payload) throws Exception {
        statsService.onGiftLifecycleEvent(objectMapper.readValue(payload, GiftLifecycleEvent.class));
    }
}
