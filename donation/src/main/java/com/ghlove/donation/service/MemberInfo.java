package com.ghlove.donation.service;

/** member 서비스 GET /api/users/{id} 응답 매핑. */
public record MemberInfo(Long userId, String userName, String birthday, String address, String loginId) {
}
