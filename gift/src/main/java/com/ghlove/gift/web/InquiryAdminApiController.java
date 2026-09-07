package com.ghlove.gift.web;

import com.ghlove.gift.domain.Gift;
import com.ghlove.gift.domain.Inquiry;
import com.ghlove.gift.service.GiftException;
import com.ghlove.gift.service.GiftService;
import com.ghlove.gift.service.InquiryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 답례품 상품문의(Q&A) 관리자 대응 (SFR-005 "고객 후기/문의 관리") - 원래 제공자(셀러)
 * 셀프서비스(/my/inquiries)만 있고 admin 콘솔에는 전혀 노출되지 않던 gap을 닫는다.
 * cross-service API - admin 콘솔(/admin/gift-inquiries)이 사용한다.
 */
@RestController
@RequestMapping("/api/admin/gift-inquiries")
@RequiredArgsConstructor
public class InquiryAdminApiController {

    private final InquiryService inquiryService;
    private final GiftService giftService;

    @GetMapping
    public List<Inquiry> search(@RequestParam(required = false) Long itemId,
                                 @RequestParam(required = false) String keyword,
                                 @RequestParam(required = false) String status,
                                 @RequestParam(required = false) String displayFlag) {
        return inquiryService.adminSearch(itemId, keyword, status, displayFlag);
    }

    @GetMapping("/item-names")
    public Map<Long, String> itemNames(@RequestParam List<Long> itemIds) {
        return giftService.giftsOf(itemIds).stream()
                .collect(Collectors.toMap(Gift::getItemId, Gift::getItemName, (a, b) -> a));
    }

    @GetMapping("/report-counts")
    public Map<Long, Long> reportCounts(@RequestParam List<Long> inquiryIds) {
        return inquiryService.reportCountsOf(inquiryIds);
    }

    @PostMapping("/{id}/answer")
    public Inquiry answer(@PathVariable Long id, @RequestParam String answer) {
        try {
            return inquiryService.answer(id, answer);
        } catch (GiftException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/{id}/display")
    public Inquiry display(@PathVariable Long id, @RequestParam boolean display) {
        try {
            return inquiryService.setDisplay(id, display);
        } catch (GiftException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
}
