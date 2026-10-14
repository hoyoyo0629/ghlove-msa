package com.ghlove.admin.web.support;

import lombok.Getter;
import lombok.Setter;

/**
 * 공지사항(전체공지) 검색조건 (메뉴 5111) - AS-IS {@code NoticeParam} 중 이 화면이 쓰는 부분만 재현.
 *
 * <p><b>지자체공지사항(5110)과 다른 점</b>: AS-IS {@code NoticeManagerController}는 등록일
 * 검색조건을 <b>오늘로 채우지 않는다</b>({@code LocgovNoticeManagerController}만
 * {@code StringUtils.defaultIfEmpty(..., today)}를 한다). 그래서 이 화면은 날짜칸이 빈 채로
 * 뜨고, 날짜를 직접 넣지 않으면 기간 조건 없이 전체를 조회한다.
 */
@Getter
@Setter
public class NoticeSearchParam {

    /** 화면 select는 SUBJECT/CONTENT 둘뿐이다("전체"는 AS-IS가 주석처리). */
    private String where = "SUBJECT";
    private String query;
    private String startCreateDate;
    private String endCreateDate;
    private int itemsPerPage = 10;
    private int page = 1;
}
