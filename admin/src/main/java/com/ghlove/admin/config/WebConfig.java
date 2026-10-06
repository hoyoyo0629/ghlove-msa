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

    /**
     * 업로드 파일을 <b>두 경로로 같이</b> 서비스한다.
     * <ul>
     *   <li>{@code /uploads/**} - admin 콘솔이 자기 화면에서 쓰는 경로(기존 데이터도 이 형태)</li>
     *   <li>{@code /admin/uploads/**} - <b>storefront가 쓰는 경로.</b> 스토어프론트는 서비스
     *       접두사 규칙(Kong strip_path = Vite 프록시)으로 호출하므로
     *       {@code /admin/uploads/...}가 8086의 {@code /uploads/...}로 들어온다
     *       (답례품이 {@code /gift/uploads/...}를 쓰는 것과 같은 규칙)</li>
     * </ul>
     * 에디터로 넣은 이미지는 <b>본문 HTML에 그대로 박혀 스토어프론트에서 렌더</b>되므로,
     * 업로드 시점에 접두사까지 포함한 경로를 심어야 양쪽에서 다 보인다(AS-IS는 이 자리에
     * 절대 CDN URL을 심어 같은 문제를 피했다). 2026-10-06: 팝업 본문 이미지가 5173에서
     * 404나던 원인이다.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = "file:" + uploadDir.replace("\\", "/") + "/";
        registry.addResourceHandler("/uploads/**", "/admin/uploads/**")
                .addResourceLocations(location);
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
                        "/admin/notices", "/admin/notices/**",
                        // 지자체공지사항(메뉴 5110) - 공지사항(5111)과 같은 표를 쓰고 조회범위만 다르다
                        "/admin/locgov-notices", "/stats", "/stats/**",
                        "/settlements", "/settlements/**", "/delivery-tracking", "/batch/**",
                        "/admin/manager-requests", "/admin/manager-requests/*/*/approve", "/admin/manager-requests/*/*/reject",
                        "/admin/manager-requests/popup/**",
                        "/give-state", "/give-state/**", "/give-operation", "/give-operation/**",
                        "/give-point", "/give-point/**", "/give-reqmng", "/give-reqmng/**",
                        "/give-statistics", "/designated-projects", "/designated-projects/**",
                        "/offgive", "/offgive/**", "/qna-admin", "/qna-admin/**",
                        // 고객센터 FAQ 관리(메뉴 5104) - 공개 /faqs와 같은 표를 쓰지만 이쪽은 운영자만
                        "/faq-admin", "/faq-admin/**",
                        "/reconciliation/**", "/shop-statistics/**", "/access", "/access/**",
                        "/log/**", "/privacy-log/**", "/admin/my-cert", "/admin/my-cert/**",
                        "/mail-config", "/mail-config/**", "/isms-config", "/isms-config/**",
                        "/email", "/email/**", "/batch-job", "/batch-job/**",
                        "/message", "/message/**", "/batch-log", "/batch-log/**",
                        // 개인정보 엑셀 다운로드 사유 기록 - 로그인한 운영자만 (공개 /common/message는 제외)
                        "/common/opmanager/privacy-access-log", "/common/opmanager/privacy-access-log-update",
                        "/site-config", "/site-config/**", "/policy", "/policy/**", "/community/**",
                        "/seller", "/seller/**", "/brand", "/brand/**",
                        "/coupon", "/coupon/**", "/coupon-regular", "/coupon-regular/**",
                        "/admin/orders", "/admin/orders/**", "/admin/claims", "/admin/claims/**",
                        "/admin/locgovs", "/admin/locgovs/**",
                        // 도로명주소 검색 팝업 - 운영자만 쓰는 공통 팝업이라 같이 게이트한다
                        "/admin/juso-popup",
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
                        // 1:1 문의(메뉴 5102) - 공개 Q&A(5112)와 같은 OP_QNA를 QNA_TYPE으로 갈라 본다.
                        // 옛 /admin/shop-inquiries는 AS-IS 입점문의를 잘못 올려 둔 화면이라 제거했다.
                        "/admin/inquiries", "/admin/inquiries/**",
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
                        "/admin/nts-receipt-logs", "/admin/nts-receipt-logs/**",
                        // 스마트에디터 사진·동영상·CTP 팝업(AS-IS SmartEditorController). 운영자 화면에서만
                        // 열리고 파일을 디스크에 쓰므로 로그인 게이트 안에 둔다 - AS-IS는 프론트 공용 URL이라
                        // 열려 있었지만, TO-BE admin은 운영자 전용 서비스라 공개할 이유가 없다(의도적 편차).
                        "/smarteditor/**")
                .excludePathPatterns("/admin/login", "/admin/logout",
                        "/admin/manager-requests/new", "/admin/manager-requests/complete");
    }
}
