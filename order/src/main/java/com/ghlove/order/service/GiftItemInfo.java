package com.ghlove.order.service;

/** Response shape of gift service's read-only GET /api/gifts/{id} lookup.
 * thumbnailPath is relative to gift service (e.g. "/uploads/xxx.jpg") - callers must
 * prefix it with gift's base URL before rendering. shipping* fields are SFR-005
 * "배송비·택배사 설정" (재검토 라운드) - used by OrderService to compute deliveryFee. */
public record GiftItemInfo(Long itemId, String itemName, Long sellerId, Integer salePrice,
                            Integer stockQuantity, String soldOut, String dataStatusCode,
                            String locgovCode, String thumbnailPath, String deliveryCompanyName,
                            String shippingType, Integer shipping, Integer shippingFreeAmount,
                            Integer shippingExtraCharge1, Integer shippingExtraCharge2) {
}
