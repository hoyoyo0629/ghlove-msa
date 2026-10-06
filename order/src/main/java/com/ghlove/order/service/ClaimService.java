package com.ghlove.order.service;

import com.ghlove.order.domain.Claim;
import com.ghlove.order.domain.Order;
import com.ghlove.order.domain.OrderItem;
import com.ghlove.order.domain.Shipment;
import com.ghlove.order.event.OrderSagaPublisher;
import com.ghlove.order.repository.ClaimRepository;
import com.ghlove.order.repository.OrderItemRepository;
import com.ghlove.order.repository.OrderRepository;
import com.ghlove.order.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 클레임 프로세스 - 반품/교환 (SFR-006: "주문취소, 반품/교환 요청, 승인/거절, 회수/
 * 재발송, 포인트 복원, 재고 복원"). 교환은 원 주문을 취소 처리한 뒤 사용자가 새로
 * 주문하는 방식(반품+재주문)으로 처리한다 - 아이템을 원자적으로 맞바꾸는 것보다
 * 훨씬 단순하고, 실제 이커머스에서도 흔히 쓰이는 방식.
 *
 * 상태 흐름: REQUESTED -(승인)-> APPROVED -(회수 확인)-> COMPLETED (이 시점에
 * 주문을 CANCELLED로 전환하고 ORDER_CANCELLED를 발행해 gift/point가 재고·포인트를
 * 복원하게 한다) / REQUESTED -(거절)-> REJECTED (주문은 CONFIRMED로 되돌림).
 */
@Service
@RequiredArgsConstructor
public class ClaimService {

    private static final String ITEM_STATUS_CONFIRMED = "CONFIRMED";
    private static final String CLAIM_TYPE_RETURN = "RETURN";
    private static final String CLAIM_TYPE_EXCHANGE = "EXCHANGE";
    private static final String DELIVERY_DELIVERED = "DELIVERED";
    private static final String DELIVERY_CONFIRMED = "CONFIRMED";
    private static final String DELIVERY_SHIPPED = "SHIPPED";
    private static final String DELIVERY_IN_TRANSIT = "IN_TRANSIT";

    private static final String CLAIM_STATUS_REQUESTED = "REQUESTED";
    private static final String CLAIM_STATUS_APPROVED = "APPROVED";
    private static final String CLAIM_STATUS_REJECTED = "REJECTED";
    private static final String CLAIM_STATUS_COMPLETED = "COMPLETED";

    private final ClaimRepository claimRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ShipmentRepository shipmentRepository;
    private final OrderSagaPublisher orderSagaPublisher;
    private final ShipmentSagaService shipmentSagaService;
    private final GiftClient giftClient;

    public List<Claim> pending() {
        return claimRepository.findByStatusOrderByClaimIdDesc(CLAIM_STATUS_REQUESTED);
    }

    public List<Claim> approved() {
        return claimRepository.findByStatusOrderByClaimIdDesc(CLAIM_STATUS_APPROVED);
    }

    /** 마이페이지 "취소반품교환" - 본인이 신청한 클레임만, 최신순. */
    public List<Claim> myClaims(Long userId) {
        List<String> orderIds = orderRepository.findByUserIdOrderByCreatedDateDesc(userId).stream()
                .map(Order::getOrderId).toList();
        if (orderIds.isEmpty()) {
            return List.of();
        }
        return claimRepository.findByOrderIdIn(orderIds).stream()
                .sorted((a, b) -> b.getClaimId().compareTo(a.getClaimId()))
                .toList();
    }

    public Claim get(Long claimId) {
        return claimRepository.findById(claimId).orElseThrow(() -> new ClaimException("클레임을 찾을 수 없습니다."));
    }

    /** 품목 단위 반품/교환 신청 (멀티아이템 재설계). 대상 품목·출고 상태로 가능 여부를 판정한다. */
    @Transactional
    public Claim request(String orderId, Long orderItemId, String claimType, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ClaimException("주문을 찾을 수 없습니다."));
        OrderItem item = orderItemRepository.findById(orderItemId)
                .filter(i -> orderId.equals(i.getOrderId()))
                .orElseThrow(() -> new ClaimException("주문 품목을 찾을 수 없습니다."));
        if (!ITEM_STATUS_CONFIRMED.equals(item.getItemStatus())) {
            throw new ClaimException("확정된 품목만 반품/교환을 신청할 수 있습니다.");
        }
        Shipment shipment = item.getShipmentId() != null
                ? shipmentRepository.findById(item.getShipmentId()).orElse(null) : null;

        // 유형별로 가능한 시점이 다르다 (AS-IS mypage/orderList.html:285~345의 버튼 노출 조건).
        if (CLAIM_TYPE_RETURN.equals(claimType) || CLAIM_TYPE_EXCHANGE.equals(claimType)) {
            // 교환·반품은 배송완료(35)에서만. "배송중인 답례품은 교환/반품이 불가합니다. 배송완료 후 신청."(`:180`)
            if (!isDelivered(shipment)) {
                throw new ClaimException("배송완료된 품목만 반품/교환을 신청할 수 있습니다. 배송중인 품목은 배송완료 처리 후 신청해 주세요.");
            }
            // 상품 자체가 반품 대상인지도 본다 (`itemReturnFlag == 'Y' && mobileItemYn == 'N'`).
            if (!giftClient.fetch(item.getItemId()).returnable()) {
                throw new ClaimException("이 답례품은 교환·반품이 불가합니다.");
            }
        } else {
            // 주문취소는 발송 전(10·20)에서만. 발송 이후에는 반품으로 처리해야 한다.
            if (isShipped(shipment)) {
                throw new ClaimException("이미 발송된 품목은 취소할 수 없습니다. 반품/교환을 신청해 주세요.");
            }
        }
        if (claimRepository.findByOrderItemIdAndStatus(orderItemId, CLAIM_STATUS_REQUESTED).isPresent()) {
            throw new ClaimException("이미 접수된 반품/교환 신청이 있습니다.");
        }

        Claim claim = new Claim();
        claim.setOrderId(orderId);
        claim.setOrderItemId(orderItemId);
        claim.setShipmentId(item.getShipmentId());
        claim.setClaimType(claimType);
        claim.setReason(reason);
        claim.setStatus(CLAIM_STATUS_REQUESTED);
        claim.setCreatedDate(LocalDateTime.now());
        Claim saved = claimRepository.save(claim);

        orderSagaPublisher.publishClaimUpdated(saved, order);
        return saved;
    }

    @Transactional
    public Claim approve(Long claimId) {
        Claim claim = requireStatus(claimId, CLAIM_STATUS_REQUESTED, "접수 상태인 클레임만 승인할 수 있습니다.");
        claim.setStatus(CLAIM_STATUS_APPROVED);
        Claim saved = claimRepository.save(claim);

        Order order = orderRepository.findById(claim.getOrderId()).orElseThrow();
        orderSagaPublisher.publishClaimUpdated(saved, order);
        return saved;
    }

    @Transactional
    public Claim reject(Long claimId) {
        Claim claim = requireStatus(claimId, CLAIM_STATUS_REQUESTED, "접수 상태인 클레임만 거절할 수 있습니다.");
        claim.setStatus(CLAIM_STATUS_REJECTED);
        claim.setProcessedDate(LocalDateTime.now());
        Claim saved = claimRepository.save(claim);

        // 거절은 품목 상태를 되돌릴 필요가 없다 - 신청 중에도 품목은 CONFIRMED 그대로였다.
        Order order = orderRepository.findById(claim.getOrderId()).orElseThrow();
        orderSagaPublisher.publishClaimUpdated(saved, order);
        return saved;
    }

    /** 회수/재발송 확인 후 처리완료 - 이 시점에 <b>그 품목만</b> 취소 처리하고 재고/포인트 보상을 트리거한다. */
    @Transactional
    public Claim complete(Long claimId) {
        Claim claim = requireStatus(claimId, CLAIM_STATUS_APPROVED, "승인된 클레임만 처리완료할 수 있습니다.");
        claim.setStatus(CLAIM_STATUS_COMPLETED);
        claim.setProcessedDate(LocalDateTime.now());
        Claim savedClaim = claimRepository.save(claim);

        if (claim.getOrderItemId() != null) {
            shipmentSagaService.cancelItem(claim.getOrderItemId(),
                    "반품/교환 처리완료 (클레임 #" + claim.getClaimId() + ")");
        }
        Order order = orderRepository.findById(claim.getOrderId()).orElse(null);
        if (order != null) {
            orderSagaPublisher.publishClaimUpdated(savedClaim, order);
        }
        return savedClaim;
    }

    private Claim requireStatus(Long claimId, String requiredStatus, String errorMessage) {
        Claim claim = get(claimId);
        if (!requiredStatus.equals(claim.getStatus())) {
            throw new ClaimException(errorMessage);
        }
        return claim;
    }

    /** 출고가 배송완료(또는 구매확정) 이후인가 - 반품/교환 가능 시점. */
    private static boolean isDelivered(Shipment s) {
        String d = s != null ? s.getDeliveryStatus() : null;
        return DELIVERY_DELIVERED.equals(d) || DELIVERY_CONFIRMED.equals(d);
    }

    /** 출고가 발송(송장등록) 이후인가 - 취소 불가 시점. */
    private static boolean isShipped(Shipment s) {
        String d = s != null ? s.getDeliveryStatus() : null;
        return DELIVERY_SHIPPED.equals(d) || DELIVERY_IN_TRANSIT.equals(d)
                || DELIVERY_DELIVERED.equals(d) || DELIVERY_CONFIRMED.equals(d);
    }
}
