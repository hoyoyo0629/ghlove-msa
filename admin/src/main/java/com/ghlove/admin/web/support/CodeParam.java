package com.ghlove.admin.web.support;

import lombok.Getter;
import lombok.Setter;

/**
 * AS-IS saleson.shop.code.support.CodeParam 재현 - 공통코드 목록 검색조건.
 * 왼쪽 코드구분 패널을 클릭하면 hidden codeType에 값을 넣고 폼을 submit한다(AS-IS fnCodeSearch).
 * 검색구분(where)은 AS-IS 셀렉트에 ID(코드값)·LABEL(코드라벨) 둘뿐이다.
 */
@Getter
@Setter
public class CodeParam {

    private String codeType = "";
    private String where = "ID";
    private String query = "";
    private int page = 1;
}
