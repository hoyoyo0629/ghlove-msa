package com.ghlove.gift.event;

/** 답례품 상태 전이(등록/승인/반려/판매중지/폐지)마다 발행 - admin의 답례품 통계 ReadModel용 (SFR-007/009). */
public record GiftLifecycleEvent(Long itemId, Long sellerId, String categoryCode,
                                  String dataStatusCode, Integer salePrice, String locgovCode) {
}
