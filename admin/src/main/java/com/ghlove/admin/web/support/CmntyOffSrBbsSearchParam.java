package com.ghlove.admin.web.support;

import lombok.Getter;
import lombok.Setter;

/**
 * 오프라인 담당자 SR 게시판(메뉴 11406) 검색조건 - AS-IS {@code CmntyOffSrBbsDto}의 검색 필드 중
 * list.jsp가 보내는 것 그대로다.
 *
 * <p>SR 게시판(11404)과 다른 점: 지자체(시도/시군구) 대신 <b>은행({@code shBank})</b>으로 좁히고,
 * 검색구분에 작성자 대신 <b>소속지점({@code PSITN})</b>이 있다.
 */
@Getter
@Setter
public class CmntyOffSrBbsSearchParam {

    /** 소속 - ""(전체)/ROLE_ADMIN_1(시스템 관리자)/ROLE_ADMIN_3(행정안전부)/ROLE_ADMIN_7(오프라인 담당자). */
    private String searchRole;

    /** 은행코드(공통코드 OFF_BANK_LIST) - 소속이 '오프라인 담당자'일 때만 보인다. */
    private String shBank;

    /** 검색구분 - ALL(전체)/SUBJECT(제목)/PSITN(소속지점). AS-IS는 USERNAME을 주석처리해 두었다. */
    private String where = "ALL";

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
