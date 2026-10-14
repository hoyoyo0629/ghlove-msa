package com.ghlove.admin.web.support;

import org.springframework.stereotype.Component;

/**
 * AS-IS가 운영관리 화면 날짜에 쓰는 포맷을 재현하는 뷰 헬퍼. AS-IS는 날짜를 저장은
 * {@code yyyyMMddHHmmss}(VARCHAR(14) / *_PNTTM)로 하고, 화면에는 매퍼의
 * {@code DATE_FORMAT(col,'%Y-%m-%d')}(날짜) 또는 {@code '%Y-%m-%d %H:%i:%s'}(일시)로
 * 바꿔 내려주거나 EL 함수 {@code op:date()}로 포맷한다. TO-BE는 그 값을 원시 문자열로 들고
 * 있어 화면에서 포맷해야 한다 - 각 템플릿이 substring을 제각각 쓰던 것을 이 하나로 통일한다.
 *
 * <p>템플릿에서 {@code ${@opDate.ymd(값)}} / {@code ${@opDate.ymdHms(값)}}로 호출한다.
 * 이미 '-'가 들어간(이미 포맷된) 값이나 비어있는 값은 그대로/빈 문자열로 돌려줘 멱등하다.
 */
@Component("opDate")
public class OpDate {

    /** yyyyMMddHHmmss(또는 앞 8자리 이상) → yyyy-MM-dd. */
    public String ymd(String raw) {
        String d = digits(raw);
        if (d.length() >= 8) {
            return d.substring(0, 4) + "-" + d.substring(4, 6) + "-" + d.substring(6, 8);
        }
        return fallback(raw);
    }

    /** yyyyMMddHHmmss → yyyy-MM-dd HH:mm:ss (자리수가 모자라면 있는 만큼만). */
    public String ymdHms(String raw) {
        String d = digits(raw);
        if (d.length() >= 14) {
            return d.substring(0, 4) + "-" + d.substring(4, 6) + "-" + d.substring(6, 8)
                    + " " + d.substring(8, 10) + ":" + d.substring(10, 12) + ":" + d.substring(12, 14);
        }
        if (d.length() >= 12) {
            return d.substring(0, 4) + "-" + d.substring(4, 6) + "-" + d.substring(6, 8)
                    + " " + d.substring(8, 10) + ":" + d.substring(10, 12);
        }
        return ymd(raw);
    }

    /**
     * yyyyMM → yyyy-MM. 월별 통계의 "일자" 칸처럼 6자리로 오는 값에 쓴다
     * (AS-IS {@code op:date} 가 월별 화면에서 받는 값이 6자리다).
     * 8자리가 들어오면 {@link #ymd(String)} 로 넘긴다.
     */
    public String ym(String raw) {
        String d = digits(raw);
        if (d.length() >= 8) {
            return ymd(raw);
        }
        if (d.length() >= 6) {
            return d.substring(0, 4) + "-" + d.substring(4, 6);
        }
        return fallback(raw);
    }

    /**
     * yyyyMM → 그 달 <b>말일</b>의 yyyyMMdd (예: 202610 → 20261031).
     *
     * <p>월별 통계에서 일자별 화면으로 드릴다운할 때 쓴다. AS-IS는 이 자리에
     * {@code searchDate + "99"} 를 넘기는데(문자열 비교 상한으로는 동작한다) 받는 화면의
     * 기간 입력칸에 <b>20261099 같은 없는 날짜가 그대로 찍힌다</b> - 사용자 지적(2026-10-13)으로
     * 실제 말일을 넘기도록 바꿨다. 조회 범위는 둘 다 같다.
     */
    public String monthEnd(String raw) {
        String d = digits(raw);
        if (d.length() < 6) {
            return fallback(raw);
        }
        try {
            java.time.YearMonth ym = java.time.YearMonth.of(
                    Integer.parseInt(d.substring(0, 4)), Integer.parseInt(d.substring(4, 6)));
            return d.substring(0, 6) + String.format("%02d", ym.lengthOfMonth());
        } catch (RuntimeException e) {
            return fallback(raw);
        }
    }

    private static String digits(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.replaceAll("[^0-9]", "");
    }

    /** 숫자 8자리를 못 채우면(이미 'yyyy-MM-dd' 같은 포맷이거나 빈 값) 원본을 그대로 둔다. */
    private static String fallback(String raw) {
        return raw == null ? "" : raw.trim();
    }
}
