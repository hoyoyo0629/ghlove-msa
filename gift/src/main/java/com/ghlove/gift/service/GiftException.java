package com.ghlove.gift.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 답례품 도메인 업무예외. 컨트롤러 인라인 catch 에서 에러응답으로 변환되며 삼켜지므로, 생성 시점에
 * WARN 으로 한 번 남긴다(throw/catch 위치 무관 추적 + 미래 로직 자동 커버). 업무흐름이라 스택은 노이즈 - 메시지만.
 */
public class GiftException extends RuntimeException {
    private static final Logger log = LoggerFactory.getLogger(GiftException.class);

    public GiftException(String message) {
        super(message);
        log.warn("[업무예외] GiftException: {}", message);
    }
}
