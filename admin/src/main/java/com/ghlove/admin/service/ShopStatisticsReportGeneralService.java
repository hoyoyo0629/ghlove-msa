package com.ghlove.admin.service;

import com.ghlove.admin.web.support.OpNum;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.Tuple;
import jakarta.persistence.TupleElement;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 통계 > 보고서 > 총괄 현황 (AS-IS {@code ShopStatisticsManagerController#generalStats/searchGeneralStats} +
 * {@code analysis-mapper.xml} getListMbrTnocsStats 등 + {@code ShopStatisticsReportExcelView}).
 *
 * <p>이전 구현은 원장(회원·기부·답례품)을 그때그때 집계해 월별 표를 그리는 축소판(연도 입력 하나, 탭 없음,
 * 지자체별 표 없음, 엑셀 없음)이었다. AS-IS는 <b>통계 배치가 미리 적재해 둔 사전집계 표</b>
 * (MBR_TNOCS_STATS 등 14종)를 SELECT만 한다 - 같은 표를 그대로 두고 읽는다(적재는
 * database/ddl/migration-admin-report-stats-asis-load.sql, 이 표를 채우는 배치는 아직 TO-BE에 없다).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShopStatisticsReportGeneralService {

    /** 월 컬럼 접미사 - 1월~12월. */
    private static final String[] MON = {"jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec"};

    private final EntityManager em;
    private final LocgovClient locgovClient;
    private final OpNum opNum = new OpNum();

    /** AS-IS locgovService.getUpperLocgovCode - 광역이면 자기 자신(donation 서비스의 지자체 목록으로 찾는다). */
    public String upperLocgovCode(String locgovCode) {
        return locgovClient.allLocgovs().stream()
                .filter(l -> locgovCode.equals(l.locgovCode()))
                .map(LocgovClient.LocgovInfo::upperLocgovCode)
                .filter(u -> u != null && !u.isEmpty())
                .findFirst().orElse(locgovCode);
    }

    // ---- 기준년도 select (AS-IS getCrtrYearFor*List: CRTR_YR > '2022' GROUP BY CRTR_YR) ----

    /** AS-IS GET: 탭 1=회원수, 2·3·5=기부, 그 외(4 포함)=답례품. POST는 5가 답례품 쪽이다(AS-IS 분기 그대로). */
    public List<String> crtrYears(String tabId, boolean post) {
        String table;
        if ("1".equals(tabId)) {
            table = "mbr_tnocs_stats";
        } else if ("2".equals(tabId) || "3".equals(tabId) || (!post && "5".equals(tabId))) {
            table = "dntn_tnocs_stats";
        } else {
            table = "gds_tnocs_stats";
        }
        @SuppressWarnings("unchecked")
        List<String> years = em.createNativeQuery(
                "select crtr_yr from admin." + table + " where crtr_yr > '2022' group by crtr_yr order by crtr_yr")
                .getResultList();
        return years;
    }

    // ---- 화면 표 4종 ----

    /** 회원 수 표의 한 행 - 월별(검색월까지) + 합계. */
    public record MbrRow(String crtrYr, List<String> cells) {
    }

    /** 시/도 표(기부건수·2000만원·답례품)의 한 행 - 올해 총계, 전년도, 월별 (올해·전년도·전년대비). */
    public record ProvinceRow(String crtrYr, String name, List<String> cells) {
    }

    public List<MbrRow> mbrRows(String shYear, int shMonth) {
        List<MbrRow> rows = new ArrayList<>();
        for (Map<String, Object> r : select("mbr_tnocs_stats", "", shYear, null, null, null)) {
            List<String> cells = new ArrayList<>();
            for (int m = 1; m <= Math.min(shMonth, 12); m++) {
                cells.add(opNum.format(num(r.get("mbr_cnt_" + MON[m - 1]))));
            }
            cells.add(opNum.format(num(r.get("mbr_tnocs"))));
            rows.add(new MbrRow(str(r.get("crtr_yr")), cells));
        }
        return rows;
    }

    /** @param table dntn_tnocs_stats / dntn_tnocs_max_amt_stats / gds_tnocs_stats */
    public List<ProvinceRow> provinceRows(String table, String shYear, int shMonth) {
        List<ProvinceRow> rows = new ArrayList<>();
        for (Map<String, Object> r : select(table, "order by mctpv_cd", shYear, null, null, null)) {
            List<String> cells = new ArrayList<>();
            cells.add(opNum.format(num(r.get("now_yr"))));
            cells.add(opNum.format(num(r.get("prvyr"))));
            for (int m = 1; m <= Math.min(shMonth, 12); m++) {
                String s = MON[m - 1];
                cells.add(opNum.format(num(r.get("now_yr_" + s))));
                cells.add(opNum.format(num(r.get("prvyr_" + s))));
                cells.add(rateOnScreen(str(r.get("prvyr_now_yr_rt_" + s))));
            }
            rows.add(new ProvinceRow(str(r.get("crtr_yr")), str(r.get("mctpv_nm")), cells));
        }
        return rows;
    }

    /** JSP: '0'이면 0, '-1'이면 -, 그 외 op:numberFormat(100*Float.parseFloat(값)) + "%". */
    private String rateOnScreen(String rt) {
        if (rt == null) {
            return "";
        }
        String v = rt.replace(",", "");
        if (v.equals("0")) {
            return "0";
        }
        if (v.equals("-1")) {
            return "-";
        }
        // EL: Long * Float -> 둘 다 Double로 승격해 곱한다(float→double 확장)
        return opNum.format(100 * (double) Float.parseFloat(v)) + "%";
    }

    // ---- 통계 > 보고서 > 총괄 누계 현황 (AS-IS report/generalTotal.jsp) ----

    /** 기부방법별 표의 한 행 - op:numberFormat을 거친 문자열. */
    public record PathRow(String name, String gramt, String tnocs, String gramtrt, String rt) {
    }

    /**
     * 총괄 누계 현황 4개 탭의 화면값.
     * @param crtrYr 탭 전환 JS가 쓰는 "기부방법별 목록의 마지막 행 기준년"(목록이 비면 빈 문자열)
     * @param amtCount 금액별 "건수" 행의 칸들 - 라벨 칸 포함, AS-IS처럼 코드가 있는 칸만 나온다
     * @param hab 거주지역->기부지역 - 값은 numberFormat 없이 원값 그대로
     */
    public record GeneralTotalView(String crtrYr, List<PathRow> path, List<String> amtCount, List<String> amtRate,
                                   List<String> ageCount, List<String> ageRate, List<List<String>> hab) {
    }

    private static final java.util.Set<String> AMT_CODES = java.util.Set.of("10", "20", "30", "40", "50", "60", "70");
    private static final java.util.Set<String> AGE_CODES = java.util.Set.of("10", "20", "30", "40", "50", "60", "70", "80", "90");
    private static final String[] HAB_COLS = {"seoul", "busan", "daegu", "incheon", "gwangju", "daejeon", "ulsan", "sejong",
            "gyeonggi", "chungbuk", "chungnam", "jeonnam", "gyeongbuk", "gyeongnam", "jeju", "gangwon", "jeonbuk"};

    /** AS-IS POST: 기준년·월의 표 4종(DNTN_PATH/AMT/AGE_TNOCS_STATS, HAB_DNTN_MCTPV_TNOCS_STATS)을 읽는다. */
    public GeneralTotalView generalTotal(String shYear, int shMonth) {
        List<Map<String, Object>> path = byYearMonth("dntn_path_tnocs_stats", "dntn_path_cd", shYear, shMonth);
        List<Map<String, Object>> amt = byYearMonth("dntn_amt_tnocs_stats", "dntn_amt_cd", shYear, shMonth);
        // 개발 데이터에 40대·50대가 같은 코드('50')로 들어 있어 코드만으로는 순서가 정해지지 않는다 - 이름으로 고정
        List<Map<String, Object>> age = byYearMonth("dntn_age_tnocs_stats", "dntn_age_cd, dntn_age", shYear, shMonth);
        List<Map<String, Object>> hab = byYearMonth("hab_dntn_mctpv_tnocs_stats", "hab_mctpv_cd", shYear, shMonth);

        List<PathRow> pathRows = new ArrayList<>();
        String crtrYr = "";
        for (Map<String, Object> r : path) {
            pathRows.add(new PathRow(numberFormat(str(r.get("dntn_path"))), numberFormat(String.valueOf(num(r.get("gramt")))),
                    numberFormat(String.valueOf(num(r.get("tnocs")))), numberFormat(str(r.get("gramtrt"))) + "%",
                    numberFormat(str(r.get("rt"))) + "%"));
            crtrYr = str(r.get("crtr_yr"));
        }
        List<String> amtCount = new ArrayList<>();
        List<String> amtRate = new ArrayList<>();
        bandCells(amt, "dntn_amt_cd", AMT_CODES, amtCount, amtRate);
        List<String> ageCount = new ArrayList<>();
        List<String> ageRate = new ArrayList<>();
        bandCells(age, "dntn_age_cd", AGE_CODES, ageCount, ageRate);

        List<List<String>> habRows = new ArrayList<>();
        for (Map<String, Object> r : hab) {
            List<String> cells = new ArrayList<>();
            cells.add(str(r.get("hab_mctpv_nm")) == null ? "" : str(r.get("hab_mctpv_nm")));
            for (String c : HAB_COLS) {
                cells.add(String.valueOf(num(r.get("dntn_cnt_" + c))));
            }
            habRows.add(cells);
        }
        return new GeneralTotalView(crtrYr, pathRows, amtCount, amtRate, ageCount, ageRate, habRows);
    }

    /** JSP의 코드별 c:if 사슬 - '00'은 라벨("건수"/"비율") + 값 두 칸, 정의된 다른 코드는 값 한 칸, 그 밖의 코드는 칸 없음. */
    private void bandCells(List<Map<String, Object>> list, String codeKey, java.util.Set<String> codes,
                           List<String> countCells, List<String> rateCells) {
        for (Map<String, Object> r : list) {
            String code = str(r.get(codeKey));
            String count = numberFormat(String.valueOf(num(r.get("tnocs"))));
            String rate = numberFormat(str(r.get("rt"))) + "%";
            if ("00".equals(code)) {
                countCells.add("건수");
                countCells.add(count);
                rateCells.add("비율");
                rateCells.add(rate);
            } else if (codes.contains(code)) {
                countCells.add(count);
                rateCells.add(rate);
            }
        }
    }

    // ---- 통계 > 보고서 > 지자체별 누계 현황 (AS-IS report/mctpv.jsp) ----

    /**
     * 시/도별 누계 표 - 월마다 (올해 백만원, 전년도 백만원, 전년대비) 3칸. 금액은 StatisticsReport.getNowYrMJan 그대로
     * {@code (int) Math.round(원값 / 1,000,000)}.
     * @param table mctpv_dntn_stats / mctpv_dntn_max_amt_stats / mctpv_gds_stats
     * @param dashZero 2000만원 탭 - 값 0과 전년대비 '0'을 "-"로 찍는다(다른 두 탭은 0 그대로)
     */
    public List<ProvinceRow> mctpvRows(String table, String shYear, int shMonth, boolean dashZero) {
        List<ProvinceRow> rows = new ArrayList<>();
        for (Map<String, Object> r : select(table, "order by mctpv_cd", shYear == null || shYear.isEmpty() ? null : shYear, null, null, null)) {
            List<String> cells = new ArrayList<>();
            for (int m = 1; m <= Math.min(shMonth, 12); m++) {
                String s = MON[m - 1];
                cells.add(millions(num(r.get("now_yr_" + s)), dashZero));
                cells.add(millions(num(r.get("prvyr_" + s)), dashZero));
                String rt = rateOnScreen(str(r.get("prvyr_now_yr_rt_" + s)));
                cells.add(dashZero && "0".equals(rt) ? "-" : rt);
            }
            rows.add(new ProvinceRow(str(r.get("crtr_yr")), str(r.get("mctpv_nm")), cells));
        }
        return rows;
    }

    private String millions(long won, boolean dashZero) {
        long m = (int) Math.round((double) won / 1_000_000);
        return dashZero && m == 0 ? "-" : opNum.format(m);
    }

    // ---- 통계 > 보고서 > 지자체 현황 (AS-IS report/lclgv.jsp) ----

    /** 시/도·지자체 select 옵션 - 코드와 이름. */
    public record CodeName(String code, String name) {
        /** AS-IS가 StatisticsReport JSON으로 내려주던 키 이름(lclgvCd/lclgvNm) - options-by-lclgv 응답용. */
        public Map<String, String> asLclgvJson() {
            Map<String, String> m = new LinkedHashMap<>();
            m.put("lclgvCd", code);
            m.put("lclgvNm", name);
            return m;
        }
    }

    /** 지자체 표의 한 행 - 탭 전환 JS가 마지막 행의 기준년·시/도·지자체 코드를 쓴다. */
    public record LclgvRow(String crtrYr, String mctpvCd, String lclgvCd, String mctpvNm, String lclgvNm, List<String> cells) {
    }

    /** AS-IS getMctpvForDntnList / getMctpvForGdsList - 3번 탭(답례품)이면 답례품 표, 아니면 기부 표. 총계('00000') 제외. */
    public List<CodeName> mctpvOptions(String tabId) {
        String table = "3".equals(tabId) ? "mctpv_gds_stats" : "mctpv_dntn_stats";
        return codeNames("select max(mctpv_nm), mctpv_cd from admin." + table
                + " where crtr_yr > '2022' and mctpv_cd != '00000' group by mctpv_cd order by mctpv_cd", null);
    }

    /** AS-IS getLclgvForDntnList / getLclgvForGdsList - 시/도 안의 지자체(총계 제외). */
    public List<CodeName> lclgvOptions(String tabId, String mctpv) {
        String table = "3".equals(tabId) ? "lclgv_gds_stats" : "lclgv_aod_stats";
        return codeNames("select max(lclgv_nm), lclgv_cd from admin." + table
                + " where mctpv_cd = :m and lclgv_cd != '00000' group by lclgv_cd order by lclgv_cd", mctpv);
    }

    /**
     * 지자체 담당자(ROLE_ADMIN_5/6) 범위 - 소속 지자체(lclgvCd)와 그 상위 시/도(mctpvCd), 화면에 이름으로 찍히는 소속
     * (광역이면 시/도, 기초면 "시/도 지자체"). 담당자가 아니거나 소속이 없으면 scoped=false 또는 빈 값.
     * 지자체 현황·연간통계 현황이 같은 규칙을 쓴다(AS-IS 두 JSP의 지자체 칸).
     */
    public record Scope(boolean scoped, String mctpvCd, String lclgvCd, String name) {
        public boolean hasLocgov() {
            return scoped && !lclgvCd.isEmpty();
        }
    }

    public Scope scope(com.ghlove.admin.domain.Manager manager, List<CodeName> mctpv) {
        boolean scoped = manager != null && com.ghlove.admin.service.MenuService.isLocgovScoped(manager);
        String lclgvCd = manager == null || manager.getLocgovCode() == null ? "" : manager.getLocgovCode();
        String mctpvCd = lclgvCd.isEmpty() ? "" : upperLocgovCode(lclgvCd);
        String name = "";
        if (scoped && !lclgvCd.isEmpty()) {
            String mctpvNm = mctpv.stream().filter(m -> mctpvCd.equals(m.code())).map(CodeName::name).findFirst().orElse("");
            name = mctpvNm;
            if (!lclgvCd.equals(mctpvCd)) {
                String lclgvNm = lclgvOptions("3", mctpvCd).stream()
                        .filter(l -> lclgvCd.equals(l.code())).map(CodeName::name).findFirst().orElse("");
                name = (mctpvNm + " " + lclgvNm).trim();
            }
        }
        return new Scope(scoped, mctpvCd, lclgvCd, name);
    }

    private List<CodeName> codeNames(String sql, String param) {
        Query q = em.createNativeQuery(sql);
        if (param != null) {
            q.setParameter("m", param);
        }
        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();
        List<CodeName> list = new ArrayList<>();
        for (Object[] r : rows) {
            list.add(new CodeName(str(r[1]), str(r[0])));
        }
        return list;
    }

    /**
     * 지자체 표 - 올해 총계·전년도, 월마다 (올해, 전년도, 전년대비). 금액은 StatisticsReport.getNowYrK 그대로
     * {@code (int) Math.round(원값 / 1000)}(천원).
     * @param dashZero 기부금 두 탭(전체·2000만원) - 값 0과 전년대비 '0'을 "-"로 찍는다. 답례품 탭은 0 그대로.
     */
    public List<LclgvRow> lclgvRows(String table, String shYear, int shMonth, String shMctpv, String shLclgv, boolean dashZero) {
        List<LclgvRow> rows = new ArrayList<>();
        for (Map<String, Object> r : byLocgov(table, true, shYear, shMctpv, shLclgv)) {
            List<String> cells = new ArrayList<>();
            cells.add(thousands(num(r.get("now_yr")), dashZero));
            cells.add(thousands(num(r.get("prvyr")), dashZero));
            for (int m = 1; m <= Math.min(shMonth, 12); m++) {
                String s = MON[m - 1];
                cells.add(thousands(num(r.get("now_yr_" + s)), dashZero));
                cells.add(thousands(num(r.get("prvyr_" + s)), dashZero));
                String rt = rateOnScreen(str(r.get("prvyr_now_yr_rt_" + s)));
                cells.add(dashZero && "0".equals(rt) ? "-" : rt);
            }
            rows.add(new LclgvRow(str(r.get("crtr_yr")), str(r.get("mctpv_cd")), str(r.get("lclgv_cd")),
                    str(r.get("mctpv_nm")), str(r.get("lclgv_nm")), cells));
        }
        return rows;
    }

    private String thousands(long won, boolean dashZero) {
        long k = (int) Math.round((double) won / 1000);
        return dashZero && k == 0 ? "-" : opNum.format(k);
    }

    // ---- 엑셀용 조회 (AS-IS getListMctpvDntnStats 등) ----

    /** 총괄 표 4종 원본 행(엑셀) - 기준년도 null이면 최신. 회원수 표는 정렬이 없고 나머지는 시/도 코드순. */
    public List<Map<String, Object>> totals(String table, String shYear) {
        return select(table, table.equals("mbr_tnocs_stats") ? "" : "order by mctpv_cd", shYear, null, null, null);
    }

    /** 월별 표(시/도·지자체) - 기준년도(없으면 최신)·시/도·지자체 조건. 정렬은 시/도(지자체) 코드. */
    public List<Map<String, Object>> byLocgov(String table, boolean withLclgv, String shYear, String shMctpv, String shLclgv) {
        String order = withLclgv ? "order by mctpv_cd, lclgv_cd" : "order by mctpv_cd";
        return select(table, order, shYear == null || shYear.isEmpty() ? null : shYear, shMctpv, withLclgv ? shLclgv : null, null);
    }

    /** 기부방법별·금액별·연령별·거주지→기부지 - 기준년월 조건(년이 없으면 가장 최근 년월). */
    public List<Map<String, Object>> byYearMonth(String table, String orderColumn, String shYear, int shMonth) {
        return select(table, "order by " + orderColumn, shYear, null, null, shMonth);
    }

    // ---- 공통 ----

    /**
     * @param shYear null이면 그 표의 최신 기준년도(년월 표는 최신 년월). 빈 문자열은 "그 값과 같은 행"이라 결과가 없다 -
     *               AS-IS {@code <if test='shYear == null'>} 그대로.
     * @param shMonth 년월 표만 사용 - null이면 년(=)만 본다.
     */
    private List<Map<String, Object>> select(String table, String orderBy, String shYear, String shMctpv, String shLclgv, Integer shMonth) {
        boolean yearMonthTable = shMonth != null;
        StringBuilder sql = new StringBuilder("select * from admin.").append(table).append(" where 1=1");
        if (shYear == null) {
            if (yearMonthTable) {
                sql.append(" and crtr_yr || crtr_mm = (select max(crtr_yr || crtr_mm) from admin.").append(table).append(")");
            } else {
                sql.append(" and crtr_yr = (select max(crtr_yr) from admin.").append(table).append(")");
            }
        } else {
            sql.append(" and crtr_yr = :y");
            if (yearMonthTable) {
                sql.append(" and crtr_mm = :m");
            }
        }
        if (shMctpv != null && !shMctpv.isEmpty()) {
            sql.append(" and mctpv_cd = :mctpv");
            if (shLclgv != null && !shLclgv.isEmpty()) {
                sql.append(" and lclgv_cd = :lclgv");
            }
        }
        sql.append(' ').append(orderBy);
        Query q = em.createNativeQuery(sql.toString(), Tuple.class);
        if (shYear != null) {
            q.setParameter("y", shYear);
            if (yearMonthTable) {
                q.setParameter("m", shMonth);
            }
        }
        if (shMctpv != null && !shMctpv.isEmpty()) {
            q.setParameter("mctpv", shMctpv);
            if (shLclgv != null && !shLclgv.isEmpty()) {
                q.setParameter("lclgv", shLclgv);
            }
        }
        @SuppressWarnings("unchecked")
        List<Tuple> tuples = q.getResultList();
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Tuple t : tuples) {
            Map<String, Object> row = new LinkedHashMap<>();
            for (TupleElement<?> e : t.getElements()) {
                row.put(e.getAlias().toLowerCase(Locale.ROOT), t.get(e.getAlias()));
            }
            rows.add(row);
        }
        return rows;
    }

    /** MyBatis가 NULL 컬럼을 primitive long에 매핑하면 setter가 불리지 않아 0이다. STRING 컬럼(now_yr 등)도 숫자로 읽는다. */
    private static long num(Object v) {
        if (v == null) {
            return 0L;
        }
        if (v instanceof Number n) {
            return n.longValue();
        }
        try {
            return Long.parseLong(v.toString().trim());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private static String str(Object v) {
        return v == null ? null : v.toString();
    }

    // ---- 엑셀 셀 포맷 (opframework StringUtils.numberFormat(String)) ----

    /** 빈 값은 그대로, 숫자로 읽히면 천단위 콤마(소수 3자리까지), 아니면 원본 - "12.5%" 같은 값은 그대로 남는다. */
    public static String numberFormat(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        try {
            return NumberFormat.getNumberInstance(Locale.KOREA).format(Double.parseDouble(value));
        } catch (Exception e) {
            return value;
        }
    }

    /** 엑셀 전년대비 칸 - 0이면 0, -1이면 -, 그 외 소수 2자리 퍼센트("130.66%"). */
    public static String rateInExcel(String rt) {
        if (rt == null) {
            return "null";
        }
        String v = rt.replace(",", "");
        if (v.equals("0")) {
            return "0";
        }
        if (v.equals("-1")) {
            return "-";
        }
        return numberFormat(String.format("%.2f", 100 * Float.parseFloat(v)) + "%");
    }

    public static long numOf(Object v) {
        return num(v);
    }

    public static String strOf(Object v) {
        return str(v);
    }
}
