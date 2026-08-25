package com.ghlove.gift.event;

import com.ghlove.gift.domain.Gift;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/** 답례품 통계 ReadModel용 이벤트 발행 (admin이 DB per Service라 직접 못 읽으므로 - SFR-007/009 gap fill). */
@Component
@RequiredArgsConstructor
@Slf4j
public class GiftLifecyclePublisher {

    public static final String TOPIC = "gift.lifecycle";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publish(Gift gift) {
        GiftLifecycleEvent event = new GiftLifecycleEvent(gift.getItemId(), gift.getSellerId(),
                gift.getCategoryCode(), gift.getDataStatusCode(), gift.getSalePrice(), gift.getLocgovCode());
        kafkaTemplate.send(TOPIC, String.valueOf(gift.getItemId()), event).whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish GiftLifecycleEvent for itemId={}", gift.getItemId(), ex);
            } else {
                log.info("Published GiftLifecycleEvent itemId={} status={}", gift.getItemId(), gift.getDataStatusCode());
            }
        });
    }
}
