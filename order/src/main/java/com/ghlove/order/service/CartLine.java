package com.ghlove.order.service;

/**
 * 장바구니 화면 한 줄 (AS-IS list-items 한 행). lineTotal = unitPrice * quantity.
 *
 * <p>deliveryFee는 답례품별 배송정책(GIFT_SHIPPING_TYPE)을 그대로 적용한 이 줄의 배송비이고
 * payable = lineTotal + deliveryFee 로, OrderService가 주문 시점에 저장하는 POINT_AMOUNT
 * (lineTotal - 쿠폰할인 + deliveryFee)와 같은 식이다 - 화면이 "무료배송"으로 하드코딩돼
 * 실제 차감액과 어긋나던 문제를 없애기 위해 서버가 계산해 내려준다. 쿠폰 할인은 주문결제
 * 화면에서 선택하므로 여기(장바구니)에서는 반영하지 않고, 선택 후 금액은
 * {@link CartService#previewCheckout}이 같은 계산기로 다시 내려준다.
 */
public record CartLine(Long cartItemId, Long itemId, String itemName, String thumbnailUrl,
                        Integer quantity, Integer unitPrice, long lineTotal,
                        long deliveryFee, long payable, String optionName, Integer optionPrice,
                        String textOption) {
}
