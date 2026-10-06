package com.ghlove.admin.web.support;

import lombok.Getter;
import lombok.Setter;

/**
 * AS-IS saleson.shop.policy.support.PolicyParam 재현 - 약관관리 목록 검색조건.
 * 검색구분(where)은 TITLE·CONTENT 둘, 정책구분/전시여부는 빈값이면 전체다
 * (policy-mapper.xml sqlSearchWhere). itemsPerPage 기본 10은 AS-IS 컨트롤러가 채운다.
 */
@Getter
@Setter
public class PolicyParam {

    private String where = "TITLE";
    private String query = "";
    private String policyType = "";
    private String exhibitionStatus = "";
    private int itemsPerPage = 10;
    private int page = 1;

    public String queryCondition() {
        return (query == null || query.isBlank()) ? null : query;
    }

    public String policyTypeCondition() {
        return (policyType == null || policyType.isBlank()) ? null : policyType;
    }

    public String exhibitionStatusCondition() {
        return (exhibitionStatus == null || exhibitionStatus.isBlank()) ? null : exhibitionStatus;
    }
}
