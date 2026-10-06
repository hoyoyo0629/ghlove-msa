package com.ghlove.admin.web.support;

import lombok.Getter;
import lombok.Setter;

/**
 * AS-IS saleson.common.sms.SmsIpsParam 재현 - 문자전송이력 검색조건.
 * 문자 구분(smsTypeStr = SVC_ID, 빈값이면 전체), 검색내용(query: 이름·전화번호 등),
 * 생성일 범위(searchStartDate ~ searchEndDate, yyyyMMdd).
 */
@Getter
@Setter
public class SmsLogParam {

    private String smsTypeStr = "";
    private String query = "";
    private String searchStartDate = "";
    private String searchEndDate = "";
    private int itemsPerPage = 10;
    private int page = 1;
}
