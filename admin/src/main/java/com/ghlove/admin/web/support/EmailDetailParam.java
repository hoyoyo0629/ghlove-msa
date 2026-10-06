package com.ghlove.admin.web.support;

import lombok.Getter;
import lombok.Setter;

/** AS-IS saleson.shop.email.support.EmailDetailParam - 상세화면 발송인원 목록의 페이징 조건. */
@Getter
@Setter
public class EmailDetailParam {

    private long emailId;

    private int page = 1;

    private int itemsPerPage = 10;
}
