package com.ghlove.order.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ghlove.order.service.OffgiveOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * 오프라인 답례품(기탁서 등록) 결제처리 트리거 - AS-IS는 수납완료 배치(cntrSunapBatch → procOffOrder)가
 * 수납완료된 기부의 입금대기 주문을 결제완료로 바꾸고 포인트를 쓴다. TO-BE는 point가 그 기부의
 * 포인트를 적립한 사실(EARN, refKey=기부일련번호)을 받아서 처리한다 - 적립이 커밋된 뒤라
 * 이어지는 포인트 차감이 잔액 부족으로 엇갈리지 않는다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PointLedgerListener {

    private static final String TOPIC = "point.ledger";
    private static final String TXN_EARN = "EARN";

    private final OffgiveOrderService offgiveOrderService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = TOPIC, groupId = "order-service")
    public void onEvent(String payload) throws Exception {
        PointLedgerEvent event = objectMapper.readValue(payload, PointLedgerEvent.class);
        if (!TXN_EARN.equals(event.txnType()) || event.refKey() == null) {
            return;
        }
        offgiveOrderService.onDonationPointCredited(event.refKey());
    }
}
