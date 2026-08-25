package com.ghlove.admin.service.integration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

/**
 * UMS 카카오 알림톡 발송 연계 (AS-IS MiceAlimtalkSender - 정산 상태 변경 시 제공자에게
 * 알림톡 발송). member 서비스의 동명 클라이언트와 동일한 패턴이지만 서비스마다 자기
 * DB/설정만 갖는다는 이 프로젝트의 원칙에 따라 별도로 둔다. enabled=false(방화벽
 * 미개방)면 실제 전송 없이 로그만 남긴다.
 */
@Component
@Slf4j
public class NotificationClient {

    private final boolean enabled;
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

    public void sendAlimtalk(String toUserKey, String templateCode, Map<String, String> variables) {
        if (!enabled) {
            log.info("[ums] disabled - mock 알림톡 발송 to={} template={} vars={}", toUserKey, templateCode, variables);
            return;
        }
        try {
            restClient.post().uri("/api/kakao")
                    .body(Map.of("clientId", clientId, "to", toUserKey, "templateCode", templateCode, "variables", variables))
                    .retrieve().toBodilessEntity();
        } catch (RestClientException e) {
            log.warn("알림톡 발송 실패 to={} template={}", toUserKey, templateCode, e);
        }
    }
}
