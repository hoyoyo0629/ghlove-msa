package com.ghlove.order.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * admin 전용 경로만 게이트한다 - 회원이 직접 쓰는 {@code /orders/**}(주문 생성/상세/취소/
 * 클레임 신청 등, JwtVerifier로 본인 확인)와 {@code /claims/my}(마이페이지)는 대상이 아니다.
 * {@code /orders/{orderId}/invoice}, {@code /orders/{orderId}/delivery-status}, {@code /claims},
 * {@code /claims/{id}/approve|reject|complete}는 원래 무인증이던 운영자 전용 경로라 이번에
 * 같이 게이트한다 (AdminApiAuthInterceptor 참고).
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final AdminApiAuthInterceptor adminApiAuthInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminApiAuthInterceptor)
                .addPathPatterns(
                        "/api/admin/orders", "/api/admin/orders/**",
                        "/api/admin/claims", "/api/admin/claims/**",
                        "/api/admin/excel-download-logs", "/api/admin/excel-download-logs/**",
                        "/claims", "/claims/*/approve", "/claims/*/reject", "/claims/*/complete",
                        "/orders/*/invoice", "/orders/*/delivery-status");
        // SFR-006 재검토 라운드 - /api/admin/orders/all(SFR-009 StatsService ReadModel
        // 재동기화용)이 시크릿 헤더 없이 무인증으로 남아있던 잔존 gap을 닫았다(admin의
        // OrderClient.allForResync()가 이제 시크릿을 보내므로 더 이상 예외처리가 필요 없다).
    }
}
