package com.ghlove.admin.web.support;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * AS-IS saleson.shop.user.support.PersonInChargeSearchParam - 담당자 관리 화면
 * (운영관리자 4501 / 지자체담당자관리 4402) 공통 검색조건.
 */
@Getter
@Setter
public class PersonInChargeSearchParam {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyyMMdd");

    /** 검색구분 - 4501: LOGIN_ID/USER_NAME/PHONE_NUMBER, 4402: +LOCGOV_NM(시스템권한만). */
    private String srchKey;

    private String srchValue;

    /** 등록일 범위 - AS-IS 컨트롤러가 비면 오늘로 채운다. */
    private String srchStartCreated;

    private String srchEndCreated;

    /** 회원구분 체크박스 - "ALL" 또는 ROLE_ADMIN_*. AS-IS는 진입 시 전체를 체크한다. */
    private List<String> srchArrAuthority = new ArrayList<>();

    /** 사용여부 - ""(전체) / 9(사용) / 2(중지). */
    private String srchStatusCode;

    private int page = 1;

    private int itemsPerPage = 10;

    /** AS-IS 폼이 hidden으로 들고 다니는 값들(쿼리에는 쓰이지 않는다). */
    private String sort;
    private String orderBy;
    private String query;

    /** AS-IS 컨트롤러 - 등록일이 비면 오늘로 채운다. */
    public void applyDefaults() {
        String today = LocalDate.now().format(DAY);
        if (srchStartCreated == null || srchStartCreated.isBlank()) {
            srchStartCreated = today;
        }
        if (srchEndCreated == null || srchEndCreated.isBlank()) {
            srchEndCreated = today;
        }
        if (itemsPerPage <= 0) {
            itemsPerPage = Pagination.DEFAULT_ITEMS_PER_PAGE;
        }
    }

    /** 회원구분 체크박스 - 아무것도 안 골랐거나 ALL이면 그 화면의 전체 권한. */
    public boolean matchesAuthorities(String authority, List<String> screenAuthorities) {
        if (authority == null || !screenAuthorities.contains(authority)) {
            return false;
        }
        if (srchArrAuthority.isEmpty() || srchArrAuthority.contains("ALL")) {
            return true;
        }
        return srchArrAuthority.contains(authority);
    }

    /**
     * 사용여부 - 빈 값이면 전체. 화면이 보내는 값은 AS-IS 그대로 '9'/'2'이고 실제 컬럼값은
     * {@code ACTIVE}/{@code LOCKED}라, AS-IS 표기로 바꿔 비교한다
     * ({@code Manager.getAsIsStatusCode()}와 같은 규칙).
     */
    public boolean matchesStatus(String statusCode) {
        if (srchStatusCode == null || srchStatusCode.isBlank()) {
            return true;
        }
        String asIs = "LOCKED".equals(statusCode) ? "2" : "9";
        return srchStatusCode.equals(asIs);
    }

    /** 검색구분 + 검색어 - AS-IS는 LIKE(부분일치)다. */
    public boolean matchesKeyword(String loginId, String userName, String phoneNumber) {
        if (srchValue == null || srchValue.isBlank()) {
            return true;
        }
        String keyword = srchValue.trim();
        return switch (srchKey == null ? "LOGIN_ID" : srchKey) {
            case "USER_NAME" -> userName != null && userName.contains(keyword);
            case "PHONE_NUMBER" -> phoneNumber != null && phoneNumber.contains(keyword);
            default -> loginId != null && loginId.contains(keyword);
        };
    }
}
