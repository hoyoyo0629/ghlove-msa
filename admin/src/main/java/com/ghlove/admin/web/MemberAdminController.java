package com.ghlove.admin.web;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.service.ManagerAuthService;
import com.ghlove.admin.service.ManagerException;
import com.ghlove.admin.service.MemberAdminClient;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * D3 일반회원 검색/상세/관리 (AS-IS opmanager/user/general-customer - GeneralCustomerManagerController,
 * docs/as-is-admin-gap-deep-audit-part2.md 배치D 참고). 실제 데이터는 member 서비스에 있고
 * (MemberAdminClient), 여기는 admin 콘솔의 로그인/RBAC과 화면만 담당한다.
 *
 * 개인정보(전화/주소/생년월일 등) 비마스킹 열람은 [[admin-pii-display-no-masking]] 관행대로
 * 기본적으로 마스킹하지 않지만, 이 화면은 태스크 지시서가 명시적으로 요구한 "관리자 비밀번호
 * 재확인 게이트"를 세션 단위로 간단히 구현했다 - 상세화면 접속 시 자기 자신(로그인한 관리자)의
 * 비밀번호를 한 번 더 입력해야 상세 개인정보 블록이 열린다(세션에 열람 이력을 저장, 재방문 시
 * 다시 묻지 않음).
 */
@Controller
@RequestMapping("/admin/members")
@RequiredArgsConstructor
public class MemberAdminController {

    private static final String SESSION_REVEALED_KEY = "revealedMemberIds";

    private final MemberAdminClient memberAdminClient;
    private final ManagerAuthService managerAuthService;

    @GetMapping
    public String list(@RequestParam(required = false) String fromDate,
                        @RequestParam(required = false) String toDate,
                        @RequestParam(required = false) String srchKey,
                        @RequestParam(required = false) String srchValue,
                        @RequestParam(required = false) String sbscrbSeCode,
                        @RequestParam(required = false) String receiveEmail,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        Model model) {
        String effectiveFrom = fromDate == null || fromDate.isBlank() ? today() : fromDate;
        String effectiveTo = toDate == null || toDate.isBlank() ? today() : toDate;
        MemberAdminClient.SearchResult result = memberAdminClient.search(
                effectiveFrom, effectiveTo, srchKey, srchValue, sbscrbSeCode, receiveEmail, page, size);
        model.addAttribute("list", result.content());
        model.addAttribute("totalCount", result.totalElements());
        model.addAttribute("totalPages", result.totalPages());
        model.addAttribute("currentPage", page);
        model.addAttribute("size", size);
        model.addAttribute("fromDate", effectiveFrom);
        model.addAttribute("toDate", effectiveTo);
        model.addAttribute("srchKey", srchKey);
        model.addAttribute("srchValue", srchValue);
        model.addAttribute("sbscrbSeCode", sbscrbSeCode);
        model.addAttribute("receiveEmail", receiveEmail);
        return "member-admin/list";
    }

    @GetMapping("/{userId}")
    public String detail(@PathVariable Long userId, HttpSession session, Model model) {
        MemberAdminClient.Detail detail = memberAdminClient.detail(userId);
        if (detail == null) {
            model.addAttribute("errorMessage", "회원 정보를 찾을 수 없습니다.");
            return "member-admin/detail";
        }
        model.addAttribute("member", detail);
        model.addAttribute("revealed", revealedIds(session).contains(userId));
        return "member-admin/detail";
    }

    /** 관리자 자신의 비밀번호 재확인 게이트 - 통과하면 이번 세션 동안은 다시 묻지 않는다. */
    @PostMapping("/{userId}/reveal")
    public String reveal(@PathVariable Long userId, @RequestParam String password,
                          HttpSession session, Model model) {
        Manager manager = (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
        try {
            managerAuthService.checkCredentials(manager.getLoginId(), password);
            revealedIds(session).add(userId);
        } catch (ManagerException e) {
            model.addAttribute("member", memberAdminClient.detail(userId));
            model.addAttribute("revealed", false);
            model.addAttribute("errorMessage", e.getMessage());
            return "member-admin/detail";
        }
        return "redirect:/admin/members/" + userId;
    }

    @PostMapping("/{userId}/withdraw")
    public String withdraw(@PathVariable Long userId, @RequestParam(required = false) String reason,
                            Model model) {
        try {
            memberAdminClient.withdraw(userId, reason);
        } catch (ManagerException e) {
            model.addAttribute("member", memberAdminClient.detail(userId));
            model.addAttribute("errorMessage", e.getMessage());
            return "member-admin/detail";
        }
        return "redirect:/admin/members/" + userId;
    }

    @SuppressWarnings("unchecked")
    private static Set<Long> revealedIds(HttpSession session) {
        Set<Long> ids = (Set<Long>) session.getAttribute(SESSION_REVEALED_KEY);
        if (ids == null) {
            ids = new HashSet<>();
            session.setAttribute(SESSION_REVEALED_KEY, ids);
        }
        return ids;
    }

    private static String today() {
        return LocalDate.now().toString();
    }
}
