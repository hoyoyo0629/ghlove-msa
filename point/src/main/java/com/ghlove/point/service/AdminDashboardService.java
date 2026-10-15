package com.ghlove.point.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 관리자 메인화면(AS-IS {@code /opmanager} main/index.jsp) 중 G_CNTR_USE_POINT 몫 - AS-IS
 * {@code getOpmanagerMainInfo}의 답례품 금액 2행과 {@code getOpmanagerMainTableInfo}의 답례품 신청 2행 포팅.
 * 사용구분 {@code USE_SE_CODE '1'}(답례품 주문 사용)만 센다. 지자체명 라벨은 donation 소유라 admin이 붙인다.
 */
@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private static final DateTimeFormatter YMD = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final EntityManager entityManager;

    /** AS-IS OpmanagerMainCount(id·label·count). */
    public record CountRow(String id, String label, long count) {
    }

    /** this-year-presentamt(올해 답례품 사용 포인트) / total-presentamt(전체, 백만원 단위 trunc). */
    public List<CountRow> mainInfo(String userRole, String locgovCd) {
        boolean sys = "SYS".equals(userRole);
        String locgov = sys ? "" : "and cntr_locgov_code = :locgovCd ";
        String today = LocalDate.now().format(YMD);
        String yearStart = LocalDate.now().getYear() + "0101";

        Query thisYear = entityManager.createNativeQuery("select sum(cntr_use_point) from point.g_cntr_use_point "
                        + "where point_use_de between :fromDe and :toDe and use_se_code = '1' " + locgov)
                .setParameter("fromDe", yearStart).setParameter("toDe", today);
        Query total = entityManager.createNativeQuery("select trunc(sum(cntr_use_point) / 1000000) "
                + "from point.g_cntr_use_point where use_se_code = '1' " + locgov);
        if (!sys) {
            thisYear.setParameter("locgovCd", locgovCd == null ? "" : locgovCd);
            total.setParameter("locgovCd", locgovCd == null ? "" : locgovCd);
        }
        return List.of(new CountRow("this-year-presentamt", null, num(thisYear.getSingleResult())),
                new CountRow("total-presentamt", null, num(total.getSingleResult())));
    }

    /**
     * 하단 "답례품 현황" 표(AS-IS main-search) - 기부일자가 속한 해의 1월 1일 ~ 기부일자.
     * 신청 건수는 AS-IS처럼 주문코드로 GROUP BY 한 묶음 수(주문코드가 빈 행들은 한 묶음으로 센다).
     */
    public List<CountRow> tableInfo(String date) {
        String yearStart = date.substring(0, 4) + "0101";
        String where = "from point.g_cntr_use_point where use_se_code = '1' "
                + "and point_use_de >= :fromDe and point_use_de <= :toDe";
        Object cnt = entityManager.createNativeQuery("select count(*) from (select order_code " + where
                        + " group by order_code) t")
                .setParameter("fromDe", yearStart).setParameter("toDe", date).getSingleResult();
        Object amt = entityManager.createNativeQuery("select coalesce(sum(cntr_use_point), 0) " + where)
                .setParameter("fromDe", yearStart).setParameter("toDe", date).getSingleResult();
        return List.of(new CountRow("total-present", "총답례품현황(건)", num(cnt)),
                new CountRow("total-presentamt", null, num(amt)));
    }

    private static long num(Object o) {
        return o == null ? 0L : ((Number) o).longValue();
    }
}
