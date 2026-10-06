package com.ghlove.admin.service;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * FAQ 질문유형 - AS-IS {@code saleson.common.enumeration.FaqType}을 코드·문구·순서 그대로 옮긴 것이다.
 *
 * <p><b>이 enum이 정본이다</b>. AS-IS FAQ 화면들은 질문유형 목록을 공통코드가 아니라
 * {@code enumMapper.get("FaqType")}으로 가져온다 - 운영자 FAQ 관리(5104, {@code /opmanager/faq}),
 * 공개 FAQ 페이지({@code /faq/list.html}), 공개 API({@code /api/faq}) 모두 같다.
 * {@code op_common_code}의 {@code FAQ_TYPE} 1~6은 원제품(SalesOn) 쇼핑몰용 잔재라 끄여 두었다.
 *
 * <p><b>TO-BE 초기 시드가 코드를 바꿔 넣어 둔 것을 이번에 되돌렸다</b>: 시드는 같은 11종을
 * {@code JOIN}·{@code DONATE}·{@code POINT}… 라는 <b>자체 코드</b>로 만들어
 * {@code op_community_locgovfaq}(AS-IS에서는 중지된 11403 지자체FAQ의 표)에 넣고 공개화면을 거기에
 * 물려 두었다. 라벨은 아래 {@code title}과 11건 모두 똑같아서 코드만 1:1로 되돌렸다
 * (database/ddl/migration-admin-faq-5104.sql).
 */
public enum FaqType {

    F_LOGIN("회원가입/로그인"),
    F_CNTR_SYSTEM("기부하기"),
    F_CNTR_POINT("기부포인트"),
    F_OFF_CNTR("오프라인기부"),
    F_CNTR_DESIGNATED("특정사업기부"),
    F_API_PLATFORM("세액공제"),
    F_PRESENT_PURC("답례품"),
    F_ORDER("주문/배송"),
    F_OPEN("민간플랫폼"),
    F_SYSTEM("시스템"),
    F_ETC("기타");

    private final String title;

    FaqType(String title) {
        this.title = title;
    }

    public String getCode() {
        return name();
    }

    public String getTitle() {
        return title;
    }

    /** 코드 → 질문유형명. 모르는 코드는 코드를 그대로 보여준다(라벨이 비는 것보다 낫다). */
    public static String titleOf(String code) {
        return Arrays.stream(values())
                .filter(t -> t.name().equals(code))
                .map(FaqType::getTitle)
                .findFirst()
                .orElse(code == null ? "" : code);
    }

    /** 화면 셀렉트·API용 - 코드 → 질문유형명(enum 선언 순서 유지). */
    public static Map<String, String> options() {
        Map<String, String> options = new LinkedHashMap<>();
        for (FaqType type : values()) {
            options.put(type.name(), type.title);
        }
        return options;
    }
}
