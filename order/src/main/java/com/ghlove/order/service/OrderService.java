package com.ghlove.order.service;

import com.ghlove.order.domain.Order;
import com.ghlove.order.domain.OrderItem;
import com.ghlove.order.domain.Shipment;
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
    // 멀티아이템 - 품목 단위 부분취소
    private final com.ghlove.order.repository.OrderItemRepository orderItemRepository;
    private final com.ghlove.order.repository.ShipmentRepository shipmentRepository;
    private final ShipmentSagaService shipmentSagaService;
    // 배송비 G4 - 우편번호로 제주/도서산간 판정(AS-IS OP_ISLAND)
    private final com.ghlove.order.repository.IslandRepository islandRepository;

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
        return createOrder(userId, giftClient.fetch(itemId), quantity, deliveryInfo, couponIssueId);
    }

    /**
     * 답례품 정보를 이미 조회해 둔 호출자(장바구니 체크아웃)가 같은 상품을 두 번 조회하지
     * 않도록 재사용하는 오버로드 - 검증/계산 규칙은 위 오버로드와 완전히 동일하다.
     */
    public Order createOrder(Long userId, GiftItemInfo gift, Integer quantity, DeliveryInfo deliveryInfo,
                              Integer couponIssueId) {
        return createOrder(userId, gift, quantity, deliveryInfo, couponIssueId, null, 0);
    }

    /** 옵션 선택형 주문 - 옵션명 스냅샷 + 옵션 추가금액을 단가에 가산해 포인트를 계산한다(AS-IS 동일). */
    @Transactional
    public Order createOrder(Long userId, GiftItemInfo gift, Integer quantity, DeliveryInfo deliveryInfo,
                              Integer couponIssueId, String optionName, Integer optionPrice) {
        if (userId == null || userId <= 0) {
            throw new OrderException("회원 ID를 입력해 주세요.");
        }
        if (quantity == null || quantity <= 0) {
            throw new OrderException("수량은 1개 이상이어야 합니다.");
        }

        Long itemId = gift.itemId();
        if (!"APPROVED".equals(gift.dataStatusCode())) {
            throw new OrderException("현재 주문할 수 없는 답례품입니다.");
        }
        if (gift.stockQuantity() == null || gift.stockQuantity() < quantity) {
            throw new OrderException("재고가 부족합니다. (현재 재고: " + gift.stockQuantity() + ")");
        }

        int optPrice = optionPrice != null ? optionPrice : 0;
        Order order = new Order();
        order.setOrderId(generateOrderId());
        order.setUserId(userId);
        order.setItemId(itemId);
        order.setSellerId(gift.sellerId());
        order.setItemName(gift.itemName());
        order.setOptionName(optionName);
        order.setOptionPrice(optPrice);
        order.setQuantity(quantity);
        order.setUnitPrice(gift.salePrice());
        long lineTotal = (long) (gift.salePrice() + optPrice) * quantity;

        long discount = 0;
        if (couponIssueId != null) {
            discount = couponService.applyToOrder(userId, couponIssueId, order.getOrderId(), itemId, lineTotal, quantity);
        }
        long deliveryFee = deliveryFeeOf(gift, quantity, lineTotal,
                deliveryInfo != null ? deliveryInfo.zipcode() : null);
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
                                String addressDetail, String requestNote, String zipcode) {
    }

    /**
     * SFR-005 "배송비·택배사 설정, 배송정책" - 답례품별 배송정책(GIFT_SHIPPING_TYPE 1~6)을
     * 읽어 주문 시점 배송비를 계산한다. 계산 로직 전체(무료/판매자·출고지·상품 조건부/개당
     * BOX/고정 + 제주·도서산간 추가 + 묶음배송)는 AS-IS Shipping.getShippingGroups()를 옮긴
     * {@link DeliveryFeeCalculator}에 있다. 이 메서드는 <b>단일 라인</b> 배송비(레거시 단건주문
     * createOrder용)이며, 묶음배송 그룹 계산은 {@link DeliveryFeeCalculator#compute}를 라인 여러
     * 개로 호출하는 {@code CartService}에서 이뤄진다.
     *
     * @param zipcode 수취인 우편번호(제주/도서산간 판정용). 없으면 추가배송비 없음.
     */
    public long deliveryFeeOf(GiftItemInfo gift, int quantity, long lineTotal, String zipcode) {
        String islandType = islandTypeOf(zipcode);
        var line = new DeliveryFeeCalculator.Line(0L, gift.shippingType(),
                gift.shipping() != null ? gift.shipping() : 0, gift.shippingFreeAmount(),
                gift.shippingItemCount(), gift.shippingExtraCharge1(), gift.shippingExtraCharge2(),
                gift.shippingGroupCode(), gift.shipmentGroupCode(), null, quantity, lineTotal, 0L);
        return DeliveryFeeCalculator.compute(List.of(line), islandType).getOrDefault(0L, 0L);
    }

    /** 수취인 우편번호로 제주(JEJU)/도서산간(ISLAND)을 판정한다 - AS-IS
     *  OrderMapper.getIslandTypeByZipcode(OP_ISLAND 조회). 매칭 없으면 "". */
    public String islandTypeOf(String zipcode) {
        return islandRepository.islandTypeByZipcode(zipcode);
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
        // AS-IS는 "주문취소" 버튼을 주문상태 10(결제완료)·20(배송준비)에서만 보여준다
        // (mypage/orderList.html:287). 배송이 시작된 30(배송중)부터는 취소가 아니라
        // 반품/교환으로 처리해야 하므로 여기서도 발송 이후를 막는다 - 막지 않으면
        // 이미 나간 물건의 재고·포인트가 보상 트랜잭션으로 되돌아가 버린다.
        if (isShipped(order)) {
            throw new OrderException("이미 발송된 주문은 취소할 수 없습니다. 반품/교환을 신청해 주세요.");
        }
        order.setOrderStatus(STATUS_CANCELLED);
        order.setCancelReason("고객 요청 취소");
        order.setUpdatedDate(LocalDateTime.now());
        Order saved = orderRepository.save(order);
        orderSagaPublisher.publishCancelled(saved);
        return saved;
    }

    /**
     * 품목 단위 부분취소 (멀티아이템, 발송 전). 사용자가 주문상세에서 특정 답례품 한 줄만 취소한다.
     * 그 품목이 확정 상태이고 소속 출고가 아직 발송되지 않았을 때만 가능하며, 취소·보상·집계는
     * {@link ShipmentSagaService#cancelItem}이 처리한다(ITEM_CANCELLED로 재고·포인트 그 품목분만 복원).
     */
    @Transactional
    public void cancelItem(String orderId, Long orderItemId, Long userId) {
        Order order = detail(orderId);
        if (!order.getUserId().equals(userId)) {
            throw new OrderException("본인 주문만 취소할 수 있습니다.");
        }
        OrderItem item = orderItemRepository.findById(orderItemId)
                .filter(i -> orderId.equals(i.getOrderId()))
                .orElseThrow(() -> new OrderException("주문 품목을 찾을 수 없습니다."));
        if (!STATUS_CONFIRMED.equals(item.getItemStatus())) {
            throw new OrderException("확정된 품목만 취소할 수 있습니다.");
        }
        Shipment shipment = item.getShipmentId() != null
                ? shipmentRepository.findById(item.getShipmentId()).orElse(null) : null;
        if (shipment != null && isShipmentShipped(shipment.getDeliveryStatus())) {
            throw new OrderException("이미 발송된 품목은 취소할 수 없습니다. 반품/교환을 신청해 주세요.");
        }
        shipmentSagaService.cancelItem(orderItemId, "고객 요청 취소");
    }

    private static boolean isShipmentShipped(String d) {
        return DELIVERY_SHIPPED.equals(d) || DELIVERY_IN_TRANSIT.equals(d)
                || DELIVERY_DELIVERED.equals(d) || DELIVERY_CONFIRMED.equals(d);
    }

    /**
     * 이미 발송된 주문인가 - 배송상태가 SHIPPED 이후(운송중/배송완료/구매확정)면 true.
     * AS-IS 주문상태로는 30 이상에 해당한다.
     */
    public static boolean isShipped(Order order) {
        String d = order.getDeliveryStatus();
        return DELIVERY_SHIPPED.equals(d) || DELIVERY_IN_TRANSIT.equals(d)
                || DELIVERY_DELIVERED.equals(d) || DELIVERY_CONFIRMED.equals(d);
    }

    /** 배송완료 이후인가 - AS-IS 주문상태 35(배송완료)/58(교환배송완료) 이상. */
    public static boolean isDelivered(Order order) {
        String d = order.getDeliveryStatus();
        return DELIVERY_DELIVERED.equals(d) || DELIVERY_CONFIRMED.equals(d);
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

    /**
     * 회원이 직접 누르는 "배송완료" (AS-IS `POST /api/order/shpping-complete`).
     *
     * <p>AS-IS는 이걸 <b>운영자가 아니라 구매자</b>가 누른다 - 주문목록·상세의 배송중(30) 건에
     * "배송완료" 버튼이 뜨고(`mypage/orderList.html:305`), 누르면 35(배송완료)로 바뀐다.
     * 성격은 배송사 상태 갱신이 아니라 <b>수령 확인</b>이고, 그래야 교환/반품이 열린다
     * (`:180` "배송중인 답례품은 교환/반품이 불가합니다. 배송완료 버튼 클릭 후 신청 가능합니다.").
     *
     * <p>운영자용 {@link #updateDeliveryStatus}와 달리 <b>본인 주문만</b> 처리할 수 있고,
     * 목적지가 배송완료로 고정이다.
     */
    @Transactional
    public Order markDelivered(String orderId, Long userId) {
        Order order = detail(orderId);
        if (!order.getUserId().equals(userId)) {
            throw new OrderException("본인 주문만 배송완료 처리할 수 있습니다.");
        }
        if (order.getDeliveryStatus() == null) {
            throw new OrderException("아직 발송되지 않은 주문입니다.");
        }
        if (DELIVERY_DELIVERED.equals(order.getDeliveryStatus())
                || DELIVERY_CONFIRMED.equals(order.getDeliveryStatus())) {
            throw new OrderException("이미 배송완료 처리된 주문입니다.");
        }
        order.setDeliveryStatus(DELIVERY_DELIVERED);
        order.setDeliveredDate(LocalDateTime.now());
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
            // 쿠폰 구매 트리거 자동발급 - 2026-09-09 중단 (issuePurchaseTriggeredCoupons 주석 참고).
            issuePurchaseTriggeredCoupons(saved);
        } else {
            order.setOrderStatus(STATUS_CANCELLED);
            Order saved = orderRepository.save(order);
            orderSagaPublisher.publishCancelled(saved);
        }
    }

    /**
     * 쿠폰 발행시점(4:상품구매후발행, 5:첫구매) 자동발급 - 이 주문 자신이 확정되는 순간 트리거한다.
     *
     * <p><b>2026-09-09부터 중단(본문 전체 주석 처리).</b> 쿠폰 기능이 AS-IS에서도 현재
     * 사용되지 않아 화면 진입점을 감추기로 했고, 발급만 계속되면 회원이 볼 수 없는 쿠폰이
     * 주문할 때마다 쌓이기 때문이다. 가입/생일/정기발행을 담당하던
     * {@link CouponBatchScheduler}는 같은 이유로 설정 스위치
     * (ghlove.coupon.auto-issue.enabled=false)로 이미 꺼져 있고, 배치가 아닌 이 경로는
     * SAGA 주문확정 흐름 안에 있어 스위치 대신 코드 주석으로 막았다.
     *
     * <p>되살리려면 아래 본문 주석만 풀면 된다 - CouponService 쪽 발급 로직과 DB는
     * 그대로 살아 있다. 상세 경위는 docs/cart-review-2026-09-09.md §4-5.
     */
    private void issuePurchaseTriggeredCoupons(Order order) {
        // couponService.issueAfterItemPurchase(order.getUserId(), order.getItemId());
        // boolean firstConfirmed = orderRepository.findByUserIdOrderByCreatedDateDesc(order.getUserId()).stream()
        //         .filter(o -> STATUS_CONFIRMED.equals(o.getOrderStatus()))
        //         .count() <= 1;
        // if (firstConfirmed) {
        //     couponService.issueFirstPurchaseCoupons(order.getUserId());
        // }
    }

    private void appendCancelReason(Order order, String reason) {
        order.setCancelReason(order.getCancelReason() == null ? reason : order.getCancelReason() + "; " + reason);
    }

    /** 주문번호 생성 - 멀티아이템 체크아웃({@link CartService})도 헤더 주문번호를 이걸로 만든다. */
    String generateOrderId() {
        int suffix = RANDOM.nextInt(9000) + 1000;
        return "O" + ID_FORMAT.format(LocalDateTime.now()) + suffix;
    }
}
