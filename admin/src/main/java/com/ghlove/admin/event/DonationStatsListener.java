package com.ghlove.admin.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ghlove.admin.service.StatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/** Consumes donation.lifecycle purely to build admin's local stats ReadModel - never publishes anything back. */
@Component
@RequiredArgsConstructor
@Slf4j
public class DonationStatsListener {

    private static final String HEADER_EVENT_TYPE = "eventType";

    private final StatsService statsService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "donation.lifecycle", groupId = "admin-service")
    public void onEvent(String payload, @Header(name = HEADER_EVENT_TYPE, required = false) byte[] eventTypeHeader) throws Exception {
        String eventType = eventTypeHeader != null ? new String(eventTypeHeader) : null;
        if (eventType == null) {
            return;
        }
        switch (eventType) {
            case "COMPLETED" -> statsService.onDonationCompleted(objectMapper.readValue(payload, DonationCompletedEvent.class));
            case "CANCELLED" -> statsService.onDonationCancelled(objectMapper.readValue(payload, DonationCancelledEvent.class));
            default -> { /* ignore */ }
        }
    }
}
