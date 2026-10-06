package com.ghlove.gift.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderSagaPublisher {

    public static final String TOPIC = "order.saga";
    public static final String HEADER_EVENT_TYPE = "eventType";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishStockReserved(String orderId, Long itemId, Integer quantity) {
        send(orderId, new StockReservedEvent(orderId, itemId, quantity, LocalDateTime.now()), "STOCK_RESERVED");
    }

    public void publishStockReserveFailed(String orderId, Long itemId, String reason) {
        send(orderId, new StockReserveFailedEvent(orderId, itemId, reason, LocalDateTime.now()), "STOCK_RESERVE_FAILED");
    }

    // ── 멀티아이템(출고 단위 SAGA) - key=orderId(한 주문의 출고 이벤트 순서 보존) ──
    public void publishShipmentStockReserved(Long shipmentId, String orderId) {
        send(orderId, new ShipmentStockReservedEvent(shipmentId, orderId, LocalDateTime.now()), "SHIPMENT_STOCK_RESERVED");
    }

    public void publishShipmentStockReserveFailed(Long shipmentId, String orderId, String reason) {
        send(orderId, new ShipmentStockReserveFailedEvent(shipmentId, orderId, reason, LocalDateTime.now()),
                "SHIPMENT_STOCK_RESERVE_FAILED");
    }

    private void send(String key, Object event, String eventType) {
        ProducerRecord<String, Object> record = new ProducerRecord<>(TOPIC, null, key, event,
                List.of(new RecordHeader(HEADER_EVENT_TYPE, eventType.getBytes(StandardCharsets.UTF_8))));

        kafkaTemplate.send(record).whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish {} event for orderId={}", eventType, key, ex);
            } else {
                log.info("Published {} event orderId={} to partition={} offset={}",
                        eventType, key, result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
            }
        });
    }
}
