package com.ghlove.order.service;

import com.ghlove.order.domain.Order;
import com.ghlove.order.domain.OrderItem;
import com.ghlove.order.domain.Shipment;
import com.ghlove.order.event.OrderSagaPublisher;
import com.ghlove.order.repository.OrderItemRepository;
import com.ghlove.order.repository.OrderRepository;
import com.ghlove.order.repository.ShipmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * 기탁서 등록 답례품 주문(AS-IS saveOffGiveOrder + procOffOrder) 회귀 테스트 - 포인트를 보지 않고
 * 입금대기로 만들고(재고·포인트 미차감, SAGA 미발행), 기부 포인트 적립 뒤에야 출고 SAGA를 개시하는지 확인한다.
 */
@ExtendWith(MockitoExtension.class)
class OffgiveOrderServiceTest {

    @Mock
    private GiftClient giftClient;
    @Mock
    private OrderService orderService;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ShipmentRepository shipmentRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private OrderSagaPublisher orderSagaPublisher;
    @Mock
    private OffgiveDonationClient offgiveDonationClient;

    @InjectMocks
    private OffgiveOrderService offgiveOrderService;

    private static GiftItemInfo gift(long itemId, int salePrice, int stock, String shippingType, int shipping) {
        return gift(itemId, salePrice, stock, shippingType, shipping, "N");
    }

    private static GiftItemInfo gift(long itemId, int salePrice, int stock, String shippingType, int shipping,
                                     String mobileItemYn) {
        return new GiftItemInfo(itemId, "답례품" + itemId, 9001L, salePrice, stock, "0", "1",
                "11230", null, "CJ", shippingType, shipping, null, null, null, "Y", mobileItemYn, null,
                null, null, null, null, null);
    }

    private static OffgiveOrderService.OffgiveOrderRequest request(List<OffgiveOrderService.GiftLine> lines,
                                                                   String receiverName) {
        return new OffgiveOrderService.OffgiveOrderRequest(77L, "2026101300001", lines, receiverName,
                "010-1111-2222", "서울특별시 강남구 테헤란로 1", "101호", "06236");
    }

    @Test
    void createsWaitingDepositOrderWithoutPointCheckOrSaga() {
        when(giftClient.fetch(1000L)).thenReturn(gift(1000L, 80000, 46, "6", 3000));
        when(giftClient.fetch(1020L)).thenReturn(gift(1020L, 30000, 50, "1", 0));
        when(orderService.islandTypeOf("06236")).thenReturn("");
        when(orderService.generateOrderId()).thenReturn("K0000000123");
        when(shipmentRepository.save(any(Shipment.class))).thenAnswer(invocation -> {
            Shipment s = invocation.getArgument(0);
            s.setShipmentId(500L);
            return s;
        });

        String orderId = offgiveOrderService.create(request(List.of(
                new OffgiveOrderService.GiftLine(1000L, null, 1),
                new OffgiveOrderService.GiftLine(1020L, null, 2)), "김수령"));

        assertThat(orderId).isEqualTo("K0000000123");

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        Order header = orderCaptor.getValue();
        assertThat(header.getOrderStatus()).isEqualTo("WAITING_DEPOSIT");
        // AS-IS getGivePayOrder: SUM(SALE_PRICE*QUANTITY) - 배송비는 기록만 하고 차감액엔 넣지 않는다
        assertThat(header.getPointAmount()).isEqualTo(80000L + 60000L);
        assertThat(header.getDeliveryFee()).isEqualTo(3000L);
        assertThat(header.getItemName()).isEqualTo("답례품1000 외 1건");

        ArgumentCaptor<Shipment> shipmentCaptor = ArgumentCaptor.forClass(Shipment.class);
        verify(shipmentRepository).save(shipmentCaptor.capture());
        assertThat(shipmentCaptor.getValue().getShipmentStatus()).isEqualTo("WAITING_DEPOSIT");
        assertThat(shipmentCaptor.getValue().getPointAmount()).isEqualTo(140000L);

        ArgumentCaptor<OrderItem> itemCaptor = ArgumentCaptor.forClass(OrderItem.class);
        verify(orderItemRepository, times(2)).save(itemCaptor.capture());
        assertThat(itemCaptor.getAllValues()).allSatisfy(item -> {
            assertThat(item.getItemStatus()).isEqualTo("WAITING_DEPOSIT");
            assertThat(item.getCntrSn()).isEqualTo("2026101300001");
            assertThat(item.getOffProcYn()).isEqualTo("N");
            assertThat(item.getShipmentId()).isEqualTo(500L);
        });

        // 등록 시점엔 재고·포인트를 건드리지 않는다(SAGA 미발행)
        verifyNoInteractions(orderSagaPublisher);
    }

    @Test
    void rejectsMissingReceiverNameLikeAsIs0331() {
        when(giftClient.fetch(1000L)).thenReturn(gift(1000L, 80000, 46, "6", 3000));

        assertThatThrownBy(() -> offgiveOrderService.create(request(List.of(
                new OffgiveOrderService.GiftLine(1000L, null, 1)), " ")))
                .isInstanceOf(OrderException.class)
                .hasMessage("받으시는분 이름을 입력해주세요.");
        verify(orderRepository, never()).save(any());
    }

    @Test
    void rejectsWhenSameItemLinesExceedStock() {
        when(giftClient.fetch(1000L)).thenReturn(gift(1000L, 80000, 2, "6", 3000));

        assertThatThrownBy(() -> offgiveOrderService.create(request(List.of(
                new OffgiveOrderService.GiftLine(1000L, null, 2),
                new OffgiveOrderService.GiftLine(1000L, null, 1)), "김수령")))
                .isInstanceOf(OrderException.class)
                .hasMessage("상품의 재고가 없습니다.");
    }

    @Test
    void releasesWaitingShipmentOnlyOnceAfterDonationPointCredited() {
        OrderItem item = new OrderItem();
        item.setOrderItemId(1L);
        item.setShipmentId(500L);
        item.setOrderId("K0000000123");
        item.setItemStatus("WAITING_DEPOSIT");
        item.setCntrSn("2026101300001");
        item.setOffProcYn("N");
        Shipment shipment = new Shipment();
        shipment.setShipmentId(500L);
        shipment.setOrderId("K0000000123");
        shipment.setShipmentStatus("WAITING_DEPOSIT");
        Order order = new Order();
        order.setOrderId("K0000000123");
        order.setUserId(77L);
        order.setOrderStatus("WAITING_DEPOSIT");

        when(orderItemRepository.findByCntrSnAndOffProcYn("2026101300001", "N")).thenReturn(List.of(item));
        when(shipmentRepository.findById(500L)).thenReturn(Optional.of(shipment));
        when(orderRepository.findById("K0000000123")).thenReturn(Optional.of(order));

        offgiveOrderService.onDonationPointCredited("2026101300001");

        assertThat(item.getOffProcYn()).isEqualTo("Y");
        assertThat(item.getItemStatus()).isEqualTo("PENDING");
        assertThat(shipment.getShipmentStatus()).isEqualTo("PENDING");
        assertThat(order.getOrderStatus()).isEqualTo("PENDING");
        verify(orderSagaPublisher).publishShipmentCreated(shipment, 77L, List.of(item));

        // 같은 적립 이벤트가 다시 와도(OFF_PROC_YN='Y') 다시 발행하지 않는다
        when(orderItemRepository.findByCntrSnAndOffProcYn("2026101300001", "N")).thenReturn(List.of());
        offgiveOrderService.onDonationPointCredited("2026101300001");
        verify(orderSagaPublisher, times(1)).publishShipmentCreated(any(), any(), anyList());
    }

    @Test
    void splitsMobileItemIntoQuantityOneLinesLikeAsIs() {
        when(giftClient.fetch(1030L)).thenReturn(gift(1030L, 10000, 50, "1", 0, "Y"));
        when(orderService.islandTypeOf("06236")).thenReturn("");
        when(orderService.generateOrderId()).thenReturn("K0000000124");
        when(shipmentRepository.save(any(Shipment.class))).thenAnswer(invocation -> {
            Shipment s = invocation.getArgument(0);
            s.setShipmentId(501L);
            return s;
        });

        offgiveOrderService.create(request(List.of(new OffgiveOrderService.GiftLine(1030L, null, 3)), "김수령"));

        // 수량 3인 모바일 답례품 → 수량 1짜리 3줄(원본 1 + 복제 2), 합계 포인트는 그대로
        ArgumentCaptor<OrderItem> itemCaptor = ArgumentCaptor.forClass(OrderItem.class);
        verify(orderItemRepository, times(3)).save(itemCaptor.capture());
        assertThat(itemCaptor.getAllValues()).allSatisfy(item -> {
            assertThat(item.getItemId()).isEqualTo(1030L);
            assertThat(item.getQuantity()).isEqualTo(1);
            assertThat(item.getPointAmount()).isEqualTo(10000L);
        });
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        assertThat(orderCaptor.getValue().getPointAmount()).isEqualTo(30000L);
        assertThat(orderCaptor.getValue().getQuantity()).isEqualTo(3);
    }

    @Test
    void cancelsWaitingItemsOfDeletedUnpaidDonationLikeAsIsItemStatusUpdateBatch() {
        java.time.LocalDate today = java.time.LocalDate.of(2026, 10, 13);
        OrderItem item = new OrderItem();
        item.setOrderItemId(2L);
        item.setShipmentId(600L);
        item.setOrderId("K0000000200");
        item.setItemStatus("WAITING_DEPOSIT");
        item.setCntrSn("2026090100001");
        Shipment shipment = new Shipment();
        shipment.setShipmentId(600L);
        shipment.setShipmentStatus("WAITING_DEPOSIT");
        Order order = new Order();
        order.setOrderId("K0000000200");
        order.setOrderStatus("WAITING_DEPOSIT");

        // AS-IS selectOffItem: 90일 전 00:00:00 ~ 10일 전 23:59:59 (= 9일 전 00:00 미만)
        when(orderItemRepository.findCntrSnsByItemStatusCreatedBetween("WAITING_DEPOSIT",
                java.time.LocalDateTime.of(2026, 7, 15, 0, 0), java.time.LocalDateTime.of(2026, 10, 4, 0, 0)))
                .thenReturn(List.of("2026090100001", "2026090100002"));
        // 두 번째 기부는 삭제되지 않았다(수납됐거나 아직 유효) → 건드리지 않는다
        when(offgiveDonationClient.deletedUnpaidOffline(List.of("2026090100001", "2026090100002")))
                .thenReturn(List.of("2026090100001"));
        when(orderItemRepository.findByCntrSnAndItemStatus("2026090100001", "WAITING_DEPOSIT")).thenReturn(List.of(item));
        when(orderItemRepository.findByShipmentId(600L)).thenReturn(List.of(item));
        when(orderItemRepository.findByOrderId("K0000000200")).thenReturn(List.of(item));
        when(shipmentRepository.findById(600L)).thenReturn(Optional.of(shipment));
        when(orderRepository.findById("K0000000200")).thenReturn(Optional.of(order));

        int cancelled = offgiveOrderService.cancelUnpaidDeleted(today);

        assertThat(cancelled).isEqualTo(1);
        assertThat(item.getItemStatus()).isEqualTo("CANCELLED");
        assertThat(shipment.getShipmentStatus()).isEqualTo("CANCELLED");
        assertThat(order.getOrderStatus()).isEqualTo("CANCELLED");
        verify(orderItemRepository, never()).findByCntrSnAndItemStatus("2026090100002", "WAITING_DEPOSIT");
        // 차감된 게 없으니 보상 이벤트도 없다
        verifyNoInteractions(orderSagaPublisher);
    }
}
