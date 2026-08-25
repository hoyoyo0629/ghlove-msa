package com.ghlove.admin.web;

import com.ghlove.admin.domain.LocgovFaq;
import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.repository.LocgovFaqRepository;
import com.ghlove.admin.service.CommonCodeService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/** 지자체FAQ 관리 (AS-IS opmanager/community/locv-faq) - 고객센터 FAQ(OP_COMMUNITY_LOCGOVFAQ)의
 *  관리자 CRUD 화면. AS-IS는 ROLE_ADMIN_1~4(시스템/행안부)만 쓰기 가능, ROLE_ADMIN_5~6(지자체)은
 *  조회만 가능한 6단계 권한이지만, 이 프로젝트는 ROLE_ADMIN/ROLE_OPERATOR 2단계뿐이라
 *  ROLE_ADMIN 전체를 AS-IS의 "mois"(행안부) 권한과 동일하게 취급한다. */
@Controller
@RequestMapping("/community/locv-faq")
@RequiredArgsConstructor
public class LocgFaqAdminController {

    private final LocgovFaqRepository locgovFaqRepository;
    private final CommonCodeService commonCodeService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("faqs", locgovFaqRepository.findAllByOrderByIdDesc());
        model.addAttribute("faqTypes", commonCodeService.labelsOf("FAQ_TYPE"));
        return "community/locv-faq/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("faq", new LocgovFaq());
        model.addAttribute("faqTypes", commonCodeService.labelsOf("FAQ_TYPE"));
        return "community/locv-faq/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Integer id, Model model) {
        model.addAttribute("faq", locgovFaqRepository.findById(id).orElseThrow());
        model.addAttribute("faqTypes", commonCodeService.labelsOf("FAQ_TYPE"));
        return "community/locv-faq/form";
    }

    @PostMapping
    public String create(LocgovFaq form, HttpSession session) {
        Manager manager = (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
        form.setId(null);
        form.setAdminId(manager.getUserId());
        form.setUseYn("Y");
        form.setHits(0);
        form.setCreatedDate(LocalDateTime.now());
        form.setUpdatedDate(LocalDateTime.now());
        locgovFaqRepository.save(form);
        return "redirect:/community/locv-faq";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Integer id, LocgovFaq form, HttpSession session) {
        Manager manager = (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
        LocgovFaq faq = locgovFaqRepository.findById(id).orElseThrow();
        faq.setFaqType(form.getFaqType());
        faq.setSubject(form.getSubject());
        faq.setContent(form.getContent());
        faq.setUpdatedBy(manager.getUserId());
        faq.setUpdatedDate(LocalDateTime.now());
        locgovFaqRepository.save(faq);
        return "redirect:/community/locv-faq";
    }

    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable Integer id) {
        LocgovFaq faq = locgovFaqRepository.findById(id).orElseThrow();
        faq.setUseYn("Y".equals(faq.getUseYn()) ? "N" : "Y");
        locgovFaqRepository.save(faq);
        return "redirect:/community/locv-faq";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Integer id) {
        locgovFaqRepository.deleteById(id);
        return "redirect:/community/locv-faq";
    }
}
