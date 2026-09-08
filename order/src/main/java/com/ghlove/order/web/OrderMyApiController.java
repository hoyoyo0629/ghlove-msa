package com.ghlove.order.web;

import com.ghlove.order.domain.Order;
import com.ghlove.order.service.ClaimException;
import com.ghlove.order.service.ClaimService;
import com.ghlove.order.service.JwtVerifier;
import com.ghlove.order.service.OrderException;
import com.ghlove.order.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

/** storefront(Vue3 SPA)용 "주문조회"/"주문상세" JSON API - {@link OrderController}(Thymeleaf)와
 *  완전히 같은 {@link OrderService} 로직을 감싼다. */
@RestController
@RequiredArgsConstructor
public class OrderMyApiController {

    private final OrderService orderService;
    private final ClaimService claimService;
    private final JwtVerifier jwtVerifier;

    private Long requireUser(HttpServletRequest request) {
        return jwtVerifier.currentUserId(request).orElse(null);
    }

    public record OrderRowDto(String orderId, Long itemId, String itemName, Integer quantity, Long pointAmount,
                               String orderStatus, String orderStatusLabel, java.time.LocalDateTime createdDate) {
    }

    @GetMapping("/api/orders")
    public ResponseEntity<?> myOrders(@RequestParam(required = false) String searchStartDate,
                                       @RequestParam(required = false) String searchEndDate,
                                       @RequestParam(required = false) String itemName,
                                       HttpServletRequest request) {
        Long userId = requireUser(request);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        Map<String, String> labels = orderService.codesOf("ORDER_STATUS");
        LocalDate start = parseDate(searchStartDate);
        LocalDate end = parseDate(searchEndDate);
        var rows = orderService.myOrders(userId).stream()
                .filter(o -> start == null || !o.getCreatedDate().toLocalDate().isBefore(start))
                .filter(o -> end == null || !o.getCreatedDate().toLocalDate().isAfter(end))
                .filter(o -> itemName == null || itemName.isBlank()
                        || (o.getItemName() != null && o.getItemName().contains(itemName)))
                .map(o -> new OrderRowDto(o.getOrderId(), o.getItemId(), o.getItemName(), o.getQuantity(), o.getPointAmount(),
                        o.getOrderStatus(), labels.getOrDefault(o.getOrderStatus(), o.getOrderStatus()), o.getCreatedDate()))
                .toList();
        return ResponseEntity.ok(rows);
    }

    public record CodeOption(String key, String label) {
    }

    public record OrderDetailDto(String orderId, Long itemId, String itemName, Integer quantity, Integer unitPrice,
                                  Long pointAmount, Long deliveryFee, String orderStatus, String orderStatusLabel,
                                  String cancelReason, String deliveryStatus, String deliveryStatusLabel,
                                  String carrierCode, String carrierLabel, String invoiceNo, String deliveryAddress,
                                  String deliveryAddressDetail, List<CodeOption> claimTypes, List<CodeOption> carriers) {
    }

    @GetMapping("/api/orders/{orderId}")
    public ResponseEntity<?> detail(@PathVariable String orderId, HttpServletRequest request) {
        Long userId = requireUser(request);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        Order order;
        try {
            order = orderService.detail(orderId);
        } catch (OrderException e) {
            return ResponseEntity.status(404).body(Map.of("message", e.getMessage()));
        }
        if (!userId.equals(order.getUserId())) {
            return ResponseEntity.status(403).body(Map.of("message", "본인 주문만 조회할 수 있습니다."));
        }
        Map<String, String> statusLabels = orderService.codesOf("ORDER_STATUS");
        Map<String, String> deliveryStatusLabels = orderService.codesOf("DELIVERY_STATUS");
        Map<String, String> carrierLabels = orderService.codesOf("DELIVERY_CARRIER");
        List<CodeOption> claimTypes = toOptions(orderService.codesOf("CLAIM_TYPE"));
        List<CodeOption> carriers = toOptions(carrierLabels);

        return ResponseEntity.ok(new OrderDetailDto(order.getOrderId(), order.getItemId(), order.getItemName(), order.getQuantity(),
                order.getUnitPrice(), order.getPointAmount(), order.getDeliveryFee(), order.getOrderStatus(),
                statusLabels.getOrDefault(order.getOrderStatus(), order.getOrderStatus()), order.getCancelReason(),
                order.getDeliveryStatus(),
                order.getDeliveryStatus() == null ? null : deliveryStatusLabels.getOrDefault(order.getDeliveryStatus(), order.getDeliveryStatus()),
                order.getCarrierCode(),
                order.getCarrierCode() == null ? null : carrierLabels.getOrDefault(order.getCarrierCode(), order.getCarrierCode()),
                order.getInvoiceNo(), order.getDeliveryAddress(), order.getDeliveryAddressDetail(), claimTypes, carriers));
    }

    private List<CodeOption> toOptions(Map<String, String> labels) {
        return labels.entrySet().stream().map(e -> new CodeOption(e.getKey(), e.getValue())).toList();
    }

    @PostMapping("/api/orders/{orderId}/cancel")
    public ResponseEntity<?> cancel(@PathVariable String orderId, HttpServletRequest request) {
        Long userId = requireUser(request);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        if (!ownsOrder(orderId, userId)) {
            return ResponseEntity.status(403).build();
        }
        try {
            orderService.cancel(orderId);
            return ResponseEntity.noContent().build();
        } catch (OrderException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/api/orders/{orderId}/confirm-receipt")
    public ResponseEntity<?> confirmReceipt(@PathVariable String orderId, HttpServletRequest request) {
        Long userId = requireUser(request);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            orderService.confirmReceipt(orderId, userId);
            return ResponseEntity.noContent().build();
        } catch (OrderException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    public record DeliveryAddressRequest(String address, String addressDetail) {
    }

    @PostMapping("/api/orders/{orderId}/delivery-address")
    public ResponseEntity<?> changeDeliveryAddress(@PathVariable String orderId, @RequestBody DeliveryAddressRequest req,
                                                     HttpServletRequest request) {
        Long userId = requireUser(request);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            orderService.changeDeliveryAddress(orderId, userId, req.address(), req.addressDetail());
            return ResponseEntity.noContent().build();
        } catch (OrderException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // 송장번호 등록·배송상태 변경은 SFR-006상 판매자/운영관리 기능이다(택배사 연계는 운영관리
    // 담당). 예전에는 고객 API(OrderMyApiController)에 소유권 검사조차 없이 노출돼 있어 구매자(또는
    // 익명)가 자기 주문을 임의로 "배송완료"로 위조해 구매확정→정산까지 앞당길 수 있었다.
    // 이 두 기능은 admin 인증(X-Internal-Secret)이 걸린 OrderAdminApiController
    // (/api/admin/orders/{id}/invoice, /delivery-status)로만 수행하도록 여기서는 제거했다.

    public record ClaimRequest(String claimType, String reason) {
    }

    @PostMapping("/api/orders/{orderId}/claim")
    public ResponseEntity<?> requestClaim(@PathVariable String orderId, @RequestBody ClaimRequest req,
                                           HttpServletRequest request) {
        Long userId = requireUser(request);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        if (!ownsOrder(orderId, userId)) {
            return ResponseEntity.status(403).build();
        }
        try {
            claimService.request(orderId, req.claimType(), req.reason());
            return ResponseEntity.noContent().build();
        } catch (ClaimException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    private boolean ownsOrder(String orderId, Long userId) {
        try {
            return userId.equals(orderService.detail(orderId).getUserId());
        } catch (OrderException e) {
            return false;
        }
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
