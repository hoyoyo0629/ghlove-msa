package com.ghlove.admin.web;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.repository.RoleRepository;
import com.ghlove.admin.service.ManagerException;
import com.ghlove.admin.service.PersonInChargeAdminService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * D6/D9 관리자 사후관리(지자체담당자/운영담당자) - AS-IS LocgovPersonInChargeManagerController +
 * OperPersonInChargeManagerController를 탭 하나로 통합했다
 * (docs/as-is-admin-gap-deep-audit-part2.md 배치D D6/D9 권장사항 반영).
 */
@Controller
@RequestMapping("/admin/person-in-charge")
@RequiredArgsConstructor
public class PersonInChargeAdminController {

    private final PersonInChargeAdminService service;
    private final RoleRepository roleRepository;

    @GetMapping
    public String list(@RequestParam(defaultValue = "locgov") String scope,
                        @RequestParam(required = false) String fromDate,
                        @RequestParam(required = false) String toDate,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(required = false) String errorMessage,
                        HttpSession session, Model model) {
        Manager viewer = viewer(session);
        // 지자체담당자(5/6)는 자기 지자체 화면만 의미가 있다 - 운영담당자 탭을 억지로 요청해도 locgov로 되돌린다.
        String effectiveScope = com.ghlove.admin.service.MenuService.isLocgovScoped(viewer)
                ? PersonInChargeAdminService.SCOPE_LOCGOV : scope;
        PersonInChargeAdminService.SearchResult result = service.search(effectiveScope, fromDate, toDate, viewer, page, size);
        model.addAttribute("scope", effectiveScope);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("list", result.content());
        model.addAttribute("totalCount", result.totalElements());
        model.addAttribute("totalPages", result.totalPages());
        model.addAttribute("currentPage", page);
        model.addAttribute("size", size);
        model.addAttribute("errorMessage", errorMessage);
        model.addAttribute("roles", roleRepository.findAllByOrderByRoleSeq());
        model.addAttribute("locgovScoped", com.ghlove.admin.service.MenuService.isLocgovScoped(viewer));
        return "person-in-charge/list";
    }

    @GetMapping("/{userId}/edit")
    public String editForm(@PathVariable Long userId, @RequestParam(defaultValue = "locgov") String scope, Model model) {
        model.addAttribute("manager", service.get(userId));
        model.addAttribute("scope", scope);
        model.addAttribute("roles", roleRepository.findAllByOrderByRoleSeq());
        return "person-in-charge/form";
    }

    @PostMapping("/{userId}")
    public String update(@PathVariable Long userId, @RequestParam String scope,
                          @RequestParam(required = false) String userName,
                          @RequestParam(required = false) String email,
                          @RequestParam String authority,
                          @RequestParam(required = false) String statusCode, Model model) {
        try {
            service.update(scope, userId, userName, email, authority, statusCode);
        } catch (ManagerException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("manager", service.get(userId));
            model.addAttribute("scope", scope);
            model.addAttribute("roles", roleRepository.findAllByOrderByRoleSeq());
            return "person-in-charge/form";
        }
        return "redirect:/admin/person-in-charge/" + userId + "/edit?scope=" + scope;
    }

    @PostMapping("/{userId}/delete")
    public String delete(@PathVariable Long userId, @RequestParam String scope, HttpSession session) {
        Manager viewer = viewer(session);
        try {
            service.delete(scope, userId, viewer);
        } catch (ManagerException e) {
            return "redirect:/admin/person-in-charge?scope=" + scope + "&errorMessage=" + encode(e.getMessage());
        }
        if (userId.equals(viewer.getUserId())) {
            // 본인 삭제 - 세션 무효화(D6 지시서: 본인삭제시 세션무효화).
            session.invalidate();
            return "redirect:/admin/login";
        }
        return "redirect:/admin/person-in-charge?scope=" + scope;
    }

    private static Manager viewer(HttpSession session) {
        return (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
