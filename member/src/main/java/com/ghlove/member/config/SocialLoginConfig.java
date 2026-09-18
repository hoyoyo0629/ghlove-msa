package com.ghlove.member.config;

import com.ghlove.member.service.integration.KakaoCertClient;
import com.ghlove.member.service.integration.OAuth2LoginClient;
import com.ghlove.member.service.integration.OAuth2LoginClient.Provider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SocialLoginConfig {

    /**
     * 카카오는 일반 OAuth 로그인이 아니라 <b>인증서비스</b>(카카오톡 지갑 본인인증)를 쓴다 -
     * AS-IS도 프로필 조회형 카카오 로그인은 쓰지 않고 처음부터 인증서비스(CI 발급)로만 붙였다.
     * 그래서 네이버와 달리 OAuth2LoginClient가 아닌 전용 클라이언트를 둔다.
     */
    @Bean
    public KakaoCertClient kakaoCertClient(
            @Value("${ghlove.integrations.kakao.enabled}") boolean enabled,
            @Value("${ghlove.integrations.kakao.js-key}") String jsKey,
            @Value("${ghlove.integrations.kakao.settle-id}") String settleId,
            @Value("${ghlove.integrations.kakao.identify-items}") String identifyItems,
            @Value("${ghlove.integrations.kakao.login-redirect-uri}") String loginRedirectUri,
            @Value("${ghlove.integrations.kakao.join-redirect-uri}") String joinRedirectUri,
            @Value("${ghlove.integrations.kakao.toolkit.base-url}") String toolkitBaseUrl,
            @Value("${ghlove.integrations.kakao.toolkit.access-token}") String toolkitAccessToken,
            @Value("${ghlove.integrations.kakao.toolkit.verify-path}") String verifyPath) {
        return new KakaoCertClient(enabled, jsKey, settleId, identifyItems, loginRedirectUri,
                joinRedirectUri, toolkitBaseUrl, toolkitAccessToken, verifyPath);
    }

    @Bean
    public OAuth2LoginClient naverLoginClient(
            @Value("${ghlove.integrations.naver.enabled}") boolean enabled,
            @Value("${ghlove.integrations.naver.authorize-url}") String authorizeUrl,
            @Value("${ghlove.integrations.naver.token-url}") String tokenUrl,
            @Value("${ghlove.integrations.naver.profile-url}") String profileUrl,
            @Value("${ghlove.integrations.naver.client-id}") String clientId,
            @Value("${ghlove.integrations.naver.client-secret}") String clientSecret,
            @Value("${ghlove.integrations.naver.redirect-uri}") String redirectUri) {
        return new OAuth2LoginClient(Provider.NAVER, enabled, authorizeUrl, tokenUrl, profileUrl,
                clientId, clientSecret, redirectUri);
    }
}
