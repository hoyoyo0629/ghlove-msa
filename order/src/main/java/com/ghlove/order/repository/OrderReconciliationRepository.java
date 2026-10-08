package com.ghlove.order.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 포인트사용 정합성검증(admin 메뉴 7210)에 넘길 주문측 집계 - AS-IS
 * {@code pointcheck-mapper.getPointCheckList}의 주문 파트를 이식한 조회 전용 쿼리다.
 *
 * <p>AS-IS는 {@code OP_ORDER_ITEM} ∪ {@code OP_ORDER_ITEM_HOLD}(같은 키의 확정 주문항목이 없는
 * 미정 항목만)를 주문번호로 묶어 결제금액 {@code SUM(SALE_PRICE*QUANTITY+ETC_AMT)}, 취소금액
 * (주문상태 65·75), 입금대기금액(주문상태 0), 미정주문건수를 만들고 {@code OP_REMITTANCE_DETAIL}에서
 * 정산금액을 더한다.
 *
 * <p><b>TO-BE 주문 모델이 달라 대응을 바꿨다</b> - 이 프로젝트의 실제 주문 데이터는
 * {@code ord.od_order}에 있고, AS-IS 이름의 {@code ord.op_order_item}은 7개 컬럼만 있는 미완성
 * 잔재(0행)라 AS-IS 쿼리를 그대로 돌릴 수 없다({@code sale_price}·{@code etc_amt}·
 * {@code created_date}·{@code locgov_code}가 없다). 그래서 금액은 포인트 결제액
 * {@code od_order.point_amount}로, 취소금액은 주문상태 {@code CANCELLED}로 대응시켰다
 * (TO-BE는 결제수단이 포인트뿐이라 결제금액 = 사용포인트가 정합의 기준이다).
 * 입금대기금액은 TO-BE에 '입금대기' 상태가 없어 늘 0이다.
 * 정산금액·미정주문건수는 AS-IS와 같은 표({@code op_remittance_detail},
 * {@code op_order_item_hold})에서 그대로 읽는다 - 지금은 두 표에 쌓이는 것이 없어 0이다.
 *
 * <p><b>2026-10-08 추가</b>: AS-IS {@code pointcheck-mapper}는 네 군데 전부
 * {@code ORDER_CODE LIKE 'AB%'}로 필터한다 - 일반 고객주문("K" 접두사, {@code OrderService
 * .generateOrderId})이 아니라 <b>관리자 수기/엑셀 주문등록</b>({@code OrderAdminService
 * .createManualOrder}, "AB" 접두사)만 보는 화면이다. 처음엔 이 필터를 빠뜨려 전체 주문을
 * 보여주고 있었다 - [[order-code-format-matches-asis]] 참고.
 */
@Repository
@RequiredArgsConstructor
public class OrderReconciliationRepository {

    private final EntityManager entityManager;

    /** AS-IS 주문 파트 집계 한 줄. */
    public record OrderAmountRow(String orderCode, String locgovCode, String orderStatus,
                                 long orderAmt, long orderAmtCancel, long orderAmt0,
                                 long remittanceAmt, long holdCnt, LocalDateTime createdDate) {
    }

    public List<OrderAmountRow> orderAmounts(String query, LocalDateTime from, LocalDateTime to) {
        String sql = """
                select o.order_id                                                        as order_code
                     , max(o.locgov_code)                                                as locgov_code
                     , string_agg(distinct o.order_status, '+')                          as order_status
                     , sum(coalesce(o.point_amount, 0))                                  as order_amt
                     , sum(case when o.order_status = 'CANCELLED'
                                then coalesce(o.point_amount, 0) else 0 end)             as order_amt_cancel
                     , 0                                                                 as order_amt_0
                     , coalesce((select sum(cast(rd.sale_price * rd.quantity + rd.etc_amt as bigint))
                                   from ord.op_remittance_detail rd
                                  where rd.order_code = o.order_id), 0)                  as remittance_amt
                     , coalesce((select count(*) from ord.op_order_item_hold h
                                  where h.order_code = o.order_id
                                    and not exists (select 1 from ord.op_order_item oi
                                                     where oi.order_code = h.order_code
                                                       and oi.order_sequence = h.order_sequence
                                                       and oi.item_sequence = h.item_sequence)), 0) as hold_cnt
                     , max(o.created_date)                                               as created_date
                  from ord.od_order o
                 where o.order_id like 'AB%'
                """
                + (isBlank(query) ? "" : "   and o.order_id like concat('%', :query, '%')\n")
                + """
                   and (cast(:fromDate as timestamp) is null or o.created_date >= :fromDate)
                   and (cast(:toDate as timestamp) is null or o.created_date <= :toDate)
                 group by o.order_id
                 order by o.order_id desc
                """;

        Query nativeQuery = entityManager.createNativeQuery(sql);
        if (!isBlank(query)) {
            nativeQuery.setParameter("query", query);
        }
        nativeQuery.setParameter("fromDate", from);
        nativeQuery.setParameter("toDate", to);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = nativeQuery.getResultList();
        return rows.stream()
                .map(r -> new OrderAmountRow(
                        (String) r[0],
                        (String) r[1],
                        (String) r[2],
                        longOf(r[3]),
                        longOf(r[4]),
                        longOf(r[5]),
                        longOf(r[6]),
                        longOf(r[7]),
                        r[8] == null ? null : ((java.sql.Timestamp) r[8]).toLocalDateTime()))
                .toList();
    }

    private static long longOf(Object value) {
        return value == null ? 0L : ((Number) value).longValue();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
