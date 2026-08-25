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
 * Consumes order.saga for the two event types this service cares about
 * (ORDER_CREATED -> try to deduct points, ORDER_CANCELLED -> restore if we
 * deducted). All other event types on this topic (published by order/gift)
 * are ignored.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderSagaListener {

    private final PointService pointService;
    private final OrderSagaPublisher orderSagaPublisher;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = OrderSagaPublisher.TOPIC, groupId = "point-service")
    public void onEvent(String payload, @Header(name = OrderSagaPublisher.HEADER_EVENT_TYPE, required = false) byte[] eventTypeHeader,
                         @Header(KafkaHeaders.RECEIVED_KEY) String orderId) throws Exception {
        String eventType = eventTypeHeader != null ? new String(eventTypeHeader) : null;
        if (eventType == null) {
            return;
        }

        switch (eventType) {
            case "ORDER_CREATED" -> {
                OrderCreatedEvent event = objectMapper.readValue(payload, OrderCreatedEvent.class);
                log.info("Received OrderCreatedEvent: {}", event);
                boolean deducted = pointService.deductForOrder(event.orderId(), event.userId(), event.pointAmount(), event.locgovCode());
                if (deducted) {
                    orderSagaPublisher.publishPointDeducted(event.orderId(), event.userId(), event.pointAmount());
                } else {
                    orderSagaPublisher.publishPointDeductFailed(event.orderId(), event.userId(), "포인트 부족");
                }
            }
            case "ORDER_CANCELLED" -> {
                OrderCancelledEvent event = objectMapper.readValue(payload, OrderCancelledEvent.class);
                log.info("Received OrderCancelledEvent: {}", event);
                pointService.restoreForOrder(event.orderId());
            }
            default -> { /* not relevant to point service */ }
        }
    }
}
