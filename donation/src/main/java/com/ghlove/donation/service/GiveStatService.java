package com.ghlove.donation.service;

import com.ghlove.donation.repository.GiveStatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 기부 통계(AS-IS opmanager/give/statistics) - 전체(all) 집계를 AS-IS GiveStatisticsTotal(JSON)
 * 모양으로 변환한다. admin이 cross-service로 호출하며 {isSuccess,data} 봉투는 admin에서 싼다.
 */
@Service
@RequiredArgsConstructor
public class GiveStatService {

    private final GiveStatRepository repository;

    public record AllSummary(long cntrCnt, long cntrPerson, long cntrAmt) {
    }

    /** 월별 행(필드명은 AS-IS all/detail.jsp JS가 읽는 그대로). */
    public record MonthRow(int cntrMonth, long cntrCnt, long cntrPerson, long cntrAmt, long cntrOnline, long cntrOffline) {
    }

    /** 시간대 행. */
    public record HourRow(int cntrHour, long cntrAmt) {
    }

    /** 지자체별 1행(연도+지자체 그룹). */
    public record LocgovYearRow(int cntrYear, String upperLocgovCode, String upperLocgovNm, String locgovCode,
                                String locgovNm, long cntrAmt, long givePersons, long giveCnt) {
    }

    public AllSummary allSummary(String year, String locgovCode) {
        List<Object[]> rows = repository.allSummary(y(year), n(locgovCode));
        if (rows.isEmpty()) {
            return new AllSummary(0, 0, 0);
        }
        Object[] r = rows.get(0);
        return new AllSummary(l(r[0]), l(r[1]), l(r[2]));
    }

    /**
     * 월별 집계 - <b>1~12월을 모두 채워서</b> 돌려준다.
     *
     * <p>AS-IS {@code GiveStatisticsServiceImpl.statisticsGiveAllTotalByMonth} 가 쿼리 결과
     * (기부가 있는 달만 나온다) 위에 1~12 루프를 돌며 빈 달을 0으로 메운다. 그 보정이 없으면
     * 차트 x축이 "1,2,3,5,6,8,9" 처럼 <b>데이터가 있는 달만</b> 찍혀 그래프가 어긋난다.
     */
    public List<MonthRow> allByMonth(String year, String locgovCode) {
        Map<Integer, MonthRow> found = repository.allByMonth(y(year), n(locgovCode)).stream()
                .map(r -> new MonthRow(i(r[0]), l(r[1]), l(r[2]), l(r[3]), l(r[4]), l(r[5])))
                .collect(Collectors.toMap(MonthRow::cntrMonth, m -> m, (a, b) -> a));
        List<MonthRow> result = new ArrayList<>(12);
        for (int m = 1; m <= 12; m++) {
            result.add(found.getOrDefault(m, new MonthRow(m, 0L, 0L, 0L, 0L, 0L)));
        }
        return result;
    }

    /**
     * 시간대별 집계 - <b>0~23시를 모두 채워서</b> 돌려준다.
     *
     * <p>AS-IS {@code statisticsGiveAllTotalByHour} 와 같다. 해당 날짜에 기부가 한 건도 없으면
     * 쿼리는 0행을 주는데, AS-IS는 24행(금액 0)으로 만들어 넘긴다 - 그래서 <b>0원인 날도
     * 그래프가 그려진다</b>(2026-10-13 사용자 지적으로 확인).
     */
    public List<HourRow> allByHour(String date, String locgovCode) {
        Map<Integer, HourRow> found = repository.allByHour(n(date), n(locgovCode)).stream()
                .map(r -> new HourRow(i(r[0]), l(r[1])))
                .collect(Collectors.toMap(HourRow::cntrHour, h -> h, (a, b) -> a));
        List<HourRow> result = new ArrayList<>(24);
        for (int h = 0; h <= 23; h++) {
            result.add(found.getOrDefault(h, new HourRow(h, 0L)));
        }
        return result;
    }

    public List<LocgovYearRow> locgovList(String year, String shLocgovCode) {
        return repository.locgovList(n(year), n(shLocgovCode)).stream()
                .map(r -> new LocgovYearRow(i(r[0]), (String) r[1], (String) r[2], (String) r[3], (String) r[4],
                        l(r[5]), l(r[6]), l(r[7]))).toList();
    }

    private static int i(Object o) {
        return o == null ? 0 : ((Number) o).intValue();
    }

    private static long l(Object o) {
        return o == null ? 0L : ((Number) o).longValue();
    }

    private static String n(String s) {
        return s == null ? "" : s.trim();
    }

    private static String y(String s) {
        String v = n(s);
        return v.isEmpty() ? String.valueOf(java.time.LocalDate.now().getYear()) : v;
    }
}
