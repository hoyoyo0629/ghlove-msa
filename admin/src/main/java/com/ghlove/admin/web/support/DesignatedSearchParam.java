package com.ghlove.admin.web.support;

import lombok.Getter;
import lombok.Setter;

/**
 * 특정사업 목록(메뉴 17101) 검색조건 - AS-IS {@code DesignatedDonationSearchParam} 중
 * list.jsp가 보내는 것 그대로다.
 *
 * <p><b>AS-IS 결함 1건 - 고쳤다</b>: 사업명 입력칸은 {@code query}로 전송하는데 목록 SQL은
 * {@code searchKeyword}를 본다(화면이 예전 필드명에서 바뀌었는데 SQL이 따라가지 않았다).
 * 그래서 AS-IS에서는 <b>사업명 검색이 전혀 걸리지 않는다</b> - TO-BE는 {@code query}로 건다.
 *
 * <p>출력수는 AS-IS가 <b>10/50/100/200/500</b>이다(다른 화면과 다르다).
 */
@Getter
@Setter
public class DesignatedSearchParam {

    /** 광역 - 시군구를 고르면 그쪽이 우선한다(AS-IS choose/when). */
    private String upperLocgovCode;

    private String locgovCode;

    /** 사업구분 - TO-BE 공통코드 {@code DSGN_BSNS_TYPE}(100~400). */
    private String bsnsType;

    /** 모금기간 시작(yyyyMMdd) - 종료와 둘 다 있으면 "기간이 겹치는 사업"을 찾는다. */
    private String prjStDt;

    private String prjEdDt;

    /** 사업명(부분일치). */
    private String query;

    /** 상태 - ""(전체)/2(진행)/9(종료)/1(대기). 화면 값이고 서버에서 TO-BE 값으로 옮긴다. */
    private String prjStatus;

    /** 공개여부 - ""(전체)/Y/N. */
    private String displayFlag;

    private int page = 1;

    private int itemsPerPage = 10;

    public void applyDefaults() {
        if (page <= 0) {
            page = 1;
        }
        if (itemsPerPage <= 0) {
            itemsPerPage = 10;
        }
    }
}
