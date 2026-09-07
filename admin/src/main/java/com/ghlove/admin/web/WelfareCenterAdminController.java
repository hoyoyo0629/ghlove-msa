package com.ghlove.admin.web;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.service.MenuService;
import com.ghlove.admin.service.WelfareCenterAdminClient;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/** 행정복지센터(주민센터, 오프라인 기부접수 거점) 관리 (AS-IS opmanager/welfareCenter -
 * WelfareCenterManagerController). */
@Controller
@RequestMapping("/admin/welfare-centers")
@RequiredArgsConstructor
public class WelfareCenterAdminController {

    private final WelfareCenterAdminClient client;

    @GetMapping
    public String list(HttpSession session, Model model) {
        Manager manager = manager(session);
        String scopedLclgvCd = MenuService.isLocgovScoped(manager) ? manager.getLocgovCode() : null;
        model.addAttribute("centers", client.list(scopedLclgvCd));
        model.addAttribute("locgovScoped", MenuService.isLocgovScoped(manager));
        return "welfare-center-admin/list";
    }

    @GetMapping("/new")
    public String newForm(HttpSession session, Model model) {
        Manager manager = manager(session);
        model.addAttribute("center", null);
        model.addAttribute("fixedLclgvCd", MenuService.isLocgovScoped(manager) ? manager.getLocgovCode() : null);
        return "welfare-center-admin/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, HttpSession session, Model model) {
        Manager manager = manager(session);
        String scopedLclgvCd = MenuService.isLocgovScoped(manager) ? manager.getLocgovCode() : null;
        var center = client.list(scopedLclgvCd).stream().filter(c -> c.id().equals(id)).findFirst().orElseThrow();
        model.addAttribute("center", center);
        model.addAttribute("fixedLclgvCd", scopedLclgvCd);
        return "welfare-center-admin/form";
    }

    @PostMapping
    public String create(@RequestParam(required = false) String lclgvCd, @RequestParam String name,
                          @RequestParam(required = false) String code, HttpSession session) {
        Manager manager = manager(session);
        String effectiveLclgvCd = MenuService.isLocgovScoped(manager) ? manager.getLocgovCode() : lclgvCd;
        client.create(effectiveLclgvCd, name, code);
        return "redirect:/admin/welfare-centers";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @RequestParam(required = false) String lclgvCd, @RequestParam String name,
                          @RequestParam(required = false) String code, @RequestParam(required = false) String useYn) {
        client.update(id, lclgvCd, name, code, useYn);
        return "redirect:/admin/welfare-centers";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        client.delete(id);
        return "redirect:/admin/welfare-centers";
    }

    private static Manager manager(HttpSession session) {
        return (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
    }
}
