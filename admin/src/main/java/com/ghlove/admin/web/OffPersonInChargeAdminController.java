package com.ghlove.admin.web;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.service.LocgovClient;
import com.ghlove.admin.service.ManagerException;
import com.ghlove.admin.service.OffPersonInChargeAdminService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/** D8 오프라인담당자 관리 (AS-IS OffPersonInChargeManagerController). */
@Controller
@RequestMapping("/admin/off-person-in-charge")
@RequiredArgsConstructor
public class OffPersonInChargeAdminController {

    private final OffPersonInChargeAdminService service;
    private final LocgovClient locgovClient;

    @GetMapping
    public String list(@RequestParam(required = false) String locgovCode,
                        @RequestParam(required = false) String keyword,
                        @RequestParam(required = false) String errorMessage,
                        @RequestParam(required = false) String createdLoginId,
                        @RequestParam(required = false) String tempPassword,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        Model model) {
        OffPersonInChargeAdminService.SearchResult result = service.search(locgovCode, keyword, page, size);
        model.addAttribute("list", result.content());
        model.addAttribute("totalCount", result.totalElements());
        model.addAttribute("totalPages", result.totalPages());
        model.addAttribute("currentPage", page);
        model.addAttribute("size", size);
        model.addAttribute("locgovCode", locgovCode);
        model.addAttribute("keyword", keyword);
        model.addAttribute("errorMessage", errorMessage);
        model.addAttribute("createdLoginId", createdLoginId);
        model.addAttribute("tempPassword", tempPassword);
        model.addAttribute("allLocgovs", locgovClient.allLocgovs());
        model.addAttribute("provinces", provinces());
        return "off-person-in-charge/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("manager", new Manager());
        model.addAttribute("allLocgovs", locgovClient.allLocgovs());
        model.addAttribute("provinces", provinces());
        return "off-person-in-charge/form";
    }

    @PostMapping
    public String create(@RequestParam String loginId, @RequestParam(required = false) String userName,
                          @RequestParam(required = false) String email, @RequestParam String authority,
                          @RequestParam String locgovCode, @RequestParam(required = false) String bankCode,
                          @RequestParam(required = false) String empId, @RequestParam(required = false) String psitnNm,
                          @RequestParam(required = false) String psitnDeptNm, @RequestParam(required = false) String ofcpsNm,
                          Model model) {
        try {
            String tempPassword = service.create(loginId, userName, email, authority, locgovCode,
                    bankCode, empId, psitnNm, psitnDeptNm, ofcpsNm);
            return "redirect:/admin/off-person-in-charge?createdLoginId=" + encode(loginId)
                    + "&tempPassword=" + encode(tempPassword);
        } catch (ManagerException e) {
            model.addAttribute("errorMessage", e.getMessage());
            Manager form = new Manager();
            form.setLoginId(loginId);
            form.setUserName(userName);
            form.setEmail(email);
            form.setAuthority(authority);
            form.setLocgovCode(locgovCode);
            form.setBankCode(bankCode);
            form.setEmpId(empId);
            form.setPsitnNm(psitnNm);
            form.setPsitnDeptNm(psitnDeptNm);
            form.setOfcpsNm(ofcpsNm);
            model.addAttribute("manager", form);
            model.addAttribute("allLocgovs", locgovClient.allLocgovs());
            model.addAttribute("provinces", provinces());
            return "off-person-in-charge/form";
        }
    }

    @GetMapping("/{userId}/edit")
    public String editForm(@PathVariable Long userId, Model model) {
        model.addAttribute("manager", service.get(userId));
        model.addAttribute("allLocgovs", locgovClient.allLocgovs());
        model.addAttribute("provinces", provinces());
        return "off-person-in-charge/form";
    }

    @PostMapping("/{userId}")
    public String update(@PathVariable Long userId, @RequestParam(required = false) String userName,
                          @RequestParam(required = false) String email, @RequestParam String authority,
                          @RequestParam String locgovCode, @RequestParam(required = false) String statusCode,
                          @RequestParam(required = false) String bankCode, @RequestParam(required = false) String empId,
                          @RequestParam(required = false) String psitnNm, @RequestParam(required = false) String psitnDeptNm,
                          @RequestParam(required = false) String ofcpsNm, Model model) {
        try {
            service.update(userId, userName, email, authority, locgovCode, statusCode,
                    bankCode, empId, psitnNm, psitnDeptNm, ofcpsNm);
        } catch (ManagerException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("manager", service.get(userId));
            model.addAttribute("allLocgovs", locgovClient.allLocgovs());
            model.addAttribute("provinces", provinces());
            return "off-person-in-charge/form";
        }
        return "redirect:/admin/off-person-in-charge/" + userId + "/edit";
    }

    @PostMapping("/{userId}/delete")
    public String delete(@PathVariable Long userId, HttpSession session) {
        Manager viewer = (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
        try {
            service.delete(userId, viewer.getUserId());
        } catch (ManagerException e) {
            return "redirect:/admin/off-person-in-charge?errorMessage=" + encode(e.getMessage());
        }
        return "redirect:/admin/off-person-in-charge";
    }

    @PostMapping("/{userId}/reset-password")
    public String resetPassword(@PathVariable Long userId) {
        String tempPassword = service.resetPassword(userId);
        Manager manager = service.get(userId);
        return "redirect:/admin/off-person-in-charge?createdLoginId=" + encode(manager.getLoginId())
                + "&tempPassword=" + encode(tempPassword);
    }

    /** 아이디 중복확인 (AJAX). */
    @GetMapping("/check-login-id")
    @ResponseBody
    public Map<String, Boolean> checkLoginId(@RequestParam String loginId) {
        return Map.of("available", service.loginIdAvailable(loginId));
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
