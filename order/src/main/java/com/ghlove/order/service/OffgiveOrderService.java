package com.ghlove.order.service;

import com.ghlove.order.domain.Order;
import com.ghlove.order.domain.OrderItem;
import com.ghlove.order.domain.Shipment;
import com.ghlove.order.event.OrderSagaPublisher;
import com.ghlove.order.repository.OrderItemRepository;
import com.ghlove.order.repository.OrderRepository;
import com.ghlove.order.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 오프라인기부접수(기탁서 등록, admin 15102) 답례품 주문 - AS-IS {@code OrderServiceImpl.saveOffGiveOrder}
 * (등록 시 주문 생성)와 {@code NgDonationBatchServiceImpl.procOffOrder}(수납완료 뒤 결제처리) 이식.
 *
 * <p><b>등록</b>(saveOffGiveOrder): 포인트를 보지 않는다 - AS-IS {@code offPayAmountVerificationGivePoint}의
 * 포인트 부족 판정은 주석처리돼 있다(기부가 아직 '신고'라 포인트가 없다). 답례품·재고·배송지만 검증하고
 * 주문을 '입금대기'(AS-IS ORDER_STATUS '0')로 저장하며, 품목마다 기부일련번호(AS-IS FILLER9)를 남긴다.
 * AS-IS 끝의 "재고 차감" 블록은 {@code buy.setStockMap()}을 부르는 곳이 없어
 * {@code updateStockDeduction(null)}에서 바로 끝난다 - 등록 시점엔 재고도 줄지 않는다(그대로 따른다).
 *
 * <p><b>수납완료 뒤</b>(procOffOrder): AS-IS 배치는 수납완료된 기부의 미처리(OFF_PROC_YN='N') 주문을
 * 결제완료로 바꾸고 포인트 사용("답례품 구매")·재고 차감을 한 뒤 OFF_PROC_YN='Y'로 남긴다. TO-BE는
 * point가 그 기부의 포인트를 적립한 뒤(point.ledger EARN, refKey=기부일련번호)
 * {@link #onDonationPointCredited}가 입금대기 출고를 PENDING으로 돌리고 {@code SHIPMENT_CREATED}를
 * 발행한다 - 재고(gift)·포인트(point) 차감은 체크아웃과 같은 출고 SAGA가 하고 둘 다 성공하면 CONFIRMED.
 * 포인트는 AS-IS처럼 모자라도 남은 만큼만 차감하고 성공한다(이벤트의 cntrSn으로 point가 구분한다).
 * 기부완료 이벤트에 바로 반응하지 않는 이유: 적립보다 차감 요청이 먼저 도착하면 포인트 부족으로 취소된다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OffgiveOrderService {

    /** AS-IS ORDER_STATUS '0' 입금대기 - 기부 수납완료 전의 오프라인 답례품 주문. */
    public static final String STATUS_WAITING_DEPOSIT = "WAITING_DEPOSIT";

    private static final String STATUS_APPROVED = "APPROVED";
    private static final String SOLD_OUT = "1";
    private static final String OFF_PROC_N = "N";
    private static final String OFF_PROC_Y = "Y";

    private final GiftClient giftClient;
    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final ShipmentRepository shipmentRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderSagaPublisher orderSagaPublisher;
    private final OffgiveDonationClient offgiveDonationClient;

    /** AS-IS orderItems 한 줄 - {@code itemId||수량||옵션ID}. */
    public record GiftLine(Long itemId, Long itemOptionId, Integer quantity) {
    }

    /** AS-IS GiftOrderVo - 구매자(userId)·기부일련번호(linkMngKey)·수령인(Receiver). */
    public record OffgiveOrderRequest(Long userId, String cntrSn, List<GiftLine> items, String receiverName,
                                       String receiverPhone, String deliveryAddress, String deliveryAddressDetail,
                                       String zipcode) {
    }

    private record Line(GiftItemInfo gift, GiftClient.OptionInfo option, int quantity, long lineTotal) {
        int optionPrice() {
            return option != null && option.optionPrice() != null ? option.optionPrice() : 0;
        }
    }

    /** 기탁서 상세의 답례품 부분 - {@link #summaryOf}. */
    public record OffgiveOrderSummary(String options, String rcvPost, String rcvAddress, String rcvAddressDetail) {
    }

    /**
     * 기탁서 상세(AS-IS getOffgive)의 선택 답례품·배송지. AS-IS는 이 기부일련번호(FILLER9)의 품목마다
     * {@code ITEM_NAME || OPTIONS}(옵션 표시문자열을 상품명 뒤에 구분자 없이)를 {@code <br/>}로 잇고, 배송지는 그 주문의
     * 배송정보(우편번호·주소·상세주소)다. OPTIONS는 AS-IS {@code ShopUtils.viewOptionTextNoUl}의 결과라
     * 옵션명 뒤에 추가금액이 0이 아니면 {@code " (+1,000원)"}이 붙는다. 품목이 없으면 null(화면이 두 행을 숨긴다).
     */
    @Transactional(readOnly = true)
    public OffgiveOrderSummary summaryOf(String cntrSn) {
        List<OrderItem> items = orderItemRepository.findByCntrSnOrderByOrderItemIdAsc(cntrSn);
        List<String> texts = new ArrayList<>();
        for (OrderItem item : items) {
            String text = (item.getItemName() == null ? "" : item.getItemName()) + optionsText(item);
            if (!text.isEmpty()) {
                texts.add(org.springframework.web.util.HtmlUtils.htmlEscape(text));
            }
        }
        if (texts.isEmpty()) {
            return null;
        }
        Order order = orderRepository.findById(items.get(0).getOrderId()).orElse(null);
        return new OffgiveOrderSummary(String.join("<br/>", texts),
                order != null ? order.getDeliveryZipcode() : null,
                order != null ? order.getDeliveryAddress() : null,
                order != null ? order.getDeliveryAddressDetail() : null);
    }

    /** AS-IS viewOptionTextNoUl - 옵션 표시명 + 추가금액(0이 아니면 " (+1,000원)"). 옵션 없으면 빈 문자열. */
    private static String optionsText(OrderItem item) {
        if (item.getOptionName() == null || item.getOptionName().isEmpty()) {
            return "";
        }
        String t = item.getOptionName();
        int addPrice = item.getOptionPrice() == null ? 0 : item.getOptionPrice();
        if (addPrice != 0) {
            // AS-IS 그대로 - 음수면 부호가 두 번 붙는다("(--500원)").
            t += " (" + (addPrice > 0 ? "+" : "-") + new java.text.DecimalFormat("#,###").format(addPrice) + "원)";
        }
        return t;
    }

    // ==================== 등록 (AS-IS saveOffGiveOrder) ====================

    @Transactional
    public String create(OffgiveOrderRequest req) {
        if (req.cntrSn() == null || req.cntrSn().isBlank()) {
            throw new OrderException("기부일련번호가 없습니다.");
        }

        // 4. orderItems 처리 - 형식이 맞지 않는 줄은 건너뛴다(AS-IS itemInfo.length != 3 → continue).
        List<Line> lines = new ArrayList<>();
        if (req.items() != null) {
            for (GiftLine giftLine : req.items()) {
                if (giftLine == null || giftLine.itemId() == null || giftLine.quantity() == null) {
                    continue;
                }
                GiftItemInfo gift = giftClient.fetch(giftLine.itemId());
                GiftClient.OptionInfo option = null;
                if (giftLine.itemOptionId() != null && giftLine.itemOptionId() > 0) {
                    option = giftClient.fetchOption(giftLine.itemOptionId());
                    // AS-IS는 그 답례품의 옵션목록에서 같은 ID만 고른다 - 못 찾으면 buyGiveGoodsVerification이 막는다.
                    if (option.itemId() == null || !option.itemId().equals(gift.itemId())) {
                        throw new OrderException(gift.itemName() + " 답례품의 옵션 구성이 변경되었습니다.");
                    }
                }
                int optionPrice = option != null && option.optionPrice() != null ? option.optionPrice() : 0;
                lines.add(new Line(gift, option, giftLine.quantity(),
                        (long) (gift.salePrice() + optionPrice) * giftLine.quantity()));
            }
        }

        // 데이터 검증 - AS-IS offPayAmountVerificationGivePoint(재고·판매가능 검증만, 포인트 판정은 주석처리)
        verifyGoods(lines);

        // 배송지 검증 - AS-IS 0331~0335. 모바일 상품이 있으면 우편번호·주소·상세주소는 보지 않는다.
        boolean existMobileItem = lines.stream().anyMatch(l -> "Y".equalsIgnoreCase(l.gift().mobileItemYn()));
        if (isBlank(req.receiverName())) {
            throw new OrderException("받으시는분 이름을 입력해주세요.");
        }
        if (isBlank(req.receiverPhone())) {
            throw new OrderException("배송지 휴대폰번호를 입력해주세요.");
        }
        if (isBlank(req.zipcode()) && !existMobileItem) {
            throw new OrderException("배송지 우편번호를 입력해주세요.");
        }
        if (isBlank(req.deliveryAddress()) && !existMobileItem) {
            throw new OrderException("배송지 주소를 입력해주세요.");
        }
        if (isBlank(req.deliveryAddressDetail()) && !existMobileItem) {
            throw new OrderException("배송지 상세주소를 입력해주세요.");
        }

        // 지자체별 상품 분류(AS-IS buyLocgovs) = 출고
        Map<String, List<Line>> byLocgov = new LinkedHashMap<>();
        for (Line line : lines) {
            String locgovCode = line.gift().locgovCode() != null ? line.gift().locgovCode() : "";
            byLocgov.computeIfAbsent(locgovCode, k -> new ArrayList<>()).add(line);
        }

        // 5. 상품배송정책 - AS-IS buyReceiver.setShipping(getIslandTypeByZipcode(zipcode))로 배송비를 잡아
        // OP_ORDER_SHIPPING에 남기지만, 결제금액(OP_ORDER_PAYMENT)과 수납 후 포인트 사용액
        // (getGivePayOrder: SUM(SALE_PRICE*QUANTITY))에는 넣지 않는다 - 배송비는 기록만, 차감액은 품목합.
        String islandType = orderService.islandTypeOf(req.zipcode());
        // 같은 답례품·수량을 두 줄로 고르면 record 값이 같아지므로 줄 객체 자체(identity)로 구분한다.
        Map<Line, Long> feeByLine = new IdentityHashMap<>();
        for (List<Line> group : byLocgov.values()) {
            List<DeliveryFeeCalculator.Line> shipLines = new ArrayList<>();
            for (int i = 0; i < group.size(); i++) {
                Line l = group.get(i);
                GiftItemInfo g = l.gift();
                shipLines.add(new DeliveryFeeCalculator.Line((long) i, g.shippingType(),
                        g.shipping() != null ? g.shipping() : 0, g.shippingFreeAmount(), g.shippingItemCount(),
                        g.shippingExtraCharge1(), g.shippingExtraCharge2(), g.shippingGroupCode(),
                        g.shipmentGroupCode(), null, l.quantity(), l.lineTotal(), i));
            }
            Map<Long, Long> fees = DeliveryFeeCalculator.compute(shipLines, islandType);
            for (int i = 0; i < group.size(); i++) {
                feeByLine.put(group.get(i), fees.getOrDefault((long) i, 0L));
            }
        }

        LocalDateTime now = LocalDateTime.now();
        // 1. 주문번호생성 - AS-IS getNewOrderCode(OrderCodePrefix.FRONT) = "K"+10자리
        String orderId = orderService.generateOrderId();

        Order header = new Order();
        header.setOrderId(orderId);
        header.setUserId(req.userId());
        header.setOrderStatus(STATUS_WAITING_DEPOSIT);
        header.setPointAmount(lines.stream().mapToLong(Line::lineTotal).sum());
        header.setDeliveryFee(feeByLine.values().stream().mapToLong(Long::longValue).sum());
        header.setDiscountAmount(0L);
        header.setCreatedDate(now);
        header.setUpdatedDate(now);
        // 헤더 품목 컬럼은 목록 표시용 요약(정본은 OrderItem) - checkoutMultiItem과 같은 규칙.
        Line rep = lines.get(0);
        header.setItemId(rep.gift().itemId());
        header.setSellerId(rep.gift().sellerId());
        header.setItemName(lines.size() == 1 ? rep.gift().itemName()
                : rep.gift().itemName() + " 외 " + (lines.size() - 1) + "건");
        header.setQuantity(lines.stream().mapToInt(Line::quantity).sum());
        header.setLocgovCode(byLocgov.size() == 1 ? rep.gift().locgovCode() : null);
        header.setReceiverName(req.receiverName());
        header.setReceiverPhone(req.receiverPhone());
        header.setDeliveryAddress(req.deliveryAddress());
        header.setDeliveryAddressDetail(req.deliveryAddressDetail());
        header.setDeliveryZipcode(req.zipcode());
        orderRepository.save(header);

        for (Map.Entry<String, List<Line>> entry : byLocgov.entrySet()) {
            List<Line> group = entry.getValue();
            Shipment shipment = new Shipment();
            shipment.setOrderId(orderId);
            shipment.setLocgovCode(entry.getKey().isEmpty() ? null : entry.getKey());
            shipment.setPointAmount(group.stream().mapToLong(Line::lineTotal).sum());
            shipment.setDeliveryFee(group.stream().mapToLong(l -> feeByLine.getOrDefault(l, 0L)).sum());
            shipment.setShipmentStatus(STATUS_WAITING_DEPOSIT);
            shipment.setCreatedDate(now);
            shipment.setUpdatedDate(now);
            shipmentRepository.save(shipment);

            // 모바일 상품 분할 - AS-IS "모바일 상품 존재할 경우 분할 작업 처리": 수량 N인 모바일 답례품은
            // 수량 1로 바꾸고 N-1개의 복제(각 수량 1)를 뒤에 붙인다. 배송비는 분할 전 수량으로 위에서 이미 잡았다
            // (AS-IS도 buyReceiver.setShipping 뒤에 분할한다).
            List<Line> mobileCopies = new ArrayList<>();
            for (Line l : group) {
                boolean mobile = "Y".equalsIgnoreCase(l.gift().mobileItemYn());
                saveItem(shipment.getShipmentId(), orderId, l, mobile ? 1 : l.quantity(), req.cntrSn(), now);
                if (mobile) {
                    for (int i = 1; i < l.quantity(); i++) {
                        mobileCopies.add(l);
                    }
                }
            }
            for (Line l : mobileCopies) {
                saveItem(shipment.getShipmentId(), orderId, l, 1, req.cntrSn(), now);
            }
        }
        log.info("Offline gift order {} created as WAITING_DEPOSIT for cntrSn={}", orderId, req.cntrSn());
        return orderId;
    }

    private void saveItem(Long shipmentId, String orderId, Line l, int quantity, String cntrSn, LocalDateTime now) {
        OrderItem item = new OrderItem();
        item.setShipmentId(shipmentId);
        item.setOrderId(orderId);
        item.setItemId(l.gift().itemId());
        item.setItemOptionId(l.option() != null ? l.option().itemOptionId() : 0L);
        item.setSellerId(l.gift().sellerId());
        item.setItemName(l.gift().itemName());
        item.setOptionName(l.option() != null ? l.option().optionName() : null);
        item.setOptionPrice(l.optionPrice());
        item.setQuantity(quantity);
        item.setUnitPrice(l.gift().salePrice());
        item.setPointAmount((long) (l.gift().salePrice() + l.optionPrice()) * quantity);
        item.setItemStatus(STATUS_WAITING_DEPOSIT);
        item.setCntrSn(cntrSn);
        item.setOffProcYn(OFF_PROC_N);
        item.setCreatedDate(now);
        item.setUpdatedDate(now);
        orderItemRepository.save(item);
    }

    /**
     * AS-IS setOrderItemInfo + buyGiveGoodsVerification - 판매가능(승인·비품절), 선택 옵션 품절,
     * 1회 최대 주문수량, 재고(같은 답례품 주문수량 합 ≤ 재고)를 본다. 포인트는 보지 않는다.
     */
    private void verifyGoods(List<Line> lines) {
        if (lines.isEmpty()) {
            throw new OrderException("장바구니에 담긴 상품이 없습니다.");
        }
        Map<Long, Integer> sumByItem = new LinkedHashMap<>();
        for (Line l : lines) {
            sumByItem.merge(l.gift().itemId(), l.quantity(), Integer::sum);
        }
        for (Line l : lines) {
            GiftItemInfo gift = l.gift();
            if (l.quantity() <= 0) {
                throw new OrderException("주문에 실패 했습니다.");
            }
            if (!STATUS_APPROVED.equals(gift.dataStatusCode()) || SOLD_OUT.equals(gift.soldOut())) {
                throw new OrderException("상품의 재고가 없습니다.");
            }
            if (l.option() != null && l.option().soldOut()) {
                throw new OrderException(gift.itemName() + " 답례품의 옵션 구성이 변경되었거나 재고가 없습니다.");
            }
            Integer max = gift.orderMaxQuantity();
            if (max != null && max > 0 && l.quantity() > max) {
                throw new OrderException(gift.itemName() + " 답례품의 최대 구매가능수량을 확인해주세요.");
            }
            int stock = gift.stockQuantity() == null ? 0 : gift.stockQuantity();
            if (sumByItem.get(gift.itemId()) > stock) {
                throw new OrderException("상품의 재고가 없습니다.");
            }
        }
    }

    // ==================== 수납완료 뒤 결제처리 (AS-IS procOffOrder) ====================

    /**
     * 기부의 포인트가 적립된 뒤(point.ledger EARN, refKey=기부일련번호) 그 기부의 미처리 오프라인
     * 답례품을 결제처리한다. AS-IS selectOffOrderList(G_CNTR 수납완료 ⋈ OP_ORDER_ITEM.FILLER9,
     * OFF_PROC_YN='N') → 상태 0→10 · 포인트 사용 · 재고 차감 → updateOrderOffProcYn('Y').
     * TO-BE에선 출고를 PENDING으로 돌려 {@code SHIPMENT_CREATED}를 발행하면 gift(재고)·point(포인트
     * "답례품 구매")가 차감하고 {@link ShipmentSagaService}가 CONFIRMED(=결제완료)로 확정한다.
     * 같은 이벤트가 다시 와도 OFF_PROC_YN='Y'라 다시 처리하지 않는다.
     */
    @Transactional
    public void onDonationPointCredited(String cntrSn) {
        if (cntrSn == null || cntrSn.isBlank()) {
            return;
        }
        List<OrderItem> items = orderItemRepository.findByCntrSnAndOffProcYn(cntrSn, OFF_PROC_N);
        if (items.isEmpty()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        Map<Long, List<OrderItem>> byShipment = new LinkedHashMap<>();
        for (OrderItem item : items) {
            item.setOffProcYn(OFF_PROC_Y);
            item.setUpdatedDate(now);
            if (STATUS_WAITING_DEPOSIT.equals(item.getItemStatus())) {
                item.setItemStatus(ShipmentSagaService.Status.PENDING);
                byShipment.computeIfAbsent(item.getShipmentId(), k -> new ArrayList<>()).add(item);
            }
        }
        orderItemRepository.saveAll(items);

        for (Map.Entry<Long, List<OrderItem>> entry : byShipment.entrySet()) {
            Shipment shipment = shipmentRepository.findById(entry.getKey()).orElse(null);
            if (shipment == null || !STATUS_WAITING_DEPOSIT.equals(shipment.getShipmentStatus())) {
                continue;
            }
            shipment.setShipmentStatus(ShipmentSagaService.Status.PENDING);
            shipment.setUpdatedDate(now);
            shipmentRepository.save(shipment);

            Order order = orderRepository.findById(shipment.getOrderId()).orElse(null);
            if (order == null) {
                continue;
            }
            if (STATUS_WAITING_DEPOSIT.equals(order.getOrderStatus())) {
                order.setOrderStatus(ShipmentSagaService.Status.PENDING);
                order.setUpdatedDate(now);
                orderRepository.save(order);
            }
            orderSagaPublisher.publishShipmentCreated(shipment, order.getUserId(), entry.getValue());
            log.info("Offline gift order {} shipment {} released for payment (cntrSn={})",
                    order.getOrderId(), shipment.getShipmentId(), cntrSn);
        }
    }

    // ==================== 미수납 답례품 정리 (AS-IS allDayOffItemUpdateBatch) ====================

    /**
     * AS-IS {@code OffgiveBatchServiceImpl.ItemStatusUpdateBatch} - 오프라인 기부가 미납으로 삭제됐는데
     * 그 기부로 만든 답례품 주문이 입금대기로 남아 있으면 주문취소(AS-IS ORDER_STATUS '99')로 내린다.
     * 대상은 생성일이 90일 전 00:00:00 ~ 10일 전 23:59:59인 입금대기 품목(AS-IS selectOffItem)이고, 기부가
     * 오프라인·미납·삭제인지는 donation이 확인한다(AS-IS selectGiveSunapCheck 이중 확인).
     *
     * <p>AS-IS updateItemStatus는 그 기부일련번호의 품목을 상태 구분 없이 전부 '99'로 바꾼다. 미납 기부의 주문은
     * 결제처리(수납완료 뒤)를 거치지 않아 전부 입금대기라 결과가 같고, 여기서는 입금대기 품목만 바꾼다 -
     * 포인트·재고가 차감된 품목을 보상 없이 취소로 덮는 일이 없게 하려는 것이다. 차감된 게 없으니 보상 이벤트도 없다.
     *
     * @return 취소한 품목 수
     */
    @Transactional
    public int cancelUnpaidDeleted(LocalDate today) {
        LocalDateTime from = today.minusDays(90).atStartOfDay();
        LocalDateTime to = today.minusDays(9).atStartOfDay();
        List<String> cntrSns = orderItemRepository.findCntrSnsByItemStatusCreatedBetween(STATUS_WAITING_DEPOSIT, from, to);
        if (cntrSns.isEmpty()) {
            return 0;
        }
        int cancelled = 0;
        for (String cntrSn : offgiveDonationClient.deletedUnpaidOffline(cntrSns)) {
            cancelled += cancelWaitingItems(cntrSn);
        }
        return cancelled;
    }

    private int cancelWaitingItems(String cntrSn) {
        List<OrderItem> items = orderItemRepository.findByCntrSnAndItemStatus(cntrSn, STATUS_WAITING_DEPOSIT);
        if (items.isEmpty()) {
            return 0;
        }
        LocalDateTime now = LocalDateTime.now();
        Set<Long> shipmentIds = new LinkedHashSet<>();
        Set<String> orderIds = new LinkedHashSet<>();
        for (OrderItem item : items) {
            item.setItemStatus(ShipmentSagaService.Status.CANCELLED);
            item.setUpdatedDate(now);
            shipmentIds.add(item.getShipmentId());
            orderIds.add(item.getOrderId());
        }
        orderItemRepository.saveAll(items);

        // 품목 상태가 정본이고 출고·헤더는 집계값이다 - 전부 취소된 출고/주문만 취소로 내린다.
        for (Long shipmentId : shipmentIds) {
            if (shipmentId == null) {
                continue;
            }
            boolean allCancelled = orderItemRepository.findByShipmentId(shipmentId).stream()
                    .allMatch(i -> ShipmentSagaService.Status.CANCELLED.equals(i.getItemStatus()));
            if (allCancelled) {
                shipmentRepository.findById(shipmentId).ifPresent(s -> {
                    s.setShipmentStatus(ShipmentSagaService.Status.CANCELLED);
                    s.setUpdatedDate(now);
                    shipmentRepository.save(s);
                });
            }
        }
        for (String orderId : orderIds) {
            boolean allCancelled = orderItemRepository.findByOrderId(orderId).stream()
                    .allMatch(i -> ShipmentSagaService.Status.CANCELLED.equals(i.getItemStatus()));
            if (allCancelled) {
                orderRepository.findById(orderId).ifPresent(o -> {
                    o.setOrderStatus(ShipmentSagaService.Status.CANCELLED);
                    o.setUpdatedDate(now);
                    orderRepository.save(o);
                });
            }
        }
        log.info("Offline gift order items for deleted unpaid donation cntrSn={} cancelled: {}", cntrSn, items.size());
        return items.size();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
