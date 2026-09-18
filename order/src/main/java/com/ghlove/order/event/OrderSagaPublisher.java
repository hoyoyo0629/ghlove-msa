package com.ghlove.order.event;

import com.ghlove.order.domain.Claim;
import com.ghlove.order.domain.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

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
 *
 * <p>모든 발행은 호출 트랜잭션이 커밋된 뒤로 미뤄진다({@link #send}). 장바구니 체크아웃처럼
 * 한 트랜잭션 안에서 주문을 여러 건 만드는 경로에서, 중간에 실패해 롤백되면 DB에는 주문이
 * 하나도 남지 않는데 앞서 만든 주문의 ORDER_CREATED만 브로커에 나가 gift가 재고를
 * 예약하고 point가 포인트를 차감하는 "유령 이벤트"가 생겼기 때문이다 (대응하는 주문 행이
 * 없어 보상 이벤트조차 발생하지 않는 영구 누수).
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

    /** 트랜잭션이 열려 있으면 커밋 직후에, 없으면 즉시 발행한다 (롤백되면 아예 발행하지 않는다). */
    private void send(String key, Object event, String eventType) {
        ProducerRecord<String, Object> record = new ProducerRecord<>(TOPIC, null, key, event,
                List.of(new RecordHeader(HEADER_EVENT_TYPE, eventType.getBytes(StandardCharsets.UTF_8))));

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    doSend(record, key, eventType);
                }
            });
            return;
        }
        doSend(record, key, eventType);
    }

    private void doSend(ProducerRecord<String, Object> record, String key, String eventType) {
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
