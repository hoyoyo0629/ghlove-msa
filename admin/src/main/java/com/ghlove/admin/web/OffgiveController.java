package com.ghlove.admin.web;

import com.ghlove.admin.service.LocgovClient;
import com.ghlove.admin.service.ManagerException;
import com.ghlove.admin.service.MemberClient;
import com.ghlove.admin.service.OffgiveClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/** 오프라인기부 접수 관리 (AS-IS opmanager/offgive). 실제 데이터/도메인효과는 donation
 *  서비스에 있고(OffgiveClient), 여기는 admin 콘솔의 로그인/RBAC과 화면만 담당한다.
 *  취소/변경신청은 이미 있는 give-reqmng으로 그대로 연결한다(재구현하지 않음 - 승인 시
 *  포인트 사용이력 체크·자동 취소·포인트 회수까지 이미 완전히 검증되어 있다). 실명인증은
 *  담당자가 현장에서 직접 신분증을 확인하는 걸로 대체한다(온라인 SMS 인증과 달리 원격
 *  증빙이 필요 없는 대면 접수라 이 프로젝트 스코프에서 의도적으로 단순화 - 사용자 확인
 *  없이 진행된 판단이라 offgive-form.html에도 동일하게 주석으로 남긴다). */
@Controller
@RequiredArgsConstructor
public class OffgiveController {

    private final OffgiveClient offgiveClient;
    private final LocgovClient locgovClient;
    private final MemberClient memberClient;

    @GetMapping("/offgive")
    public String list(Model model) {
        var list = offgiveClient.list();
        Map<Long, MemberClient.MemberInfo> membersById = new LinkedHashMap<>();
        for (var d : list) {
            membersById.computeIfAbsent(d.userId(), memberClient::fetchOrNull);
        }
        model.addAttribute("list", list);
        model.addAttribute("membersById", membersById);
        return "offgive/list";
    }

    @GetMapping("/offgive/new")
    public String newForm(Model model) {
        model.addAttribute("provinces", provinces());
        model.addAttribute("allLocgovs", locgovClient.allLocgovs());
        return "offgive/form";
    }

    @GetMapping("/offgive/{cntrSn}")
    public String detail(@PathVariable String cntrSn, Model model) {
        try {
            var donation = offgiveClient.get(cntrSn);
            model.addAttribute("donation", donation);
            model.addAttribute("member", memberClient.fetchOrNull(donation.userId()));
        } catch (ManagerException e) {
            model.addAttribute("errorMessage", e.getMessage());
        }
        return "offgive/detail";
    }

    @PostMapping("/offgive")
    public String register(@RequestParam(required = false) Long userId,
                            @RequestParam(required = false) String walkInName,
                            @RequestParam(required = false) String walkInPhone,
                            @RequestParam(required = false) String walkInBirthday,
                            @RequestParam(required = false) String walkInAddress,
                            @RequestParam String locgovCode, @RequestParam BigDecimal amount,
                            @RequestParam(required = false) String rceptBankCode,
                            @RequestParam(required = false) String rceptBankNm,
                            Model model) {
        try {
            OffgiveClient.RegisterResult result = offgiveClient.register(userId, walkInName, walkInPhone,
                    stripDashes(walkInBirthday), walkInAddress, locgovCode, amount, rceptBankCode, rceptBankNm);
            model.addAttribute("result", result);
            return "offgive/complete";
        } catch (ManagerException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("provinces", provinces());
            model.addAttribute("allLocgovs", locgovClient.allLocgovs());
            return "offgive/form";
        }
    }

    private static String stripDashes(String ymd) {
        return ymd != null ? ymd.replace("-", "") : null;
    }

    private Map<String, String> provinces() {
        Map<String, String> map = new LinkedHashMap<>();
        for (LocgovClient.LocgovInfo l : locgovClient.allLocgovs()) {
            map.putIfAbsent(l.upperLocgovCode(), l.upperLocgovNm());
        }
        return map;
    }
}
