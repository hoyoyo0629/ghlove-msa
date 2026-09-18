package com.ghlove.donation.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Optional;

/**
 * 개인정보처리방침/이용약관/저작권정책 본문을 admin 서비스(약관관리, OP_POLICY)에서 읽어온다 -
 * NoticeClient와 동일한 읽기 전용 동기 조회 패턴.
 *
 * <p>AS-IS는 이 셋을 OP_POLICY에 두고 opmanager 약관관리 화면에서 개정했고, 공개 화면은
 * {@code /api/policy/clause|protect|copyright}로 받아 뿌렸다. TO-BE도 편집 주체는 admin이므로
 * 여기서 admin을 원본으로 삼는다 - 그래야 운영자가 재배포 없이 약관을 개정할 수 있다.
 *
 * <p>전시중인 본문이 아직 없으면(admin이 404) {@link Optional#empty()}를 돌려준다. 호출측은
 * 그때 서비스에 들어있는 기존 원문(policy-content/*.html)으로 폴백한다 - 법정 고지 문서라
 * DB 적재 전에 화면이 비어버리면 안 된다.
 */
@Component
@Slf4j
public class PolicyClient {

    private final RestClient restClient;

    public PolicyClient(@Value("${ghlove.admin-service.base-url}") String adminServiceBaseUrl) {
        this.restClient = RestClient.create(adminServiceBaseUrl);
    }

    /** admin PolicyApiController.PolicyDto와 같은 모양(필요한 필드만). */
    public record PolicyContent(String title, String content) {
    }

    /**
     * @param name AS-IS API와 같은 이름 - clause(이용약관) / protect(개인정보처리방침) / copyright(저작권정책)
     */
    public Optional<PolicyContent> currentPolicy(String name) {
        try {
            PolicyContent policy = restClient.get()
                    .uri("/api/policies/{name}", name)
                    .retrieve()
                    // 전시중인 본문이 없을 뿐이라 오류가 아니다 - 폴백하도록 빈 값으로 돌린다.
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> { })
                    .body(PolicyContent.class);
            if (policy == null || policy.content() == null || policy.content().isBlank()) {
                return Optional.empty();
            }
            return Optional.of(policy);
        } catch (RestClientException e) {
            log.warn("Failed to fetch policy '{}' from admin service - falling back to bundled content", name, e);
            return Optional.empty();
        }
    }
}
