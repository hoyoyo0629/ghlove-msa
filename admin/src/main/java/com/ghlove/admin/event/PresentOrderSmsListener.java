package com.ghlove.admin.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ghlove.admin.service.PresentOrderSmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * order.saga의 PRESENT_ORDER_PAID(답례품 주문 결제완료 - 온라인 주문·기탁서 등록 답례품 공통)를 받아 국민비서
 * 답례품 주문 문자를 적재한다(AS-IS saveGiveOrderTempAndPay·procOffOrder의 문자 발송부). 통계용
 * {@link OrderStatsListener}와 <b>그룹을 따로 쓴다</b> - 같은 그룹이면 두 리스너가 파티션을 나눠 가져 서로
 * 메시지를 빼앗는다.
 *
 * <p>OFFGIVE_ORDER_PAID는 2026-10-14 이름을 바꾸기 전(기탁서 전용이던 때)의 같은 이벤트다 - 토픽에 남아 있는
 * 것도 처리한다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PresentOrderSmsListener {

    private static final String HEADER_EVENT_TYPE = "eventType";
    private static final String PRESENT_ORDER_PAID = "PRESENT_ORDER_PAID";
    private static final String OFFGIVE_ORDER_PAID = "OFFGIVE_ORDER_PAID";

    private final PresentOrderSmsService presentOrderSmsService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "order.saga", groupId = "admin-sms-service")
    public void onEvent(String payload, @Header(name = HEADER_EVENT_TYPE, required = false) byte[] eventTypeHeader)
            throws Exception {
        String eventType = eventTypeHeader != null ? new String(eventTypeHeader) : null;
        if (!PRESENT_ORDER_PAID.equals(eventType) && !OFFGIVE_ORDER_PAID.equals(eventType)) {
            return;
        }
        PresentOrderPaidEvent event = objectMapper.readValue(payload, PresentOrderPaidEvent.class);
        presentOrderSmsService.send(event.orderId(), event.userId(),
                event.items() == null ? java.util.List.of() : event.items().stream()
                        .map(l -> new PresentOrderSmsService.BuyItem(l.itemId(), l.sellerId(), l.itemName(),
                                l.optionName()))
                        .toList(),
                event.occurredAt());
    }
}
