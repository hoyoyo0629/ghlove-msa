package com.ghlove.admin.web;

import com.ghlove.admin.service.StatisticsLocgovClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/** 관심 지자체 통계 (AS-IS opmanager/statistics - StatisticsLocgovManagerController, B11).
 *  마이페이지 "관심지자체"(G_INTRST_LOCGOV) 등록행을 donation 서비스에서 월별/지자체별로
 *  집계해온 결과를 그대로 보여준다 - 목록은 연도별 지자체 등록건수(검색 가능), 상세는
 *  지자체 하나의 월별 추이. */
@Controller
@RequiredArgsConstructor
public class StatisticsLocgovController {

    private final StatisticsLocgovClient client;

    @GetMapping("/statistics/interest-locgov")
    public String list(@RequestParam(required = false) String year,
                        @RequestParam(required = false) String locgovNm,
                        Model model) {
        StatisticsLocgovClient.ListResult result = client.search(year, locgovNm);
        model.addAttribute("result", result);
        model.addAttribute("years", client.years());
        model.addAttribute("year", year);
        model.addAttribute("locgovNm", locgovNm);
        return "statistics-locgov/list";
    }

    @GetMapping("/statistics/interest-locgov/{locgovCode}")
    public String detail(@PathVariable String locgovCode,
                          @RequestParam(required = false) String year,
                          Model model) {
        model.addAttribute("result", client.detail(locgovCode, year));
        model.addAttribute("years", client.years());
        model.addAttribute("year", year);
        return "statistics-locgov/detail";
    }
}
