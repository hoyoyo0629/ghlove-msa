package com.ghlove.donation.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** 지정기부사업 대표이미지처럼 <img src="..."> 로 직접 참조되는 업로드 파일을 정적
 *  리소스로 서빙한다 (증빙서류 등 다운로드 전용 파일은 기존처럼 별도 컨트롤러로 스트리밍
 *  - 그건 브라우저에 직접 임베드될 필요가 없어서 이 매핑을 타지 않는다). */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${donation.upload.dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = "file:" + uploadDir.replace("\\", "/") + "/";
        registry.addResourceHandler("/uploads/donation/**").addResourceLocations(location);
    }
}
