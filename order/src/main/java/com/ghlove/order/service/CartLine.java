package com.ghlove.order.service;

/** 장바구니 화면 한 줄 (AS-IS list-items 한 행). lineTotal = unitPrice * quantity. */
public record CartLine(Long cartItemId, Long itemId, String itemName, String thumbnailUrl,
                        Integer quantity, Integer unitPrice, long lineTotal) {
}
