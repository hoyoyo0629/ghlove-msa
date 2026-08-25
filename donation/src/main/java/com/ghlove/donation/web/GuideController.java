package com.ghlove.donation.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** 안내사항 GNB 하위 정적 안내 페이지 (AS-IS donation/guide1·guide2·guide5·guide6.html).
 * 로그인 여부와 무관한 순수 정적 콘텐츠라 모델 데이터가 필요 없다. */
@Controller
public class GuideController {

    @GetMapping("/guide1")
    public String donationGuide() {
        return "guide/guide1";
    }

    @GetMapping("/guide2")
    public String onlineGuide() {
        return "guide/guide2";
    }

    @GetMapping("/guide5")
    public String offlineGuide() {
        return "guide/guide5";
    }

    @GetMapping("/guide6")
    public String cautionGuide() {
        return "guide/guide6";
    }
}
