package com.ghlove.donation.service;

import com.ghlove.donation.repository.DesignatedStatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 지정기부 월별통계(특정사업 월별통계) - AS-IS designated-donation/analysis/month 재현.
 * DesignatedStatRepository의 네이티브 집계 3종을 AS-IS DesignatedStat(JSON) 모양으로 변환한다.
 * admin 서비스가 cross-service로 호출하며, 응답을 {isSuccess,data} 봉투로 싸는 일은 admin 쪽에서 한다.
 */
@Service
@RequiredArgsConstructor
public class DesignatedStatService {

    private final DesignatedStatRepository repository;

    /** 사업구분/모금액 비율 공통 행(캠페인=2행, 모금액=3행). */
    public record BsnsStatRow(int tp, String columnTpDesc,
                              long prjBsns100, long prjBsns200, long prjBsns300, long prjBsns400, long prjBsnsTot) {
    }

    /** 월별 추이 행(목표금액/모금액/참여자수/사업건수 = 4행). */
    public record MonthStatRow(int tp, String columnTpDesc,
                               long m01, long m02, long m03, long m04, long m05, long m06,
                               long m07, long m08, long m09, long m10, long m11, long m12, long total) {
    }

    public List<BsnsStatRow> campaign(String shWdr, String shLocgovCode, String selYear, String prjStatus) {
        return repository.selectMonthCampaign(n(shWdr), n(shLocgovCode), year(selYear), n0(prjStatus)).stream()
                .map(DesignatedStatService::toBsnsRow).toList();
    }

    public List<BsnsStatRow> amountRaised(String shWdr, String shLocgovCode, String selYear, String prjStatus) {
        return repository.selectMonthAmountRaised(n(shWdr), n(shLocgovCode), year(selYear), n0(prjStatus)).stream()
                .map(DesignatedStatService::toBsnsRow).toList();
    }

    public List<MonthStatRow> amount(String shWdr, String shLocgovCode, String selYear, String prjStatus) {
        return repository.selectMonthAmount(n(shWdr), n(shLocgovCode), year(selYear), n0(prjStatus)).stream()
                .map(DesignatedStatService::toMonthRow).toList();
    }

    private static BsnsStatRow toBsnsRow(Object[] r) {
        return new BsnsStatRow(i(r[0]), (String) r[1], l(r[2]), l(r[3]), l(r[4]), l(r[5]), l(r[6]));
    }

    private static MonthStatRow toMonthRow(Object[] r) {
        return new MonthStatRow(i(r[0]), (String) r[1],
                l(r[2]), l(r[3]), l(r[4]), l(r[5]), l(r[6]), l(r[7]),
                l(r[8]), l(r[9]), l(r[10]), l(r[11]), l(r[12]), l(r[13]), l(r[14]));
    }

    // ---- 지자체별 통계 ----

    /** 요약 집계. */
    public record LocgovSummary(long totTargetAmt, long totCntrAmt, BigDecimal totAchvRt, long totCntrCnt) {
    }

    /** 지자체 1행. */
    public record LocgovStatRow(String locgovCode, String locgovNm, long prjCnt, long statu2Cnt, long statu9Cnt,
                                long totCntrCnt, long bsnsType100Cnt, long bsnsType200Cnt, long bsnsType300Cnt,
                                long bsnsType400Cnt, long targetAmt, long cntrAmt, BigDecimal achvRt) {
    }

    public LocgovSummary locgovSummary(String shWdr, String shLocgovCode, String bsnsType, String frDt, String toDt, String prjStatus) {
        List<Object[]> rows = repository.selectLocgovSummary(n(shWdr), n(shLocgovCode), b0(bsnsType), fr(frDt), to(toDt), n0(prjStatus));
        if (rows.isEmpty()) {
            return new LocgovSummary(0, 0, BigDecimal.ZERO, 0);
        }
        Object[] r = rows.get(0);
        return new LocgovSummary(l(r[0]), l(r[1]), bd(r[2]), l(r[3]));
    }

    public List<LocgovStatRow> locgovList(String shWdr, String shLocgovCode, String bsnsType, String frDt, String toDt, String prjStatus) {
        return repository.selectLocgovList(n(shWdr), n(shLocgovCode), b0(bsnsType), fr(frDt), to(toDt), n0(prjStatus)).stream()
                .map(r -> new LocgovStatRow((String) r[0], (String) r[1], l(r[2]), l(r[3]), l(r[4]), l(r[5]),
                        l(r[6]), l(r[7]), l(r[8]), l(r[9]), l(r[10]), l(r[11]), bd(r[12])))
                .toList();
    }

    private static BigDecimal bd(Object o) {
        return o == null ? BigDecimal.ZERO : (o instanceof BigDecimal d ? d : new BigDecimal(o.toString()));
    }

    /** bsnsType 빈값이면 '0'(전체). */
    private static String b0(String s) {
        String v = n(s);
        return v.isEmpty() ? "0" : v;
    }

    /** 기간 시작 빈값이면 전체(00000000). */
    private static String fr(String s) {
        String v = n(s);
        return v.isEmpty() ? "00000000" : v;
    }

    /** 기간 종료 빈값이면 전체(99999999). */
    private static String to(String s) {
        String v = n(s);
        return v.isEmpty() ? "99999999" : v;
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

    /** prjStatus는 빈값이면 '0'(전체)로 간주 - AS-IS 라디오 기본값. */
    private static String n0(String s) {
        String v = n(s);
        return v.isEmpty() ? "0" : v;
    }

    /** selYear는 빈값이면 올해 - AS-IS는 selYear 드롭다운에 항상 값이 있으나 방어적으로 처리. */
    private static String year(String s) {
        String v = n(s);
        return v.isEmpty() ? String.valueOf(LocalDate.now().getYear()) : v;
    }
}
