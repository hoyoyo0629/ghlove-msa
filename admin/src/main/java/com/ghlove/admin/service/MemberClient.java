package com.ghlove.admin.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** 관리자권한요청 폼 프리필용 - 신청자는 JWT로 이미 신원이 확인된 기존 회원이라(SFR-010),
 *  member 서비스의 GET /api/users/{id}에서 이름/휴대폰/이메일/생년월일을 그대로 받아온다. */
@Component
public class MemberClient {

    private final RestClient restClient;

    public MemberClient(@Value("${ghlove.member-service.base-url}") String memberServiceBaseUrl,
                        @Value("${ghlove.internal.admin-secret}") String adminSecret) {
        // member의 /api/admin/**, /api/users/{id} 는 내부 전용이라 공유 시크릿을 실어야 통과한다.
        this.restClient = RestClient.builder()
                .baseUrl(memberServiceBaseUrl)
                .defaultHeader("X-Internal-Secret", adminSecret)
                .build();
    }

    public MemberInfo fetchOrNull(Long userId) {
        try {
            return restClient.get()
                    .uri("/api/users/{id}", userId)
                    .retrieve()
                    .body(MemberInfo.class);
        } catch (RestClientException e) {
            return null;
        }
    }

    public record MemberInfo(Long userId, String userName, String birthday, String address, String loginId,
                              String phoneNumber, String email, String post, String addressDetail) {
    }

    /** 회원 아이디·비밀번호 확인 결과 - result는 NOT_FOUND / WRONG_PASSWORD / OK. member를 못 부르면 null. */
    public record CredentialCheck(String result, Long userId) {
    }

    /** 관리자 로그인의 일반회원 확인·권한 요청 폼 진입 - 세션 없이 아이디·비밀번호만 확인한다. */
    public CredentialCheck checkCredential(String loginId, String password) {
        try {
            var form = new org.springframework.util.LinkedMultiValueMap<String, String>();
            form.add("loginId", loginId);
            form.add("password", password);
            return restClient.post().uri("/api/admin/members/credential-check")
                    .contentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form).retrieve().body(CredentialCheck.class);
        } catch (RestClientException e) {
            return null;
        }
    }

    // ===== 관리자 계정 ↔ 회원 계정 연결 (member ManagerAccountLinkService) =====

    /** 관리자 쪽이 복사·대조에 쓰는 회원 정보. password는 해시, asIsStatusCode는 AS-IS 표기(9/4/3). */
    public record ManagerSyncInfo(Long userId, String loginId, String password, String userName, String email,
                                  String phoneNumber, String telNumber, String birthday, String statusCode,
                                  String asIsStatusCode, String passwordType, String passwordExpiredDate,
                                  String mberCi, String mberDn, String mberFinDn) {
    }

    /** 회원이 없으면(204) null, member를 못 부르면 예외 대신 null. */
    public ManagerSyncInfo managerSyncInfo(Long userId) {
        try {
            return restClient.get().uri("/api/admin/members/{id}/manager-sync", userId)
                    .retrieve().body(ManagerSyncInfo.class);
        } catch (RestClientException e) {
            return null;
        }
    }

    public ManagerSyncInfo managerSyncInfoByLoginId(String loginId) {
        try {
            return restClient.get().uri("/api/admin/members/manager-sync/by-login-id?loginId={loginId}", loginId)
                    .retrieve().body(ManagerSyncInfo.class);
        } catch (RestClientException e) {
            return null;
        }
    }

    /** 관리자 직접 등록용 회원번호(AS-IS selectNewUserId). 실패하면 예외 - 번호 없이 관리자를 만들 수 없다. */
    @SuppressWarnings("unchecked")
    public Long nextUserId() {
        Map<String, Object> body = restClient.post().uri("/api/admin/members/next-user-id").retrieve().body(Map.class);
        Object id = body != null ? body.get("userId") : null;
        if (!(id instanceof Number n)) {
            throw new ManagerException("회원번호를 발급받지 못했습니다.");
        }
        return n.longValue();
    }

    @SuppressWarnings("unchecked")
    public boolean loginIdExists(String loginId) {
        Map<String, Object> body = restClient.get().uri("/api/admin/members/login-id-exists?loginId={loginId}", loginId)
                .retrieve().body(Map.class);
        return body != null && Boolean.TRUE.equals(body.get("exists"));
    }

    /** 오프라인 담당자 등록 - 회원 계정을 먼저 만들고 그 번호를 돌려준다(AS-IS insertUser + insertUserDetail). */
    @SuppressWarnings("unchecked")
    public Long createManagerAccount(String loginId, String rawPassword, String userName, String email) {
        var form = new org.springframework.util.LinkedMultiValueMap<String, String>();
        form.add("loginId", loginId);
        form.add("password", rawPassword);
        form.add("userName", userName);
        form.add("email", email);
        try {
            Map<String, Object> body = restClient.post().uri("/api/admin/members/manager-account")
                    .contentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form).retrieve().body(Map.class);
            Object id = body != null ? body.get("userId") : null;
            if (id instanceof Number n) {
                return n.longValue();
            }
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            throw new ManagerException("이미 사용 중인 아이디입니다.");
        }
        throw new ManagerException("회원 계정을 만들지 못했습니다.");
    }

    /** 관리자 비밀번호 변경 팝업의 결과를 회원에도(AS-IS updateTempPasswordChange 4·5단계). */
    public void applyPasswordFromManager(Long userId, String passwordHash, String passwordType, String passwordExpiredDate) {
        var form = new org.springframework.util.LinkedMultiValueMap<String, String>();
        form.add("passwordHash", passwordHash);
        form.add("passwordType", passwordType);
        form.add("passwordExpiredDate", passwordExpiredDate);
        restClient.post().uri("/api/admin/members/{id}/password-from-manager", userId)
                .contentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED)
                .body(form).retrieve().toBodilessEntity();
    }

    /** 오프라인 담당자 수정 - 회원 이름·이메일·휴대폰(·지점 연락처) 동기화(AS-IS updateUserSync/updateUserDetailSync). */
    public void syncManagerProfile(Long userId, String userName, String email, String phoneNumber, String telNumber) {
        var form = new org.springframework.util.LinkedMultiValueMap<String, String>();
        form.add("userName", userName);
        form.add("email", email);
        form.add("phoneNumber", phoneNumber);
        form.add("telNumber", telNumber);
        restClient.post().uri("/api/admin/members/{id}/manager-profile", userId)
                .contentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED)
                .body(form).retrieve().toBodilessEntity();
    }

    /** 오프라인 담당자 삭제 - 지점 연락처 비우기(AS-IS deleteChargerTelNumber). */
    public void clearTelNumber(List<Long> userIds) {
        var form = new org.springframework.util.LinkedMultiValueMap<String, String>();
        userIds.forEach(id -> form.add("userIds", String.valueOf(id)));
        restClient.post().uri("/api/admin/members/tel-number/clear")
                .contentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED)
                .body(form).retrieve().toBodilessEntity();
    }

    /** 인증서 등록 화면 본인 확인용 회원(AS-IS getPkiManagerFormValid/Off). */
    public record PkiUser(Long userId, String phoneNumber, String mberCi, String mberDn, String mberFinDn) {
    }

    public PkiUser pkiUser(String loginId, String birthday, String charge) {
        try {
            return restClient.get().uri("/api/admin/members/pki-user?loginId={loginId}&birthday={birthday}&charge={charge}",
                            loginId, birthday == null ? "" : birthday, charge)
                    .retrieve().body(PkiUser.class);
        } catch (RestClientException e) {
            return null;
        }
    }

    /** 인증서 DN 등록(AS-IS updatePkiManagerMberDn) - 결과코드 SUCC/FAIL. */
    @SuppressWarnings("unchecked")
    public String updateDn(Long userId, String mberDn, String mberFinDn) {
        var form = new org.springframework.util.LinkedMultiValueMap<String, String>();
        form.add("mberDn", mberDn == null ? "" : mberDn);
        form.add("mberFinDn", mberFinDn == null ? "" : mberFinDn);
        try {
            Map<String, Object> body = restClient.post().uri("/api/admin/members/{id}/dn", userId)
                    .contentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form).retrieve().body(Map.class);
            return body != null ? String.valueOf(body.get("code")) : "FAIL";
        } catch (RestClientException e) {
            return "FAIL";
        }
    }

    /** 인증서 DN 삭제(AS-IS updateDeletePkiManagerMberDn) - 결과코드 SUCC/FAIL. */
    @SuppressWarnings("unchecked")
    public String deleteDn(Long userId, String type, String mberDn, String mberFinDn) {
        var form = new org.springframework.util.LinkedMultiValueMap<String, String>();
        form.add("type", type);
        form.add("mberDn", mberDn == null ? "" : mberDn);
        form.add("mberFinDn", mberFinDn == null ? "" : mberFinDn);
        try {
            Map<String, Object> body = restClient.post().uri("/api/admin/members/{id}/dn/delete", userId)
                    .contentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form).retrieve().body(Map.class);
            return body != null ? String.valueOf(body.get("code")) : "FAIL";
        } catch (RestClientException e) {
            return "FAIL";
        }
    }

    /** 인증서 로그인 - DN으로 회원 찾기(AS-IS getUserByMberDn / getUserByMberFinDn). */
    public ManagerSyncInfo byDn(String mberDn, String mberFinDn) {
        try {
            // DN에 ',' '=' '+' 등이 들어가므로 템플릿 변수로 넘겨 값 전체를 인코딩한다.
            return restClient.get().uri("/api/admin/members/by-dn?mberDn={dn}&mberFinDn={finDn}",
                            mberDn == null ? "" : mberDn, mberFinDn == null ? "" : mberFinDn)
                    .retrieve().body(ManagerSyncInfo.class);
        } catch (RestClientException e) {
            return null;
        }
    }

    /** 기탁서 등록(15102) SCI 실명인증 결과(CI)로 기존회원 조회 - AS-IS user-check. */
    public ByCiInfo findByCi(String mberCi) {
        try {
            return restClient.get().uri("/api/users/by-ci?mberCi={ci}", mberCi).retrieve().body(ByCiInfo.class);
        } catch (RestClientException e) {
            return null;
        }
    }

    public record ByCiInfo(Long userId, String userName, String loginId, String phoneNumber,
                            String post, String address, String addressDetail) {
    }

    /** 기탁서 등록 "휴대폰번호 수정하기" - AS-IS updatePhoneNumber. 수정 건수(0이면 실패)를 돌려준다. */
    @SuppressWarnings("unchecked")
    public int updatePhoneNumber(Long userId, String phoneNumber) {
        try {
            Map<String, Object> body = restClient.post()
                    .uri(uriBuilder -> uriBuilder.path("/api/users/{id}/phone-number")
                            .queryParam("phoneNumber", phoneNumber).build(userId))
                    .retrieve().body(Map.class);
            Object updated = body != null ? body.get("updated") : null;
            return updated instanceof Number n ? n.intValue() : 0;
        } catch (RestClientException e) {
            return 0;
        }
    }

    /** report "총괄 현황"(회원수현황) + "총괄 누계 현황"(연령별 기부건수)용 - 회원 전체 가입일/생년월일 스냅샷. */
    public List<MemberSnapshot> allForResync() {
        try {
            List<MemberSnapshot> members = restClient.get()
                    .uri("/api/admin/members/all")
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<MemberSnapshot>>() {
                    });
            return members != null ? members : List.of();
        } catch (RestClientException e) {
            return List.of();
        }
    }

    public record MemberSnapshot(Long userId, LocalDateTime createdDate, String birthday) {
    }

    /** "회원 로그인 로그 관리" 화면용 (AS-IS opmanager/log/user-login-log) - member가 SFR-002로
     *  이미 쓰고 있는 실제 로그인 감사 이력을 그대로 읽어온다. */
    public List<LoginLog> loginLogs() {
        try {
            List<LoginLog> logs = restClient.get()
                    .uri("/api/admin/login-log")
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<LoginLog>>() {
                    });
            return logs != null ? logs : List.of();
        } catch (RestClientException e) {
            return List.of();
        }
    }

    public record LoginLog(Integer loginLogId, String loginType, String loginId, String successFlag,
                            String remoteAddr, String memo, String loginDate) {
    }

    /** "회원 액션 로그 관리" 화면용 (AS-IS opmanager/log/user-action-log). */
    public List<UserActionLog> userActionLogs() {
        try {
            List<UserActionLog> logs = restClient.get()
                    .uri("/api/admin/user-action-log")
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<UserActionLog>>() {
                    });
            return logs != null ? logs : List.of();
        } catch (RestClientException e) {
            return List.of();
        }
    }

    public record UserActionLog(Integer actionLogId, String createdDate, String remoteAddr,
                                 String requestUri, String requestMethod, String loginId) {
    }

    /** "회원 권한/상태변경 이력 관리" 화면용 (SFR-002 "권한 변경, 계정 잠금/해제" 감사로그). */
    public List<ChangeLog> changeLogs() {
        try {
            List<ChangeLog> logs = restClient.get()
                    .uri("/api/admin/change-log")
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<ChangeLog>>() {
                    });
            return logs != null ? logs : List.of();
        } catch (RestClientException e) {
            return List.of();
        }
    }

    public record ChangeLog(Long changeLogId, Long userId, String loginId, String parameter,
                             String remoteAddr, String createdDate) {
    }
}
