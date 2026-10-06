package com.ghlove.admin.web.support;

import lombok.Getter;
import lombok.Setter;

/** AS-IS saleson.shop.message.support.MessageParam - 메세지 관리 목록 검색조건. */
@Getter
@Setter
public class MessageParam {

    /** 검색구분 - ID(아이디) / MESSAGE(메세지). */
    private String where;

    private String query;

    private int page = 1;

    /** 화면출력 - AS-IS는 폼이 itemsPerPage를 직접 넘긴다(10/20/50/100). */
    private int itemsPerPage = 10;
}
