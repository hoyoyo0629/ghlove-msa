package com.ghlove.point.config;

import com.ghlove.point.web.InternalApiAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * SFR-004/007: 관리자 전용 경로(/api/admin/**: 전체 원장 조회, 기부 크레딧, 지자체 적립률
 * 등록/변경, 포인트 이력·통계)만 게이트한다. 대민(storefront)이 직접 쓰는 /api/my/**,
 * /use, /reservations 및 서비스간 조회(/api/balance 등)는 대상이 아니다.
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final InternalApiAuthInterceptor internalApiAuthInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(internalApiAuthInterceptor)
                .addPathPatterns("/api/admin/**");
    }
}
