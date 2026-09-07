package com.ghlove.order.service;

import com.ghlove.order.domain.Order;
import com.ghlove.order.event.OrderSagaPublisher;
import com.ghlove.order.event.PointDeductFailedEvent;
import com.ghlove.order.event.PointDeductedEvent;
import com.ghlove.order.event.StockReserveFailedEvent;
import com.ghlove.order.event.StockReservedEvent;
import com.ghlove.order.repository.CommonCodeRepository;
import com.ghlove.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_CONFIRMED = "CONFIRMED";
    private static final String STATUS_CANCELLED = "CANCELLED";
    private static final String OUTCOME_RESERVED = "RESERVED";
    private static final String OUTCOME_DEDUCTED = "DEDUCTED";
    private static final String OUTCOME_FAILED = "FAILED";
    private static final String DELIVERY_SHIPPED = "SHIPPED";
    private static final String DELIVERY_IN_TRANSIT = "IN_TRANSIT";
    private static final String DELIVERY_DELIVERED = "DELIVERED";
    private static final String DELIVERY_CONFIRMED = "CONFIRMED";
    private static final DateTimeFormatter ID_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final SecureRandom RANDOM = new SecureRandom();

    private final OrderRepository orderRepository;
    private final CommonCodeRepository commonCodeRepository;
    private final OrderSagaPublisher orderSagaPublisher;
    private final GiftClient giftClient;
    private final CouponService couponService;

    public Map<String, String> codesOf(String codeType) {
        return commonCodeRepository.findByCodeTypeAndLanguageAndUseYnOrderByOrdering(codeType, "ko", "Y").stream()
                .collect(Collectors.toMap(
                        com.ghlove.order.domain.CommonCode::getId,
                        com.ghlove.order.domain.CommonCode::getLabel,
                        (a, b) -> a, java.util.LinkedHashMap::new));
    }

    public List<Order> myOrders(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedDateDesc(userId);
    }

    public Order detail(String orderId) {
        return orderRepository.findById(orderId).orElseThrow(() -> new OrderException("주문을 찾을 수 없습니다."));
    }

    /** 주문완료(AS-IS order/step2.html) 화면용 - 방금 결제 완료한 주문 여러 건을 한 번에 조회. */
    public List<Order> ordersOf(List<String> orderIds) {
        return orderRepository.findByOrderIdIn(orderIds);
    }

    /**
     * 주문 생성 (PENDING). 재고차감/포인트차감은 여기서 하지 않고 ORDER_CREATED
     * 이벤트로만 트리거한다 - 실제 처리는 gift/point 서비스가 각자 비동기로 수행
     * (choreography SAGA). 가격 조회는 SAGA의 일부가 아닌 단순 참조용 동기 호출.
     */
    @Transactional
    public Order createOrder(Long userId, Long itemId, Integer quantity) {
        return createOrder(userId, itemId, quantity, null, null);
    }

    /**
     * 주문결제(AS-IS order/step1.html) 화면에서 받는사람 정보를 입력받아 주문에 같이
     * 저장한다. 진짜 PG 결제 연동은 없어서(포인트 차감이 곧 "결제") 이 정보는 참조용이다.
     */
    @Transactional
    public Order createOrder(Long userId, Long itemId, Integer quantity, DeliveryInfo deliveryInfo) {
        return createOrder(userId, itemId, quantity, deliveryInfo, null);
    }

    /** couponIssueId가 있으면 CouponService가 이 상품라인 총액을 기준으로 할인을 확정하고
     *  쿠폰을 사용처리한다 - POINT_AMOUNT는 할인 반영된 실차감액으로 저장된다. */
    @Transactional
    public Order createOrder(Long userId, Long itemId, Integer quantity, DeliveryInfo deliveryInfo, Integer couponIssueId) {
        if (userId == null || userId <= 0) {
            throw new OrderException("회원 ID를 입력해 주세요.");
        }
        if (quantity == null || quantity <= 0) {
            throw new OrderException("수량은 1개 이상이어야 합니다.");
        }

        GiftItemInfo gift = giftClient.fetch(itemId);
        if (!"APPROVED".equals(gift.dataStatusCode())) {
            throw new OrderException("현재 주문할 수 없는 답례품입니다.");
        }
        if (gift.stockQuantity() == null || gift.stockQuantity() < quantity) {
            throw new OrderException("재고가 부족합니다. (현재 재고: " + gift.stockQuantity() + ")");
        }

        Order order = new Order();
        order.setOrderId(generateOrderId());
        order.setUserId(userId);
        order.setItemId(itemId);
        order.setSellerId(gift.sellerId());
        order.setItemName(gift.itemName());
        order.setQuantity(quantity);
        order.setUnitPrice(gift.salePrice());
        long lineTotal = (long) gift.salePrice() * quantity;

        long discount = 0;
        if (couponIssueId != null) {
            discount = couponService.applyToOrder(userId, couponIssueId, order.getOrderId(), itemId, lineTotal, quantity);
        }
        long deliveryFee = calculateDeliveryFee(gift, quantity, lineTotal,
                deliveryInfo != null ? deliveryInfo.address() : null);
        order.setDiscountAmount(discount);
        order.setCouponIssueId(couponIssueId);
        order.setDeliveryFee(deliveryFee);
        order.setPointAmount(lineTotal - discount + deliveryFee);
        order.setLocgovCode(gift.locgovCode());
        order.setOrderStatus(STATUS_PENDING);
        order.setCreatedDate(LocalDateTime.now());
        order.setUpdatedDate(LocalDateTime.now());
        if (deliveryInfo != null) {
            order.setReceiverName(deliveryInfo.receiverName());
            order.setReceiverPhone(deliveryInfo.receiverPhone());
            order.setDeliveryAddress(deliveryInfo.address());
            order.setDeliveryAddressDetail(deliveryInfo.addressDetail());
            order.setRequestNote(deliveryInfo.requestNote());
        }
        Order saved = orderRepository.save(order);

        orderSagaPublisher.publishCreated(saved);
        return saved;
    }

    public record DeliveryInfo(String receiverName, String receiverPhone, String address,
                                String addressDetail, String requestNote) {
    }

    /**
     * SFR-005 "배송비·택배사 설정, 배송정책" (재검토 라운드) - gift가 답례품별로 관리하는
     * 배송정책(GIFT_SHIPPING_TYPE 1~6)을 읽어 주문 시점 배송비를 계산한다. 출고지/반송지/
     * 묶음배송 등 AS-IS의 세부 정책까지는 재현하지 않고, order가 실제로 참조할 수 있는
     * 핵심(무료/조건부무료/개당/고정 + 제주·도서산간 추가배송비)만 다룬다.
     */
    private long calculateDeliveryFee(GiftItemInfo gift, int quantity, long lineTotal, String address) {
        String shippingType = gift.shippingType();
        long base = gift.shipping() != null ? gift.shipping() : 0;
        long fee;
        if (shippingType == null || "1".equals(shippingType)) {
            fee = 0;
        } else if ("5".equals(shippingType)) {
            fee = base * quantity;
        } else if ("2".equals(shippingType) || "3".equals(shippingType) || "4".equals(shippingType)) {
            Integer freeAmount = gift.shippingFreeAmount();
            fee = (freeAmount != null && lineTotal >= freeAmount) ? 0 : base;
        } else {
            fee = base;
        }

        if (address != null) {
            if (address.contains("제주") && gift.shippingExtraCharge1() != null) {
                fee += gift.shippingExtraCharge1();
            } else if (isRemoteIsland(address) && gift.shippingExtraCharge2() != null) {
                fee += gift.shippingExtraCharge2();
            }
        }
        return fee;
    }

    /** 도서산간 간이 판정 - 전국 완전한 도서지역 목록이 이 프로젝트 어디에도 없어(우편번호
     *  기반 판정표 부재), 주소 문자열에 흔한 도서지역명이 포함되는지만 본다(제주 전용
     *  {@link #calculateDeliveryFee}의 별도 분기와 동일한 느슨한 문자열 매칭 - donation의
     *  residenceLocgovOf()와 같은 관행). */
    private boolean isRemoteIsland(String address) {
        return address.contains("울릉") || address.contains("백령") || address.contains("연평")
                || address.contains("흑산") || address.contains("추자");
    }

    @Transactional
    public void onStockReserved(StockReservedEvent event) {
        Order order = pendingOrderOrNull(event.orderId());
        if (order == null || order.getStockOutcome() != null) {
            return;
        }
        order.setStockOutcome(OUTCOME_RESERVED);
        resolveIfReady(order);
    }

    @Transactional
    public void onStockReserveFailed(StockReserveFailedEvent event) {
        Order order = pendingOrderOrNull(event.orderId());
        if (order == null || order.getStockOutcome() != null) {
            return;
        }
        order.setStockOutcome(OUTCOME_FAILED);
        appendCancelReason(order, "재고 부족: " + event.reason());
        resolveIfReady(order);
    }

    @Transactional
    public void onPointDeducted(PointDeductedEvent event) {
        Order order = pendingOrderOrNull(event.orderId());
        if (order == null || order.getPointOutcome() != null) {
            return;
        }
        order.setPointOutcome(OUTCOME_DEDUCTED);
        resolveIfReady(order);
    }

    @Transactional
    public void onPointDeductFailed(PointDeductFailedEvent event) {
        Order order = pendingOrderOrNull(event.orderId());
        if (order == null || order.getPointOutcome() != null) {
            return;
        }
        order.setPointOutcome(OUTCOME_FAILED);
        appendCancelReason(order, "포인트 부족: " + event.reason());
        resolveIfReady(order);
    }

    /** 사용자 요청에 의한 확정 주문 취소 - gift/point가 order.saga의 ORDER_CANCELLED를 구독해 각자 보상 처리한다. */
    @Transactional
    public Order cancel(String orderId) {
        Order order = detail(orderId);
        if (!STATUS_CONFIRMED.equals(order.getOrderStatus())) {
            throw new OrderException("확정된 주문만 취소할 수 있습니다.");
        }
        order.setOrderStatus(STATUS_CANCELLED);
        order.setCancelReason("고객 요청 취소");
        order.setUpdatedDate(LocalDateTime.now());
        Order saved = orderRepository.save(order);
        orderSagaPublisher.publishCancelled(saved);
        return saved;
    }

    /** 송장 등록 (발송 처리) - 확정된 주문에 대해서만, 1회만 가능. */
    @Transactional
    public Order registerInvoice(String orderId, String carrierCode, String invoiceNo) {
        Order order = detail(orderId);
        if (!STATUS_CONFIRMED.equals(order.getOrderStatus())) {
            throw new OrderException("주문이 확정된 건만 송장을 등록할 수 있습니다.");
        }
        if (order.getDeliveryStatus() != null) {
            throw new OrderException("이미 송장이 등록된 주문입니다.");
        }
        if (carrierCode == null || carrierCode.isBlank() || invoiceNo == null || invoiceNo.isBlank()) {
            throw new OrderException("택배사와 송장번호를 입력해 주세요.");
        }
        order.setCarrierCode(carrierCode);
        order.setInvoiceNo(invoiceNo);
        order.setDeliveryStatus(DELIVERY_SHIPPED);
        order.setShippedDate(LocalDateTime.now());
        order.setUpdatedDate(LocalDateTime.now());
        Order saved = orderRepository.save(order);
        orderSagaPublisher.publishDeliveryUpdated(saved);
        return saved;
    }

    /**
     * 배송상태 갱신 (배송중/배송완료) - 실제 택배사 조회는 admin의 SmartDeliveryClient가
     * 담당하고(역할분리), 그 결과를 운영자가 여기에 반영하는 구조. 구매확정 이후에는
     * 더 이상 변경할 수 없다.
     */
    @Transactional
    public Order updateDeliveryStatus(String orderId, String deliveryStatus) {
        Order order = detail(orderId);
        if (order.getDeliveryStatus() == null) {
            throw new OrderException("아직 송장이 등록되지 않은 주문입니다.");
        }
        if (DELIVERY_CONFIRMED.equals(order.getDeliveryStatus())) {
            throw new OrderException("이미 구매확정된 주문입니다.");
        }
        if (!DELIVERY_IN_TRANSIT.equals(deliveryStatus) && !DELIVERY_DELIVERED.equals(deliveryStatus)) {
            throw new OrderException("올바른 배송상태가 아닙니다.");
        }
        order.setDeliveryStatus(deliveryStatus);
        if (DELIVERY_DELIVERED.equals(deliveryStatus)) {
            order.setDeliveredDate(LocalDateTime.now());
        }
        order.setUpdatedDate(LocalDateTime.now());
        Order saved = orderRepository.save(order);
        orderSagaPublisher.publishDeliveryUpdated(saved);
        return saved;
    }

    /** 수취확인(구매확정) - 배송완료 상태에서 구매자 본인만 가능. */
    @Transactional
    public Order confirmReceipt(String orderId, Long userId) {
        Order order = detail(orderId);
        if (!order.getUserId().equals(userId)) {
            throw new OrderException("본인 주문만 구매확정할 수 있습니다.");
        }
        if (!DELIVERY_DELIVERED.equals(order.getDeliveryStatus())) {
            throw new OrderException("배송완료 상태인 주문만 구매확정할 수 있습니다.");
        }
        order.setDeliveryStatus(DELIVERY_CONFIRMED);
        order.setConfirmedDate(LocalDateTime.now());
        order.setUpdatedDate(LocalDateTime.now());
        Order saved = orderRepository.save(order);
        orderSagaPublisher.publishDeliveryUpdated(saved);
        return saved;
    }

    /** 배송지 변경 - 아직 발송 전(송장 미등록)인 주문만, 구매자 본인만 가능. */
    @Transactional
    public Order changeDeliveryAddress(String orderId, Long userId, String address, String addressDetail) {
        Order order = detail(orderId);
        if (!order.getUserId().equals(userId)) {
            throw new OrderException("본인 주문만 배송지를 변경할 수 있습니다.");
        }
        if (order.getDeliveryStatus() != null) {
            throw new OrderException("이미 발송된 주문은 배송지를 변경할 수 없습니다.");
        }
        if (address == null || address.isBlank()) {
            throw new OrderException("배송지 주소를 입력해 주세요.");
        }
        order.setDeliveryAddress(address);
        order.setDeliveryAddressDetail(addressDetail);
        order.setUpdatedDate(LocalDateTime.now());
        return orderRepository.save(order);
    }

    private Order pendingOrderOrNull(String orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            log.warn("Received order.saga event for unknown orderId={}", orderId);
            return null;
        }
        if (!STATUS_PENDING.equals(order.getOrderStatus())) {
            log.info("Order {} already resolved ({}) - ignoring late/duplicate event", orderId, order.getOrderStatus());
            return null;
        }
        return order;
    }

    private void resolveIfReady(Order order) {
        if (order.getStockOutcome() == null || order.getPointOutcome() == null) {
            orderRepository.save(order);
            return;
        }

        order.setUpdatedDate(LocalDateTime.now());
        if (OUTCOME_RESERVED.equals(order.getStockOutcome()) && OUTCOME_DEDUCTED.equals(order.getPointOutcome())) {
            order.setOrderStatus(STATUS_CONFIRMED);
            Order saved = orderRepository.save(order);
            orderSagaPublisher.publishConfirmed(saved);
            issuePurchaseTriggeredCoupons(saved);
        } else {
            order.setOrderStatus(STATUS_CANCELLED);
            Order saved = orderRepository.save(order);
            orderSagaPublisher.publishCancelled(saved);
        }
    }

    /** 쿠폰 발행시점(4:상품구매후발행, 5:첫구매) 자동발급 - 이 주문 자신이 확정되는 순간 트리거한다. */
    private void issuePurchaseTriggeredCoupons(Order order) {
        couponService.issueAfterItemPurchase(order.getUserId(), order.getItemId());
        boolean firstConfirmed = orderRepository.findByUserIdOrderByCreatedDateDesc(order.getUserId()).stream()
                .filter(o -> STATUS_CONFIRMED.equals(o.getOrderStatus()))
                .count() <= 1;
        if (firstConfirmed) {
            couponService.issueFirstPurchaseCoupons(order.getUserId());
        }
    }

    private void appendCancelReason(Order order, String reason) {
        order.setCancelReason(order.getCancelReason() == null ? reason : order.getCancelReason() + "; " + reason);
    }

    private String generateOrderId() {
        int suffix = RANDOM.nextInt(9000) + 1000;
        return "O" + ID_FORMAT.format(LocalDateTime.now()) + suffix;
    }
}
