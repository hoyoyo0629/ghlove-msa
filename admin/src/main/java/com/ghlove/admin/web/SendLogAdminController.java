package com.ghlove.admin.web;

import com.ghlove.admin.domain.IpsSendingMaster;
import com.ghlove.admin.repository.SendMailLogRepository;
import com.ghlove.admin.repository.SendSmsLogRepository;
import com.ghlove.admin.service.SmsIpsService;
import com.ghlove.admin.service.SmsType;
import com.ghlove.admin.web.support.Pagination;
import com.ghlove.admin.web.support.SmsLogParam;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

/** 메일/SMS 발송 이력 조회 (AS-IS opmanager/send-mail-log, send-sms-log). 조회 전용 -
 * 실제 발송이력 기록은 MailConfigService/추후 SMS연동이 이 테이블에 쌓아야 하는데 아직
 * 연동이 안 돼있어(다음 라운드 과제) 현재는 화면만 준비된 상태다. */
@Controller
@RequiredArgsConstructor
public class SendLogAdminController {

    private final SendMailLogRepository sendMailLogRepository;
    private final SendSmsLogRepository sendSmsLogRepository;
    private final SmsIpsService smsIpsService;

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

    /**
     * 문자전송이력(7208) - AS-IS saleson.shop.sms.SmsController(/opmanager/sms-log/list) 재현.
     * AS-IS는 이 화면을 {@code op_send_sms_log}가 아니라 외부 문자발송 연계 표
     * {@code TIF_IPS_SNDNG_M}에서 읽는다(SmsIpsService). 문자 구분·검색내용·생성일 범위로 걸러
     * 9컬럼으로 보여준다. 예전 TO-BE는 TO-BE가 만든 op_send_sms_log를 검색 없이 전부 뿌렸다.
     */
    @RequestMapping(value = "/admin/send-sms-logs", method = { RequestMethod.GET, RequestMethod.POST })
    public String smsList(@ModelAttribute("searchParam") SmsLogParam searchParam,
                          HttpServletRequest request, Model model) {
        if (searchParam.getItemsPerPage() <= 0) {
            searchParam.setItemsPerPage(10);
        }
        List<IpsSendingMaster> all = smsIpsService.search(
                blankToNull(searchParam.getSmsTypeStr()),
                blankToNull(searchParam.getQuery()),
                blankToNull(searchParam.getSearchStartDate()),
                blankToNull(searchParam.getSearchEndDate()));

        Pagination pagination = Pagination.of(all.size(), searchParam.getPage(), searchParam.getItemsPerPage())
                .withLinkFrom(request);
        model.addAttribute("smsLogList", all.stream()
                .skip(pagination.getStartRow())
                .limit(pagination.getItemsPerPage())
                .toList());
        model.addAttribute("count", all.size());
        model.addAttribute("pagination", pagination);
        model.addAttribute("smsTypes", SmsType.values());
        model.addAttribute("smsTypeTitles", SmsType.options());
        model.addAttribute("limitAmtString", smsIpsService.donationLimitAmtDetail());
        return "send-log-admin/sms-list";
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
