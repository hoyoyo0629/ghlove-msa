package com.ghlove.donation.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 관리자 메인화면(AS-IS {@code /opmanager} main/index.jsp) 중 G_CNTR 몫 - AS-IS {@code slave-main-mapper.xml}의
 * {@code getOpmanagerMainInfo}(기부 4행·오프라인 접수 1행) / {@code getOpmanagerMainAmountChart} /
 * {@code getOpmanagerMainCountChart} / {@code getOpmanagerMainChartInfo} 와 {@code main-mapper.xml}의
 * {@code getOpmanagerMainTableInfo}(기부 10행) 포팅. 회원·포인트·주문·답례품 행은 각 서비스가 내고 admin이 합친다.
 *
 * <p>코드 치환은 다른 기부 통계와 같다: {@code CNTR_STTUS_CODE '200'} → {@code 'COMPLETED'}. 그 밖의 조건
 * (삭제여부·수납일 유무를 보는 쿼리와 안 보는 쿼리가 섞여 있는 것까지)은 AS-IS 쿼리마다 그대로 따랐다.
 * {@code userRole}은 AS-IS {@code getLoginUserAdminRoleCheck()} 값(SYS/LOC/OFF/WCM) - 'SYS'가 아니면 지자체·지점 조건이 붙는다.
 */
@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private static final String COMPLETED = "COMPLETED";
    private static final DateTimeFormatter YMD = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter CHART_DAY = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final EntityManager entityManager;

    /** AS-IS OpmanagerMainCount(id·label·count) - count가 원시 long이라 SUM이 null이면 0이다. */
    public record CountRow(String id, String label, long count) {
    }

    /** AS-IS MainChart(cntrDay·cntrAmt). */
    public record ChartRow(String cntrDay, long cntrAmt) {
    }

    /** main-info의 기부 몫: 오프라인 접수(오늘) / 올해·전체 기부금액·건수. */
    public List<CountRow> mainInfo(String userRole, String locgovCd, String bankCode, String bankNm) {
        boolean sys = "SYS".equals(userRole);
        String today = LocalDate.now().format(YMD);
        String yearStart = LocalDate.now().getYear() + "0101";
        String label = locgovLabel(locgovCd);
        List<CountRow> rows = new ArrayList<>();

        // offline-receipt: SYS면 오늘 오프라인 접수 전체, 아니면 소속지점(은행)·지점명이 있을 때만 그 조건을 더한다.
        StringBuilder off = new StringBuilder("select count(*) from donation.g_cntr "
                + "where cntr_path_code in ('200', '300') and cntr_de = :toDe ");
        boolean byBank = !sys && notEmpty(bankCode);
        boolean byBankNm = !sys && notEmpty(bankNm);
        if (byBank) off.append("and rcept_bank_code = :bankCode ");
        if (byBankNm) off.append("and rcept_bank_nm = :bankNm ");
        Query q = entityManager.createNativeQuery(off.toString()).setParameter("toDe", today);
        if (byBank) q.setParameter("bankCode", bankCode);
        if (byBankNm) q.setParameter("bankNm", bankNm);
        rows.add(new CountRow("offline-receipt", "오프라인 접수(오늘)", num(q.getSingleResult())));

        String locgov = sys ? "" : "and cntr_locgov_code = :locgovCd ";
        String thisYear = "and cntr_de between :fromDe and :toDe ";
        rows.add(new CountRow("this-year-cntramt", label, num(single(
                "select sum(cntr_amt) from donation.g_cntr where cntr_sttus_code = :completed " + locgov + thisYear,
                sys, locgovCd, yearStart, today))));
        rows.add(new CountRow("this-year-cntr", "총기부현황(건)", num(single(
                "select count(*) from donation.g_cntr where cntr_sttus_code = :completed " + locgov + thisYear,
                sys, locgovCd, yearStart, today))));
        // 전체 금액은 백만원 단위로 잘라서(trunc) 준다 - 화면 단위가 "(백만원)".
        rows.add(new CountRow("total-cntramt", label, num(single(
                "select trunc(sum(cntr_amt) / 1000000) from donation.g_cntr where cntr_sttus_code = :completed " + locgov,
                sys, locgovCd, null, null))));
        rows.add(new CountRow("total-cntr", "총기부현황(건)", num(single(
                "select count(*) from donation.g_cntr where cntr_sttus_code = :completed " + locgov,
                sys, locgovCd, null, null))));
        return rows;
    }

    /** 기부금액 차트 - 최근 7일(오늘 포함), 금액은 천원 단위(AS-IS 정수 나눗셈 {@code NVL(CNTR_AMT,0)/1000}). */
    public List<ChartRow> amountChart(String userRole, String locgovCd) {
        return chart("sum(cntr_amt)", userRole, locgovCd, 1000);
    }

    /** 기부건수 차트 - 최근 7일(오늘 포함). */
    public List<ChartRow> countChart(String userRole, String locgovCd) {
        return chart("count(cntr_amt)", userRole, locgovCd, 1);
    }

    /** 일주일(오늘 포함 7일) 기부금액·건수 총계/평균 - 평균은 AS-IS처럼 7로 나눈 정수. */
    public List<CountRow> chartInfo(String userRole, String locgovCd) {
        boolean sys = "SYS".equals(userRole);
        LocalDate now = LocalDate.now();
        String from = now.minusDays(6).format(YMD);
        String to = now.format(YMD);
        String where = "from donation.g_cntr where cntr_de between :fromDe and :toDe "
                + (sys ? "" : "and cntr_locgov_code = :locgovCd ")
                + "and sttemnt_pay_de is not null and delete_at = 'N' and cntr_sttus_code = :completed";
        long totalAmt = num(single("select coalesce(sum(cntr_amt), 0) " + where, sys, locgovCd, from, to));
        long totalCount = num(single("select count(*) " + where, sys, locgovCd, from, to));
        return List.of(
                new CountRow("total-amt", "일주일 기부금액 총계", totalAmt),
                new CountRow("avg-amt", "일주일 기부금액 평균", totalAmt / 7),
                new CountRow("total-count", "일주일 기부건수 총계", totalCount),
                new CountRow("avg-count", "일주일 기부건수 평균", totalCount / 7));
    }

    /**
     * 하단 "기부 현황" 표(AS-IS main-search) - 기부일자가 속한 해의 1월 1일 ~ 기부일자.
     * 일반/지정은 {@code DSGN_DNTN_BIZ_ID}가 0·null이면 일반, 그 외 지정. 온라인은 경로 100, 오프라인은 200·300.
     */
    public List<CountRow> tableInfo(String date) {
        String yearStart = date.substring(0, 4) + "0101";
        String base = "from donation.g_cntr where sttemnt_pay_de is not null and delete_at = 'N' "
                + "and cntr_sttus_code = :completed and cntr_de >= :fromDe and cntr_de <= :toDe ";
        String online = "and cntr_path_code = '100' ";
        String offline = "and cntr_path_code in ('200', '300') ";
        String general = "and (dsgn_dntn_biz_id = 0 or dsgn_dntn_biz_id is null)";
        String designated = "and (dsgn_dntn_biz_id <> 0 and dsgn_dntn_biz_id is not null)";
        String cnt = "select count(*) " + base;
        String amt = "select coalesce(sum(cntr_amt), 0) " + base;
        return List.of(
                new CountRow("total-cntr", "총 기부 건수", num(single(cnt, true, null, yearStart, date))),
                new CountRow("online-cntr", "온라인 일반기부 건수", num(single(cnt + online + general, true, null, yearStart, date))),
                new CountRow("online-prj-cntr", "온라인 지정기부 건수", num(single(cnt + online + designated, true, null, yearStart, date))),
                new CountRow("offline-cntr", "오프라인 일반기부 건수", num(single(cnt + offline + general, true, null, yearStart, date))),
                new CountRow("offline-prj-cntr", "오프라인 지정기부 건수", num(single(cnt + offline + designated, true, null, yearStart, date))),
                // AS-IS가 locgovCd 없이 부르는 쿼리라 지자체명 라벨은 늘 null이다.
                new CountRow("total-cntramt", null, num(single(amt, true, null, yearStart, date))),
                new CountRow("online-cntramt", "온라인 일반기부 금액", num(single(amt + online + general, true, null, yearStart, date))),
                new CountRow("online-prj-cntramt", "온라인 지정기부 금액", num(single(amt + online + designated, true, null, yearStart, date))),
                new CountRow("offline-cntramt", "오프라인 일반기부 금액", num(single(amt + offline + general, true, null, yearStart, date))),
                new CountRow("offline-prj-cntramt", "오프라인 지정기부 금액", num(single(amt + offline + designated, true, null, yearStart, date))));
    }

    /** AS-IS {@code (select concat('(',upper_locgov_nm,' ',locgov_nm,')') from g_locgov where locgov_code = #{locgovCd})}. */
    public String locgovLabel(String locgovCd) {
        if (!notEmpty(locgovCd)) {
            return null;
        }
        List<?> list = entityManager.createNativeQuery(
                        "select concat('(', upper_locgov_nm, ' ', locgov_nm, ')') from donation.g_locgov where locgov_code = :c")
                .setParameter("c", locgovCd).getResultList();
        return list.isEmpty() ? null : (String) list.get(0);
    }

    private List<ChartRow> chart(String agg, String userRole, String locgovCd, long divisor) {
        boolean sys = "SYS".equals(userRole);
        LocalDate now = LocalDate.now();
        String sql = "select cntr_de, " + agg + " from donation.g_cntr where cntr_de between :fromDe and :toDe "
                + (sys ? "" : "and cntr_locgov_code = :locgovCd ")
                + "and sttemnt_pay_de is not null and delete_at = 'N' and cntr_sttus_code = :completed group by cntr_de";
        Query q = bind(sql, sys, locgovCd, now.minusDays(6).format(YMD), now.format(YMD));
        java.util.Map<String, Long> byDay = new java.util.HashMap<>();
        for (Object row : q.getResultList()) {
            Object[] r = (Object[]) row;
            byDay.put((String) r[0], num(r[1]));
        }
        // AS-IS는 SYSDATE-6 ~ SYSDATE 7일을 만들어 OUTER JOIN 한다 - 기부가 없는 날도 0으로 찍힌다.
        List<ChartRow> rows = new ArrayList<>(7);
        for (int i = 6; i >= 0; i--) {
            LocalDate d = now.minusDays(i);
            rows.add(new ChartRow(d.format(CHART_DAY), byDay.getOrDefault(d.format(YMD), 0L) / divisor));
        }
        return rows;
    }

    private Object single(String sql, boolean sys, String locgovCd, String fromDe, String toDe) {
        return bind(sql, sys, locgovCd, fromDe, toDe).getSingleResult();
    }

    /** 쿼리에 들어 있는 이름만 바인딩한다(쿼리마다 쓰는 조건이 달라서). */
    private Query bind(String sql, boolean sys, String locgovCd, String fromDe, String toDe) {
        Query q = entityManager.createNativeQuery(sql);
        if (sql.contains(":completed")) q.setParameter("completed", COMPLETED);
        if (!sys && sql.contains(":locgovCd")) q.setParameter("locgovCd", locgovCd == null ? "" : locgovCd);
        if (sql.contains(":fromDe")) q.setParameter("fromDe", fromDe);
        if (sql.contains(":toDe")) q.setParameter("toDe", toDe);
        return q;
    }

    private static boolean notEmpty(String s) {
        return s != null && !s.isEmpty();
    }

    private static long num(Object o) {
        return o == null ? 0L : ((Number) o).longValue();
    }
}
