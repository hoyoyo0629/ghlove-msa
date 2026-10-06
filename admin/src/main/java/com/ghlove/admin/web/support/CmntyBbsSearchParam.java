package com.ghlove.admin.web.support;

import lombok.Getter;
import lombok.Setter;

/**
 * 소통방(메뉴 11401) 검색조건 - AS-IS {@code CmntyBbsRequestDto} 중 list.jsp가 보내는 것 그대로다.
 *
 * <p>AS-IS는 등록일자 기본값을 <b>채우지 않는다</b>(비워두면 전체 기간). 화면이 오늘·1주일·한달·
 * 3개월·1년 버튼을 주지만 누르지 않으면 조건이 없다 - 날짜를 오늘로 채우는 15101과 다르다.
 */
@Getter
@Setter
public class CmntyBbsSearchParam {

    /** 소속 - ""(전체)/ROLE_ADMIN_1(시스템 관리자)/ROLE_ADMIN_3(행정안전부)/ROLE_ADMIN_5(지자체). */
    private String searchRole;

    /** 시·도 선택(공통코드 WDR) - 지자체를 고를 때만 보인다. 조회조건이 아니라 시군구 목록을 좁히는 값이다. */
    private String shWdr;

    /** 시·군·구(작성자의 지자체코드). */
    private String locgovCode;

    /** 검색구분 - ALL(전체)/SUBJECT(제목). AS-IS는 USERNAME(작성자)을 주석처리해 두었다. */
    private String where = "ALL";

    private String query;

    /** 등록일자 범위(yyyyMMdd) - 둘 다 있어야 조건이 걸린다(AS-IS mapper 그대로). */
    private String startDt;

    private String endDt;

    private int page = 1;

    private int itemsPerPage = Pagination.DEFAULT_ITEMS_PER_PAGE;

    /** AS-IS 폼이 hidden으로 들고 다니는 값들. */
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
