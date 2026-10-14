package com.ghlove.admin.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 통계 > 답례품 선호도 (AS-IS {@code getOpWishlistList}/{@code getOpWishlistListCount}).
 *
 * <p>찜({@code OP_WISHLIST}) 행을 품목별로 세어 "등록수" 내림차순으로 보여준다. AS-IS는
 * OP_ITEM·OP_SELLER·G_LOCGOV를 내부조인하므로 품목/판매자/지자체 중 하나라도 없으면 그 찜은
 * 집계에서 빠진다. 찜(gift)·품목/판매자(gift)·지자체(donation)가 서비스별로 나뉘어 있어 admin이
 * 각 클라이언트로 붙인다. 지자체는 AS-IS {@code S.LOCGOV_CODE}(판매자 소속)인데 TO-BE 품목이
 * 자기 {@code locgovCode}를 갖고 있어 그걸 쓴다.
 * "상품코드"(AS-IS {@code ITEM_CODE})는 TO-BE 모델에 없어 링크는 itemId로 만든다.
 */
@Component
@RequiredArgsConstructor
public class ShopStatisticsWishService {

    private final GiftClient giftClient;
    private final LocgovClient locgovClient;

    /** @param locgovCode 우선 적용, 없으면 upperLocgovCode, 없으면 전체. itemName 은 부분일치. */
    public List<WishRow> search(String upperLocgovCode, String locgovCode, String itemName) {
        Map<Long, GiftClient.ItemSnapshot> items = new HashMap<>();
        for (GiftClient.ItemSnapshot i : giftClient.allForResync()) {
            items.put(i.itemId(), i);
        }
        Map<Long, String> companyBySeller = new HashMap<>();
        for (GiftClient.SellerDetail s : giftClient.sellers()) {
            companyBySeller.put(s.sellerId(), s.companyName());
        }
        Map<String, LocgovClient.LocgovInfo> locgovs = new HashMap<>();
        for (LocgovClient.LocgovInfo l : locgovClient.allLocgovs()) {
            locgovs.put(l.locgovCode(), l);
        }

        Map<Long, Long> countByItem = new HashMap<>();
        for (GiftClient.WishlistEntry w : giftClient.wishlistAll()) {
            countByItem.merge(w.itemId(), 1L, Long::sum);
        }

        List<WishRow> rows = new ArrayList<>();
        countByItem.forEach((itemId, count) -> {
            GiftClient.ItemSnapshot item = items.get(itemId);
            if (item == null || !companyBySeller.containsKey(item.sellerId())) {
                return;
            }
            LocgovClient.LocgovInfo l = item.locgovCode() == null ? null : locgovs.get(item.locgovCode());
            if (l == null) {
                return;
            }
            if (hasText(locgovCode)) {
                if (!locgovCode.equals(l.locgovCode())) {
                    return;
                }
            } else if (hasText(upperLocgovCode) && !upperLocgovCode.equals(l.upperLocgovCode())) {
                return;
            }
            if (hasText(itemName) && (item.itemName() == null || !item.itemName().contains(itemName))) {
                return;
            }
            rows.add(new WishRow(count, itemId, item.itemName(), companyBySeller.get(item.sellerId()),
                    item.salePrice() == null ? 0 : item.salePrice(),
                    (l.upperLocgovNm() != null ? l.upperLocgovNm() + " " : "") + l.locgovNm()));
        });
        // AS-IS ORDER BY REGISTRATION_NUMBER DESC (동률은 DB 임의순 - 안정적으로 품목ID순)
        rows.sort(Comparator.comparingLong(WishRow::registrationNumber).reversed()
                .thenComparingLong(WishRow::itemId));
        return rows;
    }

    private static boolean hasText(String s) {
        return s != null && !s.isBlank();
    }

    public record WishRow(long registrationNumber, long itemId, String itemName, String companyName,
                           long salePrice, String locgovNm) {
    }
}
