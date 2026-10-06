package com.ghlove.admin.web.support;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 일반회원관리(메뉴 4101) 목록 검색조건 - AS-IS
 * {@code saleson.shop.user.support.GeneralCustomerSearchParam} 중 list.jsp가 보내는 항목.
 *
 * <p>AS-IS 컨트롤러가 <b>가입일 범위를 비워 두면 오늘로 채운다</b>(GET·POST 모두). 화면에서도
 * 가입일은 필수표시(*)이고 검색 시 비어 있으면 "가입일을 입력 하십시오"로 막는다.
 */
@Getter
@Setter
public class GeneralCustomerSearchParam {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyyMMdd");

    /** 검색구분 - LOGIN_ID(아이디)/USER_NAME(이름)/ADDRESS(주소). */
    private String srchKey;

    private String srchValue;

    /** 가입일 범위(yyyyMMdd) - 비면 오늘. */
    private String srchStartCreated;

    private String srchEndCreated;

    /** 가입구분 - ""(전체)/100(온라인)/200(오프라인). */
    private String srchSbscrbSeCode;

    /** Email 수신동의 - ""(전체)/0(동의)/1(비동의). */
    private String srchReceiveEmail;

    private int page = 1;

    private int itemsPerPage = 10;

    /** AS-IS 폼이 hidden으로 들고 다니는 값들(이 화면의 쿼리에는 쓰이지 않는다). */
    private String sort;
    private String orderBy;
    private String query;

    /** AS-IS 컨트롤러 - 가입일이 비면 오늘로 채운다. */
    public void applyDefaults() {
        String today = LocalDate.now().format(DAY);
        if (srchStartCreated == null || srchStartCreated.isBlank()) {
            srchStartCreated = today;
        }
        if (srchEndCreated == null || srchEndCreated.isBlank()) {
            srchEndCreated = today;
        }
        if (itemsPerPage <= 0) {
            itemsPerPage = Pagination.DEFAULT_ITEMS_PER_PAGE;
        }
    }

    /** member 검색 API는 yyyy-MM-dd를 받는다 - 화면이 쓰는 yyyyMMdd를 바꿔 준다. */
    public String getFromDateForApi() {
        return toApiDate(srchStartCreated);
    }

    public String getToDateForApi() {
        return toApiDate(srchEndCreated);
    }

    /** 8자리 숫자가 아니면 null - member는 빈 값을 "기간 무제한"으로 보고, 잘못된 값이면 터진다. */
    private static String toApiDate(String yyyymmdd) {
        if (yyyymmdd == null || !yyyymmdd.matches("\\d{8}")) {
            return null;
        }
        return yyyymmdd.substring(0, 4) + "-" + yyyymmdd.substring(4, 6) + "-" + yyyymmdd.substring(6, 8);
    }
}
