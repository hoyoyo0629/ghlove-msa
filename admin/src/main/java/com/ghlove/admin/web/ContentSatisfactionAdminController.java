package com.ghlove.admin.web;

import com.ghlove.admin.service.ContentSatisfactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 콘텐츠 만족도(AS-IS opmanager/cntnts-stsfdg) - 통계 메뉴 아래 '만족도 조사'.
 * list: URL별 만족도 집계. detail: URL 단위 요약 + 년도별 월 추이 스택막대차트
 * (AS-IS와 동일하게 chart.min.js + op.chart.js의 ChartCommon.drawChart 사용 - 두 파일을 AS-IS에서 그대로 복사).
 */
@Controller
@RequiredArgsConstructor
public class ContentSatisfactionAdminController {

    private final ContentSatisfactionService service;

    @GetMapping("/admin/content-satisfaction")
    public String list(@RequestParam(required = false, defaultValue = "") String menuUrl, Model model) {
        model.addAttribute("rows", service.list(menuUrl));
        model.addAttribute("menuUrl", menuUrl);
        return "content-satisfaction/list";
    }

    @GetMapping("/admin/content-satisfaction/detail")
    public String detail(@RequestParam String menuUrl,
                         @RequestParam(required = false, defaultValue = "") String year,
                         Model model) {
        model.addAttribute("summary", service.summary(menuUrl, year));
        model.addAttribute("monthRows", service.monthly(menuUrl, year));
        model.addAttribute("years", service.years(menuUrl));
        model.addAttribute("year", year);
        return "content-satisfaction/detail";
    }
}
