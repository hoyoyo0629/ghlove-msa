package com.ghlove.admin.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ghlove.admin.service.ManagerAuthService;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** member.lifecycle MEMBER_PASSWORD_CHANGED → 관리자 비밀번호 동기화(AS-IS "관리자동기화"). payload는 member가 실제로 싣는 모양 그대로. */
class MemberPasswordSyncListenerTest {

    private static final String PAYLOAD = "{\"userId\":1115,\"loginId\":\"reqtest01\","
            + "\"passwordHash\":\"$2a$10$abc\",\"passwordType\":\"N\",\"passwordExpiredDate\":\"20270412\","
            + "\"at\":\"2026-10-14T16:25:28.1\"}";

    @Test
    void passwordChangedSyncsManager() throws Exception {
        ManagerAuthService service = mock(ManagerAuthService.class);
        MemberPasswordSyncListener listener = new MemberPasswordSyncListener(service, new ObjectMapper());

        listener.onEvent(PAYLOAD, "MEMBER_PASSWORD_CHANGED".getBytes(StandardCharsets.UTF_8));

        verify(service).syncPasswordFromMember("reqtest01", "$2a$10$abc", "N", "20270412");
    }

    @Test
    void otherMemberEventsAreIgnored() throws Exception {
        ManagerAuthService service = mock(ManagerAuthService.class);
        MemberPasswordSyncListener listener = new MemberPasswordSyncListener(service, new ObjectMapper());

        listener.onEvent("{\"userId\":1}", "LOGIN_SUCCEEDED".getBytes(StandardCharsets.UTF_8));
        listener.onEvent("{\"userId\":1}", null);

        verify(service, never()).syncPasswordFromMember(any(), any(), any(), any());
    }
}
