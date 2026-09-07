package com.ghlove.admin.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.LocalDateTime;
import java.util.List;

/** 관리자권한요청 폼 프리필용 - 신청자는 JWT로 이미 신원이 확인된 기존 회원이라(SFR-010),
 *  member 서비스의 GET /api/users/{id}에서 이름/휴대폰/이메일/생년월일을 그대로 받아온다. */
@Component
public class MemberClient {

    private final RestClient restClient;

    public MemberClient(@Value("${ghlove.member-service.base-url}") String memberServiceBaseUrl) {
        this.restClient = RestClient.create(memberServiceBaseUrl);
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
                              String phoneNumber, String email) {
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
