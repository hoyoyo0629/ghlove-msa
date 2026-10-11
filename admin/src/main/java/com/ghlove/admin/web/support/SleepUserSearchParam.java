package com.ghlove.admin.web.support;

import lombok.Getter;
import lombok.Setter;

/** AS-IS saleson.shop.user.support.SleepUserSearchParam - 휴면회원관리(메뉴 4107) 검색조건. */
@Getter
@Setter
public class SleepUserSearchParam {

    /** 검색구분 - LOGIN_ID(아이디) / USER_NAME(이름). AS-IS 화면 안내대로 **정확히 일치**로 찾는다. */
    private String srchKey = "LOGIN_ID";

    private String srchValue;

    /** 최종 방문일 범위 - AS-IS 컨트롤러가 비면 오늘로 채운다. */
    private String srchStartLoginDate;

    private String srchEndLoginDate;

    private int page = 1;

    private int itemsPerPage = 10;

    /** AS-IS 폼이 hidden으로 들고 다니는 값들(쿼리에는 쓰이지 않는다). */
    private String sort;
    private String orderBy;
    private String query;

    /** member 검색 API는 yyyy-MM-dd를 받는다 - 화면이 쓰는 yyyyMMdd를 바꿔 준다. */
    public String getSrchStartLoginDateForApi() {
        return toApiDate(srchStartLoginDate);
    }

    public String getSrchEndLoginDateForApi() {
        return toApiDate(srchEndLoginDate);
    }

    /** 8자리 숫자가 아니면 null - member는 빈 값을 "기간 무제한"으로 보고, 잘못된 값이면 터진다. */
    private static String toApiDate(String yyyymmdd) {
        if (yyyymmdd == null || !yyyymmdd.matches("\\d{8}")) {
            return null;
        }
        return yyyymmdd.substring(0, 4) + "-" + yyyymmdd.substring(4, 6) + "-" + yyyymmdd.substring(6, 8);
    }
}
