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
                        "/site-config", "/site-config/**", "/policy", "/policy/**", "/community/**",
                        "/seller", "/seller/**", "/brand", "/brand/**",
                        "/coupon", "/coupon/**", "/coupon-regular", "/coupon-regular/**",
                        "/admin/orders", "/admin/orders/**", "/admin/claims", "/admin/claims/**",
                        "/admin/locgovs", "/admin/locgovs/**",
                        "/admin/data-board", "/admin/data-board/**", "/admin/menus", "/admin/menus/**",
                        "/admin/search-keywords", "/admin/search-keywords/**", "/admin/seo", "/admin/seo/**",
                        "/admin/managers", "/admin/managers/**", "/admin/members", "/admin/members/**",
                        "/admin/secede-users", "/admin/secede-users/**", "/admin/sleep-users", "/admin/sleep-users/**",
                        "/admin/gift-categories", "/admin/gift-categories/**", "/admin/gift-items", "/admin/gift-items/**",
                        "/admin/gift-inquiries", "/admin/gift-inquiries/**",
                        "/admin/main-banners", "/admin/main-banners/**", "/admin",
                        "/admin/style-books", "/admin/style-books/**",
                        "/admin/main-display",
                        "/admin/person-in-charge", "/admin/person-in-charge/**",
                        "/admin/off-person-in-charge", "/admin/off-person-in-charge/**",
                        "/admin/roles", "/admin/roles/**",
                        "/admin/user-levels", "/admin/user-levels/**",
                        "/admin/welfare-centers", "/admin/welfare-centers/**",
                        "/admin/send-mail-logs", "/admin/send-mail-logs/**", "/admin/send-sms-logs",
                        "/admin/ums", "/admin/ums/**",
                        "/admin/shop-inquiries", "/admin/shop-inquiries/**",
                        "/admin/maintenance", "/admin/maintenance/**",
                        "/admin/manuals", "/admin/manuals/**",
                        "/admin/seller-notices", "/admin/seller-notices/**",
                        "/admin/surveys", "/admin/surveys/**",
                        "/admin/honor-users", "/admin/honor-users/**",
                        "/admin/featured", "/admin/featured/**",
                        "/admin/mobile-category-edit", "/admin/mobile-category-edit/**",
                        "/admin/excel-download-logs", "/admin/excel-download-logs/**",
                        "/admin/order-agency", "/admin/order-agency/**",
                        "/admin/pay-info", "/admin/pay-info/**",
                        "/admin/gift-reviews", "/admin/gift-reviews/**",
                        "/admin/category-teams", "/admin/category-teams/**",
                        "/admin/nts-receipt-logs", "/admin/nts-receipt-logs/**")
                .excludePathPatterns("/admin/login", "/admin/logout",
                        "/admin/manager-requests/new", "/admin/manager-requests/complete");
    }
}
