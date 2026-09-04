package com.ghlove.gift.event;

/** SFR-010 민간개방 API 호출 통계용 - admin의 오픈API 통계 ReadModel이 구독한다. */
public record OpenApiUsageEvent(String consumerUsername, String service, String endpoint, String calledAt) {
}
