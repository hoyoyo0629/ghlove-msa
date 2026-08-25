package com.ghlove.order.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ghlove.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * Consumes order.saga for the event types gift/point publish in response to
 * this service's own ORDER_CREATED/ORDER_CANCELLED events. Ignores
 * ORDER_CREATED/ORDER_CONFIRMED/ORDER_CANCELLED, since those are the ones
 * THIS service publishes, not ones it needs to react to.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderSagaListener {

    private final OrderService orderService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = OrderSagaPublisher.TOPIC, groupId = "order-service")
    public void onEvent(String payload, @Header(name = OrderSagaPublisher.HEADER_EVENT_TYPE, required = false) byte[] eventTypeHeader,
                         @Header(KafkaHeaders.RECEIVED_KEY) String orderId) throws Exception {
        String eventType = eventTypeHeader != null ? new String(eventTypeHeader) : null;
        if (eventType == null) {
            log.warn("Received order.saga message for orderId={} with no eventType header - ignoring", orderId);
            return;
        }

        switch (eventType) {
            case "STOCK_RESERVED" -> orderService.onStockReserved(objectMapper.readValue(payload, StockReservedEvent.class));
            case "STOCK_RESERVE_FAILED" -> orderService.onStockReserveFailed(objectMapper.readValue(payload, StockReserveFailedEvent.class));
            case "POINT_DEDUCTED" -> orderService.onPointDeducted(objectMapper.readValue(payload, PointDeductedEvent.class));
            case "POINT_DEDUCT_FAILED" -> orderService.onPointDeductFailed(objectMapper.readValue(payload, PointDeductFailedEvent.class));
            case "ORDER_CREATED", "ORDER_CONFIRMED", "ORDER_CANCELLED" -> { /* published by this service - not consumed here */ }
            default -> log.warn("Unknown eventType={} for orderId={} - ignoring", eventType, orderId);
        }
    }
}
