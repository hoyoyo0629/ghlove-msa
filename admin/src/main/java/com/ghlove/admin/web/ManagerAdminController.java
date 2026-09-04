package com.ghlove.admin.web;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.repository.RoleRepository;
import com.ghlove.admin.service.LocgovClient;
import com.ghlove.admin.service.ManagerAdminService;
import com.ghlove.admin.service.ManagerException;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/** D2 매니저(admin 로그인계정) CRUD (AS-IS opmanager/user/manager - UserManagerController,
 *  docs/as-is-admin-gap-deep-audit-part2.md 배치D 참고). */
@Controller
@RequestMapping("/admin/managers")
@RequiredArgsConstructor
public class ManagerAdminController {

    private final ManagerAdminService managerAdminService;
    private final RoleRepository roleRepository;
    private final LocgovClient locgovClient;

    @GetMapping
    public String list(@RequestParam(required = false) String authority,
                        @RequestParam(required = false) String keyword,
                        @RequestParam(required = false) String errorMessage,
                        @RequestParam(required = false) String createdLoginId,
                        @RequestParam(required = false) String tempPassword,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        Model model) {
        ManagerAdminService.SearchResult result = managerAdminService.search(authority, keyword, page, size);
        model.addAttribute("list", result.content());
        model.addAttribute("totalCount", result.totalElements());
        model.addAttribute("totalPages", result.totalPages());
        model.addAttribute("currentPage", page);
        model.addAttribute("size", size);
        model.addAttribute("authority", authority);
        model.addAttribute("keyword", keyword);
        model.addAttribute("errorMessage", errorMessage);
        model.addAttribute("createdLoginId", createdLoginId);
        model.addAttribute("tempPassword", tempPassword);
        model.addAttribute("roles", roleRepository.findAllByOrderByRoleSeq());
        return "manager-admin/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("manager", new Manager());
        model.addAttribute("roles", roleRepository.findAllByOrderByRoleSeq());
        model.addAttribute("provinces", provinces());
        model.addAttribute("allLocgovs", locgovClient.allLocgovs());
        return "manager-admin/form";
    }

    @PostMapping
    public String create(@RequestParam String loginId, @RequestParam(required = false) String userName,
                          @RequestParam(required = false) String email, @RequestParam String authority,
                          @RequestParam(required = false) String locgovCode, Model model) {
        try {
            String tempPassword = managerAdminService.create(loginId, userName, email, authority, locgovCode);
            return "redirect:/admin/managers?createdLoginId=" + encode(loginId)
                    + "&tempPassword=" + encode(tempPassword);
        } catch (ManagerException e) {
            model.addAttribute("errorMessage", e.getMessage());
            Manager form = new Manager();
            form.setLoginId(loginId);
            form.setUserName(userName);
            form.setEmail(email);
            form.setAuthority(authority);
            form.setLocgovCode(locgovCode);
            model.addAttribute("manager", form);
            model.addAttribute("roles", roleRepository.findAllByOrderByRoleSeq());
            model.addAttribute("provinces", provinces());
            model.addAttribute("allLocgovs", locgovClient.allLocgovs());
            return "manager-admin/form";
        }
    }

    @GetMapping("/{userId}/edit")
    public String editForm(@PathVariable Long userId, Model model) {
        model.addAttribute("manager", managerAdminService.get(userId));
        model.addAttribute("roles", roleRepository.findAllByOrderByRoleSeq());
        model.addAttribute("provinces", provinces());
        model.addAttribute("allLocgovs", locgovClient.allLocgovs());
        return "manager-admin/form";
    }

    @PostMapping("/{userId}")
    public String update(@PathVariable Long userId, @RequestParam(required = false) String userName,
                          @RequestParam(required = false) String email, @RequestParam String authority,
                          @RequestParam(required = false) String locgovCode,
                          @RequestParam(required = false) String statusCode, Model model) {
        try {
            managerAdminService.update(userId, userName, email, authority, locgovCode, statusCode);
        } catch (ManagerException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("manager", managerAdminService.get(userId));
            model.addAttribute("roles", roleRepository.findAllByOrderByRoleSeq());
            model.addAttribute("provinces", provinces());
            model.addAttribute("allLocgovs", locgovClient.allLocgovs());
            return "manager-admin/form";
        }
        return "redirect:/admin/managers/" + userId + "/edit";
    }

    @PostMapping("/{userId}/delete")
    public String delete(@PathVariable Long userId, HttpSession session) {
        Manager viewer = (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
        try {
            managerAdminService.delete(userId, viewer.getUserId());
        } catch (ManagerException e) {
            return "redirect:/admin/managers?errorMessage=" + encode(e.getMessage());
        }
        return "redirect:/admin/managers";
    }

    @PostMapping("/{userId}/reset-password")
    public String resetPassword(@PathVariable Long userId) {
        String tempPassword = managerAdminService.resetPassword(userId);
        Manager manager = managerAdminService.get(userId);
        return "redirect:/admin/managers?createdLoginId=" + encode(manager.getLoginId())
                + "&tempPassword=" + encode(tempPassword);
    }

    private Map<String, String> provinces() {
        Map<String, String> map = new LinkedHashMap<>();
        for (LocgovClient.LocgovInfo l : locgovClient.allLocgovs()) {
            map.putIfAbsent(l.upperLocgovCode(), l.upperLocgovNm());
        }
        return map;
    }

    private static String encode(String value) {
        return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8);
    }
}
