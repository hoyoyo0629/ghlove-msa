package com.ghlove.admin.web;

import com.ghlove.admin.domain.LocgovFaq;
import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.repository.LocgovFaqRepository;
import com.ghlove.admin.service.CommonCodeService;
import com.ghlove.admin.service.ManagerException;
import com.ghlove.admin.service.MenuService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/** 지자체FAQ 관리 (AS-IS opmanager/community/locv-faq) - 고객센터 FAQ(OP_COMMUNITY_LOCGOVFAQ)의
 *  관리자 CRUD 화면. AS-IS 실제 권한: ROLE_ADMIN_1~4(시스템/행안부)만 쓰기 가능,
 *  ROLE_ADMIN_5~6(지자체)은 조회만 가능 - {@link #requireWriteAccess} 참고. */
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
    public String createForm(HttpSession session, Model model) {
        requireWriteAccess(session);
        model.addAttribute("faq", new LocgovFaq());
        model.addAttribute("faqTypes", commonCodeService.labelsOf("FAQ_TYPE"));
        return "community/locv-faq/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Integer id, HttpSession session, Model model) {
        requireWriteAccess(session);
        model.addAttribute("faq", locgovFaqRepository.findById(id).orElseThrow());
        model.addAttribute("faqTypes", commonCodeService.labelsOf("FAQ_TYPE"));
        return "community/locv-faq/form";
    }

    @PostMapping
    public String create(LocgovFaq form, HttpSession session) {
        Manager manager = requireWriteAccess(session);
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
        Manager manager = requireWriteAccess(session);
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
    public String toggle(@PathVariable Integer id, HttpSession session) {
        requireWriteAccess(session);
        LocgovFaq faq = locgovFaqRepository.findById(id).orElseThrow();
        faq.setUseYn("Y".equals(faq.getUseYn()) ? "N" : "Y");
        locgovFaqRepository.save(faq);
        return "redirect:/community/locv-faq";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Integer id, HttpSession session) {
        requireWriteAccess(session);
        locgovFaqRepository.deleteById(id);
        return "redirect:/community/locv-faq";
    }

    /** AS-IS 실제 권한: 지자체담당자(ROLE_ADMIN_5/6)는 이 화면을 조회는 해도 쓰기는 못 한다. */
    private static Manager requireWriteAccess(HttpSession session) {
        Manager manager = (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
        if (MenuService.isLocgovScoped(manager)) {
            throw new ManagerException("지자체 담당자는 조회만 가능합니다.");
        }
        return manager;
    }
}
