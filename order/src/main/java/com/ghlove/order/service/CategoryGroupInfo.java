package com.ghlove.order.service;

import java.util.List;

/** Response shape of gift service's read-only GET /api/categories lookup (3단계: 대분류 → 중분류 → 개별품목). */
public record CategoryGroupInfo(String code, String name, List<CategoryItem> categories) {

    public record CategoryItem(String name, List<String> children) {
    }
}
