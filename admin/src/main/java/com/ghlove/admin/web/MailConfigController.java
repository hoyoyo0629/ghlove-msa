package com.ghlove.admin.web;

import com.ghlove.admin.domain.MailConfig;
import com.ghlove.admin.service.MailConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/** 메일설정 관리 (AS-IS opmanager/mail-config). AS-IS는 리스트 화면을 없애고 첫 템플릿
 *  코드의 등록/수정 화면으로 바로 리다이렉트하는 구조였지만("2014.05.27 리스트 페이지
 *  삭제"), 이 프로젝트는 15개 템플릿의 등록 현황을 한눈에 보는 게 실사용성이 높아 목록
 *  화면을 그대로 유지한다(AS-IS 자체가 우연히 지운 것이지 의도적 설계가 아니었음). */
@Controller
@RequestMapping("/mail-config")
@RequiredArgsConstructor
public class MailConfigController {

    private final MailConfigService mailConfigService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("templates", mailConfigService.templateList());
        return "mail-config/list";
    }

    @GetMapping("/{templateId}")
    public String form(@PathVariable String templateId, Model model) {
        model.addAttribute("mailConfig", mailConfigService.getOrNew(templateId));
        model.addAttribute("label", mailConfigService.labelOf(templateId));
        return "mail-config/form";
    }

    @PostMapping("/{templateId}")
    public String save(@PathVariable String templateId, MailConfig form) {
        form.setTemplateId(templateId);
        mailConfigService.save(form);
        return "redirect:/mail-config/" + templateId + "?message=" +
                java.net.URLEncoder.encode("저장되었습니다.", java.nio.charset.StandardCharsets.UTF_8);
    }
}
