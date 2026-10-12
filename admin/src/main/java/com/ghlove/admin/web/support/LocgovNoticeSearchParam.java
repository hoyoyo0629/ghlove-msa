package com.ghlove.admin.web.support;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 지자체공지사항 검색조건 (메뉴 5110) - AS-IS {@code NoticeParam} 중 이 화면이 실제로 쓰는
 * 부분만 재현. AS-IS 컨트롤러는 GET·POST 진입 모두에서 {@code startCreateDate}/{@code endCreateDate}가
 * 비어 있으면 <b>오늘 날짜로 채운다</b>(검색폼에 그대로 노출) - {@link #applyDefaults()}가 그 동작이다.
 */
@Getter
@Setter
public class LocgovNoticeSearchParam {

    private static final DateTimeFormatter YMD = DateTimeFormatter.ofPattern("yyyyMMdd");

    /** 화면 select는 SUBJECT/CONTENT 둘뿐이다("전체"는 AS-IS가 주석처리). */
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
