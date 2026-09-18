package com.ghlove.donation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

/**
 * footer 3개 정책문서(개인정보처리방침/저작권정책/이용약관)의 본문 조회 - Thymeleaf 화면
 * ({@code PolicyController})과 SPA용 JSON API({@code PolicyApiController})가 같은 원본을
 * 같은 규칙으로 읽어야 해서 한 곳에 모았다.
 *
 * <p>원본은 admin 서비스의 약관관리(OP_POLICY)다. 아직 전시중인 본문이 등록되지 않았으면
 * 서비스에 들어있는 원문(policy-content/*.html)으로 폴백한다 - 이 폴백은 DB 적재가 끝나면
 * 지워도 되는 과도기 장치이고, 그때까지는 admin에서 등록한 내용이 우선한다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PolicyContentService {

    /** 화면 슬러그 → (기본 제목, admin API 이름, 폴백 리소스 이름). */
    private static final Map<String, PolicySlug> SLUGS = Map.of(
            "privacy", new PolicySlug("개인정보처리방침", "protect", "privacy"),
            "copyright", new PolicySlug("저작권 정책", "copyright", "copyright"),
            "auth", new PolicySlug("이용약관 동의", "clause", "clause"));

    private record PolicySlug(String defaultTitle, String adminName, String resourceName) {
    }

    public record PolicyContent(String title, String content) {
    }

    private final PolicyClient policyClient;

    public boolean supports(String slug) {
        return SLUGS.containsKey(slug);
    }

    public Optional<PolicyContent> find(String slug) {
        PolicySlug policySlug = SLUGS.get(slug);
        if (policySlug == null) {
            return Optional.empty();
        }
        return Optional.of(policyClient.currentPolicy(policySlug.adminName())
                // 제목도 admin에서 관리하지만, 비어 있으면 화면 제목이 사라지므로 기본값을 쓴다.
                .map(p -> new PolicyContent(
                        p.title() == null || p.title().isBlank() ? policySlug.defaultTitle() : p.title(),
                        p.content()))
                .orElseGet(() -> new PolicyContent(policySlug.defaultTitle(),
                        loadBundledContent(policySlug.resourceName()))));
    }

    private String loadBundledContent(String name) {
        try {
            byte[] bytes = new ClassPathResource("policy-content/" + name + ".html").getContentAsByteArray();
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
