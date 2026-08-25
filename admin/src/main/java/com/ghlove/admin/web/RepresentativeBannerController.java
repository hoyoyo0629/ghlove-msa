package com.ghlove.admin.web;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.service.RepresentativeBannerException;
import com.ghlove.admin.service.RepresentativeBannerService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/** 지정기부 대표배너 관리 (AS-IS opmanager/designated-donation/banner). */
@Controller
@RequestMapping("/designated-projects/banners")
@RequiredArgsConstructor
public class RepresentativeBannerController {

    private final RepresentativeBannerService bannerService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("banners", bannerService.list());
        return "designated/banner-list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("banner", null);
        return "designated/banner-form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Integer id, Model model) {
        model.addAttribute("banner", bannerService.get(id));
        return "designated/banner-form";
    }

    @PostMapping
    public String create(@RequestParam String title, @RequestParam(required = false) String linkUrl,
                          @RequestParam(required = false) String bannerContent, @RequestParam Integer displayOrder,
                          @RequestParam(required = false) MultipartFile pcImage,
                          @RequestParam(required = false) MultipartFile mobileImage,
                          HttpSession session, Model model) {
        Manager manager = manager(session);
        try {
            bannerService.create(title, linkUrl, bannerContent, displayOrder, pcImage, mobileImage, manager.getUserId());
            return "redirect:/designated-projects/banners";
        } catch (RepresentativeBannerException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("banner", null);
            return "designated/banner-form";
        }
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Integer id, @RequestParam String title,
                          @RequestParam(required = false) String linkUrl,
                          @RequestParam(required = false) String bannerContent, @RequestParam Integer displayOrder,
                          @RequestParam(required = false) MultipartFile pcImage,
                          @RequestParam(required = false) MultipartFile mobileImage,
                          HttpSession session, Model model) {
        Manager manager = manager(session);
        try {
            bannerService.update(id, title, linkUrl, bannerContent, displayOrder, pcImage, mobileImage, manager.getUserId());
            return "redirect:/designated-projects/banners";
        } catch (RepresentativeBannerException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("banner", bannerService.get(id));
            return "designated/banner-form";
        }
    }

    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable Integer id, HttpSession session) {
        bannerService.toggle(id, manager(session).getUserId());
        return "redirect:/designated-projects/banners";
    }

    private static Manager manager(HttpSession session) {
        return (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
    }
}
