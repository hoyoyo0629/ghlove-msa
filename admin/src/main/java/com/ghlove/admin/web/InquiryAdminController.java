package com.ghlove.admin.web;

import com.ghlove.admin.service.InquiryAdminClient;
import com.ghlove.admin.service.ManagerException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/** 답례품 상품문의(Q&A) 관리 (SFR-005 재검토 라운드 - 원래 admin 콘솔에 노출되지 않던
 *  gap을 닫는다). gift 서비스의 G_ITEM_INQUIRY를 admin 콘솔에서 답변/블라인드 처리한다. */
@Controller
@RequestMapping("/admin/gift-inquiries")
@RequiredArgsConstructor
public class InquiryAdminController {

    private final InquiryAdminClient inquiryClient;

    @GetMapping
    public String list(@RequestParam(required = false) Long itemId,
                        @RequestParam(required = false) String keyword,
                        @RequestParam(required = false) String status,
                        @RequestParam(required = false) String displayFlag,
                        Model model) {
        List<InquiryAdminClient.InquiryDto> inquiries = inquiryClient.search(itemId, keyword, status, displayFlag);
        List<Long> itemIds = inquiries.stream().map(InquiryAdminClient.InquiryDto::itemId).distinct().toList();
        model.addAttribute("inquiries", inquiries);
        model.addAttribute("itemNames", inquiryClient.itemNames(itemIds));
        model.addAttribute("reportCounts", inquiryClient.reportCounts(
                inquiries.stream().map(InquiryAdminClient.InquiryDto::inquiryId).toList()));
        model.addAttribute("itemId", itemId);
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("displayFlag", displayFlag);
        return "gift-inquiries/list";
    }

    @PostMapping("/{id}/answer")
    public String answer(@PathVariable Long id, @RequestParam String answer, RedirectAttributes redirect) {
        try {
            inquiryClient.answer(id, answer);
        } catch (ManagerException e) {
            redirect.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/gift-inquiries";
    }

    @PostMapping("/{id}/display")
    public String display(@PathVariable Long id, @RequestParam boolean display, RedirectAttributes redirect) {
        try {
            inquiryClient.setDisplay(id, display);
        } catch (ManagerException e) {
            redirect.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/gift-inquiries";
    }
}
