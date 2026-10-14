package com.ghlove.order.service;

import com.ghlove.order.domain.Order;
import com.ghlove.order.domain.OrderItem;
import com.ghlove.order.domain.Shipment;
import com.ghlove.order.event.OrderSagaPublisher;
import com.ghlove.order.event.ShipmentPointDeductedEvent;
import com.ghlove.order.repository.OrderItemRepository;
import com.ghlove.order.repository.OrderRepository;
import com.ghlove.order.repository.ShipmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 답례품 주문이 결제완료(출고 SAGA 확정)되면 AS-IS(온라인 saveGiveOrderTempAndPay·기탁서 procOffOrder)처럼
 * 국민비서 답례품 주문 문자용 이벤트를 한 번 발행하는지 - 취소로 끝나면 발행하지 않는지 확인한다.
 */
@ExtendWith(MockitoExtension.class)
class ShipmentSagaServiceOffgivePaidTest {

    @Mock
    private ShipmentRepository shipmentRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderSagaPublisher orderSagaPublisher;

    @InjectMocks
    private ShipmentSagaService shipmentSagaService;

    private Shipment shipment;
    private Order order;

    private OrderItem item(long id, String cntrSn) {
        OrderItem item = new OrderItem();
        item.setOrderItemId(id);
        item.setShipmentId(500L);
        item.setOrderId("K0000000123");
        item.setItemStatus("PENDING");
        item.setCntrSn(cntrSn);
        return item;
    }

    private void givenStockReservedShipment(List<OrderItem> items) {
        shipment = new Shipment();
        shipment.setShipmentId(500L);
        shipment.setOrderId("K0000000123");
        shipment.setShipmentStatus("PENDING");
        shipment.setStockOutcome("RESERVED");
        order = new Order();
        order.setOrderId("K0000000123");
        order.setUserId(77L);
        order.setOrderStatus("PENDING");
        when(shipmentRepository.findById(500L)).thenReturn(Optional.of(shipment));
        when(orderItemRepository.findByShipmentId(500L)).thenReturn(items);
        when(shipmentRepository.findByOrderId("K0000000123")).thenReturn(List.of(shipment));
        when(orderItemRepository.findByOrderId("K0000000123")).thenReturn(items);
        when(orderRepository.findById("K0000000123")).thenReturn(Optional.of(order));
    }

    @Test
    void publishesPresentOrderPaidOnceWhenOfflineOrderConfirms() {
        OrderItem second = item(12L, "2026101300001");
        OrderItem first = item(11L, "2026101300001");
        givenStockReservedShipment(List.of(second, first));

        shipmentSagaService.onPointDeducted(new ShipmentPointDeductedEvent(500L, "K0000000123", LocalDateTime.now()));

        assertThat(order.getOrderStatus()).isEqualTo("CONFIRMED");
        // AS-IS selectBuyItemList처럼 품목 순서대로 넘긴다
        verify(orderSagaPublisher).publishPresentOrderPaid(order, List.of(first, second));
    }

    @Test
    void publishesForOnlineOrderToo() {
        // AS-IS saveGiveOrderTempAndPay도 주문 직후 같은 문자를 보낸다
        OrderItem only = item(11L, null);
        givenStockReservedShipment(List.of(only));

        shipmentSagaService.onPointDeducted(new ShipmentPointDeductedEvent(500L, "K0000000123", LocalDateTime.now()));

        assertThat(order.getOrderStatus()).isEqualTo("CONFIRMED");
        verify(orderSagaPublisher).publishPresentOrderPaid(order, List.of(only));
    }

    @Test
    void doesNotPublishWhenShipmentCancelled() {
        givenStockReservedShipment(List.of(item(11L, null)));

        shipmentSagaService.onPointDeductFailed(
                new com.ghlove.order.event.ShipmentPointDeductFailedEvent(500L, "K0000000123", "포인트 부족", LocalDateTime.now()));

        assertThat(order.getOrderStatus()).isEqualTo("CANCELLED");
        verify(orderSagaPublisher, never()).publishPresentOrderPaid(any(), anyList());
    }
}
