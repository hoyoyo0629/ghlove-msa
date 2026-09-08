package com.ghlove.admin.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * admin 회원관리 콘솔(docs/as-is-admin-gap-deep-audit-part2.md 배치D D2~D5) - member
 * 서비스의 /api/admin/members, /api/admin/secede-users, /api/admin/sleep-users를 호출한다.
 * 실제 데이터/도메인효과는 member 서비스에 있고, 여기는 admin 콘솔의 로그인/RBAC과 화면만
 * 담당한다(OffgiveClient와 동일한 패턴).
 */
@Component
public class MemberAdminClient {

    private final RestClient restClient;

    public MemberAdminClient(@Value("${ghlove.member-service.base-url}") String memberServiceBaseUrl,
                             @Value("${ghlove.internal.admin-secret}") String adminSecret) {
        // member의 /api/admin/** 는 내부 전용이라 공유 시크릿을 실어야 통과한다(InternalApiAuthInterceptor).
        this.restClient = RestClient.builder()
                .baseUrl(memberServiceBaseUrl)
                .defaultHeader("X-Internal-Secret", adminSecret)
                .build();
    }

    public SearchResult search(String fromDate, String toDate, String srchKey, String srchValue,
                                String sbscrbSeCode, String receiveEmail, int page, int size) {
        try {
            SearchResult result = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/api/admin/members/search")
                            .queryParamIfPresent("fromDate", opt(fromDate))
                            .queryParamIfPresent("toDate", opt(toDate))
                            .queryParamIfPresent("srchKey", opt(srchKey))
                            .queryParamIfPresent("srchValue", opt(srchValue))
                            .queryParamIfPresent("sbscrbSeCode", opt(sbscrbSeCode))
                            .queryParamIfPresent("receiveEmail", opt(receiveEmail))
                            .queryParam("page", page)
                            .queryParam("size", size)
                            .build())
                    .retrieve().body(SearchResult.class);
            return result != null ? result : new SearchResult(List.of(), 0, 0);
        } catch (RestClientException e) {
            return new SearchResult(List.of(), 0, 0);
        }
    }

    public Detail detail(Long userId) {
        try {
            return restClient.get().uri("/api/admin/members/{id}", userId).retrieve().body(Detail.class);
        } catch (RestClientException e) {
            return null;
        }
    }

    public void withdraw(Long userId, String reason) {
        try {
            restClient.post()
                    .uri(uriBuilder -> uriBuilder.path("/api/admin/members/{id}/withdraw")
                            .queryParamIfPresent("reason", opt(reason))
                            .build(userId))
                    .retrieve().toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw new ManagerException(extractMessage(e, "탈퇴 처리에 실패했습니다."));
        }
    }

    public SecedeSearchResult searchSecede(String fromDate, String toDate, String srchKey, String srchValue,
                                            int page, int size) {
        try {
            SecedeSearchResult result = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/api/admin/secede-users/search")
                            .queryParamIfPresent("fromDate", opt(fromDate))
                            .queryParamIfPresent("toDate", opt(toDate))
                            .queryParamIfPresent("srchKey", opt(srchKey))
                            .queryParamIfPresent("srchValue", opt(srchValue))
                            .queryParam("page", page)
                            .queryParam("size", size)
                            .build())
                    .retrieve().body(SecedeSearchResult.class);
            return result != null ? result : new SecedeSearchResult(List.of(), 0, 0);
        } catch (RestClientException e) {
            return new SecedeSearchResult(List.of(), 0, 0);
        }
    }

    public SleepSearchResult searchSleep(String fromDate, String toDate, String srchKey, String srchValue,
                                          int page, int size) {
        try {
            SleepSearchResult result = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/api/admin/sleep-users/search")
                            .queryParamIfPresent("fromDate", opt(fromDate))
                            .queryParamIfPresent("toDate", opt(toDate))
                            .queryParamIfPresent("srchKey", opt(srchKey))
                            .queryParamIfPresent("srchValue", opt(srchValue))
                            .queryParam("page", page)
                            .queryParam("size", size)
                            .build())
                    .retrieve().body(SleepSearchResult.class);
            return result != null ? result : new SleepSearchResult(List.of(), 0, 0);
        } catch (RestClientException e) {
            return new SleepSearchResult(List.of(), 0, 0);
        }
    }

    public int wakeup(List<Long> userIds) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        for (Long id : userIds) {
            body.add("userIds", String.valueOf(id));
        }
        try {
            Map<?, ?> result = restClient.post().uri("/api/admin/sleep-users/wakeup")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(body)
                    .retrieve().body(Map.class);
            return result != null && result.get("count") != null ? ((Number) result.get("count")).intValue() : 0;
        } catch (RestClientException e) {
            throw new ManagerException("휴면 해제 처리에 실패했습니다.");
        }
    }

    /** 계정잠금 해제 (SFR-002). */
    public void unlock(Long userId) {
        try {
            restClient.post().uri("/api/admin/members/{id}/unlock", userId).retrieve().toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw new ManagerException(extractMessage(e, "잠금 해제에 실패했습니다."));
        }
    }

    /** RBAC 권한 회수 (SFR-002). */
    public void revokeRole(Long userId, String authority) {
        try {
            restClient.post().uri("/api/admin/members/{id}/roles/{authority}/revoke", userId, authority)
                    .retrieve().toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw new ManagerException(extractMessage(e, "권한 회수에 실패했습니다."));
        }
    }

    /** 휴면전환 배치 수동 트리거 (SFR-002). */
    public int runDormancyBatch() {
        try {
            Map<?, ?> result = restClient.post().uri("/api/admin/batch/dormancy").retrieve().body(Map.class);
            return result != null && result.get("count") != null ? ((Number) result.get("count")).intValue() : 0;
        } catch (RestClientException e) {
            throw new ManagerException("휴면전환 배치 실행에 실패했습니다.");
        }
    }

    /** 데이터 파기 절차 1/2: 탈퇴회원 PII 익명화 수동 트리거 (SFR-002). */
    public int runWithdrawnDestruction() {
        try {
            Map<?, ?> result = restClient.post().uri("/api/admin/batch/data-destruction/withdrawn").retrieve().body(Map.class);
            return result != null && result.get("count") != null ? ((Number) result.get("count")).intValue() : 0;
        } catch (RestClientException e) {
            throw new ManagerException("탈퇴회원 데이터 파기 실행에 실패했습니다.");
        }
    }

    /** 데이터 파기 절차 2/2: 오래된 로그인 로그 IP 마스킹 수동 트리거 (SFR-002). */
    public int runLogDestruction() {
        try {
            Map<?, ?> result = restClient.post().uri("/api/admin/batch/data-destruction/logs").retrieve().body(Map.class);
            return result != null && result.get("count") != null ? ((Number) result.get("count")).intValue() : 0;
        } catch (RestClientException e) {
            throw new ManagerException("로그 데이터 파기 실행에 실패했습니다.");
        }
    }

    public List<DestructionHistoryRow> destructionHistory() {
        try {
            List<DestructionHistoryRow> rows = restClient.get().uri("/api/admin/batch/data-destruction/history")
                    .retrieve().body(new org.springframework.core.ParameterizedTypeReference<List<DestructionHistoryRow>>() {
                    });
            return rows != null ? rows : List.of();
        } catch (RestClientException e) {
            return List.of();
        }
    }

    public record DestructionHistoryRow(Long destructionId, Long userId, String destroyedFields, String reason,
                                         String destroyedDate) {
    }

    private static Optional<String> opt(String value) {
        return Optional.ofNullable(value == null || value.isBlank() ? null : value);
    }

    private static String extractMessage(RestClientResponseException e, String fallback) {
        try {
            var body = e.getResponseBodyAs(Map.class);
            Object msg = body != null ? body.get("message") : null;
            return msg != null ? msg.toString() : fallback;
        } catch (RuntimeException ex) {
            return fallback;
        }
    }

    public record Row(Long userId, String loginId, String userName, String email, String statusCode,
                       String sbscrbSeCode, String createdDate, String phoneNumber) {
    }

    public record SearchResult(List<Row> content, long totalElements, int totalPages) {
    }

    public record Detail(Long userId, String loginId, String userName, String email, String statusCode,
                          String sbscrbSeCode, String createdDate, String updatedDate, String loginDate,
                          Integer loginCount, String leaveDate, String phoneNumber, String address,
                          String addressDetail, String post, String gender, String birthday, String receiveEmail,
                          String receiveSms, String receiveKakao, String leaveReason, String leaveCode,
                          List<String> roles) {
    }

    public record SecedeRow(Long userId, String loginId, String userName, String leaveDate, String leaveReason,
                             String leaveCode) {
    }

    public record SecedeSearchResult(List<SecedeRow> content, long totalElements, int totalPages) {
    }

    public record SleepRow(Long userId, String loginId, String userName, String loginDate, String createdDate) {
    }

    public record SleepSearchResult(List<SleepRow> content, long totalElements, int totalPages) {
    }
}
