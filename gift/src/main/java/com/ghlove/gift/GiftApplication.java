package com.ghlove.gift;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/** @EnableAsync - 답례품 이미지 썸네일 자동생성(ThumbnailService)이 비동기로 동작하는 데 필요. */
@EnableAsync
@SpringBootApplication
public class GiftApplication {
    public static void main(String[] args) {
        SpringApplication.run(GiftApplication.class, args);
    }
}
