package com.ghlove.admin.web.support;

import lombok.Getter;
import lombok.Setter;

/**
 * AS-IS saleson.shop.log.support.LoginLogParam 재현 - 로그인 로그(관리자/사용자) 검색조건.
 * 검색구분은 로그인ID(LOGIN_ID)·접속IP(REMOTE_ADDR), 접속일 범위, 로그인 성공여부(빈값=전체),
 * 관리자 화면은 권한그룹(srcRole)까지 쓴다. 화면출력(itemsPerPage)은 숨은 입력으로 함께 넘어온다.
 */
@Getter
@Setter
public class LoginLogParam {

    private String srchKey = "LOGIN_ID";
    private String srchValue = "";
    private String srchStartLoginDate = "";
    private String srchEndLoginDate = "";
    private String srchSuccessFlag = "";
    private String srcRole = "";
    private int itemsPerPage = 10;
    private int itemsPerPageTemp = 0;
    private int page = 1;

    /** AS-IS는 LOGIN_DATE가 'yyyyMMddHHmmss' 문자열이라 날짜 8자리로 앞뒤를 비교한다. */
    public boolean matchesDate(String loginDate) {
        if (loginDate == null || loginDate.length() < 8) {
            return srchStartLoginDate.isBlank() && srchEndLoginDate.isBlank();
        }
        String day = loginDate.substring(0, 8);
        if (!srchStartLoginDate.isBlank() && day.compareTo(srchStartLoginDate) < 0) {
            return false;
        }
        return srchEndLoginDate.isBlank() || day.compareTo(srchEndLoginDate) <= 0;
    }

    public boolean matchesKeyword(String loginId, String remoteAddr) {
        if (srchValue == null || srchValue.isBlank()) {
            return true;
        }
        String keyword = srchValue.trim();
        if ("REMOTE_ADDR".equals(srchKey)) {
            return remoteAddr != null && remoteAddr.contains(keyword);
        }
        return loginId != null && loginId.contains(keyword);
    }

    public boolean matchesSuccess(String successFlag) {
        return srchSuccessFlag == null || srchSuccessFlag.isBlank() || srchSuccessFlag.equals(successFlag);
    }
}
