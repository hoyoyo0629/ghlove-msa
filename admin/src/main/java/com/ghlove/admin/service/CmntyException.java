package com.ghlove.admin.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 커뮤니티(게시판) 도메인 업무예외 - AS-IS {@code ApiException}이 던지던 자리에 대응한다.
 * 컨트롤러가 인라인으로 잡아 {@code isSuccess=false} 응답으로 바꾸므로 삼켜진다 - 그래서
 * 생성 시점에 WARN 으로 한 번 남긴다(다른 도메인 예외와 같은 관례).
 */
public class CmntyException extends RuntimeException {
    private static final Logger log = LoggerFactory.getLogger(CmntyException.class);

    public CmntyException(String message) {
        super(message);
        log.warn("[업무예외] CmntyException: {}", message);
    }
}
