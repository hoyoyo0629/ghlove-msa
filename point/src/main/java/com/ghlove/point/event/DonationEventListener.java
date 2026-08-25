package com.ghlove.point.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ghlove.point.service.PointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * Consumes donation service's events from a single topic keyed by cntrSn, so
 * COMPLETED and a later CANCELLED for the same donation are guaranteed to
 * arrive in order (Kafka only orders messages within a partition for a given
 * key - splitting these across two topics let a CANCELLED be observed before
 * its COMPLETED in practice). The event type travels as a header rather than
 * a payload field, and the payload itself is read as a raw JSON string and
 * parsed manually (see application.yml) since donation and point don't share
 * Java event classes across the service boundary.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DonationEventListener {

    private static final String HEADER_EVENT_TYPE = "eventType";

    private final PointService pointService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "donation.lifecycle", groupId = "point-service")
    public void onDonationEvent(String payload, @Header(name = HEADER_EVENT_TYPE, required = false) byte[] eventTypeHeader,
                                 @Header(KafkaHeaders.RECEIVED_KEY) String cntrSn) throws Exception {
        String eventType = eventTypeHeader != null ? new String(eventTypeHeader) : null;
        if (eventType == null) {
            log.warn("Received donation.lifecycle message for cntrSn={} with no eventType header - ignoring", cntrSn);
            return;
        }

        switch (eventType) {
            case "COMPLETED" -> {
                DonationCompletedEvent event = objectMapper.readValue(payload, DonationCompletedEvent.class);
                log.info("Received DonationCompletedEvent: {}", event);
                pointService.creditForDonation(event);
            }
            case "CANCELLED" -> {
                DonationCancelledEvent event = objectMapper.readValue(payload, DonationCancelledEvent.class);
                log.info("Received DonationCancelledEvent: {}", event);
                pointService.reverseForDonation(event);
            }
            default -> log.warn("Unknown eventType={} for cntrSn={} - ignoring", eventType, cntrSn);
        }
    }
}
