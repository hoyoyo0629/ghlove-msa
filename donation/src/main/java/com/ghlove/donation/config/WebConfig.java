package com.ghlove.donation.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.ghlove.donation.web.InternalApiAuthInterceptor;

/** 지정기부사업 대표이미지처럼 <img src="..."> 로 직접 참조되는 업로드 파일을 정적
 *  리소스로 서빙한다 (증빙서류 등 다운로드 전용 파일은 기존처럼 별도 컨트롤러로 스트리밍
 *  - 그건 브라우저에 직접 임베드될 필요가 없어서 이 매핑을 타지 않는다). */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    @Value("${donation.upload.dir}")
    private String uploadDir;

    private final InternalApiAuthInterceptor internalApiAuthInterceptor;

    /** SFR-003/007: 관리자·내부 전용 경로만 게이트한다. 대민(storefront)이 직접 쓰는
     *  /api/donate/**, /api/my/**, /api/designated-donation/**, /api/list-select/**,
     *  /api/policy/**, /api/locgovs, /interest-locgovs, 그리고 공개 /api/designated-projects·
     *  /api/give-state 는 대상이 아니다. /api/designated-projects/admin/** 만 게이트한다. */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(internalApiAuthInterceptor)
                .addPathPatterns(
                        "/api/admin/**",
                        "/api/locgov-admin/**",
                        "/api/designated-projects/admin/**",
                        "/api/cntr-reqmng/**",
                        "/api/ctbny-opratn/**",
                        "/api/offgive/**",
                        "/api/welfare-centers-admin/**",
                        "/api/nts-receipt-logs/**",
                        "/api/my-summary");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = "file:" + uploadDir.replace("\\", "/") + "/";
        registry.addResourceHandler("/uploads/donation/**").addResourceLocations(location);
    }
}
