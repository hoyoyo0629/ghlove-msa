package com.ghlove.admin.web;

import com.ghlove.admin.domain.Notice;
import com.ghlove.admin.service.OperationContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** storefront(Vue3 SPA)용 고객센터 "공지사항" JSON API - {@link OperationContentController}
 * (Thymeleaf)의 `/notices` GET/`/notices/{id}` GET과 완전히 같은 조합. 기존 `/api/notices`
 * (donation 크로스서비스 호출용, locgovCode 필수)와는 다른 경로를 쓴다. */
@RestController
@RequiredArgsConstructor
public class NoticeApiController {

    private final OperationContentService operationContentService;

    public record NoticeRowDto(Integer noticeId, String subject, boolean noticeFlag, String displayDate, Integer hits) {
    }

    public record NoticeListResponse(List<NoticeRowDto> notices, int totalCount, int currentPage, int totalPages) {
    }

    @GetMapping("/api/notices/list")
    public NoticeListResponse list(@RequestParam(required = false) String category,
                                    @RequestParam(required = false) String q,
                                    @RequestParam(required = false, defaultValue = "CREATED_DATE__DESC") String sort,
                                    @RequestParam(required = false, defaultValue = "10") int size,
                                    @RequestParam(required = false, defaultValue = "1") int page) {
        var all = operationContentService.searchPublic(category, q, sort);
        int pageSize = size > 0 ? size : 10;
        int totalPages = (int) Math.ceil(all.size() / (double) pageSize);
        int currentPage = Math.max(1, Math.min(page, Math.max(totalPages, 1)));
        int from = Math.min((currentPage - 1) * pageSize, all.size());
        int to = Math.min(from + pageSize, all.size());
        List<NoticeRowDto> rows = all.subList(from, to).stream()
                .map(n -> new NoticeRowDto(n.getNoticeId(), n.getSubject(), "Y".equals(n.getNoticeFlag()),
                        formatDate(n.getCreatedDate()), n.getHits() == null ? 0 : n.getHits()))
                .toList();
        return new NoticeListResponse(rows, all.size(), currentPage, Math.max(totalPages, 1));
    }

    public record NoticeDetailDto(Integer noticeId, String subject, String content, String displayDate, int displayHits) {
    }

    @GetMapping("/api/notices/{id}")
    public ResponseEntity<NoticeDetailDto> detail(@PathVariable Integer id) {
        Notice notice;
        try {
            operationContentService.addHit(id);
            notice = operationContentService.notice(id);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).build();
        }
        return ResponseEntity.ok(new NoticeDetailDto(notice.getNoticeId(), notice.getSubject(), notice.getContent(),
                formatDate(notice.getCreatedDate()), notice.getHits() == null ? 0 : notice.getHits()));
    }

    private static String formatDate(String createdDate) {
        if (createdDate == null || createdDate.length() < 8) {
            return "";
        }
        return createdDate.substring(0, 4) + "-" + createdDate.substring(4, 6) + "-" + createdDate.substring(6, 8);
    }
}
