package com.ghlove.admin.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ghlove.admin.service.StatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/** Consumes order.saga purely to build admin's local stats ReadModel - never publishes anything back. */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderStatsListener {

    private static final String HEADER_EVENT_TYPE = "eventType";

    private final StatsService statsService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "order.saga", groupId = "admin-service")
    public void onEvent(String payload, @Header(name = HEADER_EVENT_TYPE, required = false) byte[] eventTypeHeader) throws Exception {
        String eventType = eventTypeHeader != null ? new String(eventTypeHeader) : null;
        if (eventType == null) {
            return;
        }
        switch (eventType) {
            case "ORDER_CREATED" -> statsService.onOrderCreated(objectMapper.readValue(payload, OrderCreatedEvent.class));
            case "ORDER_CONFIRMED" -> statsService.onOrderConfirmed(objectMapper.readValue(payload, OrderConfirmedEvent.class));
            case "ORDER_CANCELLED" -> statsService.onOrderCancelled(objectMapper.readValue(payload, OrderCancelledEvent.class));
            case "ORDER_DELIVERY_UPDATED" -> statsService.onOrderDeliveryUpdated(objectMapper.readValue(payload, OrderDeliveryUpdatedEvent.class));
            default -> { /* STOCK_/POINT_ events not relevant to stats */ }
        }
    }
}
