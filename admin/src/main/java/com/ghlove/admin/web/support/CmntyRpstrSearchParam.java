package com.ghlove.admin.web.support;

import lombok.Getter;
import lombok.Setter;

/**
 * 자료실(메뉴 11402) 검색조건 - AS-IS {@code CmntyRpstrRequestDto} 중 list.jsp가 보내는 것 그대로다.
 * 소통방과 같은 구성이고 검색구분 기본값만 빈 값("전체")이다.
 */
@Getter
@Setter
public class CmntyRpstrSearchParam {

    private String searchRole;

    private String shWdr;

    private String locgovCode;

    /** 검색구분 - ""(전체)/SUBJECT(제목). AS-IS는 USERNAME(작성자)을 주석처리해 두었다. */
    private String where = "";

    private String query;

    private String startDt;

    private String endDt;

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
