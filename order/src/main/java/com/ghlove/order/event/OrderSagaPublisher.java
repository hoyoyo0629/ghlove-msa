package com.ghlove.order.event;

import com.ghlove.order.domain.Claim;
import com.ghlove.order.domain.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

/**
 * All order.saga events for a given order share this ONE topic, keyed by
 * orderId, with the event type carried as a Kafka header - required so that
 * every event for the same order lands in the same partition and is
 * processed in publish order by any one consumer (see the ordering bug found
 * and fixed in donation/point's event design: splitting an aggregate's
 * lifecycle events across multiple topics breaks cross-topic ordering).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderSagaPublisher {

    public static final String TOPIC = "order.saga";
    public static final String HEADER_EVENT_TYPE = "eventType";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishCreated(Order order) {
        OrderCreatedEvent event = new OrderCreatedEvent(
                order.getOrderId(), order.getUserId(), order.getItemId(), order.getSellerId(),
                order.getQuantity(), order.getUnitPrice(), order.getPointAmount(), order.getLocgovCode(),
                order.getItemName(), order.getReceiverName(), LocalDateTime.now());
        send(order.getOrderId(), event, "ORDER_CREATED");
    }

    public void publishConfirmed(Order order) {
        send(order.getOrderId(), new OrderConfirmedEvent(order.getOrderId(), LocalDateTime.now()), "ORDER_CONFIRMED");
    }

    public void publishCancelled(Order order) {
        send(order.getOrderId(), new OrderCancelledEvent(order.getOrderId(), order.getCancelReason(), LocalDateTime.now()),
                "ORDER_CANCELLED");
    }

    /** SFR-006 SLA ReadModel - 송장등록/배송상태갱신/구매확정 3곳 전부 이 한 메서드로 통일해서 부른다. */
    public void publishDeliveryUpdated(Order order) {
        OrderDeliveryUpdatedEvent event = new OrderDeliveryUpdatedEvent(order.getOrderId(), order.getDeliveryStatus(),
                order.getShippedDate(), order.getDeliveredDate(), order.getConfirmedDate(), LocalDateTime.now());
        send(order.getOrderId(), event, "ORDER_DELIVERY_UPDATED");
    }

    /** SFR-006 재검토 라운드 - 클레임사건/상태뷰 ReadModel gap fill. */
    public void publishClaimUpdated(Claim claim, Order order) {
        OrderClaimUpdatedEvent event = new OrderClaimUpdatedEvent(claim.getClaimId(), claim.getOrderId(),
                claim.getClaimType(), claim.getReason(), claim.getStatus(), claim.getCreatedDate(),
                claim.getProcessedDate(), order.getItemName(), order.getLocgovCode(), order.getUserId(),
                order.getReceiverName());
        send(claim.getOrderId(), event, "ORDER_CLAIM_UPDATED");
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
