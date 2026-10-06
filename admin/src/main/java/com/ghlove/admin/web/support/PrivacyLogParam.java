package com.ghlove.admin.web.support;

import lombok.Getter;
import lombok.Setter;

/**
 * AS-IS saleson.shop.log.support.PrivacyLogParam 재현 - 엑셀다운로드 사유 관리(개인정보 접근로그)
 * 검색조건. 검색구분은 관리자ID(LOGIN_ID)·다운로드 메뉴(DOWNLOAD_MENU)·사유(REASON)이고,
 * 등록일 기본값은 AS-IS 컨트롤러가 '오늘'로 채운다. 기본 목록수는 20이다.
 */
@Getter
@Setter
public class PrivacyLogParam {

    private String srchKey = "LOGIN_ID";
    private String srchValue = "";
    private String srchStartLogDate = "";
    private String srchEndLogDate = "";
    private int itemsPerPage = 20;
    private int itemsPerPageTemp = 0;
    private int page = 1;

    /**
     * AS-IS 목록 쿼리의 바깥 WHERE 그대로 - <b>관리자ID(LOGIN_ID)는 완전일치</b>({@code =}),
     * 다운로드 메뉴(NAME)와 사유(REASON)는 부분일치({@code LIKE '%키워드%'})다.
     */
    public boolean matchesKeyword(String loginId, String name, String reason) {
        if (srchValue == null || srchValue.isBlank()) {
            return true;
        }
        String keyword = srchValue.trim();
        return switch (srchKey == null ? "LOGIN_ID" : srchKey) {
            case "DOWNLOAD_MENU" -> name != null && name.contains(keyword);
            case "REASON" -> reason != null && reason.contains(keyword);
            default -> keyword.equals(loginId);
        };
    }
}
