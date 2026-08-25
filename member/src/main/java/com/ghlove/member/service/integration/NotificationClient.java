package com.ghlove.member.service.integration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

/**
 * UMS 문자/카카오 알림톡 발송 연계 (AS-IS MiceAlimtalkSender/UmsApiService -
 * `ums.api.url + /api/kakao`). enabled=false(방화벽 미개방)면 실제 전송 없이
 * 로그만 남긴다 - 회원가입 환영 알림처럼 실패해도 주 흐름을 막으면 안 되는
 * best-effort 알림에 사용한다.
 */
@Component
@Slf4j
public class NotificationClient {

    public final boolean enabled;
    private final RestClient restClient;
    private final String clientId;

    public NotificationClient(
            @Value("${ghlove.integrations.ums.enabled}") boolean enabled,
            @Value("${ghlove.integrations.ums.api-url}") String apiUrl,
            @Value("${ghlove.integrations.ums.client-id}") String clientId) {
        this.enabled = enabled;
        this.restClient = RestClient.create(apiUrl);
        this.clientId = clientId;
    }

    /** 카카오 알림톡 발송 (실패해도 예외를 던지지 않고 로그만 남긴다 - 호출부가 best-effort로 쓸 수 있도록). */
    public void sendAlimtalk(String phoneOrUserKey, String templateCode, Map<String, String> variables) {
        if (!enabled) {
            log.info("[ums] disabled - mock 알림톡 발송 to={} template={} vars={}", phoneOrUserKey, templateCode, variables);
            return;
        }
        try {
            restClient.post().uri("/api/kakao")
                    .body(Map.of("clientId", clientId, "to", phoneOrUserKey, "templateCode", templateCode, "variables", variables))
                    .retrieve().toBodilessEntity();
        } catch (RestClientException e) {
            log.warn("알림톡 발송 실패 to={} template={}", phoneOrUserKey, templateCode, e);
        }
    }
}
