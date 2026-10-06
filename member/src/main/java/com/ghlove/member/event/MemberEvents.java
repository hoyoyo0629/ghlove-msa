package com.ghlove.member.event;

import java.time.LocalDateTime;

/**
 * member.lifecycle 토픽에 실리는 회원/인증 확정사실 이벤트 (ISP p.403 이벤트스토밍:
 * MemberRegistered / LoginSucceeded / LoginFailed …). 서비스 경계를 넘어 Java 클래스를
 * 공유하지 않는 것이 이 프로젝트 규약이라(소비측 admin은 raw JSON을 직접 파싱한다) 여기의
 * record는 발행측 JSON 모양을 고정하는 용도다. eventType은 payload가 아니라 Kafka 헤더로
 * 나른다({@link MemberEventPublisher}).
 */
public final class MemberEvents {

    private MemberEvents() {
    }

    /** 회원가입 완료 (MemberRegistered). */
    public record MemberJoinedEvent(Long userId, String loginId, String loginPathCode,
                                    String sbscrbSeCode, LocalDateTime joinedAt) {
    }

    /** 로그인 성공 (LoginSucceeded). */
    public record LoginSucceededEvent(Long userId, String loginId, String remoteAddr, LocalDateTime at) {
    }

    /** 로그인 실패 (LoginFailed) - 존재하지 않는 아이디일 수 있어 userId가 없다. */
    public record LoginFailedEvent(String loginId, String remoteAddr, String reason, LocalDateTime at) {
    }

    /** 회원 탈퇴 (일반 탈퇴 + 디지털원패스/카카오 연동해지). */
    public record MemberWithdrawnEvent(Long userId, String leaveCode, LocalDateTime at) {
    }

    /** 휴면 전환 (SFR-002 휴면 배치). */
    public record MemberDormantEvent(Long userId, LocalDateTime at) {
    }
}
