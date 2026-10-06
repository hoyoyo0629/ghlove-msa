package com.ghlove.member.event;

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
 * 회원/인증 도메인 이벤트를 member.lifecycle 토픽으로 발행한다 (ISP p.403 이벤트스토밍,
 * RFP "서비스간 통신은 Kafka 이벤트 원칙"). donation/gift/order/point 발행자와 같은 규약:
 * 모든 이벤트를 한 토픽에 싣고 eventType은 Kafka 헤더로, key로 파티션 내 순서를 보장한다.
 * 발행 실패는 비차단 로그로만 남긴다 - 로그인/가입 등 원래 트랜잭션은 이미 커밋된 뒤라
 * 브로커 장애가 사용자 흐름을 막지 않는다(현재 다른 서비스 발행자와 동일한 정책).
 *
 * <p>지금은 소비자(admin 회원통계/감사 ReadModel)가 아직 없다 - publish-only 단계이며,
 * 소비 ReadModel 구축은 DA 설계 후 Phase B로 미뤄져 있다(docs/design-decisions.md §3).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MemberEventPublisher {

    public static final String TOPIC = "member.lifecycle";
    public static final String HEADER_EVENT_TYPE = "eventType";

    public static final String EVENT_MEMBER_JOINED = "MEMBER_JOINED";
    public static final String EVENT_LOGIN_SUCCEEDED = "LOGIN_SUCCEEDED";
    public static final String EVENT_LOGIN_FAILED = "LOGIN_FAILED";
    public static final String EVENT_MEMBER_WITHDRAWN = "MEMBER_WITHDRAWN";
    public static final String EVENT_MEMBER_DORMANT = "MEMBER_DORMANT";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishMemberJoined(Long userId, String loginId, String loginPathCode, String sbscrbSeCode) {
        send(String.valueOf(userId),
                new MemberEvents.MemberJoinedEvent(userId, loginId, loginPathCode, sbscrbSeCode, LocalDateTime.now()),
                EVENT_MEMBER_JOINED);
    }

    public void publishLoginSucceeded(Long userId, String loginId, String remoteAddr) {
        send(String.valueOf(userId),
                new MemberEvents.LoginSucceededEvent(userId, loginId, remoteAddr, LocalDateTime.now()),
                EVENT_LOGIN_SUCCEEDED);
    }

    public void publishLoginFailed(String loginId, String remoteAddr, String reason) {
        // 미존재 계정일 수 있어 userId가 아니라 loginId를 key로 삼는다.
        send(loginId,
                new MemberEvents.LoginFailedEvent(loginId, remoteAddr, reason, LocalDateTime.now()),
                EVENT_LOGIN_FAILED);
    }

    public void publishMemberWithdrawn(Long userId, String leaveCode) {
        send(String.valueOf(userId),
                new MemberEvents.MemberWithdrawnEvent(userId, leaveCode, LocalDateTime.now()),
                EVENT_MEMBER_WITHDRAWN);
    }

    public void publishMemberDormant(Long userId) {
        send(String.valueOf(userId),
                new MemberEvents.MemberDormantEvent(userId, LocalDateTime.now()),
                EVENT_MEMBER_DORMANT);
    }

    private void send(String key, Object event, String eventType) {
        ProducerRecord<String, Object> record = new ProducerRecord<>(TOPIC, null, key, event,
                List.of(new RecordHeader(HEADER_EVENT_TYPE, eventType.getBytes(StandardCharsets.UTF_8))));

        kafkaTemplate.send(record).whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish {} event for key={}", eventType, key, ex);
            } else {
                log.info("Published {} event key={} to partition={} offset={}",
                        eventType, key, result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
            }
        });
    }
}
