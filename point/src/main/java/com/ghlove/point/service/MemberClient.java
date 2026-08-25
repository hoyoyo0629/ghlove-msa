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

    public record MemberInfo(Long userId, String userName, String birthday) {
    }
}
