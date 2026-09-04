package com.ghlove.gift.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

/** SFR-010 "민간개방 API 기능... 관련 통계" - Kong key-auth를 통과한 민간 API 호출을 admin의
 *  통계 ReadModel(OPEN_API_USAGE_STAT)에 실어보낸다. 다른 서비스들이 각자 상태 스냅샷을
 *  admin 통계로 보내는 것과 같은 패턴(gift.lifecycle/point.ledger 참고) - 이벤트 기반, 동기 호출 없음. */
@Component
@RequiredArgsConstructor
@Slf4j
public class OpenApiUsagePublisher {

    public static final String TOPIC = "open-api.usage";
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publish(String consumerUsername, String endpoint) {
        OpenApiUsageEvent event = new OpenApiUsageEvent(consumerUsername, "gift", endpoint,
                FORMAT.format(LocalDateTime.now()));
        kafkaTemplate.send(TOPIC, consumerUsername, event).whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish OpenApiUsageEvent for consumer={}", consumerUsername, ex);
            }
        });
    }
}
