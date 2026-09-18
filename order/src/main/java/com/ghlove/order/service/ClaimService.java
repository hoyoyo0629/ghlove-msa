package com.ghlove.order.service;

import com.ghlove.order.domain.Claim;
import com.ghlove.order.domain.Order;
import com.ghlove.order.event.OrderSagaPublisher;
import com.ghlove.order.repository.ClaimRepository;
import com.ghlove.order.repository.OrderRepository;
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

    private static final String ORDER_STATUS_CONFIRMED = "CONFIRMED";
    private static final String ORDER_STATUS_CLAIM_REQUESTED = "CLAIM_REQUESTED";
    private static final String ORDER_STATUS_CANCELLED = "CANCELLED";
    private static final String CLAIM_TYPE_RETURN = "RETURN";
    private static final String CLAIM_TYPE_EXCHANGE = "EXCHANGE";

    private static final String CLAIM_STATUS_REQUESTED = "REQUESTED";
    private static final String CLAIM_STATUS_APPROVED = "APPROVED";
    private static final String CLAIM_STATUS_REJECTED = "REJECTED";
    private static final String CLAIM_STATUS_COMPLETED = "COMPLETED";

    private final ClaimRepository claimRepository;
    private final OrderRepository orderRepository;
    private final OrderSagaPublisher orderSagaPublisher;
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

    @Transactional
    public Claim request(String orderId, String claimType, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ClaimException("주문을 찾을 수 없습니다."));
        if (!ORDER_STATUS_CONFIRMED.equals(order.getOrderStatus())) {
            throw new ClaimException("주문확정 상태인 주문만 반품/교환을 신청할 수 있습니다.");
        }
        // 유형별로 가능한 시점이 다르다 (AS-IS mypage/orderList.html:285~345의 버튼 노출 조건).
        if (CLAIM_TYPE_RETURN.equals(claimType) || CLAIM_TYPE_EXCHANGE.equals(claimType)) {
            // 교환·반품은 주문상태 35(배송완료)에서만(`:318,321`). 목록 상단 안내도 같다 -
            // "배송중인 답례품은 교환/반품이 불가합니다. 배송완료 버튼 클릭 후 신청 가능합니다."(`:180`)
            if (!OrderService.isDelivered(order)) {
                throw new ClaimException("배송완료된 주문만 반품/교환을 신청할 수 있습니다. 배송중인 주문은 배송완료 처리 후 신청해 주세요.");
            }
            // 상품 자체가 반품 대상인지도 본다 (`itemReturnFlag == 'Y' && mobileItemYn == 'N'`).
            if (!giftClient.fetch(order.getItemId()).returnable()) {
                throw new ClaimException("이 답례품은 교환·반품이 불가합니다.");
            }
        } else {
            // 주문취소는 반대로 주문상태 10·20(발송 전)에서만 가능하다(`:287`). 발송 이후에는
            // 취소가 아니라 반품으로 처리해야 한다. 상품의 반품가능 여부와는 무관하다.
            if (OrderService.isShipped(order)) {
                throw new ClaimException("이미 발송된 주문은 취소할 수 없습니다. 반품/교환을 신청해 주세요.");
            }
        }
        if (claimRepository.findByOrderIdAndStatus(orderId, CLAIM_STATUS_REQUESTED).isPresent()) {
            throw new ClaimException("이미 접수된 반품/교환 신청이 있습니다.");
        }

        Claim claim = new Claim();
        claim.setOrderId(orderId);
        claim.setClaimType(claimType);
        claim.setReason(reason);
        claim.setStatus(CLAIM_STATUS_REQUESTED);
        claim.setCreatedDate(LocalDateTime.now());
        Claim saved = claimRepository.save(claim);

        order.setOrderStatus(ORDER_STATUS_CLAIM_REQUESTED);
        order.setUpdatedDate(LocalDateTime.now());
        orderRepository.save(order);
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

        Order order = orderRepository.findById(claim.getOrderId()).orElseThrow();
        order.setOrderStatus(ORDER_STATUS_CONFIRMED);
        order.setUpdatedDate(LocalDateTime.now());
        orderRepository.save(order);
        orderSagaPublisher.publishClaimUpdated(saved, order);

        return saved;
    }

    /** 회수/재발송 확인 후 처리완료 - 이 시점에 주문을 취소 처리하고 재고/포인트 보상을 트리거한다. */
    @Transactional
    public Claim complete(Long claimId) {
        Claim claim = requireStatus(claimId, CLAIM_STATUS_APPROVED, "승인된 클레임만 처리완료할 수 있습니다.");
        claim.setStatus(CLAIM_STATUS_COMPLETED);
        claim.setProcessedDate(LocalDateTime.now());
        Claim savedClaim = claimRepository.save(claim);

        Order order = orderRepository.findById(claim.getOrderId()).orElseThrow();
        order.setOrderStatus(ORDER_STATUS_CANCELLED);
        order.setCancelReason("반품/교환 처리완료 (클레임 #" + claim.getClaimId() + ")");
        order.setUpdatedDate(LocalDateTime.now());
        Order savedOrder = orderRepository.save(order);
        orderSagaPublisher.publishCancelled(savedOrder);
        orderSagaPublisher.publishClaimUpdated(savedClaim, savedOrder);

        return savedClaim;
    }

    private Claim requireStatus(Long claimId, String requiredStatus, String errorMessage) {
        Claim claim = get(claimId);
        if (!requiredStatus.equals(claim.getStatus())) {
            throw new ClaimException(errorMessage);
        }
        return claim;
    }
}
