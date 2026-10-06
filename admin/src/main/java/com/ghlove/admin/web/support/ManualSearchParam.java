package com.ghlove.admin.web.support;

import lombok.Getter;
import lombok.Setter;

/**
 * 사용자매뉴얼(메뉴 5202) 검색조건 - AS-IS {@code ManualParam} 중 list.jsp가 보내는 것 그대로다.
 *
 * <p><b>AS-IS 그대로</b>: 등록일 두 칸이 비어 있으면 컨트롤러가 <b>오늘</b>로 채운다
 * (GET 진입에서도 채운다 - 그래서 화면을 열면 오늘 날짜가 들어가 있고, 그대로 검색하면
 * 오늘 등록분만 나온다). 검색구분에 '전체' 항목은 <b>주석처리</b>되어 있어 항상 셋 중 하나다.
 */
@Getter
@Setter
public class ManualSearchParam {

    /** 검색구분 - PAGE(페이지 구분)/SUBJECT(제목)/CONTENT(내용). */
    private String where;

    private String query;

    /** 등록일 범위(yyyyMMdd). */
    private String startCreateDate;

    private String endCreateDate;

    /** AS-IS SearchParam의 정렬 칸 - 이 화면은 hidden으로만 들고 다니고 서버가 쓰지 않는다. */
    private String sort;

    private String orderBy;

    private int page = 1;

    private int itemsPerPage = Pagination.DEFAULT_ITEMS_PER_PAGE;

    public void applyDefaults(String today) {
        if (startCreateDate == null || startCreateDate.isBlank()) {
            startCreateDate = today;
        }
        if (endCreateDate == null || endCreateDate.isBlank()) {
            endCreateDate = today;
        }
        // AS-IS 화면 select에 '전체'가 없어 값이 비는 경우는 없지만, 비면 첫 항목으로 본다
        if (where == null || where.isBlank()) {
            where = "PAGE";
        }
        if (page <= 0) {
            page = 1;
        }
        if (itemsPerPage <= 0) {
            itemsPerPage = Pagination.DEFAULT_ITEMS_PER_PAGE;
        }
    }
}
