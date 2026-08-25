package com.ghlove.member.config;

import com.ghlove.member.service.integration.OAuth2LoginClient;
import com.ghlove.member.service.integration.OAuth2LoginClient.Provider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SocialLoginConfig {

    @Bean
    public OAuth2LoginClient kakaoLoginClient(
            @Value("${ghlove.integrations.kakao.enabled}") boolean enabled,
            @Value("${ghlove.integrations.kakao.authorize-url}") String authorizeUrl,
            @Value("${ghlove.integrations.kakao.token-url}") String tokenUrl,
            @Value("${ghlove.integrations.kakao.profile-url}") String profileUrl,
            @Value("${ghlove.integrations.kakao.client-id}") String clientId,
            @Value("${ghlove.integrations.kakao.client-secret}") String clientSecret,
            @Value("${ghlove.integrations.kakao.redirect-uri}") String redirectUri) {
        return new OAuth2LoginClient(Provider.KAKAO, enabled, authorizeUrl, tokenUrl, profileUrl,
                clientId, clientSecret, redirectUri);
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
