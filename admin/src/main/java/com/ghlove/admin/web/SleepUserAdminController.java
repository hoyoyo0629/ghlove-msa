package com.ghlove.admin.web;

import com.ghlove.admin.service.MemberAdminClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

/** D5 휴면회원 조회/해제 (AS-IS opmanager/user/sleep-user - SleepUserManagerController).
 *  최종 로그인일 범위 기본값이 오늘이라 처음 진입하면 보통 빈 목록이다(휴면회원은 정의상
 *  최근 로그인이 없으므로) - AS-IS와 동일하게 운영자가 직접 기간을 넓혀 조회하는 UX다. */
@Controller
@RequestMapping("/admin/sleep-users")
@RequiredArgsConstructor
public class SleepUserAdminController {

    private final MemberAdminClient memberAdminClient;

    @GetMapping
    public String list(@RequestParam(required = false) String fromDate,
                        @RequestParam(required = false) String toDate,
                        @RequestParam(required = false) String srchKey,
                        @RequestParam(required = false) String srchValue,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        Model model) {
        String effectiveFrom = fromDate == null || fromDate.isBlank() ? today() : fromDate;
        String effectiveTo = toDate == null || toDate.isBlank() ? today() : toDate;
        MemberAdminClient.SleepSearchResult result = memberAdminClient.searchSleep(
                effectiveFrom, effectiveTo, srchKey, srchValue, page, size);
        model.addAttribute("list", result.content());
        model.addAttribute("totalCount", result.totalElements());
        model.addAttribute("totalPages", result.totalPages());
        model.addAttribute("currentPage", page);
        model.addAttribute("size", size);
        model.addAttribute("fromDate", effectiveFrom);
        model.addAttribute("toDate", effectiveTo);
        model.addAttribute("srchKey", srchKey);
        model.addAttribute("srchValue", srchValue);
        return "sleep-user-admin/list";
    }

    @PostMapping("/wakeup")
    public String wakeup(@RequestParam(required = false) List<Long> userIds,
                          @RequestParam(required = false) String fromDate,
                          @RequestParam(required = false) String toDate,
                          RedirectAttributes redirectAttributes) {
        int count = (userIds == null || userIds.isEmpty()) ? 0 : memberAdminClient.wakeup(userIds);
        redirectAttributes.addFlashAttribute("wakeupCount", count);
        StringBuilder redirect = new StringBuilder("redirect:/admin/sleep-users");
        if (fromDate != null || toDate != null) {
            redirect.append("?");
            if (fromDate != null) {
                redirect.append("fromDate=").append(fromDate);
            }
            if (toDate != null) {
                redirect.append(fromDate != null ? "&" : "").append("toDate=").append(toDate);
            }
        }
        return redirect.toString();
    }

    private static String today() {
        return LocalDate.now().toString();
    }
}
