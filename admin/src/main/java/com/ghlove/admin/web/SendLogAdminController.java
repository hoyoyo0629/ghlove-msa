package com.ghlove.admin.web;

import com.ghlove.admin.repository.SendMailLogRepository;
import com.ghlove.admin.repository.SendSmsLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/** 메일/SMS 발송 이력 조회 (AS-IS opmanager/send-mail-log, send-sms-log). 조회 전용 -
 * 실제 발송이력 기록은 MailConfigService/추후 SMS연동이 이 테이블에 쌓아야 하는데 아직
 * 연동이 안 돼있어(다음 라운드 과제) 현재는 화면만 준비된 상태다. */
@Controller
@RequiredArgsConstructor
public class SendLogAdminController {

    private final SendMailLogRepository sendMailLogRepository;
    private final SendSmsLogRepository sendSmsLogRepository;

    @GetMapping("/admin/send-mail-logs")
    public String mailList(Model model) {
        model.addAttribute("logs", sendMailLogRepository.findAllByOrderBySendMailLogIdDesc());
        return "send-log-admin/mail-list";
    }

    @GetMapping("/admin/send-mail-logs/{id}")
    public String mailDetail(@PathVariable Integer id, Model model) {
        model.addAttribute("log", sendMailLogRepository.findById(id).orElseThrow());
        return "send-log-admin/mail-detail";
    }

    @GetMapping("/admin/send-sms-logs")
    public String smsList(Model model) {
        model.addAttribute("logs", sendSmsLogRepository.findAllByOrderBySendSmsLogIdDesc());
        return "send-log-admin/sms-list";
    }
}
