package com.ghlove.admin.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ghlove.admin.service.StatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/** Consumes point.ledger purely to build admin's local 포인트 통계 ReadModel - never publishes anything back. */
@Component
@RequiredArgsConstructor
@Slf4j
public class PointStatsListener {

    private final StatsService statsService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "point.ledger", groupId = "admin-service")
    public void onEvent(String payload) throws Exception {
        statsService.onPointLedgerEvent(objectMapper.readValue(payload, PointLedgerEvent.class));
    }
}
