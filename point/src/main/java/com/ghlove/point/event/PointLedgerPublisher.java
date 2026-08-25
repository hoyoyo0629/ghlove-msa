package com.ghlove.point.event;

import com.ghlove.point.domain.PointLedger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * 원장행 발행 - admin이 자기 DB를 못 읽으니(DB per Service) 포인트 통계 ReadModel을
 * 만들려면 이 이벤트를 구독해야 한다 (SFR-007/009 gap fill). 새 원장행이 생기는
 * 모든 지점(적립/사용/회수/복원/소멸)에서 호출한다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PointLedgerPublisher {

    public static final String TOPIC = "point.ledger";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publish(PointLedger ledger) {
        PointLedgerEvent event = new PointLedgerEvent(ledger.getLedgerId(), ledger.getUserId(),
                ledger.getLocgovCode(), ledger.getTxnType(), ledger.getPointAmount(), ledger.getCreatedDate());
        kafkaTemplate.send(TOPIC, String.valueOf(ledger.getUserId()), event).whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish PointLedgerEvent for ledgerId={}", ledger.getLedgerId(), ex);
            } else {
                log.info("Published PointLedgerEvent ledgerId={} txnType={}", ledger.getLedgerId(), ledger.getTxnType());
            }
        });
    }
}
