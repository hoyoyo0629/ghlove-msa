package com.ghlove.admin.web;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.service.LocgovClient;
import com.ghlove.admin.service.ManagerException;
import com.ghlove.admin.service.MemberClient;
import com.ghlove.admin.service.MenuService;
import com.ghlove.admin.service.OffgiveClient;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
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

    private static final int PAGE_SIZE_DEFAULT = 10;

    @GetMapping("/offgive")
    public String list(@RequestParam(required = false) String locgovCode,
                        @RequestParam(required = false) String startDate,
                        @RequestParam(required = false) String walkInName,
                        @RequestParam(required = false) String endDate,
                        @RequestParam(required = false, defaultValue = "1") int page,
                        HttpSession session, Model model) {
        Manager viewer = (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
        locgovCode = MenuService.effectiveLocgovCode(viewer, locgovCode);
        var matched = offgiveClient.search(locgovCode, startDate, endDate);
        Map<Long, MemberClient.MemberInfo> membersById = new LinkedHashMap<>();
        for (var d : matched) {
            membersById.computeIfAbsent(d.userId(), memberClient::fetchOrNull);
        }
        // 이름 검색은 Donation 엔티티에 사용자명이 없어(member 서비스 소유) 조회된 회원정보로
        // 여기서 후처리 필터한다 - offgive는 저트래픽 화면이라 인메모리 필터+페이지네이션으로 충분.
        List<OffgiveClient.Donation> filtered = matched;
        if (walkInName != null && !walkInName.isBlank()) {
            filtered = matched.stream()
                    .filter(d -> {
                        var m = membersById.get(d.userId());
                        return m != null && m.userName() != null && m.userName().contains(walkInName);
                    })
                    .toList();
        }

        int totalCount = filtered.size();
        int totalPages = (int) Math.ceil(totalCount / (double) PAGE_SIZE_DEFAULT);
        int currentPage = Math.max(1, Math.min(page, Math.max(totalPages, 1)));
        int from = Math.min((currentPage - 1) * PAGE_SIZE_DEFAULT, totalCount);
        int to = Math.min(from + PAGE_SIZE_DEFAULT, totalCount);

        model.addAttribute("list", filtered.subList(from, to));
        model.addAttribute("membersById", membersById);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("size", PAGE_SIZE_DEFAULT);
        model.addAttribute("locgovCode", locgovCode);
        model.addAttribute("locgovScoped", MenuService.isLocgovScoped(viewer));
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("walkInName", walkInName);
        model.addAttribute("provinces", provinces());
        model.addAttribute("allLocgovs", locgovClient.allLocgovs());
        return "offgive/list";
    }

    @GetMapping("/offgive/new")
    public String newForm(Model model) {
        model.addAttribute("provinces", provinces());
        model.addAttribute("allLocgovs", locgovClient.allLocgovs());
        return "offgive/form";
    }

    @GetMapping("/offgive/{cntrSn}")
    public String detail(@PathVariable String cntrSn, HttpSession session, Model model) {
        Manager viewer = (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
        try {
            var donation = offgiveClient.get(cntrSn);
            if (MenuService.isLocgovScoped(viewer) && !viewer.getLocgovCode().equals(donation.cntrLocgovCode())) {
                return "redirect:/offgive";
            }
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
                            @RequestParam(required = false) String signatureImage,
                            Model model) {
        try {
            OffgiveClient.RegisterResult result = offgiveClient.register(userId, walkInName, walkInPhone,
                    stripDashes(walkInBirthday), walkInAddress, locgovCode, amount, rceptBankCode, rceptBankNm,
                    signatureImage);
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
