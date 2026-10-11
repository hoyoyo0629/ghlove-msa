package com.ghlove.admin.web.support;

import lombok.Getter;
import lombok.Setter;

/** AS-IS saleson.shop.user.support.SecedeUserSearchParam - 회원탈퇴관리(메뉴 4105) 검색조건. */
@Getter
@Setter
public class SecedeUserSearchParam {

    /** 검색구분 - LOGIN_ID(아이디) / LEAVE_REASON(탈퇴사유). */
    private String srchKey = "LOGIN_ID";

    private String srchValue;

    private String srchStartLeaveDate;

    private String srchEndLeaveDate;

    /** 탈퇴구분 - ""(전체) / U(회원탈퇴) / M(관리자탈퇴). */
    private String srchLeaveType;

    private int page = 1;

    /** 화면출력(10/20/50/100) - AS-IS Pagination.getInstance(count) 기본값은 10이다. */
    private int itemsPerPage = 10;

    /** AS-IS 폼이 hidden으로 들고 다니는 값들(쿼리에는 쓰이지 않는다). */
    private String sort;
    private String orderBy;
    private String query;

    /** member 검색 API는 yyyy-MM-dd를 받는다 - 화면이 쓰는 yyyyMMdd를 바꿔 준다. */
    public String getSrchStartLeaveDateForApi() {
        return toApiDate(srchStartLeaveDate);
    }

    public String getSrchEndLeaveDateForApi() {
        return toApiDate(srchEndLeaveDate);
    }

    /** 8자리 숫자가 아니면 null - member는 빈 값을 "기간 무제한"으로 보고, 잘못된 값이면 터진다. */
    private static String toApiDate(String yyyymmdd) {
        if (yyyymmdd == null || !yyyymmdd.matches("\\d{8}")) {
            return null;
        }
        return yyyymmdd.substring(0, 4) + "-" + yyyymmdd.substring(4, 6) + "-" + yyyymmdd.substring(6, 8);
    }
}
