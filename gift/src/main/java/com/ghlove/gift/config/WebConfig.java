package com.ghlove.gift.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.ghlove.gift.web.InternalApiAuthInterceptor;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    @Value("${gift.upload.dir}")
    private String uploadDir;

    private final InternalApiAuthInterceptor internalApiAuthInterceptor;

    /** SFR-005/007: 관리자 전용 경로(/api/admin/**: 답례품·카테고리·승인/반려·문의답변·후기·
     *  전시·랭킹 관리)만 게이트한다. 대민(storefront)이 직접 쓰는 /api/gifts/**, /api/wishlist,
     *  /api/my/**, /wishlist/** 는 대상이 아니다. */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(internalApiAuthInterceptor)
                .addPathPatterns("/api/admin/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = "file:" + uploadDir.replace("\\", "/") + "/";
        registry.addResourceHandler("/uploads/**").addResourceLocations(location);
    }
}
