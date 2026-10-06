package com.ghlove.order.web;

import com.ghlove.order.domain.CouponIssue;
import com.ghlove.order.domain.Order;
import com.ghlove.order.domain.OrderItem;
import com.ghlove.order.domain.Shipment;
import com.ghlove.order.repository.OrderItemRepository;
import com.ghlove.order.repository.ShipmentRepository;
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

import java.util.ArrayList;
import java.util.Comparator;
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
    private final ShipmentRepository shipmentRepository;
    private final OrderItemRepository orderItemRepository;

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
        // 배송비까지 더한 실제 결제 예정 포인트 (예전에는 답례품 포인트 합계만 내려주고
        // 화면은 "무료배송"으로 하드코딩해, 실제 차감액과 어긋났다).
        long totalPoint = groups.stream().mapToLong(CartGroup::groupPayable).sum();

        Map<Long, List<CouponOptionDto>> couponsByCartItem = new LinkedHashMap<>();
        for (var group : groups) {
            for (var line : group.lines()) {
                couponsByCartItem.put(line.cartItemId(),
                        couponService.usableIssuesForItem(userId, line.itemId()).stream().map(CouponOptionDto::of).toList());
            }
        }
        return ResponseEntity.ok(new ReviewResponse(groups, totalPoint, couponsByCartItem));
    }

    public record PreviewRequest(List<Long> cartItemId, Map<Long, Integer> couponByCartItem, String deliveryAddress,
                                  String zipcode) {
    }

    /**
     * 쿠폰 선택/배송지 입력이 바뀔 때마다 호출하는 금액 재계산 - 실제 결제(complete)와
     * 똑같은 CartService 계산을 쓰기 때문에, 화면에 뜬 결제 포인트가 곧 차감될 포인트다.
     * (쿠폰 할인식은 정액/정률·수량비례·최소주문금액·할인상한이 얽혀 있어 화면에서 다시
     * 구현하면 반드시 어긋난다 - CouponDiscountCalculator를 서버에서 그대로 태운다.)
     */
    @PostMapping("/api/checkout/preview")
    public ResponseEntity<?> preview(@RequestBody PreviewRequest req, HttpServletRequest request) {
        Long userId = requireUser(request);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            return ResponseEntity.ok(
                    cartService.quote(userId, req.cartItemId(), req.couponByCartItem(), req.zipcode()));
        } catch (OrderException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    public record CompleteRequest(List<Long> cartItemId, String receiverName, String receiverPhone,
                                   String deliveryAddress, String deliveryAddressDetail, String requestNote,
                                   Map<Long, Integer> couponByCartItem, String zipcode) {
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
                req.deliveryAddress(), req.deliveryAddressDetail(), req.requestNote(), req.zipcode());
        try {
            // 멀티아이템 재설계 flip: 한 번의 결제 = 주문 1건(주문번호 1개). 지자체별 출고·품목은
            // 하위에 묶인다. 레거시 cartService.checkout(행마다 주문 N건)은 롤백용으로 남겨둔다.
            String orderId = cartService.checkoutMultiItem(userId, req.cartItemId(), deliveryInfo, req.couponByCartItem());
            return ResponseEntity.ok(new CompleteResponse(List.of(orderId)));
        } catch (OrderException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    public record DoneItemDto(String itemName, String optionName, String textOption, Integer quantity, long pointAmount) {
    }

    public record OrderGroupDto(String locgovNm, List<DoneItemDto> items, long groupTotal, long deliveryFee) {
    }

    public record DoneResponse(String orderId, java.time.LocalDateTime createdDate,
                                String receiverName, String receiverPhone, String deliveryAddress,
                                String deliveryAddressDetail, String requestNote,
                                List<OrderGroupDto> groups, long totalPoint) {
    }

    /**
     * 주문완료(AS-IS order/step2.html) - 방금 결제한 주문을 지자체(출고) 그룹 + 품목 목록으로
     * 보여준다. 멀티아이템 재설계 후에는 orderIds가 보통 단일 주문번호 1건이고, 그 안의
     * 지자체별 출고가 그룹이 된다. 레거시 단일품목 주문(백필 전, 출고행 없음)도 헤더로 표현한다.
     */
    @GetMapping("/api/checkout/done")
    public ResponseEntity<?> done(@RequestParam String orderIds, HttpServletRequest request) {
        Long userId = requireUser(request);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        List<String> ids = List.of(orderIds.split(","));
        var orders = orderService.ordersOf(ids).stream()
                .filter(o -> userId.equals(o.getUserId()))
                .sorted(Comparator.comparing(Order::getCreatedDate))
                .toList();
        if (orders.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "주문을 찾을 수 없습니다."));
        }
        List<String> orderIdList = orders.stream().map(Order::getOrderId).toList();

        Map<String, List<DoneItemDto>> itemsByLocgov = new LinkedHashMap<>();
        Map<String, Long> feeByLocgov = new LinkedHashMap<>();

        List<Shipment> shipments = shipmentRepository.findByOrderIdIn(orderIdList).stream()
                .sorted(Comparator.comparing(Shipment::getShipmentId)).toList();
        if (!shipments.isEmpty()) {
            for (Shipment s : shipments) {
                String code = s.getLocgovCode() != null ? s.getLocgovCode() : "";
                List<OrderItem> items = orderItemRepository.findByShipmentId(s.getShipmentId());
                itemsByLocgov.computeIfAbsent(code, k -> new ArrayList<>()).addAll(items.stream()
                        .map(i -> new DoneItemDto(i.getItemName(), i.getOptionName(), i.getTextOption(), i.getQuantity(),
                                i.getPointAmount() != null ? i.getPointAmount() : 0L))
                        .toList());
                feeByLocgov.merge(code, s.getDeliveryFee() != null ? s.getDeliveryFee() : 0L, Long::sum);
            }
        } else {
            // 레거시 단일품목 주문(백필 전) - 각 주문을 그 지자체 그룹의 품목 1건으로
            for (Order o : orders) {
                String code = o.getLocgovCode() != null ? o.getLocgovCode() : "";
                itemsByLocgov.computeIfAbsent(code, k -> new ArrayList<>())
                        .add(new DoneItemDto(o.getItemName(), o.getOptionName(), null, o.getQuantity(),
                                o.getPointAmount() != null ? o.getPointAmount() : 0L));
                feeByLocgov.merge(code, o.getDeliveryFee() != null ? o.getDeliveryFee() : 0L, Long::sum);
            }
        }

        List<OrderGroupDto> groups = itemsByLocgov.entrySet().stream()
                .map(e -> new OrderGroupDto(
                        e.getKey().isEmpty() ? "지자체 미지정" : locgovClient.nameOf(e.getKey()),
                        e.getValue(),
                        e.getValue().stream().mapToLong(DoneItemDto::pointAmount).sum(),
                        feeByLocgov.getOrDefault(e.getKey(), 0L)))
                .toList();

        Order header = orders.get(0);
        long totalPoint = groups.stream().mapToLong(OrderGroupDto::groupTotal).sum();
        return ResponseEntity.ok(new DoneResponse(
                String.join(", ", orderIdList), header.getCreatedDate(),
                header.getReceiverName(), header.getReceiverPhone(), header.getDeliveryAddress(),
                header.getDeliveryAddressDetail(), header.getRequestNote(), groups, totalPoint));
    }
}
