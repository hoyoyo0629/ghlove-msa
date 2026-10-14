package com.ghlove.donation.web;

import com.ghlove.donation.domain.Locgov;
import com.ghlove.donation.service.InterestLocgovStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * admin 콘솔 "관심 지자체"(AS-IS opmanager/statistics/locgov/like) 화면이 부르는
 * cross-service API. {@code /api/admin/**} 라 {@code InternalApiAuthInterceptor} 로 게이트된다
 * (donation WebConfig 참고).
 */
@RestController
@RequestMapping("/api/admin/interest-locgov-stats")
@RequiredArgsConstructor
public class InterestLocgovStatsApiController {

    private final InterestLocgovStatsService service;

    /** AS-IS getLocgovLikeList + getLocgovLikeListTotal - 지자체 필터 + 페이징. */
    @GetMapping
    public ListResponse search(@RequestParam(required = false) String shLocgovCode,
                                @RequestParam(defaultValue = "0") int offset,
                                @RequestParam(defaultValue = "10") int limit) {
        InterestLocgovStatsService.ListPage page = service.search(shLocgovCode, offset, limit);
        return new ListResponse(page.total(), page.rows());
    }

    /** AS-IS getLikeCntByLocgov. */
    @GetMapping("/{locgovCode}/total")
    public ResponseDto<InterestLocgovStatsService.DetailTotal> total(@PathVariable String locgovCode,
                                                                       @RequestParam(required = false) String year) {
        InterestLocgovStatsService.DetailTotal total = service.totalOf(locgovCode, year);
        Locgov l = service.locgovOf(locgovCode);
        return new ResponseDto<>(total, l != null ? l.getLocgovNm() : locgovCode,
                l != null ? l.getUpperLocgovNm() : null);
    }

    /** AS-IS statisticsLocgovLikeByMonth - admin이 그대로 {isSuccess,data} 로 감싸 ajax에 돌려준다. */
    @GetMapping("/{locgovCode}/{year}/month")
    public List<InterestLocgovStatsService.MonthRow> month(@PathVariable String locgovCode,
                                                             @PathVariable String year) {
        return service.monthlyOf(locgovCode, year);
    }

    public record ListResponse(int total, List<InterestLocgovStatsService.LocgovRow> rows) {
    }

    public record ResponseDto<T>(T total, String locgovNm, String upperLocgovNm) {
    }
}
