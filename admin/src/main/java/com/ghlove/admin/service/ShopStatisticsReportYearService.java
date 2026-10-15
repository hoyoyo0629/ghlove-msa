package com.ghlove.admin.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.Tuple;
import jakarta.persistence.TupleElement;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 통계 > 보고서 > 연간통계 현황 (AS-IS {@code ShopStatisticsManagerController#yearStats} +
 * {@code report/lclgv/excel-pastYearStatsStored} + analysis-mapper.xml getList*PastYearStatsStored).
 * 화면은 조건(년도·시/도·시/군/구)만 고르는 폼이고 실제 데이터는 엑셀이다. 엑셀은 해마다 연통계 배치가 지자체별로 적재해 둔
 * 연간 집계표 7종(YEAR_*_AMT_TNOCS)과 통계용 지자체 마스터(STAT_LOCGOV)를 SELECT만 한다(적재는
 * database/ddl/migration-admin-report-year-stats-asis-load.sql, 이 표를 채우는 배치는 아직 TO-BE에 없다).
 *
 * <p>AS-IS 매퍼는 CUBRID 문법(decode/nvl/문자열 SUM 암묵 변환)이라 PostgreSQL에 맞게 옮겼다. 조건이 하나라도 null이면
 * AS-IS는 {@code = NULL}이라 결과가 비므로 빈 목록이다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShopStatisticsReportYearService {

    private final EntityManager em;
    private final CommonCodeService commonCodeService;

    /** AS-IS 년도 select - YYYY 공통코드 중 올해 이전, 2022·2023·빈 코드 제외(올해 데이터는 다음 해 2월에 집계된다). */
    public Map<String, String> yearOptions() {
        int thisYear = LocalDate.now().getYear();
        Map<String, String> options = new LinkedHashMap<>();
        commonCodeService.labelsOf("YYYY").forEach((id, label) -> {
            if (id == null || id.isEmpty() || id.equals("2022") || id.equals("2023")) {
                return;
            }
            try {
                if (Integer.parseInt(id) < thisYear) {
                    options.put(id, label);
                }
            } catch (NumberFormatException ignored) {
                // AS-IS는 숫자가 아닌 코드에서 NumberFormatException으로 화면이 깨지지만 공통코드에 그런 값은 없다
            }
        });
        return options;
    }

    /** 총괄(YEAR_SUM_AMT_TNOCS). */
    public List<Map<String, Object>> sum(String yr, String wdr, String locgov) {
        return query("select crtr_yr, upper_locgov_nm, upper_locgov_code, locgov_nm, locgov_code, sum_tnocs, sum_amt "
                + "from admin.year_sum_amt_tnocs where crtr_yr = :y and upper_locgov_code = :w and locgov_code = :l "
                + "order by ctid", yr, wdr, locgov);
    }

    /** 기부방법별(YEAR_PATH_AMT_TNOCS). */
    public List<Map<String, Object>> path(String yr, String wdr, String locgov) {
        return query("select crtr_yr, dntn_path, gramt, tnocs, gramtrt, rt from admin.year_path_amt_tnocs "
                + "where crtr_yr = :y and upper_locgov_code = :w and locgov_code = :l order by ctid", yr, wdr, locgov);
    }

    /** 기부금액별(YEAR_PRICE_AMT_TNOCS). */
    public List<Map<String, Object>> price(String yr, String wdr, String locgov) {
        return query("select crtr_yr, gramt, tnocs, atm, atmhund from admin.year_price_amt_tnocs "
                + "where crtr_yr = :y and upper_locgov_code = :w and locgov_code = :l order by ctid", yr, wdr, locgov);
    }

    /** 연령별(YEAR_AGE_AMT_TNOCS) - 등록일시 순. */
    public List<Map<String, Object>> ages(String yr, String wdr, String locgov) {
        return query("select crtr_yr, ages, tnocs, rt, asum from admin.year_age_amt_tnocs "
                + "where crtr_yr = :y and upper_locgov_code = :w and locgov_code = :l order by frst_reg_dt, ctid", yr, wdr, locgov);
    }

    /**
     * 기부자 주소지별 광역(YEAR_PSINT_AMT_TNOCS) - 17개 광역 + 합계를 고정 순서로 깔고 집계값을 붙인다.
     * 비율은 {@code trunc(cast(.. as numeric(5,1)),1)}로 소수 1자리.
     */
    public List<Map<String, Object>> psint(String yr, String wdr, String locgov) {
        return query("with categories as ("
                + "select '서울특별시' as gramt, 1 as od union all select '부산광역시', 2 union all select '대구광역시', 3 union all "
                + "select '인천광역시', 4 union all select '광주광역시', 5 union all select '대전광역시', 6 union all select '울산광역시', 7 union all "
                + "select '세종특별자치시', 8 union all select '경기도', 9 union all select '충청북도', 10 union all select '충청남도', 11 union all "
                + "select '전라남도', 12 union all select '경상북도', 13 union all select '경상남도', 14 union all select '강원특별자치도', 15 union all "
                + "select '전북특별자치도', 16 union all select '제주특별자치도', 17 union all select '소계', 18) "
                + "select case when cs.gramt = '소계' then '합계' else cs.gramt end as gramt, "
                + "coalesce(yp.tnocs, 0) as tnocs, coalesce(yp.asum, 0) as asum, "
                + "trunc(cast(coalesce(yp.tnocs_rt, 0) as numeric(5,1)), 1) as tnocsrt, "
                + "trunc(cast(coalesce(yp.asum_rt, 0) as numeric(5,1)), 1) as asumrt "
                + "from categories cs left join ("
                + "select psint_upper_locgov_nm as gramt, sum(tnocs) as tnocs, sum(asum) as asum, "
                + "sum(cast(tnocs_rt as numeric)) as tnocs_rt, sum(cast(asum_rt as numeric)) as asum_rt "
                + "from admin.year_psint_amt_tnocs where crtr_yr = :y and upper_locgov_code = :w and locgov_code = :l "
                + "group by psint_upper_locgov_nm, psint_upper_locgov_code) yp on cs.gramt = yp.gramt "
                + "order by cs.od", yr, wdr, locgov);
    }

    /** 기부자 주소지별 전체(YEAR_PSINT_AMT_TNOCS + STAT_LOCGOV) - 통계용 지자체 전부에 집계값을 붙이고 끝에 합계. */
    public List<Map<String, Object>> psintLocgov(String yr, String wdr, String locgov) {
        return query("with locgovinfo as ("
                + "select locgov_code, locgov_nm, upper_locgov_code, upper_locgov_nm, 0 as od from admin.stat_locgov "
                + "union all select '소계', '소계', '소계', '소계', 1) "
                + "select lg.upper_locgov_nm, lg.upper_locgov_code, lg.locgov_nm, lg.locgov_code, "
                + "coalesce(yp.gramt, lg.upper_locgov_nm || ' ' || lg.locgov_nm) as gramt, "
                + "coalesce(yp.tnocs, 0) as tnocs, coalesce(yp.tnocsrt, '0') as tnocsrt, "
                + "coalesce(yp.asum, 0) as asum, coalesce(yp.asumrt, '0') as asumrt "
                + "from locgovinfo lg left join ("
                + "select case when psint_upper_locgov_nm = '소계' then '합계' else psint_upper_locgov_nm || ' ' || psint_locgov_nm end as gramt, "
                + "tnocs, tnocs_rt as tnocsrt, asum, asum_rt as asumrt, psint_locgov_code "
                + "from admin.year_psint_amt_tnocs where crtr_yr = :y and upper_locgov_code = :w and locgov_code = :l) yp "
                + "on lg.locgov_code = yp.psint_locgov_code "
                + "order by lg.od, lg.locgov_code", yr, wdr, locgov);
    }

    /** 월별(YEAR_MONTH_AMT_TNOCS) - 등록일시 순. */
    public List<Map<String, Object>> month(String yr, String wdr, String locgov) {
        return query("select crtr_yr, cate_month, tnocs, tnocs_rt, asum, asum_rt, uniq_id from admin.year_month_amt_tnocs "
                + "where crtr_yr = :y and upper_locgov_code = :w and locgov_code = :l order by frst_reg_dt, ctid", yr, wdr, locgov);
    }

    /** 답례품 인기순(YEAR_GOODS_AMT_TNOCS) - 등록일시 순. */
    public List<Map<String, Object>> goods(String yr, String wdr, String locgov) {
        return query("select crtr_yr, item_code, item_name, price, sum_price, sum_quantity from admin.year_goods_amt_tnocs "
                + "where crtr_yr = :y and upper_locgov_code = :w and locgov_code = :l order by frst_reg_dt, ctid", yr, wdr, locgov);
    }

    private List<Map<String, Object>> query(String sql, String yr, String wdr, String locgov) {
        if (yr == null || wdr == null || locgov == null) {
            return new ArrayList<>();
        }
        Query q = em.createNativeQuery(sql, Tuple.class);
        q.setParameter("y", yr);
        q.setParameter("w", wdr);
        q.setParameter("l", locgov);
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
}
