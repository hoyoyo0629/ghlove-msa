package com.ghlove.admin.web;

import com.ghlove.admin.repository.SendSmsLogRepository;
import com.ghlove.admin.repository.ShopInquiryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/** 답례품 1:1 상담문의 구버전 조회 (AS-IS opmanager/inquiry - InquiryManagerController).
 * 신규 접수는 QNA로 통합됐을 가능성이 높아 조회 전용으로만 구현 - AS-IS의 답변은
 * SMS 발송으로 처리되는데 이 프로젝트엔 실제 SMS 연동이 없어 "답변" 액션 자체는 스킵. */
@Controller
@RequiredArgsConstructor
public class ShopInquiryAdminController {

    private final ShopInquiryRepository shopInquiryRepository;
    private final SendSmsLogRepository sendSmsLogRepository;

    @GetMapping("/admin/shop-inquiries")
    public String list(Model model) {
        model.addAttribute("inquiries", shopInquiryRepository.findAllByOrderByInquiryIdDesc());
        return "shop-inquiry-admin/list";
    }

    @GetMapping("/admin/shop-inquiries/{id}")
    public String detail(@PathVariable Integer id, Model model) {
        var inquiry = shopInquiryRepository.findById(id).orElseThrow();
        model.addAttribute("inquiry", inquiry);
        model.addAttribute("smsHistory", sendSmsLogRepository.findAllByOrderBySendSmsLogIdDesc().stream()
                .filter(l -> ("INQUIRY_" + id).equals(l.getSendType()))
                .toList());
        return "shop-inquiry-admin/detail";
    }
}
