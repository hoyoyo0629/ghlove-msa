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
        return new ExternalIdentity("ONEPASS", ci, callbackParams.get("name"), null, callbackParams.get("birthday"));
    }
}
