package com.ghlove.order.web;

import com.ghlove.order.service.OrderBatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * admin 배치 autoSmartDeliveryShippingCompleteBatch가 부르는 내부 API(/api/admin/orders/** = 내부 시크릿 보호).
 * 스마트택배 연계·코드표는 admin 소유라 admin이 조회하고, 주문 상태 변경만 여기서 한다.
 */
@RestController
@RequiredArgsConstructor
public class OrderBatchApiController {

    private final OrderBatchService orderBatchService;

    @GetMapping("/api/admin/orders/batch/smart-delivery-targets")
    public List<OrderBatchService.SmartDeliveryTarget> smartDeliveryTargets(@RequestParam int days) {
        return orderBatchService.smartDeliveryTargets(days);
    }

    /** admin 배치 sendNuri2PresentOrder - 판매자별 결제완료 답례품 건수. */
    @GetMapping("/api/admin/orders/batch/present-order-counts")
    public List<OrderBatchService.SellerOrderCount> presentOrderCounts() {
        return orderBatchService.presentOrderCountsBySeller();
    }

    @PostMapping("/api/admin/orders/batch/{orderId}/delivered")
    public boolean delivered(@PathVariable String orderId) {
        return orderBatchService.markDeliveredBySmartDelivery(orderId);
    }
}
