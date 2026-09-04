package com.ghlove.admin.web;

import com.ghlove.admin.service.CommonCodeService;
import com.ghlove.admin.service.FaqService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/** 고객센터 FAQ (AS-IS faq/list.html) - 로그인 불필요, 공개 화면. */
@Controller
@RequiredArgsConstructor
public class FaqController {

    private final FaqService faqService;
    private final CommonCodeService commonCodeService;
    private static final int PAGE_SIZE_DEFAULT = 10;

    @GetMapping("/faqs")
    public String list(@RequestParam(required = false) String faqType,
                        @RequestParam(required = false) String q,
                        @RequestParam(required = false, defaultValue = "updated,DESC") String sort,
                        @RequestParam(required = false, defaultValue = "10") int size,
                        @RequestParam(required = false, defaultValue = "1") int page,
                        Model model) {
        var all = faqService.search(faqType, q, sort);
        int pageSize = size > 0 ? size : PAGE_SIZE_DEFAULT;
        int totalPages = (int) Math.ceil(all.size() / (double) pageSize);
        int currentPage = Math.max(1, Math.min(page, Math.max(totalPages, 1)));
        int from = Math.min((currentPage - 1) * pageSize, all.size());
        int to = Math.min(from + pageSize, all.size());
        List<?> pageItems = all.subList(from, to);

        model.addAttribute("faqs", pageItems);
        model.addAttribute("totalCount", all.size());
        model.addAttribute("faqType", faqType);
        model.addAttribute("q", q);
        model.addAttribute("sort", sort);
        model.addAttribute("size", pageSize);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("faqTypes", commonCodeService.labelsOf("FAQ_TYPE"));
        return "faq/list";
    }

    /** 아코디언을 펼칠 때 AJAX로 조회수만 올린다. */
    @PostMapping("/faqs/{id}/hit")
    public ResponseEntity<Void> hit(@PathVariable Integer id) {
        faqService.addHit(id);
        return ResponseEntity.noContent().build();
    }
}
