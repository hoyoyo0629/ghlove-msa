package com.ghlove.member.web;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final SessionRehydrateInterceptor sessionRehydrateInterceptor;
    private final InternalApiAuthInterceptor internalApiAuthInterceptor;

    /** D11 회원등급 아이콘 업로드 파일 서빙용 (UserLevelIconStorageService). */
    @Value("${ghlove.upload.dir}")
    private String uploadDir;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 내부 전용 경로는 공유 시크릿을 먼저 검증한다(SFR-002). /api/check-login-id(가입 중복확인,
        // 공개)는 /api/users/* 패턴에 걸리지 않으므로 열려 있다.
        registry.addInterceptor(internalApiAuthInterceptor)
                .addPathPatterns("/api/admin/**", "/api/users/*");
        registry.addInterceptor(sessionRehydrateInterceptor)
                .excludePathPatterns("/css/**", "/js/**", "/images/**", "/favicon.ico");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = "file:" + uploadDir.replace("\\", "/") + "/";
        registry.addResourceHandler("/uploads/**").addResourceLocations(location);
    }
}
