package com.ghlove.admin.web.support;

import lombok.Getter;
import lombok.Setter;

/** AS-IS saleson.shop.pointcheck.support.PointCheckParam - 포인트사용 정합성검증 검색조건. */
@Getter
@Setter
public class PointCheckParam {

    /** 주문 번호 검색 - AS-IS는 orderCode 하나뿐이다(메서드명 option은 주석처리). */
    private String searchType = "orderCode";

    private String query;

    /** 정합여부 - 빈값(전체) / Y(금액일치) / N(금액불일치). */
    private String matchYnType;

    /** 주문일자 - AS-IS도 EXECUTION_DATE 하나뿐이다(라벨은 "주문일자"). */
    private String searchDateType = "EXECUTION_DATE";

    private String searchStartDate;

    private String searchEndDate;

    private int page = 1;

    /** 화면 출력수 - AS-IS는 10/50/100/200/500이다. */
    private int itemsPerPage = 10;
}
