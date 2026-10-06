package com.ghlove.admin.service;

import com.ghlove.admin.repository.ContentSatisfactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 콘텐츠 만족도 조회(AS-IS CntntsStsfdgManagerController 재현) - URL별 집계 목록 + URL 단위 상세(요약+월별).
 * stsfdg: 4=매우 만족, 3=만족, 2=불만족, 1=매우 불만족.
 */
@Service
@RequiredArgsConstructor
public class ContentSatisfactionService {

    private final ContentSatisfactionRepository repository;

    public record UrlRow(String menuUrl, String menuNm, long veryGood, long good, long bad, long veryBad) {
        public long total() { return veryGood + good + bad + veryBad; }
    }

    public record MonthRow(String ym, long veryGood, long good, long bad, long veryBad) {}

    public record Summary(String menuUrl, String menuNm, long veryGood, long good, long bad, long veryBad, long total) {}

    private static long num(Object o) { return o == null ? 0L : ((Number) o).longValue(); }
    private static String str(Object o) { return o == null ? "" : o.toString(); }

    /** URL별 만족도 집계 목록(검색어 menuUrl 부분일치, 빈 문자열이면 전체). */
    public List<UrlRow> list(String menuUrl) {
        return repository.aggregateByUrl(menuUrl == null ? "" : menuUrl.trim()).stream()
                .map(r -> new UrlRow(str(r[0]), str(r[1]), num(r[2]), num(r[3]), num(r[4]), num(r[5])))
                .toList();
    }

    public Summary summary(String menuUrl, String year) {
        String y = year == null ? "" : year.trim();
        List<Object[]> rows = repository.summaryOf(menuUrl, y);
        Object[] r = rows.isEmpty() ? new Object[]{0, 0, 0, 0, 0, ""} : rows.get(0);
        return new Summary(menuUrl, str(r[5]), num(r[0]), num(r[1]), num(r[2]), num(r[3]), num(r[4]));
    }

    public List<MonthRow> monthly(String menuUrl, String year) {
        String y = year == null ? "" : year.trim();
        return repository.monthlyOf(menuUrl, y).stream()
                .map(r -> new MonthRow(str(r[0]), num(r[1]), num(r[2]), num(r[3]), num(r[4])))
                .toList();
    }

    public List<String> years(String menuUrl) {
        return repository.yearsOf(menuUrl);
    }
}
