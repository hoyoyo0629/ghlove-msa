package com.ghlove.order.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/** order 배치 본문 - AS-IS ghlove-batch JobServiceImpl의 주문·장바구니 job. SQL은 AS-IS 매퍼 기준, 표만 TO-BE로 치환. */
@Service
@RequiredArgsConstructor
public class OrderBatchService {

    private final EntityManager entityManager;
    private final com.ghlove.order.repository.OrderRepository orderRepository;
    private final com.ghlove.order.event.OrderSagaPublisher orderSagaPublisher;

    /**
     * autoShippingCompleteBatch(배송 완료 처리) - AS-IS {@code JobServiceImpl.autoShippingCompleteBatch}: 쇼핑몰 설정의
     * 배송완료 기준일(SHIPPING_COMPLETE_DATE)만큼 지난 배송중(AS-IS '30') 건을 배송완료('35')로.
     * AS-IS 조건 {@code LEFT(SHIPPING_DATE,8) <= (오늘-N일)} = 발송일이 N일 전 날짜까지인 것.
     * TO-BE 배송상태는 주문 단위이고 AS-IS 배송중(30, 송장 등록 시점)이 TO-BE 발송(SHIPPED, 송장 등록)·운송중(IN_TRANSIT) 둘에 해당해
     * 둘 다 배송완료(DELIVERED)로 바꾼다 - 운영자 배송상태 변경과 같은 이벤트를 낸다.
     * (AS-IS '55' 교환배송중→'58'은 TO-BE 교환이 클레임 흐름이라 주문 배송상태에 대응 값이 없다.)
     */
    @Transactional
    public String autoShippingComplete(int days) {
        java.util.List<com.ghlove.order.domain.Order> orders = byDeliveryStatusBefore(java.util.List.of("SHIPPED", "IN_TRANSIT"), "shippedDate", days);
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        for (com.ghlove.order.domain.Order o : orders) {
            o.setDeliveryStatus("DELIVERED");
            o.setDeliveredDate(now);
            o.setUpdatedDate(now);
            orderSagaPublisher.publishDeliveryUpdated(orderRepository.save(o));
        }
        return "배송완료 " + orders.size() + "건";
    }

    /** 판매자별 결제완료 답례품 건수. */
    public record SellerOrderCount(Long sellerId, long totalCnt) {
    }

    /**
     * sendNuri2PresentOrder(답례품 주문내역 알림톡) 원천 - AS-IS nuri2-mapper {@code selectPresentOrderList}: 어제 0시 이후
     * 생긴 결제완료(AS-IS ORDER_STATUS 10)·미취소 품목을 판매자별로 센다(건수 내림차순). TO-BE 품목 행은 다품목 주문의
     * {@code od_order_item}(item_status CONFIRMED) + 품목 행 없는 단품 주문({@code od_order} CONFIRMED)이고, 결제완료 다음
     * 단계(배송준비·배송중…)로 넘어간 건은 빼려고 주문의 배송상태가 아직 없는 것만 센다. AS-IS {@code DELIVERY_TYPE='2'}(배송
     * 유형)는 TO-BE에 대응 칸이 없다(전부 배송 상품). 판매자 상태·휴대폰은 gift 소유라 admin이 붙인다.
     */
    @SuppressWarnings("unchecked")
    public java.util.List<SellerOrderCount> presentOrderCountsBySeller() {
        java.time.LocalDateTime from = LocalDate.now().minusDays(1).atStartOfDay();
        java.util.List<Object[]> rows = entityManager.createNativeQuery("""
                        select seller_id, count(*) from (
                          select i.seller_id from ord.od_order_item i join ord.od_order o on o.order_id = i.order_id
                           where i.item_status = 'CONFIRMED' and o.delivery_status is null and i.created_date >= :from
                          union all
                          select o.seller_id from ord.od_order o
                           where not exists (select 1 from ord.od_order_item i where i.order_id = o.order_id)
                             and o.order_status = 'CONFIRMED' and o.delivery_status is null and o.created_date >= :from
                        ) t where seller_id is not null group by seller_id order by count(*) desc
                        """)
                .setParameter("from", from)
                .getResultList();
        return rows.stream().map(r -> new SellerOrderCount(((Number) r[0]).longValue(), ((Number) r[1]).longValue())).toList();
    }

    /** 스마트택배 조회 대상(주문번호·택배사·송장). */
    public record SmartDeliveryTarget(String orderId, String carrierCode, String invoiceNo) {
    }

    /**
     * autoSmartDeliveryShippingCompleteBatch 대상 - AS-IS {@code getBuyShippingCompleteListByParam}의 스마트택배 분기
     * (기준일 값이 "2"일 때): 배송중('30')만, 택배사 29·31 제외, 송장번호 있음, 발송일이 N일 전 날짜까지.
     * (AS-IS는 이 분기를 기준일 값이 문자 "2"인지로 고른다 - 스마트택배 기준일이 2라서 탄다.)
     */
    @SuppressWarnings("unchecked")
    public java.util.List<SmartDeliveryTarget> smartDeliveryTargets(int days) {
        java.util.List<Object[]> rows = entityManager.createNativeQuery("select order_id, carrier_code, invoice_no from ord.od_order "
                        + "where delivery_status in ('SHIPPED', 'IN_TRANSIT') and coalesce(order_status, '') <> 'CANCELLED' "
                        + "and coalesce(carrier_code, '') not in ('29', '31') and invoice_no is not null "
                        + "and shipped_date < :until order by order_id")
                .setParameter("until", LocalDate.now().minusDays(days - 1L).atStartOfDay())
                .getResultList();
        return rows.stream().map(r -> new SmartDeliveryTarget((String) r[0], (String) r[1], (String) r[2])).toList();
    }

    /** 스마트택배가 배송완료(level 6)로 확인한 주문을 배송완료로 - AS-IS updateShppingComplete. 이미 지난 상태면 그대로. */
    @Transactional
    public boolean markDeliveredBySmartDelivery(String orderId) {
        com.ghlove.order.domain.Order o = orderRepository.findById(orderId).orElse(null);
        if (o == null || !("SHIPPED".equals(o.getDeliveryStatus()) || "IN_TRANSIT".equals(o.getDeliveryStatus()))) {
            return false;
        }
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        o.setDeliveryStatus("DELIVERED");
        o.setDeliveredDate(now);
        o.setUpdatedDate(now);
        orderSagaPublisher.publishDeliveryUpdated(orderRepository.save(o));
        return true;
    }

    /** 배송상태가 status이고 기준일 칸(dateField)의 날짜가 N일 전 날짜까지인 미취소 주문 = AS-IS {@code LEFT(날짜,8) <= (오늘-N일)}. */
    private java.util.List<com.ghlove.order.domain.Order> byDeliveryStatusBefore(java.util.List<String> statuses, String dateField, int days) {
        String column = "shippedDate".equals(dateField) ? "shipped_date" : "delivered_date";
        @SuppressWarnings("unchecked")
        java.util.List<String> ids = entityManager.createNativeQuery("select order_id from ord.od_order where delivery_status in (:statuses) "
                        + "and coalesce(order_status, '') <> 'CANCELLED' and " + column + " < :until")
                .setParameter("statuses", statuses)
                .setParameter("until", LocalDate.now().minusDays(days - 1L).atStartOfDay())
                .getResultList();
        return orderRepository.findAllById(ids);
    }

    /**
     * autoConfirmPurchaseBatch(구매 확정 처리) - AS-IS {@code JobServiceImpl.autoConfirmPurchaseBatch}: 구매확정 기준일
     * (CONFIRM_PURCHASE_DATE)만큼 지난 배송완료(AS-IS '35'·교환배송완료 '58'·교환거절 '59'·반품거절 '69') 건을 구매확정('40')으로.
     * AS-IS 조건 {@code LEFT(SHIPPING_FINISH_DATE,8) <= (오늘-N일)}. TO-BE는 주문 배송상태 DELIVERED→CONFIRMED
     * (회원 수취확인과 같은 전이·이벤트). 거절된 교환/반품은 TO-BE에서 배송상태가 DELIVERED로 남아 있어 같은 조건에 든다.
     * AS-IS가 같이 하던 정산예정일 갱신·구매 포인트 적립(SalesOn 포인트)은 TO-BE에 대응 칸·기능이 없다.
     */
    @Transactional
    public String autoConfirmPurchase(int days) {
        java.util.List<com.ghlove.order.domain.Order> orders = byDeliveryStatusBefore(java.util.List.of("DELIVERED"), "deliveredDate", days);
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        for (com.ghlove.order.domain.Order o : orders) {
            o.setDeliveryStatus("CONFIRMED");
            o.setConfirmedDate(now);
            o.setUpdatedDate(now);
            orderSagaPublisher.publishDeliveryUpdated(orderRepository.save(o));
        }
        return "구매확정 " + orders.size() + "건";
    }

    /**
     * deleteCartBatch(장바구니 30일 지난 데이터 삭제) - AS-IS cart-mapper {@code deleteCartBatch}:
     * {@code CREATED_DATE <= (오늘-30일)yyyyMMdd||'999999'} = 30일 전 날짜까지 담긴 것. TO-BE 장바구니는 {@code od_cart_item}
     * (created_date가 timestamp)이라 "29일 전 0시 미만"으로 같은 범위를 잡는다.
     */
    @Transactional
    public String deleteOldCartItems() {
        int deleted = entityManager.createNativeQuery("delete from ord.od_cart_item where created_date < :until")
                .setParameter("until", LocalDate.now().minusDays(29).atStartOfDay())
                .executeUpdate();
        return "삭제 " + deleted + "건";
    }
}
