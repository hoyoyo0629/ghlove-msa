package com.ghlove.admin.web.support;

import lombok.Getter;
import lombok.Setter;

/**
 * 지자체관리(메뉴 4401) 목록 검색조건 - AS-IS
 * {@code saleson.shop.user.support.LocgovSearchParam} 중 list.jsp가 보내는 항목.
 *
 * <p>AS-IS는 이 화면에서 등록일 범위에 <b>기본값을 넣지 않는다</b>(담당자 화면들과 다르다 -
 * 비워 두면 전체 기간이고, 날짜버튼에 '전체'도 없다). 그래서 {@code applyDefaults()}가
 * 페이지당 건수만 채운다.
 */
@Getter
@Setter
public class LocgovSearchParam {

    /** 검색구분 - LOCGOV_NM(지자체명)/CHARGER_NM(담당자명)/CHARGER_CTTPC(연락처). */
    private String srchKey;

    private String srchValue;

    /** 등록일 범위(yyyyMMdd) - AS-IS는 기본값 없음. */
    private String srchStartCreated;

    private String srchEndCreated;

    private int page = 1;

    private int itemsPerPage = 10;

    /** AS-IS 폼이 hidden으로 들고 다니는 값들. */
    private String sort;
    private String orderBy;
    private String query;

    public void applyDefaults() {
        if (itemsPerPage <= 0) {
            itemsPerPage = Pagination.DEFAULT_ITEMS_PER_PAGE;
        }
    }

    /** 검색구분이 지자체명일 때 donation API에 보낼 값(그 외에는 null). */
    public String locgovNmQuery() {
        return "LOCGOV_NM".equals(effectiveSrchKey()) ? blankToNull(srchValue) : null;
    }

    public String chargerNmQuery() {
        return "CHARGER_NM".equals(effectiveSrchKey()) ? blankToNull(srchValue) : null;
    }

    public String chargerCttpcQuery() {
        return "CHARGER_CTTPC".equals(effectiveSrchKey()) ? blankToNull(srchValue) : null;
    }

    /** AS-IS select의 첫 option이 지자체명이라 고르지 않으면 그걸로 본다. */
    private String effectiveSrchKey() {
        return (srchKey == null || srchKey.isBlank()) ? "LOCGOV_NM" : srchKey;
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
