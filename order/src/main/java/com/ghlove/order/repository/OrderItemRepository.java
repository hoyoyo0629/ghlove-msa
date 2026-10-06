package com.ghlove.order.repository;

import com.ghlove.order.domain.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** 품목(OD_ORDER_ITEM) 리포지토리. Phase 1(가산적 신설)에서는 아직 호출부가 없다. */
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByShipmentId(Long shipmentId);

    List<OrderItem> findByOrderId(String orderId);

    List<OrderItem> findByShipmentIdIn(List<Long> shipmentIds);
}
