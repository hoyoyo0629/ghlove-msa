package com.ghlove.admin.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ghlove.admin.service.ManagerAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * member.lifecycle의 MEMBER_PASSWORD_CHANGED(회원 비밀번호 변경·찾기)를 받아 같은 아이디의 관리자 비밀번호도 바꾸고
 * 로그인 실패횟수를 푼다 - AS-IS UserServiceImpl "관리자동기화". 그래서 계정 잠김 안내(login-lock)의 "비밀번호 찾기"로
 * 관리자 잠김이 풀린다. 다른 회원 이벤트는 무시한다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MemberPasswordSyncListener {

    private static final String HEADER_EVENT_TYPE = "eventType";
    private static final String MEMBER_PASSWORD_CHANGED = "MEMBER_PASSWORD_CHANGED";

    private final ManagerAuthService managerAuthService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "member.lifecycle", groupId = "admin-manager-sync")
    public void onEvent(String payload, @Header(name = HEADER_EVENT_TYPE, required = false) byte[] eventTypeHeader)
            throws Exception {
        String eventType = eventTypeHeader != null ? new String(eventTypeHeader) : null;
        if (!MEMBER_PASSWORD_CHANGED.equals(eventType)) {
            return;
        }
        PasswordChanged event = objectMapper.readValue(payload, PasswordChanged.class);
        managerAuthService.syncPasswordFromMember(event.loginId(), event.passwordHash(), event.passwordType(),
                event.passwordExpiredDate());
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PasswordChanged(Long userId, String loginId, String passwordHash, String passwordType,
                                  String passwordExpiredDate) {
    }
}
