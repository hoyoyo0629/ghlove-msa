package com.ghlove.gift.web;

import com.ghlove.gift.domain.Gift;

/** Read-only JSON shape for other services (order) to look up current price/stock.
 * thumbnailPath is relative (e.g. "/uploads/xxx.jpg") - callers own prefixing it with
 * this service's base URL, since gift doesn't know its own externally-visible address. */
public record GiftItemDto(Long itemId, String itemName, Long sellerId, Integer salePrice,
                           Integer stockQuantity, String soldOut, String dataStatusCode,
                           String locgovCode, String thumbnailPath) {

    public static GiftItemDto from(Gift gift, String thumbnailImageName) {
        return new GiftItemDto(gift.getItemId(), gift.getItemName(), gift.getSellerId(), gift.getSalePrice(),
                gift.getStockQuantity(), gift.getSoldOut(), gift.getDataStatusCode(), gift.getLocgovCode(),
                thumbnailImageName != null ? "/uploads/" + thumbnailImageName : null);
    }
}
