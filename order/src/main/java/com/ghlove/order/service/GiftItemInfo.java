package com.ghlove.order.service;

/** Response shape of gift service's read-only GET /api/gifts/{id} lookup.
 * thumbnailPath is relative to gift service (e.g. "/uploads/xxx.jpg") - callers must
 * prefix it with gift's base URL before rendering. */
public record GiftItemInfo(Long itemId, String itemName, Long sellerId, Integer salePrice,
                            Integer stockQuantity, String soldOut, String dataStatusCode,
                            String locgovCode, String thumbnailPath) {
}
