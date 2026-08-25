package com.ghlove.point.event;

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

    public void publishPointDeducted(String orderId, Long userId, Long pointAmount) {
        send(orderId, new PointDeductedEvent(orderId, userId, pointAmount, LocalDateTime.now()), "POINT_DEDUCTED");
    }

    public void publishPointDeductFailed(String orderId, Long userId, String reason) {
        send(orderId, new PointDeductFailedEvent(orderId, userId, reason, LocalDateTime.now()), "POINT_DEDUCT_FAILED");
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
