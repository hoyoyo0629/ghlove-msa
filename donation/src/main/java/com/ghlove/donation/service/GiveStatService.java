package com.ghlove.donation.service;

import com.ghlove.donation.repository.GiveStatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public List<MonthRow> allByMonth(String year, String locgovCode) {
        return repository.allByMonth(y(year), n(locgovCode)).stream()
                .map(r -> new MonthRow(i(r[0]), l(r[1]), l(r[2]), l(r[3]), l(r[4]), l(r[5]))).toList();
    }

    public List<HourRow> allByHour(String date, String locgovCode) {
        return repository.allByHour(n(date), n(locgovCode)).stream()
                .map(r -> new HourRow(i(r[0]), l(r[1]))).toList();
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
