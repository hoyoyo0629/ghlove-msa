package com.ghlove.admin.web;

import com.ghlove.admin.service.CommonCodeService;
import com.ghlove.admin.service.FaqService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/** storefront(Vue3 SPA)용 고객센터 "FAQ" JSON API - {@link FaqController}(Thymeleaf)의
 * `/faqs` GET과 완전히 같은 조합. 조회수 증가(`POST /faqs/{id}/hit`)는 이미 순수 JSON
 * 액션이라 프론트가 그대로 재사용한다(여기서 다시 만들지 않음). */
@RestController
@RequiredArgsConstructor
public class FaqApiController {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final FaqService faqService;
    private final CommonCodeService commonCodeService;

    public record FaqRowDto(Integer id, String faqType, String faqTypeLabel, String subject, String content,
                             String updatedDate) {
    }

    public record FaqListResponse(List<FaqRowDto> faqs, int totalCount, int currentPage, int totalPages,
                                   Map<String, String> faqTypes) {
    }

    @GetMapping("/api/faqs")
    public FaqListResponse list(@RequestParam(required = false) String faqType,
                                 @RequestParam(required = false) String q,
                                 @RequestParam(required = false, defaultValue = "updated,DESC") String sort,
                                 @RequestParam(required = false, defaultValue = "10") int size,
                                 @RequestParam(required = false, defaultValue = "1") int page) {
        var all = faqService.search(faqType, q, sort);
        Map<String, String> faqTypes = commonCodeService.labelsOf("FAQ_TYPE");
        int pageSize = size > 0 ? size : 10;
        int totalPages = (int) Math.ceil(all.size() / (double) pageSize);
        int currentPage = Math.max(1, Math.min(page, Math.max(totalPages, 1)));
        int from = Math.min((currentPage - 1) * pageSize, all.size());
        int to = Math.min(from + pageSize, all.size());
        List<FaqRowDto> rows = all.subList(from, to).stream()
                .map(f -> new FaqRowDto(f.getId(), f.getFaqType(), faqTypes.get(f.getFaqType()), f.getSubject(),
                        f.getContent(), f.getUpdatedDate() != null ? f.getUpdatedDate().format(DATE_FORMAT) : null))
                .toList();
        return new FaqListResponse(rows, all.size(), currentPage, Math.max(totalPages, 1), faqTypes);
    }
}
