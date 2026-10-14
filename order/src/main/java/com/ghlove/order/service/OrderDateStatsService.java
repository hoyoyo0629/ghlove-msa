package com.ghlove.order.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * 일자/월/년 매출 통계 집계 - AS-IS {@code ShopStatisticsMapper.getDateStatsList} 재현.
 *
 * <p>AS-IS는 판매분과 취소분을 <b>UNION ALL 로 두 번 뽑아</b> 합친다. 취소분은 건수까지
 * {@code -1} 로 음수라서 "합계 = 판매 + 취소" 가 그대로 순액이 된다 - 그 부호 규칙을 그대로 쓴다.
 *
 * <p><b>AS-IS와 다른 점(데이터가 없어서)</b>: AS-IS는 {@code OP_ORDER_ITEM.DEVICE_TYPE}
 * (PC/MOBILE/IOS/ANDROID)으로 한 일자 안에서 행을 더 쪼갠다. TO-BE {@code od_order} 에는
 * 그 컬럼이 없어 <b>일자당 한 행</b>이 나오고, 화면은 주문방법 칸을 "-" 로 표시한다
 * (2026-10-13 사용자 결정 - 컬럼 구조는 AS-IS 그대로 두고 값만 비운다).
 *
 * <p>금액 매핑(AS-IS 컬럼 → TO-BE 컬럼):
 * <ul>
 *   <li>상품판매가 {@code (PURCHASE_PRICE + OPTION_PRICE) * QUANTITY} → {@code (unit_price + option_price) * quantity}</li>
 *   <li>할인 (쿠폰·판매자·스팟·등급·세트 5종 합) → {@code discount_amount} 하나 (TO-BE는 분리 보관하지 않는다)</li>
 *   <li>판매액 {@code SALE_PRICE * QUANTITY} → 상품판매가 - 할인</li>
 *   <li>배송비 → {@code delivery_fee}</li>
 *   <li>소계 = 판매액 + 배송비 (AS-IS {@code BaseStats.getPayAmount})</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class OrderDateStatsService {

    /** AS-IS는 취소를 {@code SALES_CANCEL_DATE} 쪽 UNION 으로 뽑는다 - TO-BE의 대응 상태값. */
    private static final String STATUS_CANCELLED = "CANCELLED";

    private final EntityManager entityManager;

    /**
     * @param type day(8자리) / month(6자리) / year(4자리) - AS-IS {@code LEFT(SALES_DATE, n)} 과 같다
     * @param startDate yyyyMMdd (없으면 전체)
     * @param endDate   yyyyMMdd (없으면 전체)
     */
    public List<DateStatRow> dateStats(String type, String startDate, String endDate) {
        int len = switch (type == null ? "day" : type) {
            case "year" -> 4;
            case "month" -> 6;
            default -> 8;
        };

        StringBuilder sql = new StringBuilder();
        sql.append("select substring(to_char(o.created_date, 'YYYYMMDD') from 1 for ").append(len).append(") as search_date, ")
           // 판매분 - 취소가 아닌 모든 주문(AS-IS는 SALES_DATE 가 있는 건)
           .append(" sum(case when o.order_status <> :cancelled then 1 else 0 end) as sale_count, ")
           .append(" sum(case when o.order_status <> :cancelled then (coalesce(o.unit_price,0) + coalesce(o.option_price,0)) * coalesce(o.quantity,0) else 0 end) as item_price, ")
           .append(" sum(case when o.order_status <> :cancelled then coalesce(o.discount_amount,0) else 0 end) as discount_amount, ")
           .append(" sum(case when o.order_status <> :cancelled then coalesce(o.delivery_fee,0) else 0 end) as shipping, ")
           // 취소분 - AS-IS 처럼 전부 음수로 담는다
           .append(" sum(case when o.order_status = :cancelled then -1 else 0 end) as cancel_count, ")
           .append(" sum(case when o.order_status = :cancelled then -((coalesce(o.unit_price,0) + coalesce(o.option_price,0)) * coalesce(o.quantity,0)) else 0 end) as cancel_item_price, ")
           .append(" sum(case when o.order_status = :cancelled then -coalesce(o.discount_amount,0) else 0 end) as cancel_discount_amount, ")
           .append(" sum(case when o.order_status = :cancelled then -coalesce(o.delivery_fee,0) else 0 end) as cancel_shipping ")
           .append("from ord.od_order o where o.created_date is not null ");

        // ★ 기간은 timestamp 끼리 비교한다. AS-IS는 날짜 컬럼이 VARCHAR 라서
        //   to_char(...) 문자열 비교를 했고, 그래서 "YYYYMM99" 같은 없는 날짜를 상한으로 쓰는
        //   편법이 가능했다. TO-BE의 created_date 는 진짜 timestamp 이므로 그 방식을 물려받지
        //   않는다 - 컬럼에 함수를 씌우지 않아 인덱스를 탈 수 있고, 종료일은 "다음날 00:00 미만"
        //   으로 잡아 그 날의 23:59:59 까지 빠짐없이 들어간다(2026-10-13 사용자 지시).
        LocalDateTime startTs = startOfDay(startDate);
        LocalDateTime endTsExclusive = startOfNextDay(endDate);
        if (startTs != null) {
            sql.append("and o.created_date >= :startTs ");
        }
        if (endTsExclusive != null) {
            sql.append("and o.created_date < :endTsExclusive ");
        }
        // AS-IS: ORDER BY T.SEARCH_DATE DESC
        sql.append("group by 1 order by 1 desc");

        Query query = entityManager.createNativeQuery(sql.toString());
        query.setParameter("cancelled", STATUS_CANCELLED);
        if (startTs != null) {
            query.setParameter("startTs", startTs);
        }
        if (endTsExclusive != null) {
            query.setParameter("endTsExclusive", endTsExclusive);
        }

        List<DateStatRow> rows = new ArrayList<>();
        @SuppressWarnings("unchecked")
        List<Object[]> result = query.getResultList();
        for (Object[] r : result) {
            long itemPrice = num(r[2]);
            long discount = num(r[3]);
            long shipping = num(r[4]);
            long cancelItemPrice = num(r[6]);
            long cancelDiscount = num(r[7]);
            long cancelShipping = num(r[8]);
            rows.add(new DateStatRow(
                    str(r[0]),
                    num(r[1]), itemPrice, discount, itemPrice - discount, shipping,
                    num(r[5]), cancelItemPrice, cancelDiscount, cancelItemPrice - cancelDiscount, cancelShipping));
        }
        return rows;
    }

    private static boolean hasText(String s) {
        return s != null && !s.isBlank();
    }

    /** yyyyMMdd → 그 날 00:00. 비었거나 못 읽으면 null(조건을 걸지 않는다). */
    private static LocalDateTime startOfDay(String ymd) {
        LocalDate d = parseYmd(ymd);
        return d == null ? null : d.atStartOfDay();
    }

    /** yyyyMMdd → <b>다음날</b> 00:00. "~ 미만"으로 비교해 종료일의 23:59:59 까지 포함시킨다. */
    private static LocalDateTime startOfNextDay(String ymd) {
        LocalDate d = parseYmd(ymd);
        return d == null ? null : d.plusDays(1).atStartOfDay();
    }

    /**
     * yyyyMMdd 를 날짜로 읽는다.
     *
     * <p>★ 일(日)이 그 달에 없는 값이면 <b>말일로 당긴다</b>. AS-IS가 월→일 드릴다운에
     * {@code YYYYMM99} 를 넘기던 흔적(옛 링크·북마크)이 들어와도 조회가 깨지지 않게 하기 위한
     * 방어다 - 문자열 비교였을 때는 그 값이 그냥 "상한"으로 동작했다.
     */
    private static LocalDate parseYmd(String ymd) {
        if (!hasText(ymd)) {
            return null;
        }
        String d = ymd.replaceAll("[^0-9]", "");
        if (d.length() < 8) {
            return null;
        }
        try {
            int year = Integer.parseInt(d.substring(0, 4));
            int month = Integer.parseInt(d.substring(4, 6));
            int day = Integer.parseInt(d.substring(6, 8));
            YearMonth ym = YearMonth.of(year, month);
            return ym.atDay(Math.min(Math.max(day, 1), ym.lengthOfMonth()));
        } catch (RuntimeException e) {
            return null;
        }
    }

    /** 네이티브 쿼리의 숫자는 드라이버에 따라 BigInteger/BigDecimal/Long 으로 온다. */
    private static long num(Object o) {
        return o == null ? 0L : ((Number) o).longValue();
    }

    private static String str(Object o) {
        return o == null ? null : o.toString();
    }

    /**
     * 한 일자(또는 월/년)의 집계 한 줄. 파생값(소계·합계)은 AS-IS {@code BaseStats} 처럼
     * 화면 쪽에서 계산한다.
     */
    public record DateStatRow(String searchDate,
                               long saleCount, long itemPrice, long discountAmount, long saleAmount, long shipping,
                               long cancelCount, long cancelItemPrice, long cancelDiscountAmount,
                               long cancelAmount, long cancelShipping) {
    }
}
