package com.ghlove.order.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 관리자 > 답례품관리 > 주문관리 > 주문 상세정보(AS-IS opmanager/order/{pageType}/order-detail/{출고번호}/{주문번호}) 읽기 조회.
 *
 * <p>AS-IS "주문"은 (주문번호, 출고번호=ORDER_SEQUENCE) 한 쌍이고 상세 화면은 그 출고의 품목만 보여준다. TO-BE는 주문 헤더({@code od_order})
 * 밑에 출고({@code od_shipment})·품목({@code od_order_item})이 있고 {@link OrderLineListService}가 이를 AS-IS의 "주문 품목 한 줄"로 합쳐 둔 것을 그대로 쓴다
 * (출고번호는 같은 주문 안에서 출고 ID 순서로 1부터, 출고가 없는 기존 단일품목 주문은 1).
 *
 * <p>클레임은 {@code od_claim} 한 표라 AS-IS의 취소·반품·교환 신청 표 셋으로 나눠 보이도록 유형·상태만 그대로 내려준다(화면이 AS-IS 상태코드/문구로 옮긴다).
 * 지자체 범위(담당 지자체 주문만)는 {@code locgovCode}로 받아 그 지자체 품목만 남긴다 - 하나도 안 남으면 "주문 내역 없음"이다.
 */
@Service
@RequiredArgsConstructor
public class OrderDetailService {

    private final EntityManager em;

    /** 품목 한 줄 - 판매가는 (단가+옵션가)×수량, 결제포인트는 할인 뒤 실결제 포인트. */
    public record Line(long lineId, Long itemId, Long sellerId, String itemName, String optionName, Integer optionPrice, String textOption,
                       int quantity, int unitPrice, long pointAmount, String statusCode, String carrierName, String invoiceNo, String shippedDate,
                       String carrierUrl) {
    }

    /** 클레임 한 건 - lineId는 대상 품목(0이면 단일품목 주문 전체). */
    public record ClaimRow(long claimId, String claimType, String status, String reason, String createdDate, long lineId) {
    }

    public record Detail(String orderId, int orderSequence, String createdDate, Long userId, String locgovCode,
                         String receiverName, String receiverPhone, String zipcode, String address, String addressDetail, String requestNote,
                         boolean paid, List<Line> lines, List<ClaimRow> claims) {
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public Optional<Detail> detail(String orderId, int orderSequence, String locgovCode) {
        Query q = em.createNativeQuery("select t.line_id, t.item_id, t.seller_id, t.item_name, t.option_name, t.option_price, t.text_option, "
                + "t.quantity, t.unit_price, t.point_amount, t.status_code, t.carrier_name, t.invoice_no, t.shipped_dt, "
                + "t.created_dt, t.user_id, t.locgov_code, t.receiver_name, t.receiver_phone, t.zipcode, t.address, t.address_detail, t.request_note, "
                // 배송추적 링크 - 택배사명으로 택배사 표(AS-IS OP_DELIVERY_COMPANY)의 조회 URL을 찾는다(코드가 달라 이름으로 잇는다)
                + "(select dc.delivery_company_url from op_delivery_company dc where dc.delivery_company_name = t.carrier_name limit 1) "
                + "from ("+ OrderLineListService.LINES + ") t where t.order_id = :orderId and t.order_sequence = :seq order by t.line_id");
        q.setParameter("orderId", orderId).setParameter("seq", orderSequence);

        List<Line> lines = new ArrayList<>();
        Object[] head = null;
        for (Object o : (List<Object>) q.getResultList()) {
            Object[] r = (Object[]) o;
            if (locgovCode != null && !locgovCode.isBlank() && !locgovCode.equals(str(r[16]))) {
                continue;
            }
            if (head == null) {
                head = r;
            }
            lines.add(new Line(((Number) r[0]).longValue(), lng(r[1]), lng(r[2]), str(r[3]), str(r[4]), integer(r[5]), str(r[6]),
                    integer(r[7]) == null ? 0 : integer(r[7]), integer(r[8]) == null ? 0 : integer(r[8]), r[9] == null ? 0L : ((Number) r[9]).longValue(),
                    str(r[10]), str(r[11]), str(r[12]), str(r[13]), str(r[23])));
        }
        if (head == null) {
            return Optional.empty();
        }

        // 결제 여부 - 포인트 차감이 실패해 결제 자체가 안 된 채 취소된 주문만 "결제취소"(AS-IS paymentType != '1').
        // 결제 뒤 취소·반품된 주문은 AS-IS도 결제완료로 남는다(취소완료 주문도 결제완료 - 2026-10 AS-IS 실화면 확인).
        Object[] header = (Object[]) em.createNativeQuery("select order_status, point_outcome, "
                        + "(select count(*) from od_shipment s where s.order_id = o.order_id and s.point_outcome = 'FAILED') "
                        + "from od_order o where o.order_id = :orderId")
                .setParameter("orderId", orderId).getResultStream().findFirst().orElse(null);
        boolean pointFailed = header != null && ("FAILED".equals(str(header[1])) || ((Number) header[2]).intValue() > 0);
        boolean paid = !("CANCELLED".equals(header == null ? null : str(header[0])) && pointFailed);

        Set<Long> lineIds = lines.stream().map(Line::lineId).collect(Collectors.toSet());
        List<ClaimRow> claims = new ArrayList<>();
        for (Object o : (List<Object>) em.createNativeQuery("select c.claim_id, c.claim_type, c.status, c.reason, "
                        + "to_char(c.created_date, 'YYYYMMDDHH24MISS'), coalesce(c.order_item_id, 0) from od_claim c where c.order_id = :orderId order by c.claim_id desc")
                .setParameter("orderId", orderId).getResultList()) {
            Object[] r = (Object[]) o;
            long lineId = ((Number) r[5]).longValue();
            if (lineIds.contains(lineId)) {
                claims.add(new ClaimRow(((Number) r[0]).longValue(), str(r[1]), str(r[2]), str(r[3]), str(r[4]), lineId));
            }
        }

        return Optional.of(new Detail(orderId, orderSequence, str(head[14]), lng(head[15]), str(head[16]),
                str(head[17]), str(head[18]), str(head[19]), str(head[20]), str(head[21]), str(head[22]), paid, lines, claims));
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
