package com.ghlove.admin.web.support;

import lombok.Getter;
import lombok.Setter;

/** AS-IS saleson.shop.email.support.EmailParam - 이메일 발송 목록 검색조건. */
@Getter
@Setter
public class EmailParam {

    /** 화면 select가 보내는 값은 SUBJECT / CONTENT다 (AS-IS 쿼리는 'S'/'C'를 봐서 안 걸린다). */
    private String searchType = "SUBJECT";

    private String searchContent;

    private String searchStartDate;

    private String searchEndDate;

    /** 목록 페이지 - AS-IS SearchParam.page. */
    private int page = 1;

    /** 화면출력 개수 - AS-IS SearchParam.itemsPerPage(기본 10). */
    private int itemsPerPage = 10;

    /** AS-IS list.jsp가 form:hidden으로 들고 다니는 값(쿼리에서는 쓰이지 않는다). */
    private String query;
}
