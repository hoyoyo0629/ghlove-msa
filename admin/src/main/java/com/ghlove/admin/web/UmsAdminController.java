package com.ghlove.admin.web;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.domain.Ums;
import com.ghlove.admin.domain.UmsDetail;
import com.ghlove.admin.repository.UmsDetailRepository;
import com.ghlove.admin.repository.UmsRepository;
import com.ghlove.admin.repository.UmsSendLogRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/** 통합알림(UMS) 관리 (AS-IS opmanager/ums - UmsManagerController). 알림 이벤트(템플릿코드)별로
 * MESSAGE(SMS)/ALIM_TALK(카카오)/PUSH 3채널을 한 화면에서 편집한다. */
@Controller
@RequestMapping("/admin/ums")
@RequiredArgsConstructor
public class UmsAdminController {

    private static final List<String> CHANNELS = List.of("MESSAGE", "ALIM_TALK", "PUSH");

    private final UmsRepository umsRepository;
    private final UmsDetailRepository umsDetailRepository;
    private final UmsSendLogRepository umsSendLogRepository;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("templates", umsRepository.findAllByOrderByIdDesc());
        return "ums-admin/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("template", null);
        model.addAttribute("channels", CHANNELS);
        model.addAttribute("details", java.util.Map.of());
        return "ums-admin/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Ums ums = umsRepository.findById(id).orElseThrow();
        var detailsByType = umsDetailRepository.findByUmsIdOrderByUmsType(id).stream()
                .collect(java.util.stream.Collectors.toMap(UmsDetail::getUmsType, d -> d));
        model.addAttribute("template", ums);
        model.addAttribute("channels", CHANNELS);
        model.addAttribute("details", detailsByType);
        return "ums-admin/form";
    }

    @PostMapping
    @Transactional
    public String create(@RequestParam String templateCode, @RequestParam String templateName,
                          @RequestParam(required = false) String nightSendFlag,
                          @RequestParam java.util.Map<String, String> allParams,
                          HttpSession session) {
        Manager manager = manager(session);
        Ums ums = new Ums();
        ums.setTemplateCode(templateCode);
        ums.setTemplateName(templateName);
        ums.setNightSendFlag(toYn(nightSendFlag));
        ums.setUsedFlag("Y");
        ums.setCreated(LocalDateTime.now());
        ums.setCreatedBy(manager.getUserId());
        ums.setUpdated(LocalDateTime.now());
        ums.setUpdatedBy(manager.getUserId());
        Ums saved = umsRepository.save(ums);
        saveDetails(saved, templateCode, allParams, manager.getUserId());
        return "redirect:/admin/ums";
    }

    @PostMapping("/{id}")
    @Transactional
    public String update(@PathVariable Long id, @RequestParam String templateCode, @RequestParam String templateName,
                          @RequestParam(required = false) String nightSendFlag,
                          @RequestParam java.util.Map<String, String> allParams,
                          HttpSession session) {
        Manager manager = manager(session);
        Ums ums = umsRepository.findById(id).orElseThrow();
        ums.setTemplateCode(templateCode);
        ums.setTemplateName(templateName);
        ums.setNightSendFlag(toYn(nightSendFlag));
        ums.setUpdated(LocalDateTime.now());
        ums.setUpdatedBy(manager.getUserId());
        umsRepository.save(ums);
        umsDetailRepository.deleteByUmsId(id);
        saveDetails(ums, templateCode, allParams, manager.getUserId());
        return "redirect:/admin/ums/" + id + "/edit";
    }

    @PostMapping("/{id}/delete")
    @Transactional
    public String delete(@PathVariable Long id) {
        umsDetailRepository.deleteByUmsId(id);
        umsRepository.deleteById(id);
        return "redirect:/admin/ums";
    }

    @GetMapping("/send-logs")
    public String sendLogs(Model model) {
        model.addAttribute("logs", umsSendLogRepository.findAllByOrderByIdDesc());
        // SFR-007 재검토 라운드 - RFP "모니터링·통계" gap fill. 성공/실패/재시도발생 건수를 집계.
        model.addAttribute("successCount", umsSendLogRepository.countBySuccessYn("Y"));
        model.addAttribute("failureCount", umsSendLogRepository.countBySuccessYn("N"));
        model.addAttribute("retriedCount", umsSendLogRepository.countByRetryCountGreaterThan(0));
        return "ums-admin/send-logs";
    }

    private void saveDetails(Ums ums, String templateCode, java.util.Map<String, String> allParams, Long managerId) {
        for (String channel : CHANNELS) {
            boolean enabled = "on".equals(allParams.get(channel + "_enabled")) || "Y".equals(allParams.get(channel + "_enabled"));
            String message = allParams.get(channel + "_message");
            if (!enabled && (message == null || message.isBlank())) {
                continue;
            }
            UmsDetail detail = new UmsDetail();
            detail.setUmsId(ums.getId());
            detail.setTemplateCode(templateCode);
            detail.setUmsType(channel);
            detail.setTitle(allParams.get(channel + "_title"));
            detail.setMessage(message);
            detail.setUsedFlag(enabled ? "Y" : "N");
            detail.setCreated(LocalDateTime.now());
            detail.setCreatedBy(managerId);
            detail.setUpdated(LocalDateTime.now());
            detail.setUpdatedBy(managerId);
            umsDetailRepository.save(detail);
        }
    }

    private static String toYn(String raw) {
        return "Y".equals(raw) || "on".equals(raw) ? "Y" : "N";
    }

    private static Manager manager(HttpSession session) {
        return (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
    }
}
