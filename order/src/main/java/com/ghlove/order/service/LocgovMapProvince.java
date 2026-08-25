package com.ghlove.order.service;

import java.util.List;

/**
 * AS-IS components/ui/lclgv_chc/map.vue의 lclgvUpperList 재현 - "지자체몰 선택하기" 지도
 * 팝업의 시/도 배치 메타데이터(좌표 클래스·강조 지도 이미지). 시/군/구 실데이터(LocgovInfo)와
 * 달리 이건 지도 UI 자체의 배치 정보라 donation 서비스가 아니라 프레젠테이션 계층(order)에 둔다.
 */
public record LocgovMapProvince(String code, String name, String mapLabel, String cssClass, String mapImage) {

    public static final List<LocgovMapProvince> ALL = List.of(
            new LocgovMapProvince("11000", "서울특별시", "서울", "text", "vtmap00.png"),
            new LocgovMapProvince("26000", "부산광역시", "부산", "text15", "vtmap01.png"),
            new LocgovMapProvince("27000", "대구광역시", "대구", "text10", "vtmap02.png"),
            new LocgovMapProvince("28000", "인천광역시", "인천", "text1", "vtmap03.png"),
            new LocgovMapProvince("12000", "전남광주통합특별시", "전남광주", "text12", "vtmap04.png"),
            new LocgovMapProvince("30000", "대전광역시", "대전", "text7", "vtmap05.png"),
            new LocgovMapProvince("31000", "울산광역시", "울산", "text11", "vtmap06.png"),
            new LocgovMapProvince("36000", "세종특별자치시", "세종", "text6", "vtmap07.png"),
            new LocgovMapProvince("41000", "경기도", "경기도", "text2", "vtmap08.png"),
            new LocgovMapProvince("51000", "강원특별자치도", "강원자치도", "text3", "vtmap09.png"),
            new LocgovMapProvince("43000", "충청북도", "충청북도", "text5", "vtmap10.png"),
            new LocgovMapProvince("44000", "충청남도", "충청남도", "text4", "vtmap11.png"),
            new LocgovMapProvince("52000", "전북특별자치도", "전북자치도", "text9", "vtmap12.png"),
            new LocgovMapProvince("47000", "경상북도", "경상북도", "text8", "vtmap14.png"),
            new LocgovMapProvince("48000", "경상남도", "경상남도", "text16", "vtmap15.png"),
            new LocgovMapProvince("50000", "제주특별자치도", "제주자치도", "text14", "vtmap16.png")
    );
}
