package com.ghlove.order.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.LocalDateTime;
import java.util.List;

/** 쿠폰 자동발급(회원가입/생일) 대상 판정용 - member의 회원 스냅샷을 읽기 전용으로 조회한다.
 *  member는 별도 이벤트를 발행하지 않으므로(Kafka 미사용) admin의 ReadModel 재동기화와 동일한
 *  방식으로, 매일 스케줄러가 이 스냅샷을 폴링해 신규가입/생일 대상을 판정한다. */
@Component
public class MemberClient {

    private final RestClient restClient;

    public MemberClient(@Value("${ghlove.member-service.base-url}") String memberServiceBaseUrl) {
        this.restClient = RestClient.create(memberServiceBaseUrl);
    }

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
}
