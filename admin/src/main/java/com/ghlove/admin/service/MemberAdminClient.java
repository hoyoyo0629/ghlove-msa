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
        withdraw(userId, reason, null);
    }

    /**
     * 관리자에 의한 회원탈퇴 - 일반회원관리(메뉴 4101) 상세의 회원탈퇴 팝업이 쓴다.
     *
     * @param leaveUserId 탈퇴를 처리한 운영자의 USER_ID (AS-IS가 OP_USER_DETAIL.LEAVE_USER_ID에
     *        남기는 값. 탈퇴회원리스트 4105의 탈퇴구분·담당자 컬럼이 이 값으로 갈린다)
     * @return AS-IS 결과코드 - {@code SUCC} / {@code ERR_ALR_SECEDE}(이미 탈퇴) /
     *         {@code ERR_ONE_PASS}(디지털원패스 회원) / {@code FAIL}
     */
    public String withdraw(Long userId, String reason, Long leaveUserId) {
        try {
            restClient.post()
                    .uri(uriBuilder -> uriBuilder.path("/api/admin/members/{id}/withdraw")
                            .queryParamIfPresent("reason", opt(reason))
                            .queryParamIfPresent("leaveUserId",
                                    Optional.ofNullable(leaveUserId).map(String::valueOf))
                            .build(userId))
                    .retrieve().toBodilessEntity();
            return "SUCC";
        } catch (RestClientResponseException e) {
            return extractCode(e);
        } catch (RestClientException e) {
            return "FAIL";
        }
    }

    /**
     * 회원 이름 일괄 조회 - 회원ID만 가진 목록에 이름을 채운다(기부혜택증 열람현황 19103 등).
     * 없는 ID는 결과에 없다 - AS-IS가 OP_USER를 <b>INNER JOIN</b>하므로 호출부는 그 행을 버린다.
     */
    public Map<Long, String> userNames(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        try {
            Map<Long, String> names = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/api/admin/members/names")
                            .queryParam("userIds", userIds).build())
                    .retrieve()
                    .body(new org.springframework.core.ParameterizedTypeReference<Map<Long, String>>() {
                    });
            return names != null ? names : Map.of();
        } catch (RestClientException e) {
            return Map.of();
        }
    }

    /**
     * 회원ID → 로그인ID 일괄조회 - Q&A 관리(5112) 목록이 쓴다. AS-IS는 {@code OP_QNA}에
     * {@code OP_USER}를 조인해 {@code LOGIN_ID}를 뽑지만 TO-BE에서 회원은 member 소유다.
     * 정상회원만 돌아오므로(AS-IS 조인조건 {@code STATUS_CODE='9'}) 탈퇴·휴면이면 빈 값이다.
     */
    public Map<Long, String> userLoginIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        try {
            Map<Long, String> loginIds = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/api/admin/members/login-ids")
                            .queryParam("userIds", userIds).build())
                    .retrieve()
                    .body(new org.springframework.core.ParameterizedTypeReference<Map<Long, String>>() {
                    });
            return loginIds != null ? loginIds : Map.of();
        } catch (RestClientException e) {
            return Map.of();
        }
    }

    /**
     * 로그인ID 부분일치 회원ID 목록 - Q&A 관리(5112)의 검색구분 '아이디'용.
     * AS-IS는 목록 SQL에서 {@code U.LOGIN_ID LIKE}로 걸지만 TO-BE는 회원ID를 먼저 받아 걸러야 한다.
     * 호출 실패 시 빈 목록이면 <b>검색결과 0건</b>이 되므로 호출부가 그 의미를 구분해 쓸 것.
     */
    public List<Long> userIdsByLoginIdLike(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }
        try {
            List<Long> ids = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/api/admin/members/ids-by-login")
                            .queryParam("keyword", keyword).build())
                    .retrieve()
                    .body(new org.springframework.core.ParameterizedTypeReference<List<Long>>() {
                    });
            return ids != null ? ids : List.of();
        } catch (RestClientException e) {
            return List.of();
        }
    }

    /**
     * 국민비서(IPS) 문자 수신자 정보 - AS-IS {@code QnaMapper.getQnaUserInfo}가 하던 조인을
     * 대신한다(Q&A 답변 저장 후 문자 적재용). 회원·상세정보가 없으면 AS-IS도 결과가 없어
     * {@code null}이고, 호출부는 그때 <b>문자를 보내지 않는다</b>.
     */
    public SmsReceiver smsReceiver(Long userId) {
        if (userId == null || userId <= 0) {
            return null;
        }
        try {
            return restClient.get()
                    .uri("/api/admin/members/{userId}/sms-receiver", userId)
                    .retrieve()
                    .body(SmsReceiver.class);
        } catch (RestClientException e) {
            return null;
        }
    }

    /** AS-IS GiveUserSmsInfo에서 Q&A 답변 문자가 쓰는 값들. */
    public record SmsReceiver(Long userId, String userName, String phoneNumber,
                              String receiveSms, String mberCi) {
    }

    /** 일반회원관리(4101) 상세 > 배송지 관리 팝업. */
    public List<DeliveryRow> deliveries(Long userId) {
        try {
            List<DeliveryRow> rows = restClient.get()
                    .uri("/api/admin/members/{id}/deliveries", userId)
                    .retrieve().body(new org.springframework.core.ParameterizedTypeReference<List<DeliveryRow>>() {
                    });
            return rows != null ? rows : List.of();
        } catch (RestClientException e) {
            return List.of();
        }
    }

    /**
     * 개인정보 열람 이력 기록 (AS-IS G_INDVDLINFO_READNG_HIST) - 운영자 비밀번호를 재확인한 뒤
     * 비마스킹 정보를 보여주기 직전에 호출한다. 실패해도 화면을 막지는 않는다(AS-IS도 그렇다).
     */
    public void recordPiiAccess(Long targetUserId, Long managerUserId) {
        try {
            restClient.post()
                    .uri(uriBuilder -> uriBuilder.path("/api/admin/members/{id}/pii-access")
                            .queryParam("managerUserId", managerUserId).build(targetUserId))
                    .retrieve().toBodilessEntity();
        } catch (RestClientException e) {
            // 이력 적재 실패가 열람 자체를 막지는 않는다
        }
    }

    /** 배송지 관리 팝업의 한 행. */
    public record DeliveryRow(Long userDeliveryId, String title, String defaultFlag, String userName,
                               String address, String addressDetail, String mobile, String phone) {
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

    /** member가 내려준 AS-IS 결과코드({@code ERR_ALR_SECEDE}/{@code ERR_ONE_PASS}). */
    private static String extractCode(RestClientResponseException e) {
        try {
            var body = e.getResponseBodyAs(Map.class);
            Object code = body != null ? body.get("code") : null;
            return code != null ? code.toString() : "FAIL";
        } catch (RuntimeException ex) {
            return "FAIL";
        }
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

    /** {@code address}/{@code addressDetail}/{@code receiveEmail}는 일반회원관리(4101) 목록 컬럼용. */
    public record Row(Long userId, String loginId, String userName, String email, String statusCode,
                       String sbscrbSeCode, String createdDate, String phoneNumber,
                       String address, String addressDetail, String receiveEmail) {
    }

    public record SearchResult(List<Row> content, long totalElements, int totalPages) {
    }

    public record Detail(Long userId, String loginId, String userName, String email, String statusCode,
                          String sbscrbSeCode, String createdDate, String updatedDate, String loginDate,
                          Integer loginCount, String leaveDate, String phoneNumber, String address,
                          String addressDetail, String post, String gender, String birthday, String receiveEmail,
                          String receiveSms, String receiveKakao, String leaveReason, String leaveCode,
                          List<String> roles, boolean onePassUser) {
    }

    /** {@code leaveUserId}가 있으면 관리자탈퇴, 없으면 회원탈퇴다(AS-IS 탈퇴구분 판정).
     *  담당자 이름·권한그룹은 admin이 자기 OP_MANAGER/OP_ROLE에서 이 ID로 찾는다. */
    public record SecedeRow(Long userId, String loginId, String userName, String leaveDate, String leaveReason,
                             String leaveCode, Long leaveUserId) {
    }

    public record SecedeSearchResult(List<SecedeRow> content, long totalElements, int totalPages) {
    }

    /** {@code address}/{@code addressDetail}는 휴면회원관리(메뉴 4107)의 "주소" 컬럼용. */
    public record SleepRow(Long userId, String loginId, String userName, String loginDate, String createdDate,
                            String address, String addressDetail) {
    }

    public record SleepSearchResult(List<SleepRow> content, long totalElements, int totalPages) {
    }
}
