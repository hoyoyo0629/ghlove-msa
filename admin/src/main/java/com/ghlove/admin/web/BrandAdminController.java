package com.ghlove.admin.web;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.service.GiftClient;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/** 브랜드관리 (AS-IS opmanager/brand) - gift 서비스의 OP_BRAND를 cross-service로 CRUD. */
@Controller
@RequestMapping("/brand")
@RequiredArgsConstructor
public class BrandAdminController {

    private final GiftClient giftClient;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("brands", giftClient.brands());
        return "brand/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("brand", new GiftClient.BrandDetail(null, null, null, null, "Y", null, null, null, null, null));
        return "brand/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Integer id, Model model) {
        model.addAttribute("brand", giftClient.brandDetail(id));
        return "brand/form";
    }

    @PostMapping
    public String create(GiftClient.BrandDetail form, HttpSession session) {
        Manager manager = (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
        giftClient.createBrand(withUserId(form, manager.getUserId()));
        return "redirect:/brand";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Integer id, GiftClient.BrandDetail form, HttpSession session) {
        Manager manager = (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
        giftClient.updateBrand(id, withUserId(form, manager.getUserId()));
        return "redirect:/brand";
    }

    private GiftClient.BrandDetail withUserId(GiftClient.BrandDetail form, Long userId) {
        return new GiftClient.BrandDetail(form.brandId(), form.brandName(), form.brandImage(), form.brandContent(),
                form.displayFlag(), form.locgovCode(), form.createdDate(), userId, form.updatedDate(), userId);
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Integer id) {
        giftClient.deleteBrand(id);
        return "redirect:/brand";
    }
}
