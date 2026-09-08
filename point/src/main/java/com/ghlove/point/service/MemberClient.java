package com.ghlove.point.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * 마이페이지 "기부포인트 조회" 화면의 "OOO 회원님" 배지 표시용 - point 서비스는 로그인
 * 세션이 없어(게이트웨이 미연동) donation/MemberClient와 동일한 패턴으로 동기 REST 조회한다.
 */
@Component
public class MemberClient {

    private final RestClient restClient;

    public MemberClient(@Value("${ghlove.member-service.base-url}") String memberServiceBaseUrl,
                        @Value("${ghlove.internal.admin-secret}") String adminSecret) {
        // member의 /api/users/{id} 는 내부 전용이라 공유 시크릿을 실어야 통과한다.
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

    public record MemberInfo(Long userId, String userName, String birthday) {
    }
}
