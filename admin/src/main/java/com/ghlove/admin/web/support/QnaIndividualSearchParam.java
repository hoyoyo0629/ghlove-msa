package com.ghlove.admin.web.support;

import lombok.Getter;
import lombok.Setter;

/**
 * 1:1 문의(메뉴 5102) 검색조건 - AS-IS {@code QnaParam} 중 list.jsp가 보내는 것 그대로다.
 *
 * <p>AS-IS 화면의 문의유형 select와 답변상태 라디오({@code answerCount})는 <b>주석처리</b>되어 있고,
 * 살아 있는 조건은 검색구분 4종 + 등록일 범위 + 상태 라디오({@code qnaAnswerCode})뿐이다.
 */
@Getter
@Setter
public class QnaIndividualSearchParam {

    /** 상태 - ""(전체)/0(답변완료)/1(답변미완료). */
    private String qnaAnswerCode;

    /** 검색구분 - GROUP(문의유형)/LOGIN_ID(아이디)/SUBJECT(제목)/QUESTION(내용). */
    private String where;

    private String query;

    /** 등록일 범위(yyyyMMdd) - AS-IS는 뒤에 000000/235959를 붙여 varchar 비교한다. */
    private String searchStartDate;

    private String searchEndDate;

    private int page = 1;

    /** AS-IS 출력수 select는 10/20/50/100이다. */
    private int itemsPerPage = Pagination.DEFAULT_ITEMS_PER_PAGE;

    public void applyDefaults() {
        if (where == null || where.isBlank()) {
            // AS-IS 화면 select의 첫 항목
            where = "GROUP";
        }
        if (page <= 0) {
            page = 1;
        }
        if (itemsPerPage <= 0) {
            itemsPerPage = Pagination.DEFAULT_ITEMS_PER_PAGE;
        }
    }
}
