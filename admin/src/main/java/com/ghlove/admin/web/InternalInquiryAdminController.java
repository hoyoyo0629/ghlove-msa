package com.ghlove.admin.web;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.domain.QnaAdmin;
import com.ghlove.admin.service.CommonCodeService;
import com.ghlove.admin.service.InternalInquiryService;
import com.ghlove.admin.service.LocgovClient;
import com.ghlove.admin.service.MenuService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 내부문의(지자체담당자 → 본사 운영자) 관리. AS-IS opmanager/qna-admin (QnaAdminManagerController).
 * 일반 1:1문의(OP_QNA, {@link QnaAdminController} /qna-admin)와는 별개 도메인이라 경로를
 * /admin/internal-inquiry로 분리한다. RBAC: 지자체담당자(ROLE_ADMIN_5/6, isLocgovScoped)는 자기
 * 지자체 문의만 조회·작성, 본사(ROLE_ADMIN_1~4)는 전체 조회 + 답변. (첨부파일·발송알림은 추후 라운드)
 */
@Controller
@RequestMapping("/admin/internal-inquiry")
@RequiredArgsConstructor
public class InternalInquiryAdminController {

    private final InternalInquiryService inquiryService;
    private final CommonCodeService commonCodeService;
    private final LocgovClient locgovClient;

    @GetMapping
    public String list(@RequestParam(required = false) String qnaGroup,
                       @RequestParam(required = false) String startDate,
                       @RequestParam(required = false) String endDate,
                       HttpSession session, Model model) {
        Manager viewer = manager(session);
        boolean scoped = MenuService.isLocgovScoped(viewer);
        model.addAttribute("inquiries", inquiryService.list(scoped, viewer.getLocgovCode(), qnaGroup, startDate, endDate));
        model.addAttribute("qnaGroups", commonCodeService.labelsOf("QNA_GROUPS"));
        model.addAttribute("locgovNames", locgovNames());
        model.addAttribute("qnaGroup", qnaGroup);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("scoped", scoped);
        return "internal-inquiry/list";
    }

    /** 문의 작성 화면 (지자체 담당자). */
    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("inquiry", new QnaAdmin());
        model.addAttribute("answer", null);
        model.addAttribute("qnaGroups", commonCodeService.labelsOf("QNA_GROUPS"));
        model.addAttribute("mode", "new");
        return "internal-inquiry/form";
    }

    @PostMapping
    public String create(@RequestParam String qnaGroup, @RequestParam String subject,
                         @RequestParam String question, @RequestParam(required = false) String locgovCode,
                         HttpSession session, RedirectAttributes redirect) {
        Manager writer = manager(session);
        String locgov = MenuService.effectiveLocgovCode(writer, locgovCode);
        if (locgov == null || locgov.isBlank()) {
            redirect.addFlashAttribute("errorMessage", "지자체가 지정되지 않았습니다.");
            return "redirect:/admin/internal-inquiry/new";
        }
        inquiryService.createInquiry(writer, locgov, qnaGroup, subject, question);
        redirect.addFlashAttribute("message", "등록되었습니다.");
        return "redirect:/admin/internal-inquiry";
    }

    /** 문의 상세 + 답변 작성/수정 화면. */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, HttpSession session, RedirectAttributes redirect, Model model) {
        Manager viewer = manager(session);
        QnaAdmin inquiry = inquiryService.get(id).filter(q -> !"N".equals(q.getDataStatusCode())).orElse(null);
        if (inquiry == null || !canAccess(viewer, inquiry)) {
            redirect.addFlashAttribute("errorMessage", "권한이 없거나 존재하지 않는 문의입니다.");
            return "redirect:/admin/internal-inquiry";
        }
        model.addAttribute("inquiry", inquiry);
        model.addAttribute("answer", inquiryService.answerOf(id).orElse(null));
        model.addAttribute("qnaGroups", commonCodeService.labelsOf("QNA_GROUPS"));
        model.addAttribute("canAnswer", !MenuService.isLocgovScoped(viewer));
        model.addAttribute("mode", "detail");
        return "internal-inquiry/form";
    }

    /** 답변 등록/수정 (본사). */
    @PostMapping("/{id}/answer")
    public String answer(@PathVariable Long id, @RequestParam(required = false) String title,
                         @RequestParam String answer, HttpSession session, RedirectAttributes redirect) {
        Manager answerer = manager(session);
        if (MenuService.isLocgovScoped(answerer)) {
            redirect.addFlashAttribute("errorMessage", "답변은 본사 운영자만 등록할 수 있습니다.");
            return "redirect:/admin/internal-inquiry/" + id;
        }
        inquiryService.saveAnswer(id, answerer, title, answer);
        redirect.addFlashAttribute("message", "답변이 저장되었습니다.");
        return "redirect:/admin/internal-inquiry/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, HttpSession session, RedirectAttributes redirect) {
        Manager viewer = manager(session);
        QnaAdmin inquiry = inquiryService.get(id).orElse(null);
        if (inquiry == null || !canAccess(viewer, inquiry)) {
            redirect.addFlashAttribute("errorMessage", "권한이 없습니다.");
            return "redirect:/admin/internal-inquiry";
        }
        inquiryService.deleteInquiry(id);
        redirect.addFlashAttribute("message", "삭제되었습니다.");
        return "redirect:/admin/internal-inquiry";
    }

    /** 지자체담당자는 자기 지자체 문의만, 본사는 전체 접근 가능. */
    private static boolean canAccess(Manager viewer, QnaAdmin inquiry) {
        if (!MenuService.isLocgovScoped(viewer)) {
            return true;
        }
        return viewer.getLocgovCode() != null && viewer.getLocgovCode().equals(inquiry.getLocgovCode());
    }

    private java.util.Map<String, String> locgovNames() {
        java.util.Map<String, String> names = new java.util.LinkedHashMap<>();
        locgovClient.allLocgovs().forEach(l -> names.put(l.locgovCode(), l.upperLocgovNm() + " " + l.locgovNm()));
        return names;
    }

    private static Manager manager(HttpSession session) {
        return (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
    }
}
