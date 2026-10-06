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
}
