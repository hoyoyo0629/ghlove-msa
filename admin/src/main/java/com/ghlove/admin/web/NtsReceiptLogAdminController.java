package com.ghlove.admin.web;

import com.ghlove.admin.service.NtsReceiptLogClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.LinkedHashMap;
import java.util.Map;

/** 기부금영수증 국세청연계 로그 조회 (AS-IS opmanager/log - LogManagerController의
 * gif-stnd-buga/gif-stnd-sunap/gif-seoul-buga/gif-seoul-sunap 4화면을 하나로 통합).
 * 국세청 연계 자체가 아직 이 프로젝트에 없어 조회 전용 - 실제 연동이 붙으면 donation
 * 서비스가 G_NTS_RCIPT_LOG에 기록하도록 이어붙이면 된다. */
@Controller
@RequestMapping("/admin/nts-receipt-logs")
@RequiredArgsConstructor
public class NtsReceiptLogAdminController {

    private static final Map<String, String> TYPE_LABELS = new LinkedHashMap<>();
    static {
        TYPE_LABELS.put("STND_BUGA", "표준양식 - 부가(발급)");
        TYPE_LABELS.put("STND_SUNAP", "표준양식 - 승인");
        TYPE_LABELS.put("SEOUL_BUGA", "서울시양식 - 부가(발급)");
        TYPE_LABELS.put("SEOUL_SUNAP", "서울시양식 - 승인");
    }

    private final NtsReceiptLogClient client;

    @GetMapping
    public String list(@RequestParam(required = false, defaultValue = "STND_BUGA") String logType, Model model) {
        model.addAttribute("logs", client.list(logType));
        model.addAttribute("selectedType", logType);
        model.addAttribute("typeLabels", TYPE_LABELS);
        return "nts-receipt-log-admin/list";
    }
}
