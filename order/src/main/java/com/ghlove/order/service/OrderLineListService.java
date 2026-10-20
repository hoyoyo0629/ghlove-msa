package com.ghlove.order.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 관리자 > 답례품관리 > 주문관리 > 전체주문관리(AS-IS opmanager/order/list, OrderMapper.getAllOrderListByParamForManager) -
 * <b>주문 품목 한 줄</b> 단위 목록. AS-IS는 OP_ORDER_ITEM(품목) 한 행이 목록 한 줄이다.
 *
 * <p>TO-BE는 주문 모델이 둘이 섞여 있어서 두 가지를 한 줄 모양으로 합친다.
 * <ul>
 *   <li>신 모델 - {@code od_order_item}(품목) ⋈ {@code od_shipment}(출고, AS-IS ORDER_SEQUENCE) ⋈ {@code od_order}(헤더)</li>
 *   <li>기존 단일품목 주문 - 출고가 없는 {@code od_order} 한 행이 한 줄(품목 정보가 헤더에 있다)</li>
 * </ul>
 *
 * <p>★주문상태: AS-IS는 품목 행에 숫자코드(0 입금대기·10 결제완료·20 배송준비중·30 배송중·35 배송완료·40 구매확정·50~59 교환·60~69 반품·
 * 70~75 취소)를 직접 저장하지만, TO-BE는 주문·출고·클레임이 상태를 나눠 갖는다. 이 서비스가 그 조합에서 AS-IS 숫자코드를 도출한다
 * (클레임 최신 건 우선 → 입금대기/취소 → 구매확정 → 배송완료 → 발송·배송중 → 그 밖은 결제완료). TO-BE에는 "배송준비중(20)"에 해당하는 상태가
 * 없어서 20은 도출되지 않고, 교환배송중(55)도 없다.
 *
 * <p>누구의 주문을 볼지(지자체 범위)와 이름으로 찾는 조건(주문자명·상호명)은 다른 서비스가 가진 정보라 admin이 코드/번호로 풀어 넘긴다.
 */
@Service
@RequiredArgsConstructor
public class OrderLineListService {

    private final EntityManager em;

    /** 검색 조건 - null이면 그 조건을 걸지 않는다(빈 목록이면 "해당 없음"이라 결과 0건). */
    public record Search(List<String> locgovCodes, String where, String query, List<Long> sellerIds, List<Long> userIds,
                         String startDate, String startTime, String endDate, String endTime, List<String> statuses) {
    }

    public record LineRow(String orderId, int orderSequence, String createdDate, Long userId, String receiverName, String receiverPhone,
                          String zipcode, String address, String addressDetail, String requestNote, String adminMemo,
                          String locgovCode, Long sellerId, Long itemId, String itemName, String optionName, Integer optionPrice,
                          String textOption, Integer quantity, Integer unitPrice, String statusCode, String carrierName,
                          String invoiceNo, String shippedDate) {
    }

    public record LinePage(int total, List<LineRow> rows) {
    }

    /** 주문 한 줄 모음 + AS-IS 상태코드 도출 - where는 이 위에 건다. */
    static final String LINES = """
            select x.*,
                   case
                     when cl.claim_type = 'CANCEL' and cl.status in ('REQUESTED', 'APPROVED') then '70'
                     when cl.claim_type = 'CANCEL' and cl.status = 'COMPLETED' then '75'
                     when cl.claim_type = 'RETURN' and cl.status in ('REQUESTED', 'APPROVED') then '60'
                     when cl.claim_type = 'RETURN' and cl.status = 'COMPLETED' then '65'
                     when cl.claim_type = 'RETURN' and cl.status = 'REJECTED' then '69'
                     when cl.claim_type = 'EXCHANGE' and cl.status in ('REQUESTED', 'APPROVED') then '50'
                     when cl.claim_type = 'EXCHANGE' and cl.status = 'COMPLETED' then '58'
                     when cl.claim_type = 'EXCHANGE' and cl.status = 'REJECTED' then '59'
                     when x.base_status in ('WAITING_DEPOSIT', 'PENDING') then '0'
                     when x.base_status = 'CANCELLED' then '75'
                     when x.confirmed_dt is not null or x.delivery_status = 'CONFIRMED' then '40'
                     when x.delivery_status = 'DELIVERED' then '35'
                     when x.delivery_status in ('SHIPPED', 'IN_TRANSIT') then '30'
                     else '10'
                   end as status_code,
                   (select cc.label from op_common_code cc where cc.code_type = 'DELIVERY_CARRIER' and cc.id = x.carrier_code limit 1) as carrier_name
              from (
                select o.order_id as order_id,
                       dense_rank() over (partition by o.order_id order by s.shipment_id) as order_sequence,
                       oi.order_item_id as line_id,
                       to_char(o.created_date, 'YYYYMMDDHH24MISS') as created_dt,
                       o.user_id as user_id, o.receiver_name as receiver_name, o.receiver_phone as receiver_phone,
                       o.delivery_zipcode as zipcode, o.delivery_address as address, o.delivery_address_detail as address_detail,
                       o.request_note as request_note, o.admin_memo as admin_memo,
                       s.locgov_code as locgov_code, oi.seller_id as seller_id, oi.item_id as item_id,
                       oi.item_name as item_name, oi.option_name as option_name, oi.option_price as option_price,
                       oi.text_option as text_option, oi.quantity as quantity, oi.unit_price as unit_price,
                       oi.item_status as base_status, s.delivery_status as delivery_status, s.confirmed_date as confirmed_dt,
                       s.carrier_code as carrier_code, s.invoice_no as invoice_no, to_char(s.shipped_date, 'YYYYMMDDHH24MISS') as shipped_dt,
                       oi.point_amount as point_amount
                  from od_order_item oi
                       inner join od_shipment s on s.shipment_id = oi.shipment_id
                       inner join od_order o on o.order_id = oi.order_id
                union all
                select o.order_id, 1, 0,
                       to_char(o.created_date, 'YYYYMMDDHH24MISS'),
                       o.user_id, o.receiver_name, o.receiver_phone, o.delivery_zipcode, o.delivery_address, o.delivery_address_detail,
                       o.request_note, o.admin_memo,
                       o.locgov_code, o.seller_id, o.item_id, o.item_name, o.option_name, o.option_price, null, o.quantity, o.unit_price,
                       o.order_status, o.delivery_status, o.confirmed_date, o.carrier_code, o.invoice_no,
                       to_char(o.shipped_date, 'YYYYMMDDHH24MISS'),
                       o.point_amount - coalesce(o.delivery_fee, 0)
                  from od_order o
                 where not exists (select 1 from od_shipment s where s.order_id = o.order_id)
              ) x
              left join lateral (
                select c.claim_type, c.status
                  from od_claim c
                 where c.order_id = x.order_id and (c.order_item_id = x.line_id or (x.line_id = 0 and c.order_item_id is null))
                 order by c.claim_id desc limit 1
              ) cl on true
            """;

    @Transactional(readOnly = true)
    public LinePage search(Search s, int offset, int limit) {
        Map<String, Object> p = new LinkedHashMap<>();
        String where = where(s, p);

        Query count = em.createNativeQuery("select count(*) from (" + LINES + ") t where " + where);
        p.forEach(count::setParameter);
        int total = ((Number) count.getSingleResult()).intValue();

        // AS-IS ORDER BY OI.CREATED_DATE DESC, ORDER_CODE DESC, ORDER_SEQUENCE ASC, SHIPPING_INFO_SEQUENCE ASC, ITEM_SEQUENCE ASC
        Query list = em.createNativeQuery("select t.order_id, t.order_sequence, t.created_dt, t.user_id, t.receiver_name, t.receiver_phone, t.zipcode, "
                + "t.address, t.address_detail, t.request_note, t.admin_memo, t.locgov_code, t.seller_id, t.item_id, t.item_name, t.option_name, "
                + "t.option_price, t.text_option, t.quantity, t.unit_price, t.status_code, t.carrier_name, t.invoice_no, t.shipped_dt "
                + "from (" + LINES + ") t where " + where
                + " order by t.created_dt desc, t.order_id desc, t.order_sequence asc, t.line_id asc limit :limit offset :offset");
        p.forEach(list::setParameter);
        list.setParameter("limit", limit > 0 ? limit : 10).setParameter("offset", Math.max(offset, 0));

        List<LineRow> rows = new ArrayList<>();
        for (Object o : list.getResultList()) {
            Object[] r = (Object[]) o;
            rows.add(new LineRow(str(r[0]), ((Number) r[1]).intValue(), str(r[2]), lng(r[3]), str(r[4]), str(r[5]), str(r[6]), str(r[7]), str(r[8]),
                    str(r[9]), str(r[10]), str(r[11]), lng(r[12]), lng(r[13]), str(r[14]), str(r[15]), integer(r[16]), str(r[17]), integer(r[18]),
                    integer(r[19]), str(r[20]), str(r[21]), str(r[22]), str(r[23])));
        }
        return new LinePage(total, rows);
    }

    /** AS-IS getAllOrderListByParamForManager OPMANAGER 분기의 where - 이 화면이 보내는 조건만. */
    private static String where(Search s, Map<String, Object> p) {
        List<String> w = new ArrayList<>();
        w.add("1 = 1");

        if (s.locgovCodes() != null) {
            if (s.locgovCodes().isEmpty()) {
                w.add("1 = 0");
            } else {
                w.add("t.locgov_code in (:locgovCodes)");
                p.put("locgovCodes", s.locgovCodes());
            }
        }
        if (s.statuses() != null && !s.statuses().isEmpty()) {
            w.add("t.status_code in (:statuses)");
            p.put("statuses", s.statuses());
        }

        // 주문일 - AS-IS: 시간까지 둘 다 고르면 시작일+시+'0000' ~ 종료일+시+'9999', 아니면 시작일 000000 ~ 종료일 239999 (문자열 비교)
        if (has(s.startDate()) && has(s.endDate())) {
            if (has(s.startTime()) && has(s.endTime())) {
                w.add("t.created_dt between :from and :to");
                p.put("from", s.startDate() + s.startTime() + "0000");
                p.put("to", s.endDate() + s.endTime() + "9999");
            } else {
                w.add("t.created_dt between :from and :to");
                p.put("from", s.startDate() + "000000");
                p.put("to", s.endDate() + "239999");
            }
        }

        if (has(s.query()) && has(s.where())) {
            switch (s.where()) {
                case "ORDER_CODE" -> {
                    w.add("t.order_id like concat('%', cast(:q as varchar), '%')");
                    p.put("q", s.query());
                }
                case "RECEIVE_NAME" -> {
                    w.add("t.receiver_name like concat('%', cast(:q as varchar), '%')");
                    p.put("q", s.query());
                }
                case "ITEM_NAME" -> {
                    w.add("t.item_name like concat('%', cast(:q as varchar), '%')");
                    p.put("q", s.query());
                }
                case "COMPANY_NAME" -> {
                    if (s.sellerIds() == null || s.sellerIds().isEmpty()) {
                        w.add("1 = 0");
                    } else {
                        w.add("t.seller_id in (:sellerIds)");
                        p.put("sellerIds", s.sellerIds());
                    }
                }
                case "USER_NAME" -> {
                    if (s.userIds() == null || s.userIds().isEmpty()) {
                        w.add("1 = 0");
                    } else {
                        w.add("t.user_id in (:userIds)");
                        p.put("userIds", s.userIds());
                    }
                }
                default -> {
                }
            }
        }
        return String.join(" and ", w);
    }

    private static boolean has(String v) {
        return v != null && !v.isBlank();
    }

    private static String str(Object o) {
        return o == null ? null : o.toString();
    }

    private static Long lng(Object o) {
        return o == null ? null : ((Number) o).longValue();
    }

    private static Integer integer(Object o) {
        return o == null ? null : ((Number) o).intValue();
    }
}
