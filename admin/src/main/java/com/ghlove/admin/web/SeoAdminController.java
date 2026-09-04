package com.ghlove.admin.web;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.domain.Seo;
import com.ghlove.admin.repository.SeoRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** 페이지별 SEO 메타 관리 (AS-IS opmanager/seo - SeoManagerController). */
@Controller
@RequestMapping("/admin/seo")
@RequiredArgsConstructor
public class SeoAdminController {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final SeoRepository seoRepository;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("seoList", seoRepository.findAllByOrderBySeoIdDesc());
        return "seo-admin/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("seo", null);
        return "seo-admin/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Integer id, Model model) {
        model.addAttribute("seo", seoRepository.findById(id).orElseThrow());
        return "seo-admin/form";
    }

    @PostMapping
    public String create(@ModelAttribute Seo form, HttpSession session) {
        Manager manager = manager(session);
        form.setSeoId(null);
        form.setIndexFlag("Y".equals(form.getIndexFlag()) || "on".equals(form.getIndexFlag()) ? "Y" : "N");
        form.setCreatedUserId(manager.getUserId());
        form.setCreatedDate(LocalDateTime.now().format(FMT));
        seoRepository.save(form);
        return "redirect:/admin/seo";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Integer id, @ModelAttribute Seo form) {
        Seo seo = seoRepository.findById(id).orElseThrow();
        seo.setSeoUrl(form.getSeoUrl());
        seo.setTitle(form.getTitle());
        seo.setKeywords(form.getKeywords());
        seo.setDescription(form.getDescription());
        seo.setHeaderContents1(form.getHeaderContents1());
        seo.setHeaderContents2(form.getHeaderContents2());
        seo.setHeaderContents3(form.getHeaderContents3());
        seo.setThemawordTitle(form.getThemawordTitle());
        seo.setThemawordDescription(form.getThemawordDescription());
        seo.setIndexFlag("Y".equals(form.getIndexFlag()) || "on".equals(form.getIndexFlag()) ? "Y" : "N");
        seoRepository.save(seo);
        return "redirect:/admin/seo";
    }

    @PostMapping("/delete")
    public String deleteSelected(@RequestParam(required = false) List<Integer> ids) {
        if (ids != null && !ids.isEmpty()) {
            seoRepository.deleteAllById(ids);
        }
        return "redirect:/admin/seo";
    }

    private static Manager manager(HttpSession session) {
        return (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
    }
}
