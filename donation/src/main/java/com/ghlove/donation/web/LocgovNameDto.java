package com.ghlove.donation.web;

/** 다른 서비스(예: order의 장바구니 지자체별 그룹핑)가 지자체명을 표시하기 위한 조회 응답. */
public record LocgovNameDto(String locgovCode, String displayName) {
}
