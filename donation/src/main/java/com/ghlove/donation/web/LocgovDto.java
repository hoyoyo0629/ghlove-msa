package com.ghlove.donation.web;

/** order의 장바구니 GNB "지자체몰 선택하기" 지도 팝업이 시/도 -> 시/군/구 목록을 그리기 위한 전체 조회 응답. */
public record LocgovDto(String locgovCode, String locgovNm, String upperLocgovCode, String upperLocgovNm) {
}
