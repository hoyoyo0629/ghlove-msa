package com.ghlove.admin.web.support;

import lombok.Getter;
import lombok.Setter;

/**
 * 담당자용 FAQ(메뉴 11405) 검색조건 - AS-IS {@code CmntyFaqBbsDto}의 검색 필드 중 list.jsp가
 * 보내는 것 그대로다.
 *
 * <p>소통방·SR 게시판의 검색조건에 <b>질문유형({@code shFaqType})</b> 하나가 더 붙는다
 * (화면에서는 select가 아니라 <b>탭</b>으로 고르고 누르면 바로 검색된다).
 */
@Getter
@Setter
public class CmntyFaqBbsSearchParam {

    private String searchRole;

    private String shWdr;

    private String locgovCode;

    /** 검색구분 - ALL(전체)/SUBJECT(제목). AS-IS는 USERNAME(작성자)을 주석처리해 두었다. */
    private String where = "ALL";

    private String query;

    private String startDt;

    private String endDt;

    /** 질문유형(공통코드 CMNTY_FAQ_TYPE의 id) - 비면 전체. */
    private String shFaqType;

    private int page = 1;

    private int itemsPerPage = Pagination.DEFAULT_ITEMS_PER_PAGE;

    private String sort;

    private String orderBy;

    public void applyDefaults() {
        if (itemsPerPage <= 0) {
            itemsPerPage = Pagination.DEFAULT_ITEMS_PER_PAGE;
        }
        if (page <= 0) {
            page = 1;
        }
    }
}
