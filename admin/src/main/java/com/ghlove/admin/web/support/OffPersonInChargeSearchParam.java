package com.ghlove.admin.web.support;

import lombok.Getter;
import lombok.Setter;

/**
 * 오프라인담당자(메뉴 4601) 목록 검색조건 - AS-IS
 * {@code opmanager/i18n/user/off-charger/list.jsp}가 보내는 항목만 담는다.
 *
 * <p>AS-IS는 담당자 화면 전체가 {@code PersonInChargeSearchParam} 하나를 공유하지만, 이 화면의
 * 검색 폼에는 <b>검색구분(지점명/아이디/이름/개인번호) + 사용여부</b>밖에 없다(등록일 범위가
 * 없다 - JSP 하단에 날짜 셋팅용 숨은 div와 {@code serachDate()}가 남아 있지만 붙을 날짜 입력칸이
 * 없어 실제로는 동작하지 않는다). 운영관리자(4501)·지자체담당자(4402)가 쓰는
 * {@link PersonInChargeSearchParam}은 등록일을 오늘로 강제하는 {@code applyDefaults()}를 갖고 있어
 * 그대로 쓰면 이 화면이 늘 "오늘 등록된 사람"만 보여주게 된다 - 그래서 분리했다.
 *
 * <p><b>검색 일치방식도 AS-IS 그대로다</b>: 지점명만 LIKE(부분일치)이고 아이디·이름·개인번호는
 * {@code =}(완전일치)다(slave-personincharge-mapper.sqlChargerListByParamWhere).
 */
@Getter
@Setter
public class OffPersonInChargeSearchParam {

    /** AS-IS select의 첫 option - 검색구분을 고르지 않으면 지점명이다. */
    public static final String DEFAULT_SRCH_KEY = "PSITN_NM";

    /** 검색구분 - PSITN_NM(지점명)/LOGIN_ID(아이디)/USER_NAME(이름)/EMP_ID(개인번호). */
    private String srchKey;

    private String srchValue;

    /** 사용여부 - ""(전체) / 9(사용) / 2(중지). */
    private String srchStatusCode;

    private int page = 1;

    private int itemsPerPage = 10;

    /** AS-IS 폼이 hidden으로 들고 다니는 값들(이 화면의 쿼리에는 쓰이지 않는다). */
    private String sort;
    private String orderBy;
    private String query;

    public void applyDefaults() {
        if (srchKey == null || srchKey.isBlank()) {
            srchKey = DEFAULT_SRCH_KEY;
        }
        if (itemsPerPage <= 0) {
            itemsPerPage = Pagination.DEFAULT_ITEMS_PER_PAGE;
        }
    }

    /**
     * 사용여부 - 빈 값이면 전체. 화면이 보내는 값은 AS-IS 그대로 '9'/'2'이고 실제 컬럼값은
     * {@code ACTIVE}/{@code LOCKED}라, AS-IS 표기로 바꿔 비교한다
     * ({@code Manager.getAsIsStatusCode()}와 같은 규칙).
     */
    public boolean matchesStatus(String statusCode) {
        if (srchStatusCode == null || srchStatusCode.isBlank()) {
            return true;
        }
        String asIs = "LOCKED".equals(statusCode) ? "2" : "9";
        return srchStatusCode.equals(asIs);
    }

    /** 검색구분 + 검색어 - 지점명만 부분일치, 나머지는 완전일치(AS-IS 매퍼 그대로). */
    public boolean matchesKeyword(String psitnNm, String loginId, String userName, String empId) {
        if (srchValue == null || srchValue.isBlank()) {
            return true;
        }
        String keyword = srchValue.trim();
        return switch (srchKey == null ? DEFAULT_SRCH_KEY : srchKey) {
            case "LOGIN_ID" -> keyword.equals(loginId);
            case "USER_NAME" -> keyword.equals(userName);
            case "EMP_ID" -> keyword.equals(empId);
            default -> psitnNm != null && psitnNm.contains(keyword);
        };
    }
}
