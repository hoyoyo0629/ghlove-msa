package com.ghlove.point.event;

/**
 * member 서비스가 member.lifecycle 토픽으로 발행하는 회원가입완료 이벤트 (MemberRegistered).
 * point는 서비스 경계를 넘어 Java 클래스를 공유하지 않으므로 필요한 필드(userId)만 선언한다 -
 * loginPathCode/sbscrbSeCode/joinedAt 등 나머지 필드는 Jackson이 무시한다.
 */
public record MemberJoinedEvent(Long userId) {
}
