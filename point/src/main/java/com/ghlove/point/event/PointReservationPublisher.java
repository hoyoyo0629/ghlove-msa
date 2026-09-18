package com.ghlove.point.event;

import com.ghlove.point.domain.PointReservation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 포인트 예약/해제 이벤트 발행 (ISP 도메인 이벤트 "포인트예약됨/해제됨").
 * 구독자를 강제하진 않지만, ISP가 포인트관리의 인터페이스로 "포인트이벤트토픽"을 명시하고
 * 있어 예약 상태 변화도 토픽으로 나가야 다른 서비스가 붙을 수 있다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PointReservationPublisher {

    public static final String TOPIC = "point.reservation";
    public static final String HEADER_EVENT_TYPE = "eventType";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishReserved(PointReservation reservation) {
        send(reservation, "POINT_RESERVED");
    }

    public void publishReleased(PointReservation reservation) {
        send(reservation, "POINT_RESERVATION_RELEASED");
    }

    private void send(PointReservation reservation, String eventType) {
        PointReservationEvent event = new PointReservationEvent(
                reservation.getReservationId(), reservation.getUserId(), reservation.getLocgovCode(),
                reservation.getAmount(), reservation.getStatus(), reservation.getRefKey(), LocalDateTime.now());
        var message = MessageBuilder.withPayload(event)
                .setHeader(KafkaHeaders.TOPIC, TOPIC)
                .setHeader(KafkaHeaders.KEY, String.valueOf(reservation.getUserId()))
                .setHeader(HEADER_EVENT_TYPE, eventType)
                .build();
        kafkaTemplate.send(message).whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish {} for reservationId={}", eventType, reservation.getReservationId(), ex);
            } else {
                log.info("Published {} reservationId={}", eventType, reservation.getReservationId());
            }
        });
    }
}
