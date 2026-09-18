package com.ghlove.order.service;

import com.ghlove.order.domain.CartItem;
import com.ghlove.order.domain.Order;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 장바구니 회귀 테스트. 여기 있는 시나리오들은 전부 실제로 데이터가 깨지던 경로다 -
 * 체크아웃 부분 실패(유령 SAGA 이벤트), 화면 금액과 실제 차감액 불일치, 결제 버튼까지
 * 미뤄지던 재고 검증, 동시 담기 UNIQUE 충돌.
 */
@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    private static final Long USER_ID = 7L;
    private static final String LOCGOV_A = "11110";
    private static final String LOCGOV_B = "26110";

    @Mock
    private com.ghlove.order.repository.CartItemRepository cartItemRepository;
    @Mock
    private GiftClient giftClient;
    @Mock
    private LocgovClient locgovClient;
    @Mock
    private PointClient pointClient;
    @Mock
    private OrderService orderService;
    @Mock
    private CouponService couponService;

    @InjectMocks
    private CartService cartService;

    // ==================== 픽스처 ====================

    private static GiftItemInfo gift(Long itemId, int salePrice, int stock, String locgovCode) {
        return new GiftItemInfo(itemId, "답례품" + itemId, 100L, salePrice, stock, "0", "APPROVED",
                locgovCode, "/uploads/" + itemId + ".jpg", "CJ", "1", 0, null, null, null);
    }

    private static GiftItemInfo soldOut(Long itemId) {
        return new GiftItemInfo(itemId, "품절품" + itemId, 100L, 1000, 0, "1", "APPROVED",
                LOCGOV_A, null, "CJ", "1", 0, null, null, null);
    }

    private static CartItem cartItem(Long cartItemId, Long itemId, int quantity) {
        CartItem c = new CartItem();
        c.setCartItemId(cartItemId);
        c.setUserId(USER_ID);
        c.setItemId(itemId);
        c.setQuantity(quantity);
        return c;
    }

    private static Order order(String orderId) {
        Order o = new Order();
        o.setOrderId(orderId);
        return o;
    }

    // ==================== 담기 / 수량변경 ====================

    @Test
    @DisplayName("담기: 재고보다 많이 담으려 하면 결제까지 가기 전에 막는다")
    void addRejectsOverStock() {
        when(giftClient.fetch(1L)).thenReturn(gift(1L, 1000, 3, LOCGOV_A));
        when(cartItemRepository.findByUserIdAndItemId(USER_ID, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.add(USER_ID, 1L, 5))
                .isInstanceOf(OrderException.class)
                .hasMessageContaining("재고가 부족합니다");
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    @DisplayName("담기: 이미 담긴 수량과 합산해 재고를 초과하면 막는다")
    void addAccumulatesAndChecksStock() {
        when(giftClient.fetch(1L)).thenReturn(gift(1L, 1000, 5, LOCGOV_A));
        when(cartItemRepository.findByUserIdAndItemId(USER_ID, 1L)).thenReturn(Optional.of(cartItem(10L, 1L, 4)));

        assertThatThrownBy(() -> cartService.add(USER_ID, 1L, 2))
                .isInstanceOf(OrderException.class)
                .hasMessageContaining("재고가 부족합니다");
    }

    @Test
    @DisplayName("담기: 수량 상한(999)을 넘기면 막는다")
    void addRejectsAboveMaxQuantity() {
        when(giftClient.fetch(1L)).thenReturn(gift(1L, 1000, 100000, LOCGOV_A));
        when(cartItemRepository.findByUserIdAndItemId(USER_ID, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.add(USER_ID, 1L, CartService.MAX_QUANTITY + 1))
                .isInstanceOf(OrderException.class)
                .hasMessageContaining("최대 " + CartService.MAX_QUANTITY + "개");
    }

    @Test
    @DisplayName("담기: UNIQUE(USER_ID,ITEM_ID) 경합으로 저장이 실패하면 상대가 만든 행에 합산한다 (예전엔 500)")
    void addRetriesOnUniqueViolation() {
        when(giftClient.fetch(1L)).thenReturn(gift(1L, 1000, 50, LOCGOV_A));
        when(cartItemRepository.findByUserIdAndItemId(USER_ID, 1L))
                .thenReturn(Optional.empty())                          // 1차: 아직 아무도 안 담음
                .thenReturn(Optional.of(cartItem(10L, 1L, 1)));        // 2차: 동시 요청이 만들어 둔 행
        when(cartItemRepository.save(any(CartItem.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate key"))
                .thenAnswer(inv -> inv.getArgument(0));

        cartService.add(USER_ID, 1L, 2);

        ArgumentCaptor<CartItem> saved = ArgumentCaptor.forClass(CartItem.class);
        verify(cartItemRepository, times(2)).save(saved.capture());
        assertThat(saved.getValue().getQuantity()).isEqualTo(3); // 기존 1 + 요청 2
    }

    @Test
    @DisplayName("수량변경: 재고를 넘는 수량은 이 시점에 거절한다")
    void updateQuantityRejectsOverStock() {
        when(cartItemRepository.findById(10L)).thenReturn(Optional.of(cartItem(10L, 1L, 1)));
        when(giftClient.fetch(1L)).thenReturn(gift(1L, 1000, 2, LOCGOV_A));

        assertThatThrownBy(() -> cartService.updateQuantity(USER_ID, 10L, 3))
                .isInstanceOf(OrderException.class)
                .hasMessageContaining("재고가 부족합니다");
        verify(cartItemRepository, never()).save(any());
    }

    // ==================== 조회 ====================

    @Test
    @DisplayName("조회: 배송비를 반영한 payable을 내려주고, 품절 항목은 자동 삭제한다")
    void viewCarriesDeliveryFeeAndDropsSoldOut() {
        when(cartItemRepository.findByUserIdOrderByCreatedDateDesc(USER_ID))
                .thenReturn(List.of(cartItem(10L, 1L, 2), cartItem(11L, 2L, 1)));
        when(giftClient.fetch(1L)).thenReturn(gift(1L, 1000, 10, LOCGOV_A));
        when(giftClient.fetch(2L)).thenReturn(soldOut(2L));
        when(orderService.deliveryFeeOf(any(), eq(2), eq(2000L), isNull())).thenReturn(3000L);
        when(locgovClient.nameOf(LOCGOV_A)).thenReturn("서울 종로구");
        when(pointClient.balanceByLocgov(USER_ID, LOCGOV_A)).thenReturn(10000L);

        List<CartGroup> groups = cartService.view(USER_ID);

        assertThat(groups).hasSize(1);
        CartGroup group = groups.get(0);
        assertThat(group.groupTotal()).isEqualTo(2000L);
        assertThat(group.groupDeliveryFee()).isEqualTo(3000L);
        assertThat(group.groupPayable()).isEqualTo(5000L);
        assertThat(group.lines()).singleElement()
                .satisfies(l -> assertThat(l.payable()).isEqualTo(5000L));
        // "품절되면 자동으로 목록에서 삭제됩니다"
        verify(cartItemRepository).deleteByCartItemIdInAndUserId(List.of(11L), USER_ID);
    }

    @Test
    @DisplayName("선택조회: 장바구니 전체가 아니라 선택된 행만 조회한다")
    void viewSelectedQueriesOnlySelectedRows() {
        when(cartItemRepository.findByCartItemIdInAndUserIdOrderByCreatedDateDesc(List.of(10L), USER_ID))
                .thenReturn(List.of(cartItem(10L, 1L, 1)));
        when(giftClient.fetch(1L)).thenReturn(gift(1L, 1000, 10, LOCGOV_A));
        when(locgovClient.nameOf(LOCGOV_A)).thenReturn("서울 종로구");
        when(pointClient.balanceByLocgov(USER_ID, LOCGOV_A)).thenReturn(10000L);

        assertThat(cartService.viewSelected(USER_ID, List.of(10L))).hasSize(1);

        verify(cartItemRepository, never()).findByUserIdOrderByCreatedDateDesc(anyLong());
    }

    // ==================== 체크아웃 ====================

    private OrderService.DeliveryInfo delivery() {
        return new OrderService.DeliveryInfo("홍길동", "010-0000-0000", "서울시 종로구 1", "101호", null);
    }

    @Test
    @DisplayName("체크아웃: 뒤쪽 줄이 재고부족이면 주문을 단 한 건도 만들지 않는다 (부분 체크아웃/유령 이벤트 방지)")
    void checkoutCreatesNothingWhenAnyLineFails() {
        when(cartItemRepository.findByCartItemIdInAndUserIdOrderByCreatedDateDesc(List.of(10L, 11L), USER_ID))
                .thenReturn(List.of(cartItem(10L, 1L, 1), cartItem(11L, 2L, 5)));
        when(giftClient.fetch(1L)).thenReturn(gift(1L, 1000, 10, LOCGOV_A));
        when(giftClient.fetch(2L)).thenReturn(gift(2L, 1000, 2, LOCGOV_A));

        assertThatThrownBy(() -> cartService.checkout(USER_ID, List.of(10L, 11L), delivery(), Map.of()))
                .isInstanceOf(OrderException.class)
                .hasMessageContaining("재고가 부족합니다");

        verify(orderService, never()).createOrder(anyLong(), any(GiftItemInfo.class), anyInt(), any(), any());
        verify(cartItemRepository, never()).deleteByCartItemIdInAndUserId(any(), anyLong());
    }

    @Test
    @DisplayName("체크아웃: 같은 쿠폰을 두 줄에 고르면 주문을 만들기 전에 막는다")
    void checkoutRejectsSameCouponOnTwoLines() {
        when(cartItemRepository.findByCartItemIdInAndUserIdOrderByCreatedDateDesc(List.of(10L, 11L), USER_ID))
                .thenReturn(List.of(cartItem(10L, 1L, 1), cartItem(11L, 2L, 1)));
        when(giftClient.fetch(1L)).thenReturn(gift(1L, 1000, 10, LOCGOV_A));
        when(giftClient.fetch(2L)).thenReturn(gift(2L, 1000, 10, LOCGOV_A));
        when(couponService.previewDiscount(eq(USER_ID), eq(500), eq(1L), anyLong(), anyInt())).thenReturn(300L);

        assertThatThrownBy(() -> cartService.checkout(USER_ID, List.of(10L, 11L), delivery(),
                Map.of(10L, 500, 11L, 500)))
                .isInstanceOf(OrderException.class)
                .hasMessageContaining("같은 쿠폰을 여러 답례품에 사용할 수 없습니다");

        verify(orderService, never()).createOrder(anyLong(), any(GiftItemInfo.class), anyInt(), any(), any());
    }

    @Test
    @DisplayName("체크아웃: 선택한 행 중 장바구니에 없는 게 있으면 아무것도 주문하지 않는다")
    void checkoutRejectsUnknownCartItem() {
        when(cartItemRepository.findByCartItemIdInAndUserIdOrderByCreatedDateDesc(List.of(10L, 11L), USER_ID))
                .thenReturn(List.of(cartItem(10L, 1L, 1)));

        assertThatThrownBy(() -> cartService.checkout(USER_ID, List.of(10L, 11L), delivery(), Map.of()))
                .isInstanceOf(OrderException.class)
                .hasMessageContaining("찾을 수 없는 답례품");

        verify(orderService, never()).createOrder(anyLong(), any(GiftItemInfo.class), anyInt(), any(), any());
    }

    @Test
    @DisplayName("체크아웃: 포인트 부족 판정에 배송비를 포함한다 (답례품 포인트만으로는 충분해도 막는다)")
    void checkoutCountsDeliveryFeeAgainstPointBalance() {
        when(cartItemRepository.findByCartItemIdInAndUserIdOrderByCreatedDateDesc(List.of(10L), USER_ID))
                .thenReturn(List.of(cartItem(10L, 1L, 1)));
        when(giftClient.fetch(1L)).thenReturn(gift(1L, 1000, 10, LOCGOV_A));
        when(orderService.deliveryFeeOf(any(), eq(1), eq(1000L), anyString())).thenReturn(3000L);
        when(pointClient.balanceByLocgov(USER_ID, LOCGOV_A)).thenReturn(2000L); // 1000P 상품엔 충분, 배송비 포함 4000P엔 부족
        when(locgovClient.nameOf(LOCGOV_A)).thenReturn("서울 종로구");

        assertThatThrownBy(() -> cartService.checkout(USER_ID, List.of(10L), delivery(), Map.of()))
                .isInstanceOf(OrderException.class)
                .hasMessageContaining("포인트가 부족합니다");

        verify(orderService, never()).createOrder(anyLong(), any(GiftItemInfo.class), anyInt(), any(), any());
    }

    @Test
    @DisplayName("체크아웃: 성공하면 지자체별로 검증 후 줄마다 주문을 만들고 장바구니를 비운다")
    void checkoutCreatesOnePerLineAndClearsCart() {
        when(cartItemRepository.findByCartItemIdInAndUserIdOrderByCreatedDateDesc(List.of(10L, 11L), USER_ID))
                .thenReturn(List.of(cartItem(10L, 1L, 1), cartItem(11L, 2L, 2)));
        when(giftClient.fetch(1L)).thenReturn(gift(1L, 1000, 10, LOCGOV_A));
        when(giftClient.fetch(2L)).thenReturn(gift(2L, 500, 10, LOCGOV_B));
        when(pointClient.balanceByLocgov(USER_ID, LOCGOV_A)).thenReturn(100000L);
        when(pointClient.balanceByLocgov(USER_ID, LOCGOV_B)).thenReturn(100000L);
        when(orderService.createOrder(eq(USER_ID), any(GiftItemInfo.class), anyInt(), any(), isNull()))
                .thenReturn(order("O1"), order("O2"));

        List<String> orderIds = cartService.checkout(USER_ID, List.of(10L, 11L), delivery(), Map.of());

        assertThat(orderIds).containsExactly("O1", "O2");
        // 답례품을 이미 조회해 뒀으므로 createOrder가 gift를 다시 조회하지 않는 오버로드로 호출된다.
        verify(orderService, times(2)).createOrder(eq(USER_ID), any(GiftItemInfo.class), anyInt(), any(), isNull());
        verify(giftClient, times(1)).fetch(1L);
        verify(cartItemRepository).deleteByCartItemIdInAndUserId(List.of(10L, 11L), USER_ID);
    }

    // ==================== 금액 미리보기 ====================

    @Test
    @DisplayName("미리보기: 결제와 똑같은 계산으로 쿠폰할인·배송비를 반영한 금액을 내려준다")
    void quoteMatchesCheckoutMath() {
        when(cartItemRepository.findByCartItemIdInAndUserIdOrderByCreatedDateDesc(List.of(10L), USER_ID))
                .thenReturn(List.of(cartItem(10L, 1L, 2)));
        when(giftClient.fetch(1L)).thenReturn(gift(1L, 1000, 10, LOCGOV_A));
        when(couponService.previewDiscount(USER_ID, 500, 1L, 2000L, 2)).thenReturn(500L);
        when(orderService.deliveryFeeOf(any(), eq(2), eq(2000L), eq("제주시 1"))).thenReturn(3000L);
        when(locgovClient.nameOf(LOCGOV_A)).thenReturn("서울 종로구");
        when(pointClient.balanceByLocgov(USER_ID, LOCGOV_A)).thenReturn(100000L);

        var quote = cartService.quote(USER_ID, List.of(10L), Map.of(10L, 500), "제주시 1");

        assertThat(quote.totalPoint()).isEqualTo(2000L);
        assertThat(quote.totalDiscount()).isEqualTo(500L);
        assertThat(quote.totalDeliveryFee()).isEqualTo(3000L);
        // OrderService가 POINT_AMOUNT에 저장하는 식과 동일: lineTotal - discount + deliveryFee
        assertThat(quote.totalPayable()).isEqualTo(4500L);
        assertThat(quote.groups()).singleElement()
                .satisfies(g -> assertThat(g.groupPayable()).isEqualTo(4500L));
    }
}
