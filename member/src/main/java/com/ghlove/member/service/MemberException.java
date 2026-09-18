package com.ghlove.member.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 회원 도메인 업무예외. 컨트롤러의 인라인 catch 에서 에러응답으로 변환되며 삼켜지므로,
 * 생성 시점에 WARN 으로 한 번 남긴다. 이렇게 해두면 throw/catch 위치와 무관하게
 * "어떤 업무규칙에 걸렸는지"를 로그만 보고도 추적할 수 있고, 앞으로 추가되는 로직도
 * throw new MemberException(...) 만으로 자동으로 로그에 남는다.
 * (버그가 아닌 업무흐름이라 스택트레이스는 노이즈 - 메시지만 남긴다)
 */
public class MemberException extends RuntimeException {
    private static final Logger log = LoggerFactory.getLogger(MemberException.class);

    public MemberException(String message) {
        super(message);
        log.warn("[업무예외] MemberException: {}", message);
    }
}
