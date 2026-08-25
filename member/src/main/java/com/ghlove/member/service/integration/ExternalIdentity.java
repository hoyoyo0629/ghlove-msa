package com.ghlove.member.service.integration;

/** 외부 연계로 확인된 신원 정보 (디지털원패스 CI 또는 카카오/네이버 SNS 프로필). */
public record ExternalIdentity(String provider, String externalId, String name, String email, String birthday) {
}
