package com.ghlove.member.service.integration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 표준 OAuth2 authorization-code 흐름을 쓰는 SNS 로그인 연계 클라이언트.
 *
 * <p>현재 이 방식을 쓰는 것은 네이버뿐이다 - 카카오는 프로필 조회형 로그인이 아니라
 * 인증서비스(카카오톡 지갑 본인인증, CI 발급)로 붙기 때문에 {@link KakaoCertClient}가
 * 따로 있다(AS-IS도 카카오는 처음부터 인증서비스만 썼다). provider별로 프로필 응답
 * JSON 모양만 다르므로 파싱 지점만 분기해 둔다.
 */
@Slf4j
public class OAuth2LoginClient {

    public enum Provider { NAVER }

    private final Provider provider;
    private final boolean enabled;
    private final String authorizeUrl;
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;
    private final RestClient tokenClient;
    private final RestClient profileClient;
    private final String profileUrl;

    public OAuth2LoginClient(Provider provider, boolean enabled, String authorizeUrl, String tokenUrl,
                              String profileUrl, String clientId, String clientSecret, String redirectUri) {
        this.provider = provider;
        this.enabled = enabled;
        this.authorizeUrl = authorizeUrl;
        this.profileUrl = profileUrl;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
        this.tokenClient = RestClient.create(tokenUrl);
        this.profileClient = RestClient.create();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String buildAuthorizeUrl(String state) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(authorizeUrl)
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("response_type", "code")
                .queryParam("state", state);
        return builder.toUriString();
    }

    @SuppressWarnings("unchecked")
    public ExternalIdentity exchange(String code) {
        Map<String, String> tokenForm = new LinkedHashMap<>();
        tokenForm.put("grant_type", "authorization_code");
        tokenForm.put("client_id", clientId);
        tokenForm.put("client_secret", clientSecret);
        tokenForm.put("redirect_uri", redirectUri);
        tokenForm.put("code", code);

        Map<String, Object> tokenResponse = tokenClient.post()
                .body(toFormBody(tokenForm))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .retrieve().body(Map.class);
        Object accessToken = tokenResponse != null ? tokenResponse.get("access_token") : null;
        if (accessToken == null) {
            throw new IllegalStateException(provider + " 액세스 토큰 발급에 실패했습니다.");
        }

        Map<String, Object> profile = profileClient.get().uri(profileUrl)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve().body(Map.class);
        return parseProfile(profile);
    }

    @SuppressWarnings("unchecked")
    private ExternalIdentity parseProfile(Map<String, Object> profile) {
        if (profile == null) {
            throw new IllegalStateException(provider + " 프로필 조회 결과가 비어 있습니다.");
        }
        // NAVER: 실제 응답은 { "response": { "id", "email", "name" } }
        Map<String, Object> response = (Map<String, Object>) profile.getOrDefault("response", profile);
        return ExternalIdentity.ofProfile("NAVER", String.valueOf(response.get("id")),
                (String) response.get("name"), (String) response.get("email"), null);
    }

    private String toFormBody(Map<String, String> form) {
        StringBuilder sb = new StringBuilder();
        form.forEach((k, v) -> {
            if (!sb.isEmpty()) {
                sb.append('&');
            }
            sb.append(k).append('=').append(v == null ? "" : v);
        });
        return sb.toString();
    }
}
