package com.ghlove.admin.web.support;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 기부혜택증 관리(메뉴 19101·19103) 검색조건 - AS-IS
 * {@code saleson.shop.lclgvHnrUser.support.LclgvHnrUserMngParam} 중 두 화면이 보내는 항목.
 *
 * <p>AS-IS 출력개수 select가 다른 화면들과 달리 <b>10/50/100/200/500</b>이다.
 * 열람현황(19103)만 기간을 쓰고 컨트롤러가 비면 오늘로 채운다. 설정목록(19101)은 기간이 없다.
 */
@Getter
@Setter
public class HonorUserSearchParam {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyyMMdd");

    /** 시도 코드(AS-IS는 공통코드 WDR). */
    private String upperLocgovCode;

    /** 시군구(지자체) 코드. */
    private String lclgvCd;

    /** 열람현황 전용 - 사용자명(부분일치). */
    private String userName;

    /** 열람현황 전용 - 기간(yyyyMMdd). 비면 오늘. */
    private String startDate;

    private String endDate;

    private int page = 1;

    private int itemsPerPage = 10;

    /** 설정목록(19101)에는 기간이 없다 - 페이지당 건수만 채운다. */
    public void applyListDefaults() {
        if (itemsPerPage <= 0) {
            itemsPerPage = Pagination.DEFAULT_ITEMS_PER_PAGE;
        }
    }

    /** 열람현황(19103) - AS-IS 컨트롤러가 기간을 오늘로 채운다. */
    public void applyViewHistDefaults() {
        String today = LocalDate.now().format(DAY);
        if (startDate == null || startDate.isBlank()) {
            startDate = today;
        }
        if (endDate == null || endDate.isBlank()) {
            endDate = today;
        }
        applyListDefaults();
    }

    /** AS-IS는 사용자명을 LIKE '%값%'로 본다. */
    public boolean matchesUserName(String name) {
        if (userName == null || userName.isBlank()) {
            return true;
        }
        return name != null && name.contains(userName.trim());
    }
}
