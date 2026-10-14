package com.ghlove.order.web;

import com.ghlove.order.service.OffgiveOrderService;
import com.ghlove.order.service.OrderException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 오프라인기부접수(기탁서 등록) 대표답례품 주문 - AS-IS {@code OffgiveServiceImpl.insertOffgive}가
 * 답례품을 고르면 {@code OrderServiceImpl.saveOffGiveOrder()}로 <b>일반 주문 테이블</b>
 * (OP_ORDER/OP_ORDER_ITEM/OP_ORDER_SHIPPING_INFO)에 '입금대기'로 적재하던 자리다 - AS-IS 자신도
 * 오프라인 전용 주문 테이블을 쓰지 않는다. 포인트 검증·차감 없이 만들고, 기부가 수납완료되면
 * 결제처리한다({@link OffgiveOrderService}).
 *
 * <p>admin(donation 경유) 전용이라 {@link com.ghlove.order.config.AdminApiAuthInterceptor}(공유시크릿)로
 * 게이트된다(WebConfig에 경로 등록).
 */
@RestController
@RequiredArgsConstructor
public class OffgiveOrderAdminApiController {

    private final OffgiveOrderService offgiveOrderService;

    /** 기탁서 상세의 선택 답례품·배송지 - 주문이 없으면 204. */
    @GetMapping("/api/admin/offgive-orders/summary")
    public ResponseEntity<OffgiveOrderService.OffgiveOrderSummary> summary(@RequestParam String cntrSn) {
        OffgiveOrderService.OffgiveOrderSummary summary = offgiveOrderService.summaryOf(cntrSn);
        return summary == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(summary);
    }

    @PostMapping("/api/admin/offgive-orders")
    public ResponseEntity<?> create(@RequestBody OffgiveOrderService.OffgiveOrderRequest req) {
        try {
            String orderId = offgiveOrderService.create(req);
            return ResponseEntity.ok(Map.of("orderId", orderId));
        } catch (OrderException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
