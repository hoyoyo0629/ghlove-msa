package com.ghlove.admin.web.support;

import lombok.Getter;
import lombok.Setter;

/**
 * AS-IS 관리자 권한 승인관리 검색조건(user/manager-request/list.jsp의 searchParam).
 * 검색구분은 아이디(LOGIN_ID)·이름(USER_NAME)·이메일(EMAIL)이고, 등록일 범위와
 * 상태(빈값=전체, 200=대기, 100=승인, 300=거절)로 거른다.
 */
@Getter
@Setter
public class ManagerRequestParam {

    private String srchKey = "LOGIN_ID";
    private String srchValue = "";
    private String srchStartCreated = "";
    private String srchEndCreated = "";
    private String srchConfmSttusCode = "";
    private int itemsPerPage = 10;
    private int page = 1;
}
