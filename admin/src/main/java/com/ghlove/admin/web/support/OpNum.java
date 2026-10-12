package com.ghlove.admin.web.support;

import org.springframework.stereotype.Component;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * AS-IS 운영관리 화면의 EL 함수 {@code op:numberFormat()}을 재현하는 뷰 헬퍼.
 *
 * <p>AS-IS 구현은 opframework-3.15.0.jar 의
 * {@code com.onlinepowers.framework.util.StringUtils#numberFormat} 이고, 디컴파일해 보면
 * {@code NumberFormat.getNumberInstance().format(값)} 한 줄이다(예외가 나면 원본 toString).
 * 즉 천단위 콤마가 붙고 <b>소수점은 최대 3자리까지만</b> 찍히며, 소수부가 0이면 아예 생략된다
 * ({@code 0.0 → "0"}, {@code 0.61 → "0.61"}, {@code 1234.5 → "1,234.5"}).
 *
 * <p>정수에는 {@code #numbers.formatInteger(v,0,'COMMA')}로 충분하지만, 달성율처럼 소수가 섞인
 * 값은 Thymeleaf의 {@code #numbers.formatDecimal}이 소수 자리수를 고정(0.61 → "0.610")해 버려
 * AS-IS와 어긋난다. 그래서 AS-IS와 같은 포맷터를 그대로 쓴다.
 *
 * <p>템플릿에서 {@code ${@opNum.format(값)}}으로 호출한다.
 */
@Component("opNum")
public class OpNum {

    /** AS-IS op:numberFormat - 천단위 콤마 + 소수점 최대 3자리(소수부 0이면 생략). */
    public String format(Number value) {
        if (value == null) {
            return "";
        }
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.KOREA);
        return nf.format(value);
    }
}
