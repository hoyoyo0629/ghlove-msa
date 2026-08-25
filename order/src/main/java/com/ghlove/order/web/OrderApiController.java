package com.ghlove.order.web;

import com.ghlove.order.domain.Order;
import com.ghlove.order.repository.ClaimRepository;
import com.ghlove.order.repository.OrderRepository;
import com.ghlove.order.service.CouponService;
import com.ghlove.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Read-only JSON API for other services (member 마이페이지 "주문조회"/"취소반품교환"/"쿠폰함") - not part of any write path. */
@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private static final String OUTCOME_DEDUCTED = "DEDUCTED";

    private final OrderService orderService;
    private final ClaimRepository claimRepository;
    private final OrderRepository orderRepository;
    private final CouponService couponService;

    @GetMapping("/api/my-summary")
    public OrderSummaryDto mySummary(@RequestParam Long userId) {
        List<Order> orders = orderService.myOrders(userId);
        List<String> orderIds = orders.stream().map(Order::getOrderId).toList();
        int claimCount = orderIds.isEmpty() ? 0 : claimRepository.findByOrderIdIn(orderIds).size();
        int usableCouponCount = couponService.myIssues(userId).usable().size();
        return new OrderSummaryDto(orders.size(), claimCount, usableCouponCount);
    }

    /** admin "주문금액-포인트사용 대사"(AS-IS view_rc_order_amt_vs_point_use_check 재구현)용 -
     *  포인트 차감이 성공(DEDUCTED)한 주문만 대사 대상이다(부분실패로 CANCELLED된 주문은
     *  애초에 포인트가 안 나갔으니 대사할 게 없음). */
    @GetMapping("/api/orders/point-deducted")
    public List<PointDeductedOrderDto> pointDeductedOrders() {
        return orderRepository.findByPointOutcomeOrderByCreatedDateDesc(OUTCOME_DEDUCTED).stream()
                .map(o -> new PointDeductedOrderDto(o.getOrderId(), o.getUserId(), o.getItemName(),
                        o.getPointAmount(), o.getOrderStatus(), o.getCreatedDate()))
                .toList();
    }

    public record PointDeductedOrderDto(String orderId, Long userId, String itemName, Long pointAmount,
                                         String orderStatus, java.time.LocalDateTime createdDate) {
    }

    /** admin StatsService ReadModel 재동기화용 (SFR-009 "원장 복구·재동기화 절차") - 전체 주문
     *  현재 상태 스냅샷. */
    @GetMapping("/api/admin/orders/all")
    public List<OrderSnapshotDto> allForResync() {
        return orderRepository.findAll().stream()
                .map(o -> new OrderSnapshotDto(o.getOrderId(), o.getUserId(), o.getItemId(), o.getSellerId(),
                        o.getQuantity(), o.getPointAmount(), o.getOrderStatus()))
                .toList();
    }

    public record OrderSnapshotDto(String orderId, Long userId, Long itemId, Long sellerId,
                                    Integer quantity, Long pointAmount, String orderStatus) {
    }
}
