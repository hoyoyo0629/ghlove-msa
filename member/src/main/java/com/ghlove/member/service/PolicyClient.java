package com.ghlove.member.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Optional;

/**
 * 회원가입 약관동의 요약 박스(이용약관/개인정보 수집·이용 동의) 본문을 admin 서비스
 * (약관관리, OP_POLICY)에서 읽어온다 - donation의 PolicyClient와 같은 패턴.
 *
 * <p>AS-IS는 saleson.api.user.JoinController.getPolicyInfo가 같은 조회를 해서
 * {@code policy}(타입0 이용약관)·{@code collectionAgree}(타입6 개인정보 수집·이용 동의)로
 * 내려준다(타입4 "개인정보제3자동의"도 같이 받지만 AS-IS agree.vue가 화면에 쓰지 않아 여기서도
 * 안 받는다). TO-BE는 admin의 {@code /api/policies/{name}}을 재사용한다(clause=타입0,
 * collection=타입6).
 *
 * <p>전시중인 본문이 없으면(admin이 404) {@link Optional#empty()} - 호출측이 그때는 약관박스를
 * 비워 보여준다(법정 고지와 달리 회원가입 자체를 막을 필요는 없다, 체크박스는 여전히 필수로 검증됨).
 */
@Component
@Slf4j
public class PolicyClient {

    private final RestClient restClient;

    public PolicyClient(@Value("${ghlove.admin-service.base-url}") String adminServiceBaseUrl) {
        this.restClient = RestClient.create(adminServiceBaseUrl);
    }

    public record PolicyContent(String title, String content) {
    }

    /** @param name admin PolicyApiController와 같은 이름 - clause(이용약관) / collection(개인정보 수집·이용 동의) */
    public Optional<PolicyContent> currentPolicy(String name) {
        try {
            PolicyContent policy = restClient.get()
                    .uri("/api/policies/{name}", name)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> { })
                    .body(PolicyContent.class);
            if (policy == null || policy.content() == null || policy.content().isBlank()) {
                return Optional.empty();
            }
            return Optional.of(policy);
        } catch (RestClientException e) {
            log.warn("Failed to fetch policy '{}' from admin service", name, e);
            return Optional.empty();
        }
    }
}
