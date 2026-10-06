package com.ghlove.admin.web;

import com.ghlove.admin.domain.MailConfig;
import com.ghlove.admin.service.MailConfigService;
import com.ghlove.admin.service.MailTemplateCodes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * 이메일 설정 - AS-IS saleson.shop.mailconfig.MailConfigManagerController(/opmanager/mail-config) 재현.
 *
 * <p>AS-IS는 목록 화면을 쓰지 않는다 - {@code /list}로 들어오면 첫 템플릿의 등록/수정 화면으로
 * 리다이렉트한다("cjh - 2014.05.27 리스트 페이지 삭제" 주석과 함께 본문이 전부 주석처리돼 있다).
 * 템플릿 간 이동은 등록/수정 화면 상단의 {@code ul.mail_list}로 한다. 예전 TO-BE는 목록 화면을
 * 그대로 두고 "AS-IS가 우연히 지운 것"이라고 적어뒀는데 근거 없는 판단이라 AS-IS대로 되돌렸다.
 */
@Controller
@RequestMapping("/mail-config")
@RequiredArgsConstructor
public class MailConfigController {

    private final MailConfigService mailConfigService;
    private final MailTemplateCodes mailTemplateCodes;

    /** AS-IS list - 첫 템플릿의 등록(없으면)/수정(있으면) 화면으로 리다이렉트. */
    @GetMapping
    public String list() {
        String templateId = mailTemplateCodes.firstTemplateCodeKey();
        String path = mailConfigService.findByTemplateId(templateId) != null ? "edit" : "create";
        return "redirect:/mail-config/" + path + "/" + templateId;
    }

    /** AS-IS create(GET create/{templateId}). */
    @GetMapping("/create/{templateId}")
    public String createForm(@PathVariable String templateId, Model model) {
        MailConfig mailConfig = new MailConfig();
        mailConfig.setTemplateId(templateId);
        mailConfig.setAdminSendFlag("N");
        return form(templateId, mailConfig, model);
    }

    /** AS-IS edit(GET edit/{templateId}) - 저장된 게 없으면 등록화면과 같은 빈 폼을 보여준다. */
    @GetMapping("/edit/{templateId}")
    public String editForm(@PathVariable String templateId, Model model) {
        MailConfig mailConfig = mailConfigService.findByTemplateId(templateId);
        if (mailConfig == null) {
            return createForm(templateId, model);
        }
        return form(templateId, mailConfig, model);
    }

    private String form(String templateId, MailConfig mailConfig, Model model) {
        model.addAttribute("mailConfig", mailConfig);
        model.addAttribute("mailTemplateCodeList", mailTemplateCodes.templateEntries());
        model.addAttribute("mailChangeCodeList", mailTemplateCodes.changeCodes(templateId));
        model.addAttribute("title", mailTemplateCodes.templateCodeTitle(templateId));
        return "mail-config/form";
    }

    /** AS-IS createAction / editAction - 저장 후 수정화면으로 돌아간다. */
    @PostMapping({ "/create/{templateId}", "/edit/{templateId}" })
    public String save(@PathVariable String templateId, @ModelAttribute MailConfig form) {
        form.setTemplateId(templateId);
        form.setTitle(mailTemplateCodes.templateCodeTitle(templateId));
        mailConfigService.save(form);
        return "redirect:/mail-config/edit/" + templateId;
    }

    /**
     * AS-IS deleteAction(delete/{mailConfigId}).
     * AS-IS는 {@code @PostMapping}인데 (지금은 쓰이지 않는) 목록 JS가 {@code location.href}로
     * GET 호출을 해 405가 나는 상태다 - 양쪽 메서드를 받아 둔다.
     */
    @RequestMapping(value = "/delete/{mailConfigId}", method = { RequestMethod.GET, RequestMethod.POST })
    public String delete(@PathVariable Integer mailConfigId) {
        mailConfigService.delete(mailConfigId);
        return "redirect:/mail-config";
    }

    /** AS-IS changeCode(change-code/{templateId}) - 메일 대체코드 팝업(500x500). */
    @GetMapping("/change-code/{templateId}")
    public String changeCode(@PathVariable String templateId, Model model) {
        model.addAttribute("title", mailTemplateCodes.templateCodeTitle(templateId));
        model.addAttribute("codeList", mailTemplateCodes.changeCodes(templateId));
        return "mail-config/change-code";
    }
}
