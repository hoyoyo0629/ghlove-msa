package com.ghlove.order.web;

import com.ghlove.order.domain.Order;
import com.ghlove.order.domain.OrderItem;
import com.ghlove.order.domain.Shipment;
import com.ghlove.order.repository.OrderItemRepository;
import com.ghlove.order.repository.ShipmentRepository;
import com.ghlove.order.service.ClaimException;
import com.ghlove.order.service.ClaimService;
import com.ghlove.order.service.JwtVerifier;
import com.ghlove.order.service.LocgovClient;
import com.ghlove.order.service.OrderException;
import com.ghlove.order.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
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
    private final LocgovClient locgovClient;
    private final ShipmentRepository shipmentRepository;
    private final OrderItemRepository orderItemRepository;

    private static final String STATUS_CANCELLED = "CANCELLED";
    private static final String STATUS_PARTIAL = "PARTIALLY_CONFIRMED";

    private Long requireUser(HttpServletRequest request) {
        return jwtVerifier.currentUserId(request).orElse(null);
    }

    /** 주문목록 한 행 - 멀티아이템이라 대표품목명 + "외 N건"과 총 수량/포인트로 요약한다(AS-IS 동일). */
    public record OrderRowDto(String orderId, Long itemId, String itemName, Integer quantity, Long pointAmount,
                               String orderStatus, String orderStatusLabel, java.time.LocalDateTime createdDate,
                               int itemCount) {
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
                .map(o -> toOrderRow(o, labels))
                .filter(r -> itemName == null || itemName.isBlank()
                        || (r.itemName() != null && r.itemName().contains(itemName)))
                .toList();
        return ResponseEntity.ok(rows);
    }

    private OrderRowDto toOrderRow(Order o, Map<String, String> labels) {
        List<OrderItem> items = orderItemRepository.findByOrderId(o.getOrderId());
        String label = statusLabel(o.getOrderStatus(), labels);
        if (items.isEmpty()) {
            // 레거시 단일품목 주문(백필 전) - 헤더 컬럼으로 표시
            return new OrderRowDto(o.getOrderId(), o.getItemId(), o.getItemName(), o.getQuantity(),
                    o.getPointAmount(), o.getOrderStatus(), label, o.getCreatedDate(), o.getItemName() != null ? 1 : 0);
        }
        OrderItem first = items.get(0);
        String repName = items.size() == 1 ? first.getItemName()
                : first.getItemName() + " 외 " + (items.size() - 1) + "건";
        int totalQty = items.stream().mapToInt(i -> i.getQuantity() != null ? i.getQuantity() : 0).sum();
        return new OrderRowDto(o.getOrderId(), first.getItemId(), repName, totalQty, o.getPointAmount(),
                o.getOrderStatus(), label, o.getCreatedDate(), items.size());
    }

    /** ORDER_STATUS 공통코드에 없는 신 모델 상태(PARTIALLY_CONFIRMED)는 친절한 한글로 보정. */
    private String statusLabel(String status, Map<String, String> labels) {
        if (STATUS_PARTIAL.equals(status)) {
            return labels.getOrDefault(status, "부분확정");
        }
        return status == null ? null : labels.getOrDefault(status, status);
    }

    public record CodeOption(String key, String label) {
    }

    /** 품목(OrderItem) 한 줄 - AS-IS orderDetail의 답례품 카드 1개. subtotal은 배송비 제외 상품소계. */
    public record OrderItemDto(Long orderItemId, Long itemId, String itemName, String optionName, Integer optionPrice,
                                String textOption, Integer quantity, Integer unitPrice, long subtotal, Long pointAmount,
                                String itemStatus, String itemStatusLabel, String cancelReason) {
    }

    /** 출고(Shipment) = 지자체 그룹. AS-IS orderDetail의 지자체 단위 답례품 목록 + 배송/주문상태. */
    public record ShipmentDto(Long shipmentId, String locgovCode, String locgovName,
                               String shipmentStatus, String shipmentStatusLabel, Long deliveryFee,
                               String deliveryStatus, String deliveryStatusLabel, String carrierCode, String carrierLabel,
                               String invoiceNo, List<OrderItemDto> items) {
    }

    public record OrderDetailDto(String orderId, java.time.LocalDateTime createdDate,
                                  String orderStatus, String orderStatusLabel, String cancelReason,
                                  String receiverName, String receiverPhone, String deliveryAddress,
                                  String deliveryAddressDetail, String requestNote,
                                  List<ShipmentDto> shipments,
                                  long orderPoint, long deliveryFeeTotal, long cancelPoint, long totalPoint,
                                  List<CodeOption> claimTypes, List<CodeOption> carriers) {
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

        List<Shipment> shipments = shipmentRepository.findByOrderId(orderId);
        List<ShipmentDto> shipmentDtos;
        if (shipments.isEmpty()) {
            // 레거시 단일품목 주문(백필 전) - 헤더를 단일 출고+단일 품목으로 표현
            shipmentDtos = List.of(legacyShipment(order, statusLabels, deliveryStatusLabels, carrierLabels));
        } else {
            shipmentDtos = shipments.stream()
                    .sorted(Comparator.comparing(Shipment::getShipmentId))
                    .map(s -> toShipmentDto(s, statusLabels, deliveryStatusLabels, carrierLabels))
                    .toList();
        }

        // 결제정보: 주문포인트(상품소계 합) + 배송비 - 취소분 = 총 결제포인트
        long orderPoint = shipmentDtos.stream()
                .flatMap(s -> s.items().stream()).mapToLong(OrderItemDto::subtotal).sum();
        long deliveryFeeTotal = shipmentDtos.stream()
                .mapToLong(s -> s.deliveryFee() != null ? s.deliveryFee() : 0L).sum();
        long cancelPoint = shipmentDtos.stream().flatMap(s -> s.items().stream())
                .filter(i -> STATUS_CANCELLED.equals(i.itemStatus()))
                .mapToLong(i -> i.pointAmount() != null ? i.pointAmount() : 0L).sum();
        long totalPoint = orderPoint + deliveryFeeTotal - cancelPoint;

        return ResponseEntity.ok(new OrderDetailDto(order.getOrderId(), order.getCreatedDate(),
                order.getOrderStatus(), statusLabel(order.getOrderStatus(), statusLabels), order.getCancelReason(),
                order.getReceiverName(), order.getReceiverPhone(), order.getDeliveryAddress(),
                order.getDeliveryAddressDetail(), order.getRequestNote(),
                shipmentDtos, orderPoint, deliveryFeeTotal, cancelPoint, totalPoint, claimTypes, carriers));
    }

    private ShipmentDto toShipmentDto(Shipment s, Map<String, String> statusLabels,
                                       Map<String, String> deliveryStatusLabels, Map<String, String> carrierLabels) {
        String locgovName = (s.getLocgovCode() == null || s.getLocgovCode().isBlank())
                ? "지자체 미지정" : locgovClient.nameOf(s.getLocgovCode());
        List<OrderItemDto> items = orderItemRepository.findByShipmentId(s.getShipmentId()).stream()
                .sorted(Comparator.comparing(OrderItem::getOrderItemId))
                .map(i -> toItemDto(i, statusLabels))
                .toList();
        return new ShipmentDto(s.getShipmentId(), s.getLocgovCode(), locgovName,
                s.getShipmentStatus(), statusLabel(s.getShipmentStatus(), statusLabels),
                s.getDeliveryFee(), s.getDeliveryStatus(),
                s.getDeliveryStatus() == null ? null : deliveryStatusLabels.getOrDefault(s.getDeliveryStatus(), s.getDeliveryStatus()),
                s.getCarrierCode(),
                s.getCarrierCode() == null ? null : carrierLabels.getOrDefault(s.getCarrierCode(), s.getCarrierCode()),
                s.getInvoiceNo(), items);
    }

    private OrderItemDto toItemDto(OrderItem i, Map<String, String> statusLabels) {
        int unit = i.getUnitPrice() != null ? i.getUnitPrice() : 0;
        int opt = i.getOptionPrice() != null ? i.getOptionPrice() : 0;
        int qty = i.getQuantity() != null ? i.getQuantity() : 0;
        long discount = i.getDiscountAmount() != null ? i.getDiscountAmount() : 0L;
        long subtotal = (long) (unit + opt) * qty - discount;
        return new OrderItemDto(i.getOrderItemId(), i.getItemId(), i.getItemName(), i.getOptionName(), i.getOptionPrice(),
                i.getTextOption(), i.getQuantity(), i.getUnitPrice(), subtotal, i.getPointAmount(),
                i.getItemStatus(), statusLabel(i.getItemStatus(), statusLabels), i.getCancelReason());
    }

    /** 백필 전 레거시 단일품목 주문을 신 모델 화면 형태(출고1·품목1)로 어댑팅. */
    private ShipmentDto legacyShipment(Order o, Map<String, String> statusLabels,
                                        Map<String, String> deliveryStatusLabels, Map<String, String> carrierLabels) {
        int unit = o.getUnitPrice() != null ? o.getUnitPrice() : 0;
        int opt = o.getOptionPrice() != null ? o.getOptionPrice() : 0;
        int qty = o.getQuantity() != null ? o.getQuantity() : 0;
        long discount = o.getDiscountAmount() != null ? o.getDiscountAmount() : 0L;
        long subtotal = (long) (unit + opt) * qty - discount;
        OrderItemDto item = new OrderItemDto(null, o.getItemId(), o.getItemName(), o.getOptionName(), o.getOptionPrice(),
                null, o.getQuantity(), o.getUnitPrice(), subtotal, o.getPointAmount(),
                o.getOrderStatus(), statusLabel(o.getOrderStatus(), statusLabels), o.getCancelReason());
        String locgovName = (o.getLocgovCode() == null || o.getLocgovCode().isBlank())
                ? "지자체 미지정" : locgovClient.nameOf(o.getLocgovCode());
        return new ShipmentDto(null, o.getLocgovCode(), locgovName,
                o.getOrderStatus(), statusLabel(o.getOrderStatus(), statusLabels), o.getDeliveryFee(),
                o.getDeliveryStatus(),
                o.getDeliveryStatus() == null ? null : deliveryStatusLabels.getOrDefault(o.getDeliveryStatus(), o.getDeliveryStatus()),
                o.getCarrierCode(),
                o.getCarrierCode() == null ? null : carrierLabels.getOrDefault(o.getCarrierCode(), o.getCarrierCode()),
                o.getInvoiceNo(), List.of(item));
    }

    private List<CodeOption> toOptions(Map<String, String> labels) {
        return labels.entrySet().stream().map(e -> new CodeOption(e.getKey(), e.getValue())).toList();
    }

    /** 레거시 단일품목 주문(백필 전)의 주문 전체 취소. 신 모델은 품목 단위 취소를 쓴다. */
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

    /** 품목 단위 부분취소 (멀티아이템, 발송 전). */
    @PostMapping("/api/orders/{orderId}/items/{orderItemId}/cancel")
    public ResponseEntity<?> cancelItem(@PathVariable String orderId, @PathVariable Long orderItemId,
                                         HttpServletRequest request) {
        Long userId = requireUser(request);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            orderService.cancelItem(orderId, orderItemId, userId);
            return ResponseEntity.noContent().build();
        } catch (OrderException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /** 회원이 누르는 "배송완료" (AS-IS `POST /api/order/shpping-complete`). */
    @PostMapping("/api/orders/{orderId}/mark-delivered")
    public ResponseEntity<?> markDelivered(@PathVariable String orderId, HttpServletRequest request) {
        Long userId = requireUser(request);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            orderService.markDelivered(orderId, userId);
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

    /** 품목 단위 반품/교환 신청 (멀티아이템). */
    @PostMapping("/api/orders/{orderId}/items/{orderItemId}/claim")
    public ResponseEntity<?> requestClaim(@PathVariable String orderId, @PathVariable Long orderItemId,
                                           @RequestBody ClaimRequest req, HttpServletRequest request) {
        Long userId = requireUser(request);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        if (!ownsOrder(orderId, userId)) {
            return ResponseEntity.status(403).build();
        }
        try {
            claimService.request(orderId, orderItemId, req.claimType(), req.reason());
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
