package com.ghlove.admin.web.support;

import lombok.Getter;
import lombok.Setter;

/**
 * Q&A 관리(메뉴 5112) 검색조건 - AS-IS {@code QnaOpenParam} 중 list.jsp가 보내는 것 그대로다.
 *
 * <p>AS-IS 컨트롤러가 정렬을 <b>고정</b>한다({@code sort="DESC"}, {@code orderBy="CREATED_DATE"}) -
 * 화면에서 바꿀 수 없다. 그래서 여기서도 기본값을 그 값으로 두고 화면은 건드리지 않는다.
 */
@Getter
@Setter
public class QnaOpenSearchParam {

    /** 답변여부 - ""(전체)/0(답변완료)/1(미답변). AS-IS qnaOpenAnswerCode 그대로. */
    private String qnaOpenAnswerCode;

    /**
     * 검색구분 - GROUP(문의유형)/SUBJECT(제목)/QUESTION(내용)/LOGIN_ID(아이디)/USER_NAME(작성자).
     * 전부 부분일치(LIKE)다.
     */
    private String where;

    private String query;

    /** 등록일 범위(yyyyMMdd) - AS-IS는 뒤에 000000/235959를 붙여 varchar 비교한다. */
    private String searchStartDate;

    private String searchEndDate;

    /** AS-IS 컨트롤러가 고정하는 값들. */
    private String sort = "DESC";

    private String orderBy = "CREATED_DATE";

    private int page = 1;

    private int itemsPerPage = Pagination.DEFAULT_ITEMS_PER_PAGE;

    public void applyDefaults() {
        // AS-IS: 정렬은 화면값을 쓰지 않고 항상 고정한다
        sort = "DESC";
        orderBy = "CREATED_DATE";
        if (page <= 0) {
            page = 1;
        }
        if (itemsPerPage <= 0) {
            itemsPerPage = Pagination.DEFAULT_ITEMS_PER_PAGE;
        }
    }
}
