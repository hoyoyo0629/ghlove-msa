package com.ghlove.admin.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <b>지금은 화면에 연결돼 있지 않다</b> - 메뉴 7210(포인트사용 정합성검증)은 AS-IS대로
 * 기부포인트 사용이력({@code G_CNTR_USE_POINT})과 대조하도록 {@link PointCheckService}로
 * 다시 만들었다(2026-10-03). 이 클래스는 포인트 <b>원장</b>({@code PT_POINT_LEDGER})과 대조하는
 * 다른 점검(아래 설명의 SAGA 드리프트 탐지)이라 지우지 않고 남겨 둔다 - 쓸 자리가 생기면
 * 그때 화면을 붙이면 된다. AS-IS에는 없는 점검이다.
 *
 * 주문금액-포인트사용 대사. AS-IS는
 * 결제수단이 여러 개(카드/현금영수증 등)라 "주문 실결제금액 vs 포인트사용액"이 갈릴 수 있었지만,
 * 이 프로젝트는 결제수단이 포인트 하나뿐이라 order.saga 선택전이(choreography)가 실제로 뭘
 * 차감했는지를 point 원장(PT_POINT_LEDGER)과 직접 대조하는 게 대사 대상이 된다 - 비동기
 * SAGA라 order.pointAmount(주문 시점에 기록된 요청액)와 point 서비스가 실제로 차감한 금액이
 * Kafka 재전달/버그로 어긋날 가능성을 잡아내는 무결성 점검.
 */
@Service
@RequiredArgsConstructor
public class ReconciliationService {

    private final OrderClient orderClient;
    private final PointClient pointClient;

    public List<Row> orderPointReconciliation() {
        List<OrderClient.PointDeductedOrder> orders = orderClient.pointDeductedOrders();
        List<String> orderIds = orders.stream().map(OrderClient.PointDeductedOrder::orderId).toList();
        Map<String, Long> usedByOrderId = pointClient.usedByOrderIds(orderIds);

        return orders.stream()
                .map(o -> {
                    long orderAmount = o.pointAmount() != null ? o.pointAmount() : 0L;
                    long usedAmount = usedByOrderId.getOrDefault(o.orderId(), 0L);
                    return new Row(o.orderId(), o.userId(), o.itemName(), orderAmount, usedAmount,
                            orderAmount == usedAmount, o.orderStatus(), o.createdDate());
                })
                .toList();
    }

    public record Row(String orderId, Long userId, String itemName, long orderPointAmount, long ledgerUsedAmount,
                       boolean matched, String orderStatus, LocalDateTime createdDate) {
    }
}
