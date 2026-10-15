package com.ghlove.admin.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 통계 > 접속통계 (AS-IS {@code StatsServiceImpl}/{@code stats-mapper.xml}의 getVisitSummary·
 * getMonthStatsList·getDayStatsList). 원천은 일자별 접속자수 집계표 {@code OP_VISIT_COUNT}(언어 구분이
 * 있어 합산한다)다. AS-IS 접속 기록({@code OP_VISIT} 상세, 1,028만건)은 이 화면이 쓰지 않는다.
 *
 * <p>※TO-BE에는 접속을 이 표에 쌓는 쪽(AS-IS {@code saveVisitData} 인터셉터)이 아직 없다 - 적재한
 * AS-IS 집계(2023-01-01~2026-06-09)만 보인다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VisitStatsService {

    private static final DateTimeFormatter YMD = DateTimeFormatter.BASIC_ISO_DATE;

    private final EntityManager em;

    /** AS-IS getVisitSummary - 값이 없으면 0. */
    public Summary summary() {
        LocalDate today = LocalDate.now();
        long total = scalar("select coalesce(sum(visit_count),0) from admin.op_visit_count");
        long todayCnt = scalar("select coalesce(sum(visit_count),0) from admin.op_visit_count where visit_date = '"
                + today.format(YMD) + "'");
        long yesterday = scalar("select coalesce(sum(visit_count),0) from admin.op_visit_count where visit_date = '"
                + today.minusDays(1).format(YMD) + "'");
        long monthMax = scalar("select coalesce(max(c),0) from (select sum(visit_count) c from admin.op_visit_count "
                + "group by substring(visit_date,1,6)) a");
        long dayMax = scalar("select coalesce(max(c),0) from (select sum(visit_count) c from admin.op_visit_count "
                + "group by visit_date) b");
        long dayMin = scalar("select coalesce(min(c),0) from (select sum(visit_count) c from admin.op_visit_count "
                + "group by visit_date) c");
        return new Summary(total, todayCnt, yesterday, monthMax, dayMax, dayMin);
    }

    /** AS-IS getMonthStatsList - 시작년~종료년(문자열 비교) 범위의 월별 합계와 그 기간 전체 대비 비율. */
    public List<MonthStats> monthStats(String startYear, String endYear) {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery(
                        "select substring(visit_date,1,6) ym, sum(visit_count) cnt from admin.op_visit_count "
                                + "where substring(visit_date,1,4) between :s and :e group by 1 order by 1")
                .setParameter("s", startYear).setParameter("e", endYear).getResultList();
        long total = rows.stream().mapToLong(r -> ((Number) r[1]).longValue()).sum();
        List<MonthStats> list = new ArrayList<>();
        for (Object[] r : rows) {
            String ym = (String) r[0];
            long cnt = ((Number) r[1]).longValue();
            list.add(new MonthStats(ym, ym.substring(0, 4) + "년 " + ym.substring(4, 6) + "월", cnt, percent(cnt, total)));
        }
        return list;
    }

    /** AS-IS getDayStatsList - 그 달 1~말일을 모두 채워(기록 없는 날은 0) 일자순으로 돌려준다. */
    public List<DayStats> dayStats(String yearMonth) {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery(
                        "select substring(visit_date,7,2) d, sum(visit_count) cnt from admin.op_visit_count "
                                + "where visit_date like :p group by 1")
                .setParameter("p", yearMonth + "%").getResultList();
        long total = rows.stream().mapToLong(r -> ((Number) r[1]).longValue()).sum();
        java.util.Map<String, Long> byDay = new java.util.HashMap<>();
        for (Object[] r : rows) {
            byDay.put((String) r[0], ((Number) r[1]).longValue());
        }
        int days = YearMonth.of(Integer.parseInt(yearMonth.substring(0, 4)),
                Integer.parseInt(yearMonth.substring(4, 6))).lengthOfMonth();
        List<DayStats> list = new ArrayList<>();
        for (int d = 1; d <= days; d++) {
            String dd = String.format("%02d", d);
            Long cnt = byDay.get(dd);
            list.add(cnt == null ? new DayStats(d, 0, "0") : new DayStats(d, cnt, percent(cnt, total)));
        }
        return list;
    }

    /** AS-IS TO_NUMBER(FORMAT(x/total*100, 2)) - 소수 둘째 자리 반올림. 분모 0이면 0. */
    private static String percent(long cnt, long total) {
        if (total == 0) {
            return "0.00";
        }
        return BigDecimal.valueOf(cnt).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP).toPlainString();
    }

    private long scalar(String sql) {
        return ((Number) em.createNativeQuery(sql).getSingleResult()).longValue();
    }

    public record Summary(long total, long today, long yesterday, long monthMax, long dayMax, long dayMin) {
    }

    public record MonthStats(String yearMonth, String title, long monthCount, String percent) {
        public double percentValue() {
            return Double.parseDouble(percent);
        }
    }

    public record DayStats(int day, long visitCount, String percent) {
        /** AS-IS DayStats.getDisplayDay - 앞자리 0 없는 일. */
        public String displayDay() {
            return Integer.toString(day);
        }
    }
}
