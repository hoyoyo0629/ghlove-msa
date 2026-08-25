package com.ghlove.gift.service;

import java.util.List;

/**
 * 답례품몰 GNB "전체 카테고리" 메가메뉴 트리 (대분류 → 중분류 → 개별품목 3단계).
 * classpath:categories.json에서 로드한다 - 실제 운영 사이트(ilovegohyang.go.kr)의
 * GET /api/category 응답을 그대로 옮긴 실제 데이터(우리 gift 도메인의 GIFT_CATEGORY
 * 6개 대분류 코드에 맞춰 code만 매핑). 세부(중분류/개별품목) 클릭은 이 MSA의 gift 도메인이
 * 그 세밀도의 코드를 갖고 있지 않아 소속 대분류로만 필터링된다.
 */
public record CategoryTreeGroup(String code, String name, List<CategoryTreeItem> categories) {

    public record CategoryTreeItem(String name, List<String> children) {
    }
}
