package com.ghlove.admin.web.support;

import lombok.Getter;
import lombok.Setter;

/**
 * 답례품제공자 공지사항 검색조건 (메뉴 5114) - AS-IS {@code SysNoticeSellerDto} 중 검색 부분.
 *
 * <p>AS-IS 컨트롤러는 등록일을 오늘로 채우지 않는다(공지사항 5111과 같고, 지자체공지·자료실·
 * SR게시판과 다르다). 그래서 날짜칸이 빈 채로 뜨고 비워 두면 전체 기간이 조회된다.
 */
@Getter
@Setter
public class SellerNoticeSearchParam {

    /** 화면 select는 SUBJECT/CONTENT 둘뿐이다. */
    private String where = "SUBJECT";
    private String query;
    private String startDt;
    private String endDt;
    private int itemsPerPage = 10;
    private int page = 1;
}
