package com.ghlove.admin.service.integration;

import com.ghlove.admin.domain.UmsSendLog;
import com.ghlove.admin.repository.UmsSendLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * UMS 카카오 알림톡 발송 연계 (AS-IS MiceAlimtalkSender - 정산 상태 변경 시 제공자에게
 * 알림톡 발송). member 서비스의 동명 클라이언트와 동일한 패턴이지만 서비스마다 자기
 * DB/설정만 갖는다는 이 프로젝트의 원칙에 따라 별도로 둔다. enabled=false(방화벽
 * 미개방)면 실제 전송 없이 로그만 남긴다.
 *
 * SFR-007 재검토 라운드 - RFP가 명시한 "대량 발송 안정성·성능, 재시도 로직·발송 제한,
 * 모니터링·통계"의 재시도+통계 부분 gap fill: (1) 실패 시 최대 {@link #MAX_RETRIES}회
 * 재시도, (2) 성공/실패/재시도횟수를 매번 OP_UMS_SEND_LOG에 남겨 "UMS 발송이력" 화면이
 * 실제 발송경로와 연결되게 한다. 발송 제한(rate limit)은 실제 카카오 연계가 아직
 * mock-gated 상태라 트래픽 자체가 없어 이번 라운드 범위에서 뺐다(실연동 시점에 재논의).
 */
@Component
@Slf4j
public class NotificationClient {

    private static final int MAX_RETRIES = 2;

    private final boolean enabled;
    private final RestClient restClient;
    private final String clientId;
    private final UmsSendLogRepository umsSendLogRepository;

    public NotificationClient(
            @Value("${ghlove.integrations.ums.enabled}") boolean enabled,
            @Value("${ghlove.integrations.ums.api-url}") String apiUrl,
            @Value("${ghlove.integrations.ums.client-id}") String clientId,
            UmsSendLogRepository umsSendLogRepository) {
        this.enabled = enabled;
        this.restClient = RestClient.create(apiUrl);
        this.clientId = clientId;
        this.umsSendLogRepository = umsSendLogRepository;
    }

    public void sendAlimtalk(String toUserKey, String templateCode, Map<String, String> variables) {
        if (!enabled) {
            log.info("[ums] disabled - mock 알림톡 발송 to={} template={} vars={}", toUserKey, templateCode, variables);
            recordLog(toUserKey, templateCode, variables, "Y", 0);
            return;
        }

        int attempt = 0;
        while (true) {
            try {
                restClient.post().uri("/api/kakao")
                        .body(Map.of("clientId", clientId, "to", toUserKey, "templateCode", templateCode, "variables", variables))
                        .retrieve().toBodilessEntity();
                recordLog(toUserKey, templateCode, variables, "Y", attempt);
                return;
            } catch (RestClientException e) {
                if (attempt >= MAX_RETRIES) {
                    log.warn("알림톡 발송 실패(재시도 {}회 소진) to={} template={}", attempt, toUserKey, templateCode, e);
                    recordLog(toUserKey, templateCode, variables, "N", attempt);
                    return;
                }
                log.warn("알림톡 발송 실패, 재시도 {}/{} to={} template={}", attempt + 1, MAX_RETRIES, toUserKey, templateCode, e);
                attempt++;
            }
        }
    }

    private void recordLog(String toUserKey, String templateCode, Map<String, String> variables, String successYn, int retryCount) {
        UmsSendLog entry = new UmsSendLog();
        entry.setTemplateCode(templateCode);
        entry.setUmsType("ALIM_TALK");
        entry.setTitle(templateCode);
        String message = String.valueOf(variables);
        entry.setMessage(message.length() > 255 ? message.substring(0, 255) : message);
        entry.setTargetKey(toUserKey);
        entry.setSuccessYn(successYn);
        entry.setRetryCount(retryCount);
        entry.setCreated(LocalDateTime.now());
        umsSendLogRepository.save(entry);
    }
}
