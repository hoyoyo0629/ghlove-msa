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
            // 멀티아이템(출고 단위 SAGA) - 출고(지자체) 단위 포인트 차감/복원
            case "SHIPMENT_CREATED" -> {
                ShipmentCreatedEvent event = objectMapper.readValue(payload, ShipmentCreatedEvent.class);
                log.info("Received ShipmentCreatedEvent: {}", event);
                long amount = event.groupPointAmount() != null ? event.groupPointAmount() : 0L;
                boolean deducted = pointService.deductForShipment(event.shipmentId(), event.orderId(),
                        event.userId(), amount, event.locgovCode(), event.lines());
                if (deducted) {
                    orderSagaPublisher.publishShipmentPointDeducted(event.shipmentId(), event.orderId());
                } else {
                    orderSagaPublisher.publishShipmentPointDeductFailed(event.shipmentId(), event.orderId(), "포인트 부족");
                }
            }
            case "SHIPMENT_CANCELLED" -> {
                ShipmentCancelledEvent event = objectMapper.readValue(payload, ShipmentCancelledEvent.class);
                log.info("Received ShipmentCancelledEvent: {}", event);
                pointService.restoreForShipment(event.shipmentId(), event.orderId(), event.orderItemIds());
            }
            case "ITEM_CANCELLED" -> {
                ItemCancelledEvent event = objectMapper.readValue(payload, ItemCancelledEvent.class);
                log.info("Received ItemCancelledEvent: {}", event);
                pointService.restoreForItem(event.orderId(), event.orderItemId());
            }
            default -> { /* not relevant to point service */ }
        }
    }
}
