package com.ghlove.point.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ghlove.point.service.PointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * member.lifecycle 토픽을 구독해 회원가입 축하 포인트를 적립한다 (AS-IS earnPoint("join")를
 * 이벤트 기반으로 이식). donation/order 소비자와 동일하게 payload는 raw JSON 문자열로 받아
 * 직접 파싱하고, eventType은 Kafka 헤더로 구분한다(서비스 간 이벤트 클래스 비공유).
 *
 * <p>MEMBER_JOINED 외의 이벤트(LOGIN_SUCCEEDED·LOGIN_FAILED·MEMBER_WITHDRAWN·MEMBER_DORMANT)는
 * point의 관심사가 아니므로 무시한다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MemberEventListener {

    private static final String HEADER_EVENT_TYPE = "eventType";

    private final PointService pointService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "member.lifecycle", groupId = "point-service")
    public void onMemberEvent(String payload,
                              @Header(name = HEADER_EVENT_TYPE, required = false) byte[] eventTypeHeader) throws Exception {
        String eventType = eventTypeHeader != null ? new String(eventTypeHeader) : null;
        if (eventType == null) {
            return;
        }
        if ("MEMBER_JOINED".equals(eventType)) {
            MemberJoinedEvent event = objectMapper.readValue(payload, MemberJoinedEvent.class);
            log.info("Received MemberJoinedEvent: {}", event);
            pointService.creditForSignup(event.userId());
        }
        // 그 외 회원 이벤트는 point의 관심사가 아니므로 무시한다.
    }
}
