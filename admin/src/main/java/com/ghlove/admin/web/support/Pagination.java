package com.ghlove.admin.web.support;

import lombok.Getter;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * AS-IS 운영관리 목록화면의 페이징 모델 (com.onlinepowers.framework.web.pagination.Pagination 재현).
 * 프레임워크 jar 소스가 없어 JSP가 실제로 쓰는 프로퍼티와 사용 형태로 역산했다 - 운영관리 JSP
 * 전수에서 쓰이는 것은 {@code itemNumber}(151곳) · {@code link}(80) · {@code totalPages}(71) ·
 * {@code totalItems}(66) · {@code currentPage}(38) · {@code previousPage}(25) · {@code nextPage}(21)뿐이다.
 *
 * {@code itemNumber}는 내림차순 NO 표기용으로, JSP가 예외 없이 {@code ${pagination.itemNumber - i.count}}
 * 형태(131곳 전부)로 쓴다. i.count가 1부터이므로 1페이지 첫 행이 전체건수가 되도록
 * {@code totalItems - (currentPage-1)*itemsPerPage + 1}이다.
 *
 * {@code link}는 '[page]' 자리표시자를 가진 URL로, 페이지네이션 조각이 이걸 페이지번호로 치환한다
 * (fragments/pagination.html). 현재 검색조건(쿼리스트링)을 유지해야 하므로 요청에서 만들어 준다.
 */
@Getter
public class Pagination {

    /** AS-IS saleson.common.web.Paging 과 동일한 기본값. */
    public static final int DEFAULT_ITEMS_PER_PAGE = 10;

    public static final String PAGE_PLACEHOLDER = "[page]";

    private final int totalItems;
    private final int itemsPerPage;
    private final int currentPage;
    private final int totalPages;
    private final int previousPage;
    private final int nextPage;
    private final int itemNumber;
    private final int startRow;
    private String link = "?page=" + PAGE_PLACEHOLDER;

    private Pagination(int totalItems, int itemsPerPage, int requestedPage) {
        this.totalItems = Math.max(totalItems, 0);
        this.itemsPerPage = itemsPerPage > 0 ? itemsPerPage : DEFAULT_ITEMS_PER_PAGE;
        this.totalPages = (int) Math.ceil((double) this.totalItems / this.itemsPerPage);
        int page = Math.max(requestedPage, 1);
        if (this.totalPages > 0 && page > this.totalPages) {
            page = this.totalPages;
        }
        this.currentPage = page;
        this.previousPage = this.currentPage > 1 ? this.currentPage - 1 : 0;
        this.nextPage = this.currentPage < this.totalPages ? this.currentPage + 1 : 0;
        this.itemNumber = this.totalItems - (this.currentPage - 1) * this.itemsPerPage + 1;
        this.startRow = (this.currentPage - 1) * this.itemsPerPage;
    }

    /** AS-IS {@code Pagination.getInstance(count)} 대응 - 페이지당 건수는 기본값. */
    public static Pagination of(int totalItems, int requestedPage) {
        return new Pagination(totalItems, DEFAULT_ITEMS_PER_PAGE, requestedPage);
    }

    public static Pagination of(int totalItems, int requestedPage, int itemsPerPage) {
        return new Pagination(totalItems, itemsPerPage, requestedPage);
    }

    /**
     * 현재 요청의 쿼리스트링에서 page만 자리표시자로 바꿔 link를 만든다 - 검색조건을 유지한 채
     * 페이지만 넘기기 위함(AS-IS도 동일하게 현재 조건을 유지한 링크를 내려준다).
     */
    public Pagination withLinkFrom(HttpServletRequest request) {
        Map<String, String> params = new LinkedHashMap<>();
        request.getParameterMap().forEach((key, values) -> {
            if (!"page".equals(key) && values != null && values.length > 0 && !values[0].isEmpty()) {
                params.put(key, values[0]);
            }
        });
        StringBuilder sb = new StringBuilder(request.getRequestURI()).append("?page=").append(PAGE_PLACEHOLDER);
        params.forEach((key, value) -> sb.append('&')
                .append(URLEncoder.encode(key, StandardCharsets.UTF_8))
                .append('=')
                .append(URLEncoder.encode(value, StandardCharsets.UTF_8)));
        this.link = sb.toString();
        return this;
    }

    /**
     * 링크를 직접 지정한다 - AS-IS가 {@code pagination.setLink("javascript:getCntrList(" +
     * Pagination.REPLACE_PAGE_PATTERN + ")")}처럼 <b>자바스크립트 호출</b>을 링크로 넣는
     * 경우가 있다(상세화면에 ajax로 끼워넣는 목록 조각들). '[page]' 자리표시자를 쓴다.
     */
    public Pagination withLink(String linkWithPagePlaceholder) {
        this.link = linkWithPagePlaceholder;
        return this;
    }

    /** 페이지네이션 조각이 쓰는 치환 - '[page]'를 페이지번호로 바꾼 URL. */
    public String linkTo(int page) {
        return link.replace(PAGE_PLACEHOLDER, String.valueOf(page));
    }

    /**
     * AS-IS pagination-manager.tag의 번호창 계산을 그대로 옮긴 것 - 10개 단위로,
     * 9페이지 미만이면 1~10, 끝에서 8페이지 안쪽이면 마지막 10개, 그 사이면 현재±5다.
     */
    public int getWindowStart() {
        if (totalPages < 10) {
            return 1;
        }
        if (currentPage < 9) {
            return 1;
        }
        if (currentPage > totalPages - 8) {
            return totalPages - 9;
        }
        return currentPage - 5;
    }

    public int getWindowEnd() {
        if (totalPages < 10) {
            return Math.max(totalPages, 1);
        }
        if (currentPage < 9) {
            return 10;
        }
        if (currentPage > totalPages - 8) {
            return totalPages;
        }
        return currentPage + 5;
    }
}
