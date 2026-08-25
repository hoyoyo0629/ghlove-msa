package com.ghlove.admin.web;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.service.QnaException;
import com.ghlove.admin.service.QnaService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/** 1:1 문의 관리자 답변 화면 (AS-IS opmanager qna 답변) - 공개 QnaController와 같은
 *  OP_QNA/OP_QNA_ANSWER를 쓰지만, 로그인은 회원 JWT가 아니라 매니저 세션이다
 *  (AdminAuthInterceptor가 게이트). URL을 /qna-admin으로 분리해 공개 /qna와 겹치지 않게 했다. */
@Controller
@RequiredArgsConstructor
public class QnaAdminController {

    private final QnaService qnaService;

    @GetMapping("/qna-admin")
    public String list(Model model) {
        model.addAttribute("inquiries", qnaService.adminList());
        return "qna-admin/list";
    }

    @GetMapping("/qna-admin/{qnaId}")
    public String detail(@PathVariable Integer qnaId, Model model) {
        var qna = qnaService.find(qnaId).orElse(null);
        if (qna == null) {
            return "redirect:/qna-admin";
        }
        model.addAttribute("qna", qna);
        model.addAttribute("answer", qnaService.answerOf(qnaId).orElse(null));
        model.addAttribute("files", qnaService.filesOf(qnaId));
        return "qna-admin/detail";
    }

    @PostMapping("/qna-admin/{qnaId}/answer")
    public String answer(@PathVariable Integer qnaId, @RequestParam String title, @RequestParam String content,
                          HttpSession session) {
        Manager manager = (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
        try {
            qnaService.answer(qnaId, manager.getUserId(), title, content);
        } catch (QnaException e) {
            return "redirect:/qna-admin/" + qnaId + "?errorMessage=" + encode(e.getMessage());
        }
        return "redirect:/qna-admin/" + qnaId;
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
