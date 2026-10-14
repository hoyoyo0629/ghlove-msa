package com.ghlove.admin.service;

import com.ghlove.admin.domain.Manager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 통계 > 답례품 구매현황 > 카테고리별 (AS-IS {@code getCategoriesStatsList}).
 *
 * <p>지자체 × 카테고리(6종) 판매건수·금액 피벗. AS-IS는 {@code OP_ORDER_ITEM}을
 * {@code OP_ITEM_CATEGORY}/{@code OP_CATEGORY_GROUP}과 조인해 카테고리를 가르는데, TO-BE는
 * 품목 하나가 {@code gift.Gift.categoryCode} 하나를 가진다(AS-IS 6종과 1:1 대응 - 월별 지자체별
 * 답례품 현황과 같다). ORDER_STATUS='40' 단일코드는 {@code CONFIRMED}로 옮긴다.
 * 카테고리가 없는(또는 6종 밖) 품목은 AS-IS 내부조인처럼 집계에서 빠진다.
 */
@Component
@RequiredArgsConstructor
public class ShopStatisticsCategoriesService {

    /** 화면(AS-IS 헤더) 열 순서 - 수산물/농축산물/지역상품권/생활용품/가공식품/관광서비스. */
    public static final List<String> CATEGORY_ORDER =
            List.of("SEAFOOD", "AGRI", "VOUCHER", "LIVING", "PROCESSED", "TOUR");

    private final OrderAdminClient orderAdminClient;
    private final LocgovClient locgovClient;
    private final GiftClient giftClient;

    public List<Row> search(Manager manager, String startDate, String endDate, String locgovCode) {
        Map<Long, String> categoryByItem = new HashMap<>();
        for (GiftClient.ItemSnapshot i : giftClient.allForResync()) {
            if (CATEGORY_ORDER.contains(i.categoryCode())) {
                categoryByItem.put(i.itemId(), i.categoryCode());
            }
        }
        Map<String, LocgovClient.LocgovInfo> locgovs = new HashMap<>();
        for (LocgovClient.LocgovInfo l : locgovClient.allLocgovs()) {
            locgovs.put(l.locgovCode(), l);
        }

        Map<String, long[]> byLocgov = new LinkedHashMap<>();
        for (OrderAdminClient.LocgovItemQtyRow r : orderAdminClient.categoryItemStats(manager, startDate, endDate)) {
            String category = categoryByItem.get(r.itemId());
            if (category == null || !locgovs.containsKey(r.locgovCode())) {
                continue;
            }
            if (locgovCode != null && !locgovCode.isBlank() && !locgovCode.equals(r.locgovCode())) {
                continue;
            }
            long[] v = byLocgov.computeIfAbsent(r.locgovCode(), k -> new long[CATEGORY_ORDER.size() * 2]);
            int idx = CATEGORY_ORDER.indexOf(category) * 2;
            v[idx] += r.quantity();
            v[idx + 1] += r.amount();
        }

        List<Row> rows = new ArrayList<>();
        byLocgov.forEach((code, v) -> {
            LocgovClient.LocgovInfo l = locgovs.get(code);
            long totalCnt = 0;
            long totalAmt = 0;
            for (int i = 0; i < v.length; i += 2) {
                totalCnt += v[i];
                totalAmt += v[i + 1];
            }
            rows.add(new Row(l.upperLocgovNm(), l.locgovNm(), v.clone(), totalCnt, totalAmt));
        });
        // AS-IS ORDER BY LOCGOV_NM
        rows.sort(Comparator.comparing(r -> r.locgovNm() == null ? "" : r.locgovNm()));
        return rows;
    }

    /** counts[2k]=건수, counts[2k+1]=금액 (k = {@link #CATEGORY_ORDER} 인덱스). */
    public record Row(String upperLocgovNm, String locgovNm, long[] counts, long totalCnt, long totalAmt) {
        public long cnt(int k) {
            return counts[k * 2];
        }

        public long amt(int k) {
            return counts[k * 2 + 1];
        }
    }
}
