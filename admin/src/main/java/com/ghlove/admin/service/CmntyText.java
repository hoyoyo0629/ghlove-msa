package com.ghlove.admin.service;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * 커뮤니티 게시판 본문 문자열 처리 - 게시판 5종이 똑같이 쓰는 두 가지만 모아 둔 것이다.
 *
 * <ul>
 *   <li>{@link #decode(String)} - AS-IS는 스마트에디터 본문을 {@code encodeURIComponent}로 보내고
 *       서버에서 {@code URLDecoder}(소통방) 또는 {@code ShopUtils.getStringParamDecodeUtf8}(그 외)로
 *       되돌린다.</li>
 *   <li>{@link #nl2br(String)} - AS-IS JSP의 {@code op:nl2br}. 본문은 에디터 HTML이라 그대로
 *       렌더링하고 줄바꿈만 {@code <br/>}로 바꾼다.</li>
 * </ul>
 */
public final class CmntyText {

    private CmntyText() {
    }

    /**
     * URL 디코딩. 인코딩되지 않은 본문이 오면 AS-IS는 예외로 터지지만, 여기서는 원문을 쓴다
     * (보이는 결과가 같고 작성한 글이 사라지지 않는다).
     */
    public static String decode(String value) {
        if (value == null) {
            return null;
        }
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return value;
        }
    }

    /** AS-IS {@code op:nl2br} - CR/LF 조합을 한 번만 바꾼다. */
    public static String nl2br(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\r\n", "<br/>").replace("\r", "<br/>").replace("\n", "<br/>");
    }
}
