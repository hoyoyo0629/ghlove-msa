package com.ghlove.member.service.integration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.UUID;

/**
 * 디지털원패스(행정안전부 SAML 통합인증) 연계 (AS-IS AuthController `/onepass-login`,
 * `/onepass-callback`, SDK `kr.go.onepass.client.*`). 실제 SAML AuthnRequest
 * 서명/응답 검증은 OpenSAML 등 전용 라이브러리와 실제 IdP 메타데이터가 있어야 하므로,
 * 여기서는 리다이렉트 URL 구성과 콜백 파라미터에서 신원 속성(CI/성명/생년월일)을 읽어오는
 * 연계 지점 자체를 재현한다 - 방화벽이 열리고 실제 IdP 메타데이터가 확보되면
 * verifyCallback() 내부만 SAML 응답 검증 로직으로 교체하면 된다.
 */
@Component
@Slf4j
public class OnePassClient {

    private final boolean enabled;
    private final String idpUrl;
    private final String siteId;
    private final String returnUrl;

    public OnePassClient(
            @Value("${ghlove.integrations.onepass.enabled}") boolean enabled,
            @Value("${ghlove.integrations.onepass.idp-url}") String idpUrl,
            @Value("${ghlove.integrations.onepass.site-id}") String siteId,
            @Value("${ghlove.integrations.onepass.return-url}") String returnUrl) {
        this.enabled = enabled;
        this.idpUrl = idpUrl;
        this.siteId = siteId;
        this.returnUrl = returnUrl;
    }

    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 디지털원패스 연동해지 (AS-IS ApiSendHandler.InterLockRelease - 원패스 연계해지 API).
     * 실연계가 열리면 원패스 연계해지 API를 호출한다 - 지금은 로그인과 마찬가지로 실연계가
     * 꺼져 있어(enabled=false) 통과시킨다(호출측이 이어서 내부 탈퇴 처리를 한다). 방화벽/실
     * IdP가 열리면 이 메서드 내부만 실제 연계해지 호출로 교체하면 된다.
     * @return 연계해지 성공 여부
     */
    public boolean releaseInterlock() {
        if (!enabled) {
            return true;
        }
        // TODO 실연계 개방 시 원패스 연계해지(InterLockRelease) 호출로 교체
        return true;
    }

    public String buildLoginRedirectUrl() {
        String state = UUID.randomUUID().toString();
        return UriComponentsBuilder.fromHttpUrl(idpUrl)
                .queryParam("siteId", siteId)
                .queryParam("returnUrl", returnUrl)
                .queryParam("state", state)
                .toUriString();
    }

    /** 콜백 파라미터(ci/name/birthday)를 신원 정보로 변환 - 실제 연동 시 SAML assertion 검증으로 대체. */
    public ExternalIdentity verifyCallback(Map<String, String> callbackParams) {
        String ci = callbackParams.get("ci");
        if (ci == null || ci.isBlank()) {
            throw new IllegalArgumentException("디지털원패스 인증 결과에 CI 값이 없습니다.");
        }
        // 디지털원패스도 본인확인(CI) 기반이라 externalId와 ci가 같은 값이다 - ci가 채워져야
        // 다른 본인인증 수단으로 이미 가입한 회원과 같은 사람으로 연결된다.
        return new ExternalIdentity("ONEPASS", ci, callbackParams.get("name"), null,
                callbackParams.get("birthday"), callbackParams.get("phoneNumber"), null, ci);
    }
}
