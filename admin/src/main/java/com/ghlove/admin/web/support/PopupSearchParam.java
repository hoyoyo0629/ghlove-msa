package com.ghlove.admin.web.support;

import lombok.Getter;
import lombok.Setter;

/**
 * AS-IS saleson.shop.popup.domain.PopupSearchParam 재현 - 팝업 목록 검색조건.
 * 팝업상태/팝업형태는 "0"이 전체(조건 제외)이고, 검색구분(where)은 AS-IS JSP에 SUBJECT 하나뿐이다.
 * 화면이 {@code <form:form modelAttribute="popupSearchParam">}로 값을 되돌려 그리므로
 * 검색 후에도 선택값이 유지되어야 한다.
 */
@Getter
@Setter
public class PopupSearchParam {

    /** AS-IS <form:option value="0">전체 - 조건에서 제외되는 값. */
    public static final String ALL = "0";

    private String popupClose = ALL;
    private String popupStyle = ALL;
    private String where = "SUBJECT";
    private String query = "";
    private String startDate = "";
    private String endDate = "";
    private int page = 1;

    /** '0'(전체)이거나 비어 있으면 조건에서 빼야 하므로 null로 바꿔 넘긴다. */
    public String popupCloseCondition() {
        return condition(popupClose);
    }

    public String popupStyleCondition() {
        return condition(popupStyle);
    }

    public String startDateCondition() {
        return blankToNull(startDate);
    }

    public String endDateCondition() {
        return blankToNull(endDate);
    }

    public String queryCondition() {
        return blankToNull(query);
    }

    private static String condition(String value) {
        return (value == null || value.isBlank() || ALL.equals(value)) ? null : value;
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
