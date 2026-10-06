package com.ghlove.admin.web.support;

import lombok.Getter;
import lombok.Setter;

/**
 * AS-IS saleson.shop.qustnr.support.QustnrSearchParam 재현 - 설문 목록 검색조건.
 * AS-IS는 설문명(searchTxt) 하나만 검색하고, itemsPerPage가 0이면 컨트롤러가 10으로 채운다.
 */
@Getter
@Setter
public class QustnrSearchParam {

    private String searchTxt = "";
    private int itemsPerPage = 10;
    private int page = 1;
}
