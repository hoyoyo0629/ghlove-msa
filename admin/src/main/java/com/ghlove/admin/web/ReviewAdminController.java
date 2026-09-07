package com.ghlove.admin.web;

import com.ghlove.admin.service.ManagerException;
import com.ghlove.admin.service.ReviewAdminClient;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/** 답례품 리뷰 관리자 대응 (AS-IS opmanager/item review - 답례품 상품관리 2단계 #4). gift
 *  서비스의 OP_ITEM_REVIEW를 admin 콘솔에서 승인(추천)/블라인드 처리하고 CSV로 내려받는다. */
@Controller
@RequestMapping("/admin/gift-reviews")
@RequiredArgsConstructor
public class ReviewAdminController {

    private final ReviewAdminClient reviewClient;

    @GetMapping
    public String list(@RequestParam(required = false) Long itemId,
                        @RequestParam(required = false) String keyword,
                        @RequestParam(required = false) String recommendFlag,
                        @RequestParam(required = false) String displayFlag,
                        Model model) {
        List<ReviewAdminClient.ReviewDto> reviews = reviewClient.search(itemId, keyword, recommendFlag, displayFlag);
        List<Long> itemIds = reviews.stream().map(ReviewAdminClient.ReviewDto::itemId).distinct().toList();
        model.addAttribute("reviews", reviews);
        model.addAttribute("itemNames", reviewClient.itemNames(itemIds));
        model.addAttribute("reportCounts", reviewClient.reportCounts(
                reviews.stream().map(ReviewAdminClient.ReviewDto::itemReviewId).toList()));
        model.addAttribute("itemId", itemId);
        model.addAttribute("keyword", keyword);
        model.addAttribute("recommendFlag", recommendFlag);
        model.addAttribute("displayFlag", displayFlag);
        return "gift-reviews/list";
    }

    @PostMapping("/{id}/recommend")
    public String recommend(@PathVariable Long id, @RequestParam boolean recommend, RedirectAttributes redirect) {
        try {
            reviewClient.setRecommend(id, recommend);
        } catch (ManagerException e) {
            redirect.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/gift-reviews";
    }

    @PostMapping("/{id}/display")
    public String display(@PathVariable Long id, @RequestParam boolean display, RedirectAttributes redirect) {
        try {
            reviewClient.setDisplay(id, display);
        } catch (ManagerException e) {
            redirect.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/gift-reviews";
    }

    @GetMapping("/export")
    public void export(@RequestParam(required = false) Long itemId,
                        @RequestParam(required = false) String keyword,
                        @RequestParam(required = false) String recommendFlag,
                        @RequestParam(required = false) String displayFlag,
                        HttpServletResponse response) throws IOException {
        byte[] csv = reviewClient.export(itemId, keyword, recommendFlag, displayFlag);
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"gift-reviews.csv\"; filename*=UTF-8''" + URLEncoder.encode("답례품리뷰.csv", StandardCharsets.UTF_8));
        response.getOutputStream().write(csv != null ? csv : new byte[0]);
    }
}
