package com.ghlove.admin.event;

/** Local copy of gift service's event contract - must stay in sync with gift/src/.../event/GiftLifecycleEvent.java. */
public record GiftLifecycleEvent(Long itemId, Long sellerId, String categoryCode,
                                  String dataStatusCode, Integer salePrice, String locgovCode) {
}
