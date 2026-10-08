package com.ghlove.order.service;

import com.ghlove.order.domain.Order;
import com.ghlove.order.domain.OrderItem;
import com.ghlove.order.domain.Shipment;
import com.ghlove.order.event.OrderSagaPublisher;
import com.ghlove.order.repository.ClaimMemoRepository;
import com.ghlove.order.repository.ClaimRepository;
import com.ghlove.order.repository.OrderItemRepository;
import com.ghlove.order.repository.OrderRepository;
import com.ghlove.order.repository.ShipmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 관리자 수기 주문등록(AS-IS OrderAdminServiceImpl.insertOrderAdmin 대응) 회귀 테스트 -
 * "AB" 코드 생성, CONFIRMED 즉시 확정(SAGA PENDING 미경유), 품목/출고 금액 계산을 확인한다.
 */
@ExtendWith(MockitoExtension.class)
class OrderAdminServiceManualOrderTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private ShipmentRepository shipmentRepository;
    @Mock
    private ClaimRepository claimRepository;
    @Mock
    private ClaimMemoRepository claimMemoRepository;
    @Mock
    private OrderService orderService;
    @Mock
    private ClaimService claimService;
    @Mock
    private GiftClient giftClient;
    @Mock
    private OrderSagaPublisher orderSagaPublisher;

    @InjectMocks
    private OrderAdminService orderAdminService;

    @Test
    void createsConfirmedOrderWithAbCodeAndPublishesCreatedThenConfirmed() {
        when(orderRepository.nextAdminOrderSeqForDate(anyString())).thenReturn(1L);
        GiftItemInfo gift = new GiftItemInfo(5L, "답례품5", 100L, 10000, 50, "0", "APPROVED",
                "11110", "/uploads/5.jpg", "CJ", "1", 0, null, null, null, "Y", "N", null,
                null, null, null, null, null);
        when(giftClient.fetch(5L)).thenReturn(gift);

        // 실제 Hibernate save()는 전달한 엔티티에 생성된 ID를 바로 채워 넣는다 - 서비스 코드가
        // save() 반환값을 재할당하지 않고 같은 인스턴스를 그대로 쓰므로(CartService와 같은
        // 패턴) 목에서도 같은 동작을 흉내낸다.
        when(shipmentRepository.save(any(Shipment.class))).thenAnswer(invocation -> {
            Shipment s = invocation.getArgument(0);
            s.setShipmentId(900L);
            return s;
        });

        OrderAdminService.ManualOrderRequest request = new OrderAdminService.ManualOrderRequest(
                5L, null, 2, null, "홍길동", "01011112222",
                "김철수", "01033334444", "12345", "서울시 종로구", "101동 202호", "부재시 경비실");

        String orderId = orderAdminService.createManualOrder(request);

        assertThat(orderId).matches("AB\\d{14}");

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        Order saved = orderCaptor.getValue();
        assertThat(saved.getOrderId()).isEqualTo(orderId);
        assertThat(saved.getOrderStatus()).isEqualTo("CONFIRMED");
        assertThat(saved.getUserId()).isEqualTo(0L);
        assertThat(saved.getPointAmount()).isEqualTo(20000L);
        assertThat(saved.getAdminMemo()).contains("홍길동").contains("01011112222");

        ArgumentCaptor<OrderItem> itemCaptor = ArgumentCaptor.forClass(OrderItem.class);
        verify(orderItemRepository).save(itemCaptor.capture());
        assertThat(itemCaptor.getValue().getShipmentId()).isEqualTo(900L);
        assertThat(itemCaptor.getValue().getPointAmount()).isEqualTo(20000L);

        verify(orderSagaPublisher).publishCreated(saved);
        verify(orderSagaPublisher).publishConfirmed(saved);
    }
}
