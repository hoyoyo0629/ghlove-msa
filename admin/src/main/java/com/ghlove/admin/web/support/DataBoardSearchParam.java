package com.ghlove.admin.web.support;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 자료실 검색조건 (메뉴 5107) - AS-IS {@code DataboardParam} 중 이 화면이 쓰는 부분만 재현.
 *
 * <p>AS-IS {@code DataboardManagerController}는 지자체공지사항(5110)과 마찬가지로 GET·POST
 * 양쪽에서 등록일을 <b>오늘로 채운다</b>(공지사항 5111만 채우지 않는다).
 */
@Getter
@Setter
public class DataBoardSearchParam {

    private static final DateTimeFormatter YMD = DateTimeFormatter.ofPattern("yyyyMMdd");

    /** 화면 select는 SUBJECT/CONTENT 둘뿐이다(FILE_TY 옵션은 AS-IS가 주석처리). */
    private String where = "SUBJECT";
    private String query;
    private String startCreateDate;
    private String endCreateDate;
    private int itemsPerPage = 10;
    private int page = 1;

    public void applyDefaults() {
        String today = LocalDate.now().format(YMD);
        if (startCreateDate == null || startCreateDate.isBlank()) {
            startCreateDate = today;
        }
        if (endCreateDate == null || endCreateDate.isBlank()) {
            endCreateDate = today;
        }
    }
}
