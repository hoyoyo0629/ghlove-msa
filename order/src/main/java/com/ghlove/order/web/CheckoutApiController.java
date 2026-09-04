package com.ghlove.order.web;

import com.ghlove.order.domain.CouponIssue;
import com.ghlove.order.domain.Order;
import com.ghlove.order.service.CartGroup;
import com.ghlove.order.service.CartService;
import com.ghlove.order.service.CouponService;
import com.ghlove.order.service.JwtVerifier;
import com.ghlove.order.service.LocgovClient;
import com.ghlove.order.service.OrderException;
import com.ghlove.order.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** storefront(Vue3 SPA)용 주문결제/주문완료 JSON API - {@link CheckoutController}(Thymeleaf)와
 *  완전히 같은 서비스 로직을 감싼다. */
@RestController
@RequiredArgsConstructor
public class CheckoutApiController {

    private final CartService cartService;
    private final OrderService orderService;
    private final LocgovClient locgovClient;
    private final JwtVerifier jwtVerifier;
    private final CouponService couponService;

    private Long requireUser(HttpServletRequest request) {
        return jwtVerifier.currentUserId(request).orElse(null);
    }

    public record ReviewRequest(List<Long> cartItemId) {
    }

    public record CouponOptionDto(Integer couponUserId, String couponName, String payType, Integer pay) {
        static CouponOptionDto of(CouponIssue c) {
            return new CouponOptionDto(c.getCouponUserId(), c.getCouponName(), c.getPayType(), c.getPay());
        }
    }

    public record ReviewResponse(List<CartGroup> groups, long totalPoint,
                                  Map<Long, List<CouponOptionDto>> couponsByCartItem) {
    }

    @PostMapping("/api/checkout/review")
    public ResponseEntity<?> review(@RequestBody ReviewRequest req, HttpServletRequest request) {
        Long userId = requireUser(request);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        var groups = cartService.viewSelected(userId, req.cartItemId());
        if (groups.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "답례품을 선택해 주세요."));
        }
        long totalPoint = groups.stream().mapToLong(CartGroup::groupTotal).sum();

        Map<Long, List<CouponOptionDto>> couponsByCartItem = new LinkedHashMap<>();
        for (var group : groups) {
            for (var line : group.lines()) {
                couponsByCartItem.put(line.cartItemId(),
                        couponService.usableIssuesForItem(userId, line.itemId()).stream().map(CouponOptionDto::of).toList());
            }
        }
        return ResponseEntity.ok(new ReviewResponse(groups, totalPoint, couponsByCartItem));
    }

    public record CompleteRequest(List<Long> cartItemId, String receiverName, String receiverPhone,
                                   String deliveryAddress, String deliveryAddressDetail, String requestNote,
                                   Map<Long, Integer> couponByCartItem) {
    }

    public record CompleteResponse(List<String> orderIds) {
    }

    @PostMapping("/api/checkout/complete")
    public ResponseEntity<?> complete(@RequestBody CompleteRequest req, HttpServletRequest request) {
        Long userId = requireUser(request);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        var deliveryInfo = new OrderService.DeliveryInfo(req.receiverName(), req.receiverPhone(),
                req.deliveryAddress(), req.deliveryAddressDetail(), req.requestNote());
        try {
            List<String> orderIds = cartService.checkout(userId, req.cartItemId(), deliveryInfo, req.couponByCartItem());
            return ResponseEntity.ok(new CompleteResponse(orderIds));
        } catch (OrderException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    public record OrderGroupDto(String locgovNm, List<Order> orders, long groupTotal) {
    }

    public record DoneResponse(List<OrderGroupDto> groups, long totalPoint) {
    }

    @GetMapping("/api/checkout/done")
    public ResponseEntity<?> done(@RequestParam String orderIds, HttpServletRequest request) {
        Long userId = requireUser(request);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        List<String> ids = List.of(orderIds.split(","));
        var orders = orderService.ordersOf(ids).stream()
                .filter(o -> userId.equals(o.getUserId()))
                .toList();
        if (orders.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "주문을 찾을 수 없습니다."));
        }

        Map<String, List<Order>> byLocgov = new LinkedHashMap<>();
        for (var order : orders) {
            String code = order.getLocgovCode() != null ? order.getLocgovCode() : "";
            byLocgov.computeIfAbsent(code, k -> new java.util.ArrayList<>()).add(order);
        }
        List<OrderGroupDto> groups = byLocgov.entrySet().stream()
                .map(e -> new OrderGroupDto(
                        e.getKey().isEmpty() ? "지자체 미지정" : locgovClient.nameOf(e.getKey()),
                        e.getValue(),
                        e.getValue().stream().mapToLong(Order::getPointAmount).sum()))
                .toList();

        return ResponseEntity.ok(new DoneResponse(groups, orders.stream().mapToLong(Order::getPointAmount).sum()));
    }
}
