package com.ghlove.admin.event;

/** gift 서비스의 OpenApiUsageEvent와 필드가 1:1로 대응해야 한다(Kafka 페이로드 역직렬화용). */
public record OpenApiUsageEvent(String consumerUsername, String service, String endpoint, String calledAt) {
}
