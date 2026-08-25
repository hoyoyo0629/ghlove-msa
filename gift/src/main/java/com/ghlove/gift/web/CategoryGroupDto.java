package com.ghlove.gift.web;

import java.util.List;

/** 답례품몰 GNB "전체 카테고리" 메가메뉴용 - order 서비스가 이 API를 조회해서 그린다. */
public record CategoryGroupDto(String code, String name, List<String> subCategories) {
}
