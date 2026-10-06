package com.ghlove.admin.web.support;

import lombok.Getter;
import lombok.Setter;

/**
 * 사업부서 관리(특정사업 메뉴 하위) 검색조건 - AS-IS {@code DesignatedPartParam} 중
 * {@code part/list.jsp}가 보내는 것 그대로다.
 *
 * <p>AS-IS 화면의 지자체 선택은 <b>ROLE_ADMIN_1~4에게만</b> 보인다(지자체담당자는 자기 지자체 고정).
 */
@Getter
@Setter
public class DepartmentSearchParam {

    private String upperLocgovCode;

    private String locgovCode;

    /** 부서명(부분일치). */
    private String deptNm;

    /** 사용유무 - ""(전체)/Y(사용)/N(미사용). */
    private String useYn;

    /** 등록일 범위(yyyyMMdd). */
    private String searchStDt;

    private String searchEdDt;
}
