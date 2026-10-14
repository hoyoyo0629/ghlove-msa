package com.ghlove.donation.service;

import com.ghlove.donation.domain.InterestLocgov;
import com.ghlove.donation.domain.Locgov;
import com.ghlove.donation.repository.InterestLocgovRepository;
import com.ghlove.donation.repository.LocgovRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 관심 지자체 통계 (AS-IS opmanager/statistics/locgov - StatisticsLocgovManagerController /
 * StatisticsLocgovServiceImpl / statistics-locgov-mapper.xml).
 *
 * <p>마이페이지 "관심지자체"(G_INTRST_LOCGOV) 등록행을 지자체별로 묶은 건수(목록)와,
 * 지자체 하나의 전체/연도별/월별 추이(상세)를 그대로 재현한다. 전용 통계 테이블은 없고
 * 원본 등록행을 그때그때 스캔해서 집계한다(AS-IS도 같은 방식 - 등록량이 배치성 수치가
 * 아니라서 충분하다).
 *
 * <p>★AS-IS 목록 쿼리(getLocgovLikeList)에는 연도 조건이 없다 - 전체 기간 누적이다.
 * 연도 select 는 상세 화면에만 있다.
 */
@Service
@RequiredArgsConstructor
public class InterestLocgovStatsService {

    private final InterestLocgovRepository interestLocgovRepository;
    private final LocgovRepository locgovRepository;

    /**
     * AS-IS getLocgovLikeList - 지자체코드(선택) 필터 + LIKE_CNT DESC 정렬 + 페이징.
     * AS-IS getLocgovLikeListTotal 과 같은 조건의 건수를 함께 돌려준다(목록 건수와 Pagination용).
     */
    public ListPage search(String shLocgovCode, int offset, int limit) {
        Map<String, Locgov> locgovByCode = new LinkedHashMap<>();
        locgovRepository.findAll().forEach(l -> locgovByCode.put(l.getLocgovCode(), l));

        Map<String, Long> countByLocgov = new LinkedHashMap<>();
        for (InterestLocgov r : interestLocgovRepository.findAll()) {
            if (shLocgovCode != null && !shLocgovCode.isBlank() && !shLocgovCode.equals(r.getLocgovCode())) {
                continue;
            }
            countByLocgov.merge(r.getLocgovCode(), 1L, Long::sum);
        }

        List<LocgovRow> all = new ArrayList<>();
        for (Map.Entry<String, Long> e : countByLocgov.entrySet()) {
            Locgov l = locgovByCode.get(e.getKey());
            String nm = l != null ? l.getLocgovNm() : e.getKey();
            String upperNm = l != null ? l.getUpperLocgovNm() : null;
            all.add(new LocgovRow(e.getKey(), nm, upperNm, e.getValue()));
        }
        // AS-IS: ORDER BY LIKE_CNT DESC
        all.sort(Comparator.comparingLong(LocgovRow::count).reversed());

        int total = all.size();
        List<LocgovRow> page = offset >= total ? List.of()
                : all.subList(offset, Math.min(offset + limit, total));
        return new ListPage(total, page);
    }

    /** AS-IS getLikeCntByLocgov - 전체 누적 건수 + 선택 연도의 건수(REGIST_DE 연도 일치). */
    public DetailTotal totalOf(String locgovCode, String year) {
        long total = 0;
        long yearTotal = 0;
        for (InterestLocgov r : interestLocgovRepository.findByLocgovCode(locgovCode)) {
            total++;
            if (year != null && !year.isBlank() && r.getRegistDe() != null && r.getRegistDe().startsWith(year)) {
                yearTotal++;
            }
        }
        return new DetailTotal(total, yearTotal);
    }

    /**
     * AS-IS statisticsLocgovLikeByMonth - 지자체+연도의 월별 등록건수, <b>1~12월을 모두 채워서</b>
     * 돌려준다(AS-IS StatisticsLocgovServiceImpl 이 쿼리 결과 위에 루프를 돌며 빈 달을 0으로 메운다 -
     * 안 하면 차트 x축이 데이터 있는 달만 찍힌다, 매출통계와 같은 함정).
     */
    public List<MonthRow> monthlyOf(String locgovCode, String year) {
        Map<Integer, Long> byMonth = new LinkedHashMap<>();
        for (InterestLocgov r : interestLocgovRepository.findByLocgovCode(locgovCode)) {
            if (r.getRegistDe() == null || r.getRegistDe().length() < 6) {
                continue;
            }
            if (year == null || year.isBlank() || !r.getRegistDe().startsWith(year)) {
                continue;
            }
            int month = Integer.parseInt(r.getRegistDe().substring(4, 6));
            byMonth.merge(month, 1L, Long::sum);
        }
        List<MonthRow> result = new ArrayList<>(12);
        for (int m = 1; m <= 12; m++) {
            result.add(new MonthRow(m, byMonth.getOrDefault(m, 0L)));
        }
        return result;
    }

    /** 지자체명 표시용(상세 화면 제목). */
    public Locgov locgovOf(String locgovCode) {
        return locgovRepository.findById(locgovCode).orElse(null);
    }

    public record LocgovRow(String locgovCode, String locgovNm, String upperLocgovNm, long count) {
    }

    public record ListPage(int total, List<LocgovRow> rows) {
    }

    public record DetailTotal(long total, long yearTotal) {
    }

    public record MonthRow(int month, long count) {
    }
}
