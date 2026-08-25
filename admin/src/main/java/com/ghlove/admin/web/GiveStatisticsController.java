package com.ghlove.admin.web;

import com.ghlove.admin.service.GiveStateService;
import com.ghlove.admin.service.GiveStatisticsService;
import com.ghlove.admin.service.LocgovClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.LinkedHashMap;
import java.util.Map;

/** 기부 통계 (AS-IS opmanager/give/statistics) - AS-IS의 8개+ 화면(전체/지자체별/기부인원/
 *  기부금액/기부건수/개인별/일자별/운영통계 + 외국인 변형) 중 지자체별·운영통계는 이미
 *  구현된 기능(give-state/give-operation/stats)과 중복이라 제외하고, 나머지를 하나의
 *  통합 화면으로 재구성했다 - 사용자 확인 하에 결정된 스코프(제안요청서 SFR-007/009가
 *  화면 단위가 아닌 분석 내용 단위로 요구사항을 기술하는 점 확인 후). 외국인 변형은 이
 *  시스템에 내/외국인 구분 자체가 없어 제외. */
@Controller
@RequiredArgsConstructor
public class GiveStatisticsController {

    private final GiveStatisticsService giveStatisticsService;
    private final GiveStateService giveStateService;
    private final LocgovClient locgovClient;

    @GetMapping("/give-statistics")
    public String view(@RequestParam(required = false) String cntrYear,
                        @RequestParam(required = false) String upperLocgovCode,
                        @RequestParam(required = false) String locgovCode,
                        Model model) {
        model.addAttribute("years", giveStateService.availableYears());
        model.addAttribute("provinces", provinces());
        model.addAttribute("allLocgovs", locgovClient.allLocgovs());
        model.addAttribute("cntrYear", cntrYear);
        model.addAttribute("upperLocgovCode", upperLocgovCode);
        model.addAttribute("locgovCode", locgovCode);

        model.addAttribute("monthRows", giveStatisticsService.monthlyTrend(cntrYear, upperLocgovCode, locgovCode));
        model.addAttribute("ageRows", giveStatisticsService.ageBracket(cntrYear, upperLocgovCode, locgovCode));
        model.addAttribute("amountRows", giveStatisticsService.amountBracket(cntrYear, upperLocgovCode, locgovCode));
        model.addAttribute("personalRows", giveStatisticsService.personalRanking(cntrYear, upperLocgovCode, locgovCode, 20));
        model.addAttribute("levyRows", giveStatisticsService.levyReport(cntrYear, upperLocgovCode, locgovCode));
        return "give/give-statistics";
    }

    private Map<String, String> provinces() {
        Map<String, String> map = new LinkedHashMap<>();
        for (LocgovClient.LocgovInfo l : locgovClient.allLocgovs()) {
            map.putIfAbsent(l.upperLocgovCode(), l.upperLocgovNm());
        }
        return map;
    }
}
