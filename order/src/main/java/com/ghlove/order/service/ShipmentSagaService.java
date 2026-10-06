package com.ghlove.order.service;

import com.ghlove.order.domain.Order;
import com.ghlove.order.domain.OrderItem;
import com.ghlove.order.domain.Shipment;
import com.ghlove.order.event.OrderSagaPublisher;
import com.ghlove.order.event.ShipmentPointDeductFailedEvent;
import com.ghlove.order.event.ShipmentPointDeductedEvent;
import com.ghlove.order.event.ShipmentStockReserveFailedEvent;
import com.ghlove.order.event.ShipmentStockReservedEvent;
import com.ghlove.order.repository.OrderItemRepository;
import com.ghlove.order.repository.OrderRepository;
import com.ghlove.order.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 출고 단위 SAGA 해소 (멀티아이템 재설계, 선택지 A). 레거시 주문 단위 SAGA
 * ({@link OrderService}의 onStockReserved/resolveIfReady)와 <b>병행</b>하는 신 경로로,
 * 상관ID를 orderId가 아니라 <b>shipmentId</b>로 삼는다. 한 주문의 출고들은 서로 독립적으로
 * 확정/취소되고({@code Shipment.shipmentStatus}), 모두 종결되면 헤더 주문 상태를 집계한다:
 * 전부 확정→CONFIRMED, 전부 취소→CANCELLED, 혼재→PARTIALLY_CONFIRMED(부분성공).
 *
 * <p>재고예약(품목별) && 포인트차감(출고 단위)이 둘 다 성공해야 출고 확정. 하나라도 실패하면
 * 출고 취소 + {@code SHIPMENT_CANCELLED} 발행으로 성공했던 반대편 레그를 보상한다
 * (gift 재고복원/point 포인트복원은 각자 자기가 실제 처리한 것만 되돌리는 멱등 연산).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ShipmentSagaService {

    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_CONFIRMED = "CONFIRMED";
    private static final String STATUS_CANCELLED = "CANCELLED";
    private static final String STATUS_PARTIAL = "PARTIALLY_CONFIRMED";
    private static final String OUTCOME_RESERVED = "RESERVED";
    private static final String OUTCOME_DEDUCTED = "DEDUCTED";
    private static final String OUTCOME_FAILED = "FAILED";

    private final ShipmentRepository shipmentRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final OrderSagaPublisher orderSagaPublisher;

    @Transactional
    public void onStockReserved(ShipmentStockReservedEvent event) {
        Shipment shipment = pendingShipmentOrNull(event.shipmentId());
        if (shipment == null || shipment.getStockOutcome() != null) {
            return;
        }
        shipment.setStockOutcome(OUTCOME_RESERVED);
        resolveShipment(shipment);
    }

    @Transactional
    public void onStockReserveFailed(ShipmentStockReserveFailedEvent event) {
        Shipment shipment = pendingShipmentOrNull(event.shipmentId());
        if (shipment == null || shipment.getStockOutcome() != null) {
            return;
        }
        shipment.setStockOutcome(OUTCOME_FAILED);
        appendCancelReason(shipment, "재고 부족: " + event.reason());
        resolveShipment(shipment);
    }

    @Transactional
    public void onPointDeducted(ShipmentPointDeductedEvent event) {
        Shipment shipment = pendingShipmentOrNull(event.shipmentId());
        if (shipment == null || shipment.getPointOutcome() != null) {
            return;
        }
        shipment.setPointOutcome(OUTCOME_DEDUCTED);
        resolveShipment(shipment);
    }

    @Transactional
    public void onPointDeductFailed(ShipmentPointDeductFailedEvent event) {
        Shipment shipment = pendingShipmentOrNull(event.shipmentId());
        if (shipment == null || shipment.getPointOutcome() != null) {
            return;
        }
        shipment.setPointOutcome(OUTCOME_FAILED);
        appendCancelReason(shipment, "포인트 부족: " + event.reason());
        resolveShipment(shipment);
    }

    /** 두 결과가 모두 도착했을 때만 종결. 확정/취소 후 헤더 상태를 집계한다. */
    private void resolveShipment(Shipment shipment) {
        if (shipment.getStockOutcome() == null || shipment.getPointOutcome() == null) {
            shipmentRepository.save(shipment);
            return;
        }

        shipment.setUpdatedDate(LocalDateTime.now());
        List<OrderItem> items = orderItemRepository.findByShipmentId(shipment.getShipmentId());
        if (OUTCOME_RESERVED.equals(shipment.getStockOutcome()) && OUTCOME_DEDUCTED.equals(shipment.getPointOutcome())) {
            shipment.setShipmentStatus(STATUS_CONFIRMED);
            shipment.setConfirmedDate(LocalDateTime.now());
            items.forEach(i -> i.setItemStatus(STATUS_CONFIRMED));
        } else {
            shipment.setShipmentStatus(STATUS_CANCELLED);
            items.forEach(i -> {
                i.setItemStatus(STATUS_CANCELLED);
                i.setCancelReason(shipment.getCancelReason());
            });
            // 성공했던 반대편 레그 보상 - gift 재고복원/point 포인트복원(각자 멱등)
            orderSagaPublisher.publishShipmentCancelled(shipment, shipment.getCancelReason(),
                    items.stream().map(OrderItem::getOrderItemId).toList());
        }
        orderItemRepository.saveAll(items);
        shipmentRepository.save(shipment);
        aggregateHeader(shipment.getOrderId());
    }

    /**
     * 주문의 모든 출고가 종결되면 헤더 상태를 <b>품목 기준</b>으로 집계(선택지 A: 부분성공 허용).
     * 품목 단위 부분취소/반품까지 반영하려면 출고 상태가 아니라 품목 상태로 판정해야 한다
     * (한 출고 안에서 일부 품목만 취소되면 출고는 CONFIRMED로 남지만 주문은 부분확정).
     */
    private void aggregateHeader(String orderId) {
        List<Shipment> shipments = shipmentRepository.findByOrderId(orderId);
        boolean anyPending = shipments.stream()
                .anyMatch(s -> s.getShipmentStatus() == null || STATUS_PENDING.equals(s.getShipmentStatus()));
        if (anyPending) {
            return;
        }
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        boolean anyConfirmed = items.stream().anyMatch(i -> STATUS_CONFIRMED.equals(i.getItemStatus()));
        boolean anyCancelled = items.stream().anyMatch(i -> STATUS_CANCELLED.equals(i.getItemStatus()));
        String headerStatus = anyConfirmed && anyCancelled ? STATUS_PARTIAL
                : anyConfirmed ? STATUS_CONFIRMED : STATUS_CANCELLED;

        orderRepository.findById(orderId).ifPresent(order -> {
            order.setOrderStatus(headerStatus);
            order.setUpdatedDate(LocalDateTime.now());
            orderRepository.save(order);
        });
    }

    // ==================== 품목 단위 부분취소/반품 (Phase 5) ====================

    /**
     * 확정된 품목 한 건을 취소 처리하고 보상을 트리거한다(사용자 부분취소 또는 반품/교환 처리완료).
     * 품목을 CANCELLED로 바꾸고 {@code ITEM_CANCELLED}를 발행해 gift(그 품목 재고)·point(그 품목
     * 차감분)를 복원시킨 뒤, 출고(모든 품목 취소 시 CANCELLED)와 헤더 상태를 재집계한다.
     * 이미 취소된 품목이면 멱등적으로 무시한다.
     */
    @Transactional
    public void cancelItem(Long orderItemId, String reason) {
        OrderItem item = orderItemRepository.findById(orderItemId).orElse(null);
        if (item == null || STATUS_CANCELLED.equals(item.getItemStatus())) {
            return;
        }
        item.setItemStatus(STATUS_CANCELLED);
        item.setCancelReason(reason);
        item.setUpdatedDate(LocalDateTime.now());
        orderItemRepository.save(item);

        orderSagaPublisher.publishItemCancelled(item, reason);

        recomputeShipmentStatus(item.getShipmentId());
        aggregateHeader(item.getOrderId());
    }

    /** 출고 내 모든 품목이 취소되면 출고도 CANCELLED로 내린다(일부만 취소면 CONFIRMED 유지). */
    private void recomputeShipmentStatus(Long shipmentId) {
        if (shipmentId == null) {
            return;
        }
        List<OrderItem> items = orderItemRepository.findByShipmentId(shipmentId);
        boolean allCancelled = !items.isEmpty()
                && items.stream().allMatch(i -> STATUS_CANCELLED.equals(i.getItemStatus()));
        if (allCancelled) {
            shipmentRepository.findById(shipmentId).ifPresent(s -> {
                s.setShipmentStatus(STATUS_CANCELLED);
                s.setUpdatedDate(LocalDateTime.now());
                shipmentRepository.save(s);
            });
        }
    }

    private Shipment pendingShipmentOrNull(Long shipmentId) {
        Shipment shipment = shipmentRepository.findById(shipmentId).orElse(null);
        if (shipment == null) {
            log.warn("Received shipment saga event for unknown shipmentId={}", shipmentId);
            return null;
        }
        String status = shipment.getShipmentStatus();
        if (status != null && !STATUS_PENDING.equals(status)) {
            log.info("Shipment {} already resolved ({}) - ignoring late/duplicate event", shipmentId, status);
            return null;
        }
        return shipment;
    }

    private void appendCancelReason(Shipment shipment, String reason) {
        shipment.setCancelReason(shipment.getCancelReason() == null ? reason
                : shipment.getCancelReason() + "; " + reason);
    }

    // ==================== 헤더 상태 상수 노출 (읽기 경로/체크아웃 공유용) ====================
    public static final class Status {
        public static final String PENDING = STATUS_PENDING;
        public static final String CONFIRMED = STATUS_CONFIRMED;
        public static final String CANCELLED = STATUS_CANCELLED;
        public static final String PARTIALLY_CONFIRMED = STATUS_PARTIAL;

        private Status() {
        }
    }
}
