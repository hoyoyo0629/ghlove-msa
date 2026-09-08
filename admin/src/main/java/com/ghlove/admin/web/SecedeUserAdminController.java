package com.ghlove.admin.web;

import com.ghlove.admin.service.MemberAdminClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

/** D4 탈퇴회원 조회 (AS-IS opmanager/user/secede-user - SecedeUserManagerController). */
@Controller
@RequestMapping("/admin/secede-users")
@RequiredArgsConstructor
public class SecedeUserAdminController {

    private final MemberAdminClient memberAdminClient;

    @GetMapping
    public String list(@RequestParam(required = false) String fromDate,
                        @RequestParam(required = false) String toDate,
                        @RequestParam(required = false) String srchKey,
                        @RequestParam(required = false) String srchValue,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        Model model) {
        String effectiveFrom = fromDate;  // 기본값 today() 제거 - 빈 값이면 member가 전체 기간으로 조회한다(A-1)
        String effectiveTo = toDate;
        MemberAdminClient.SecedeSearchResult result = memberAdminClient.searchSecede(
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
        return "secede-user-admin/list";
    }

    private static String today() {
        return LocalDate.now().toString();
    }
}
