package com.ghlove.member.service.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 외부 인증 연계에서 "사용자에게 그대로 보여줄 안내문"이 있는 실패.
 *
 * <p>AS-IS는 KakaoLinkController가 ApiError를 switch로 받아 화면 문구(errMsg)로 바꿨는데,
 * 그 문구들이 곧 실패 사유의 전부여서 여기서는 예외 메시지로 바로 들고 다닌다. 호출측(컨트롤러)은
 * getMessage()를 그대로 화면에 띄우면 된다 - 반대로 예상 못한 RuntimeException은 사용자에게
 * 보여줄 문구가 아니므로 일반 실패 문구로 감싼다.
 *
 * <p>인라인 catch 에서 삼켜져도 로그로 추적할 수 있도록 생성 시점에 WARN 으로 남긴다. 원인 예외가
 * 있는 경우(외부 시스템 오류)는 스택까지 함께 남겨 디버깅에 쓴다.
 */
public class ExternalAuthException extends RuntimeException {

    private static final Logger log = LoggerFactory.getLogger(ExternalAuthException.class);

    public ExternalAuthException(String message) {
        super(message);
        log.warn("[업무예외] ExternalAuthException: {}", message);
    }

    public ExternalAuthException(String message, Throwable cause) {
        super(message, cause);
        log.warn("[업무예외] ExternalAuthException: {}", message, cause);
    }
}
