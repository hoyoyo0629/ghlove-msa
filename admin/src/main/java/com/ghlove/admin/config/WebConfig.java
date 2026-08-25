package com.ghlove.admin.config;

import com.ghlove.admin.web.AdminAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    @Value("${admin.upload.dir}")
    private String uploadDir;

    private final AdminAuthInterceptor adminAuthInterceptor;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = "file:" + uploadDir.replace("\\", "/") + "/";
        registry.addResourceHandler("/uploads/**").addResourceLocations(location);
    }

    /** 운영관리 콘솔 경로만 게이트한다 - 고객센터 공개 콘텐츠(/notices, /data-board, /events,
     *  /faqs, /qna, /api/**)와 지자체 담당자용 /auth/cert-login*은 대상이 아니다.
     *  /admin/manager-requests/new(회원 로그인만 필요, 관리자 아닌 사람이 신청하는 화면)와
     *  /admin/manager-requests/complete는 의도적으로 제외 - 같은 /admin/manager-requests
     *  경로라도 승인관리 목록(GET, 관리자 전용)과 approve/reject만 게이트한다. */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminAuthInterceptor)
                .addPathPatterns("/codes", "/codes/**", "/banners", "/banners/**", "/popups", "/popups/**",
                        "/admin/notices", "/admin/notices/**", "/stats", "/stats/**",
                        "/settlements", "/settlements/**", "/delivery-tracking", "/batch/**",
                        "/admin/manager-requests", "/admin/manager-requests/*/*/approve", "/admin/manager-requests/*/*/reject",
                        "/give-state", "/give-state/**", "/give-operation", "/give-operation/**",
                        "/give-point", "/give-point/**", "/give-reqmng", "/give-reqmng/**",
                        "/give-statistics", "/designated-projects", "/designated-projects/**",
                        "/offgive", "/offgive/**", "/qna-admin", "/qna-admin/**",
                        "/reconciliation/**", "/shop-statistics/**", "/access", "/access/**",
                        "/log/**", "/privacy-log/**", "/admin/my-cert", "/admin/my-cert/**",
                        "/mail-config", "/mail-config/**", "/isms-config", "/isms-config/**",
                        "/email", "/email/**", "/batch-job", "/batch-job/**",
                        "/site-config", "/policy", "/policy/**", "/community/**",
                        "/seller", "/seller/**", "/brand", "/brand/**",
                        "/coupon", "/coupon/**", "/coupon-regular", "/coupon-regular/**")
                .excludePathPatterns("/admin/login", "/admin/logout",
                        "/admin/manager-requests/new", "/admin/manager-requests/complete");
    }
}
