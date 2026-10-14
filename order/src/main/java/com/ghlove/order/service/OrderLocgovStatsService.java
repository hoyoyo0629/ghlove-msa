package com.ghlove.order.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * 통계 > 답례품 구매현황 > 지자체별(AS-IS {@code ShopStatisticsMapper.getDateStatsListGhloveQuery}의
 * {@code extra="LOCGOV"/"ITEM"/"USER"} 분기) 재현.
 *
 * <p>AS-IS는 {@code OP_ORDER_ITEM}을 {@code OP_ITEM}·{@code OP_SELLER}·{@code G_LOCGOV}·
 * {@code OP_USER}와 조인해 한 쿼리에서 지자체명/상호명/회원명까지 뽑아온다. TO-BE {@code od_order}
 * 는 locgov_code·item_id·item_name·seller_id·user_id 를 이미 비정규화해 갖고 있어 order 서비스
 * 혼자서 그룹 집계까지는 끝낼 수 있지만, 지자체명(G_LOCGOV)·상호명(gift.op_seller)·회원명
 * (member.op_user)은 전부 다른 서비스 소유라 여기서는 원시 키만 돌려주고 admin이 각 서비스의
 * 기존 클라이언트(LocgovClient/GiftClient/MemberClient)로 이름을 붙인다({@code 관심지자체}
 * 화면과 같은 방식).
 *
 * <p>★취소를 {@code sales/all}과 같이 <b>양수</b>로 담는다 - {@code SUM_COUNT = SALE_COUNT -
 * CANCEL_COUNT}가 그대로 AS-IS "합계 = 주문 - 취소"다(일/월/년 화면의 음수 규칙과 다르다).
 */
@Service
@RequiredArgsConstructor
public class OrderLocgovStatsService {

    private static final String STATUS_CANCELLED = "CANCELLED";
    private static final String STATUS_CONFIRMED = "CONFIRMED";

    private final EntityManager entityManager;

    /** 지자체별 - 전체 지자체를 그룹으로, 지자체 코드만 돌려준다(이름은 admin이 붙인다). */
    public List<GroupRow> byLocgov(String startYearMonth, String endYearMonth) {
        String sql = "select o.locgov_code, null, null, null, null, "
                + saleCancelSelect()
                + "from ord.od_order o where o.created_date is not null and o.locgov_code is not null "
                + dateWhere()
                + "group by o.locgov_code";
        return run(sql, startYearMonth, endYearMonth, null, null);
    }

    /** 답례품별 - 지자체 하나로 좁혀 품목 단위로 그룹(상호명은 admin이 seller_id로 붙인다). */
    public List<GroupRow> byItem(String startYearMonth, String endYearMonth, String locgovCode) {
        String sql = "select o.locgov_code, o.item_id, o.item_name, o.seller_id, null, "
                + saleCancelSelect()
                + "from ord.od_order o where o.created_date is not null and o.locgov_code = :locgovCode "
                + dateWhere()
                + "group by o.locgov_code, o.item_id, o.item_name, o.seller_id";
        return run(sql, startYearMonth, endYearMonth, locgovCode, null);
    }

    /**
     * 통계 > 답례품 구매현황 > 월별 지자체별 답례품 현황(AS-IS {@code getMonthLocgovItemStat})의
     * 우측 절반("지자체별 답례품 구매 현황") 재료 - 전체 지자체를 가로질러 품목 단위로 그룹 짓는다
     * ({@link #byItem}은 지자체 하나로 좁히는데 이 화면은 전체가 한 번에 필요하다).
     * ★AS-IS는 이 화면에서 취소 주문을 아예 빼고 집계한다(별도 취소 열이 없다) - 그래서
     * {@link GroupRow#saleCount()}/{@link GroupRow#saleAmount()}만 쓰면 된다.
     */
    public List<GroupRow> byItemAllLocgovs(String startYearMonth, String endYearMonth) {
        String sql = "select o.locgov_code, o.item_id, o.item_name, o.seller_id, null, "
                + saleCancelSelect()
                + "from ord.od_order o where o.created_date is not null and o.locgov_code is not null "
                + dateWhere()
                + "group by o.locgov_code, o.item_id, o.item_name, o.seller_id";
        return run(sql, startYearMonth, endYearMonth, null, null);
    }

    /**
     * 통계 > 답례품 구매현황 > 농협 사업자 답례품 현황(AS-IS {@code getNhItemSalesStats})의
     * 우측 절반("주문수"/"판매개수"/"총 판매액") 재료 - 품목 단위로 그룹 짓는다.
     *
     * <p>★AS-IS는 {@code ORDER_STATUS = '40'} <b>단일 코드</b>로 좁힌다 - 다른 화면들의
     * "취소 아닌 전부"({@code <> CANCELLED})보다 좁다. TO-BE {@code order_status} 값 집합
     * (WAITING_DEPOSIT → CONFIRMED → CANCELLED)에서 '40'에 대응하는 값은 결제가 확인된
     * {@code CONFIRMED} 하나뿐이라고 보고 그렇게 좁혔다(입금대기 상태는 AS-IS도 포함하지
     * 않는 것으로 해석).
     *
     * <p>AS-IS PRICE = {@code SALE_PRICE*QUANTITY+ETC_AMT}, SALE_CNT = {@code SUM(QUANTITY)},
     * ORDER_CNT = 주문 행 수(이 화면은 1 품목 = 1행이라 그냥 COUNT(*)). "판매액" 금액 공식은
     * 이 시리즈의 다른 화면과 같은 기준((단가+옵션가)*수량-할인)을 쓴다.
     */
    public List<NhItemSaleRow> nhItemSaleStats(String startYearMonth, String endYearMonth) {
        String sql = "select o.item_id, count(*), coalesce(sum(o.quantity),0), "
                + "coalesce(sum((coalesce(o.unit_price,0) + coalesce(o.option_price,0)) * coalesce(o.quantity,0) "
                + "- coalesce(o.discount_amount,0)),0) "
                + "from ord.od_order o where o.created_date is not null and o.order_status = :confirmed "
                + dateWhere()
                + "group by o.item_id";

        LocalDateTime startTs = startOfMonth(startYearMonth);
        LocalDateTime endTs = startOfMonthAfter(endYearMonth);
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("confirmed", STATUS_CONFIRMED);
        query.setParameter("startTs", startTs != null ? startTs : LocalDateTime.of(1970, 1, 1, 0, 0));
        query.setParameter("endTs", endTs != null ? endTs : LocalDateTime.of(9999, 1, 1, 0, 0));

        List<NhItemSaleRow> rows = new ArrayList<>();
        @SuppressWarnings("unchecked")
        List<Object[]> result = query.getResultList();
        for (Object[] r : result) {
            rows.add(new NhItemSaleRow(id(r[0]), num(r[1]), num(r[2]), num(r[3])));
        }
        return rows;
    }

    public record NhItemSaleRow(Long itemId, long orderCount, long saleQuantity, long saleAmount) {
    }

    /**
     * 통계 > 답례품 구매현황 > 카테고리별(AS-IS {@code getCategoriesStatsList}) 재료 - 지자체×품목 단위
     * 판매수량·금액. 카테고리 구분은 gift 소유라 admin이 item_id로 붙인다.
     * AS-IS는 {@code ORDER_STATUS='40'} 단일코드 + 일자 범위(yyyyMMdd) - 월 단위가 아닌 일 단위다.
     */
    public List<LocgovItemQtyRow> locgovItemQuantityStats(String startDate, String endDate) {
        String sql = "select o.locgov_code, o.item_id, coalesce(sum(o.quantity),0), "
                + "coalesce(sum((coalesce(o.unit_price,0) + coalesce(o.option_price,0)) * coalesce(o.quantity,0) "
                + "- coalesce(o.discount_amount,0)),0) "
                + "from ord.od_order o where o.created_date is not null and o.locgov_code is not null "
                + "and o.order_status = :confirmed "
                + "and o.created_date >= :startTs and o.created_date < :endTs "
                + "group by o.locgov_code, o.item_id";
        LocalDateTime startTs = startOfDay(startDate);
        LocalDateTime endTs = startOfNextDay(endDate);
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("confirmed", STATUS_CONFIRMED);
        query.setParameter("startTs", startTs != null ? startTs : LocalDateTime.of(1970, 1, 1, 0, 0));
        query.setParameter("endTs", endTs != null ? endTs : LocalDateTime.of(9999, 1, 1, 0, 0));

        List<LocgovItemQtyRow> rows = new ArrayList<>();
        @SuppressWarnings("unchecked")
        List<Object[]> result = query.getResultList();
        for (Object[] r : result) {
            rows.add(new LocgovItemQtyRow(str(r[0]), id(r[1]), num(r[2]), num(r[3])));
        }
        return rows;
    }

    public record LocgovItemQtyRow(String locgovCode, Long itemId, long quantity, long amount) {
    }

    private static LocalDateTime startOfDay(String ymd) {
        java.time.LocalDate d = parseYmd(ymd);
        return d == null ? null : d.atStartOfDay();
    }

    private static LocalDateTime startOfNextDay(String ymd) {
        java.time.LocalDate d = parseYmd(ymd);
        return d == null ? null : d.plusDays(1).atStartOfDay();
    }

    private static java.time.LocalDate parseYmd(String ymd) {
        if (ymd == null) {
            return null;
        }
        String d = ymd.replaceAll("[^0-9]", "");
        if (d.length() < 8) {
            return null;
        }
        try {
            return java.time.LocalDate.of(Integer.parseInt(d.substring(0, 4)),
                    Integer.parseInt(d.substring(4, 6)), Integer.parseInt(d.substring(6, 8)));
        } catch (RuntimeException e) {
            return null;
        }
    }

    /** 답례품별 상세(회원별) - 품목 하나로 좁혀 회원 단위로 그룹(회원명은 admin이 user_id로 붙인다).
     *  item_name은 AS-IS처럼 모든 분기가 공통으로 뽑는 값이라 여기서도 같이 돌려준다 - 상세
     *  팝업 제목(답례품명)이 이 값을 그대로 쓴다. */
    public List<GroupRow> byUser(String startYearMonth, String endYearMonth, Long itemId) {
        String sql = "select null, o.item_id, o.item_name, null, o.user_id, "
                + saleCancelSelect()
                + "from ord.od_order o where o.created_date is not null and o.item_id = :itemId "
                + dateWhere()
                + "group by o.item_id, o.item_name, o.user_id";
        return run(sql, startYearMonth, endYearMonth, null, itemId);
    }

    /** AS-IS SALE_AMOUNT/CANCEL_AMOUNT = (상품판매가 - 할인). 배송비는 이 화면에 없다. */
    private static String saleCancelSelect() {
        return "sum(case when o.order_status <> :cancelled then 1 else 0 end), "
                + "sum(case when o.order_status <> :cancelled then "
                + "(coalesce(o.unit_price,0) + coalesce(o.option_price,0)) * coalesce(o.quantity,0) - coalesce(o.discount_amount,0) "
                + "else 0 end), "
                + "sum(case when o.order_status = :cancelled then 1 else 0 end), "
                + "sum(case when o.order_status = :cancelled then "
                + "(coalesce(o.unit_price,0) + coalesce(o.option_price,0)) * coalesce(o.quantity,0) - coalesce(o.discount_amount,0) "
                + "else 0 end) ";
    }

    /** ★기간은 timestamp 비교 - {@link com.ghlove.order.service.OrderDateStatsService}와 같은 이유로
     *  AS-IS의 문자열 "YYYYMM99999999" 상한을 물려받지 않는다. 종료월의 다음 달 1일 00:00 미만으로
     *  비교해 그 달 말일 23:59:59까지 포함시킨다. */
    private static String dateWhere() {
        return "and o.created_date >= :startTs and o.created_date < :endTs ";
    }

    private List<GroupRow> run(String sql, String startYearMonth, String endYearMonth,
                                String locgovCode, Long itemId) {
        LocalDateTime startTs = startOfMonth(startYearMonth);
        LocalDateTime endTs = startOfMonthAfter(endYearMonth);

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("cancelled", STATUS_CANCELLED);
        query.setParameter("startTs", startTs != null ? startTs : LocalDateTime.of(1970, 1, 1, 0, 0));
        query.setParameter("endTs", endTs != null ? endTs : LocalDateTime.of(9999, 1, 1, 0, 0));
        if (locgovCode != null) {
            query.setParameter("locgovCode", locgovCode);
        }
        if (itemId != null) {
            query.setParameter("itemId", itemId);
        }

        List<GroupRow> rows = new ArrayList<>();
        @SuppressWarnings("unchecked")
        List<Object[]> result = query.getResultList();
        for (Object[] r : result) {
            rows.add(new GroupRow(
                    str(r[0]), id(r[1]), str(r[2]), id(r[3]), id(r[4]),
                    num(r[5]), num(r[6]), num(r[7]), num(r[8])));
        }
        return rows;
    }

    /** yyyyMM → 그 달 1일 00:00. 비었거나 못 읽으면 null(조건을 걸지 않는다). */
    private static LocalDateTime startOfMonth(String ym) {
        YearMonth m = parseYm(ym);
        return m == null ? null : m.atDay(1).atStartOfDay();
    }

    /** yyyyMM → <b>다음 달</b> 1일 00:00. "~ 미만"으로 비교해 그 달 말일 23:59:59까지 포함시킨다. */
    private static LocalDateTime startOfMonthAfter(String ym) {
        YearMonth m = parseYm(ym);
        return m == null ? null : m.plusMonths(1).atDay(1).atStartOfDay();
    }

    private static YearMonth parseYm(String ym) {
        if (ym == null || ym.isBlank()) {
            return null;
        }
        String d = ym.replaceAll("[^0-9]", "");
        if (d.length() < 6) {
            return null;
        }
        try {
            return YearMonth.of(Integer.parseInt(d.substring(0, 4)), Integer.parseInt(d.substring(4, 6)));
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static long num(Object o) {
        return o == null ? 0L : ((Number) o).longValue();
    }

    private static Long id(Object o) {
        return o == null ? null : ((Number) o).longValue();
    }

    private static String str(Object o) {
        return o == null ? null : o.toString();
    }

    /**
     * 한 그룹(지자체/답례품/회원)의 집계 한 줄. 이름이 필요한 필드(지자체명·상호명·회원명)는
     * admin이 각자의 클라이언트로 채운다 - order는 키만 돌려준다.
     */
    public record GroupRow(String locgovCode, Long itemId, String itemName, Long sellerId, Long userId,
                            long saleCount, long saleAmount, long cancelCount, long cancelAmount) {

        public long sumCount() {
            return saleCount - cancelCount;
        }

        public long sumAmount() {
            return saleAmount - cancelAmount;
        }
    }
}
