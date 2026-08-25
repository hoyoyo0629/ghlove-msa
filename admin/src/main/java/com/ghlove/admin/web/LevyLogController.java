package com.ghlove.admin.web;

import com.ghlove.admin.service.DonationClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.RestClientException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/** 연계 로그 관리 (AS-IS opmanager/log의 gif-stnd-buga/sunap, gif-seoul-buga/sunap 재구현) -
 *  세외수입 부과·전자기부금영수증 국세청 연계(NextBugaRequestLog/NextSunapRequestLog 대응).
 *  이 프로젝트는 표준/서울시 연계가 하나의 흐름(LocalTaxClient)으로 통합돼 있어 DONATION_LEVY
 *  전체를 한 화면으로 보여준다. */
@Controller
@RequiredArgsConstructor
@Slf4j
public class LevyLogController {

    private final DonationClient donationClient;

    @GetMapping("/log/levy")
    public String list(Model model) {
        model.addAttribute("logs", donationClient.levyLogs());
        model.addAttribute("relayLogs", donationClient.relayLogs());
        return "log/levy-list";
    }

    /** 완료 처리 로직에 부과/수납 연계가 붙기 전에 이미 COMPLETED된 기부 건의 이력 누락을 채운다. */
    @PostMapping("/log/levy/backfill")
    public String backfill() {
        try {
            var result = donationClient.backfillLevy();
            return "redirect:/log/levy?message=" + URLEncoder.encode(
                    "백필 완료: 신규 " + result.filled() + "건, 기존 " + result.alreadyOk() + "건, 실패 " + result.failed() + "건",
                    StandardCharsets.UTF_8);
        } catch (RestClientException e) {
            log.warn("Levy backfill call failed", e);
            return "redirect:/log/levy?errorMessage=" + URLEncoder.encode("백필 실행에 실패했습니다.", StandardCharsets.UTF_8);
        }
    }
}
