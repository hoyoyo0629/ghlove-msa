package com.ghlove.member.service.integration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 표준 OAuth2 authorization-code 흐름을 쓰는 SNS 로그인 연계(카카오/네이버) 공용
 * 클라이언트. 두 공급자 모두 authorize→token→profile 3단계는 동일하고 프로필 응답
 * JSON 모양만 달라, provider별로 파싱만 분기한다 (AS-IS는 각각 별도 컨트롤러였지만
 * 실질적으로 같은 프로토콜이라 하나로 합쳤다).
 */
@Slf4j
public class OAuth2LoginClient {

    public enum Provider { KAKAO, NAVER }

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
        if (provider == Provider.KAKAO) {
            Object id = profile.get("id");
            Map<String, Object> kakaoAccount = (Map<String, Object>) profile.getOrDefault("kakao_account", Map.of());
            Map<String, Object> kakaoProfile = (Map<String, Object>) kakaoAccount.getOrDefault("profile", Map.of());
            return new ExternalIdentity("KAKAO", String.valueOf(id),
                    (String) kakaoProfile.get("nickname"), (String) kakaoAccount.get("email"), null);
        }
        // NAVER: 실제 응답은 { "response": { "id", "email", "name" } }
        Map<String, Object> response = (Map<String, Object>) profile.getOrDefault("response", profile);
        return new ExternalIdentity("NAVER", String.valueOf(response.get("id")),
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
