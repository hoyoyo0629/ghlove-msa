package com.ghlove.admin.web.support;

import org.springframework.stereotype.Component;

/**
 * AS-IS EL 함수 {@code op:strcut}({@code com.onlinepowers.framework.util.StringUtils.strcut})
 * 재현 - 목록 화면에서 긴 텍스트를 N자로 잘라 보여주고 전체 텍스트는 title 툴팁으로 둔다.
 *
 * <p>프레임워크 원본 소스는 이 저장소에 없다(별도 {@code opframework} 모듈, build.gradle에
 * {@code includeBuild}가 주석처리돼 빠져 있음) - 정확한 바이트/자리수 처리까지는 확인 못했고,
 * 가장 흔한 "N자 초과 시 잘라서 '...' 붙이기" 규칙으로 재현했다.
 */
@Component("strcut")
public class TextCut {

    public String cut(String value, int maxLen) {
        if (value == null) {
            return "";
        }
        return value.length() > maxLen ? value.substring(0, maxLen) + "..." : value;
    }
}
