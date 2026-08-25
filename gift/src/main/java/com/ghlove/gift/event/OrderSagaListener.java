package com.ghlove.gift.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ghlove.gift.service.GiftService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * Consumes order.saga for the two event types this service cares about
 * (ORDER_CREATED -> try to reserve stock, ORDER_CANCELLED -> restore if we
 * reserved). All other event types on this topic (published by order/point)
 * are ignored.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderSagaListener {

    private final GiftService giftService;
    private final OrderSagaPublisher orderSagaPublisher;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = OrderSagaPublisher.TOPIC, groupId = "gift-service")
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
                boolean reserved = giftService.reserveStockForOrder(event.orderId(), event.itemId(), event.quantity());
                if (reserved) {
                    orderSagaPublisher.publishStockReserved(event.orderId(), event.itemId(), event.quantity());
                } else {
                    orderSagaPublisher.publishStockReserveFailed(event.orderId(), event.itemId(), "재고 부족");
                }
            }
            case "ORDER_CANCELLED" -> {
                OrderCancelledEvent event = objectMapper.readValue(payload, OrderCancelledEvent.class);
                log.info("Received OrderCancelledEvent: {}", event);
                giftService.restoreStockForOrder(event.orderId());
            }
            default -> { /* not relevant to gift service */ }
        }
    }
}
