package com.ghlove.order.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * 관리자 메인화면(AS-IS {@code /opmanager} main/index.jsp)의 "답례품 현황(건)" - AS-IS {@code getOpmanagerMainInfo}의
 * {@code this-year-present}·{@code total-present} 포팅. AS-IS는 {@code OP_ORDER_ITEM} 품목 행을
 * {@code ORDER_STATUS 65(반품완료)·75(취소완료)}만 빼고 센다.
 *
 * <p>TO-BE 품목 행 치환: 다품목 주문은 {@code od_order_item} 행(취소 = {@code item_status 'CANCELLED'}),
 * 품목 행이 없는 단품 주문(멀티아이템 전환 전 주문)은 {@code od_order} 한 행이 곧 품목 하나다
 * (취소 = {@code order_status 'CANCELLED'}). TO-BE에 별도 '반품완료' 상태가 없어 취소만 뺀다.
 * 지자체는 주문의 {@code locgov_code}.
 */
@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private static final String CANCELLED = "CANCELLED";

    private final EntityManager entityManager;

    /** @param thisYear true면 AS-IS {@code created_date > '{작년}1231235959'}(올해 생성분)만. */
    public long presentCount(String userRole, String locgovCd, boolean thisYear) {
        boolean sys = "SYS".equals(userRole);
        String locgov = sys ? "" : "and o.locgov_code = :locgovCd ";
        String itemRows = "select count(*) from ord.od_order_item i join ord.od_order o on o.order_id = i.order_id "
                + "where coalesce(i.item_status, '') <> :cancelled " + locgov
                + (thisYear ? "and i.created_date >= :yearStart" : "");
        String singleOrders = "select count(*) from ord.od_order o "
                + "where not exists (select 1 from ord.od_order_item i where i.order_id = o.order_id) "
                + "and coalesce(o.order_status, '') <> :cancelled " + locgov
                + (thisYear ? "and o.created_date >= :yearStart" : "");
        return count(itemRows, sys, locgovCd, thisYear) + count(singleOrders, sys, locgovCd, thisYear);
    }

    private long count(String sql, boolean sys, String locgovCd, boolean thisYear) {
        Query q = entityManager.createNativeQuery(sql).setParameter("cancelled", CANCELLED);
        if (!sys) q.setParameter("locgovCd", locgovCd == null ? "" : locgovCd);
        if (thisYear) q.setParameter("yearStart", LocalDate.now().withDayOfYear(1).atStartOfDay());
        Object o = q.getSingleResult();
        return o == null ? 0L : ((Number) o).longValue();
    }
}
