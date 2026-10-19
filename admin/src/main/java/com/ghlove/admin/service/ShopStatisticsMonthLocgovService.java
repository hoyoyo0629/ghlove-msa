package com.ghlove.admin.service;

import com.ghlove.admin.domain.Manager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 통계 > 답례품 구매현황 > 월별 지자체별 답례품 현황 (AS-IS {@code getMonthLocgovItemStat}).
 *
 * <p>표는 두 절반으로 나뉜다.
 * <ul>
 *   <li><b>왼쪽(선정/등록 현황, 7칸)</b> - 지자체별 "현재 승인·노출중인 답례품" 카탈로그를
 *   6개 카테고리(TOUR/AGRI/SEAFOOD/PROCESSED/LIVING/VOUCHER)로 나눈 종류 수. <b>월 선택과
 *   무관한 스냅샷</b>이다(AS-IS 쿼리가 이 카운트를 월별 주문 서브쿼리가 아니라 카탈로그
 *   LEFT JOIN 바깥에서 센다 - SQL 재확인 완료). TO-BE {@code gift.Gift.categoryCode}가 AS-IS
 *   {@code OP_CATEGORY_GROUP.CODE}(tour/farm/aquatic/process/daily/ticket)와 1:1로 대응한다
 *   (라벨도 동일 - gift 자체 OP_COMMON_CODE "GIFT_CATEGORY" 확인 완료, 2026-10-14).</li>
 *   <li><b>오른쪽(구매 현황, 4칸)</b> - 선택한 년/월의 주문건수·금액·최다판매 TOP3(동률 허용).
 *   취소 주문은 아예 뺀다(AS-IS ORDER_STATUS IN (...) 활성 상태만, 별도 취소 열 없음).</li>
 * </ul>
 *
 * <p>"합계(전체)" 요약행은 검색조건(지자체)으로 좁혀진 결과 안에서의 합계다 - AS-IS도
 * {@code SUM() OVER()}가 WHERE로 걸러진 행 집합 안에서만 합산한다.
 */
@Component
@RequiredArgsConstructor
public class ShopStatisticsMonthLocgovService {

    private static final String STATUS_APPROVED = "1";   // AS-IS OP_ITEM.DATA_STATUS_CODE 1:정상
    private static final String DISPLAY_ON = "Y";

    /** AS-IS 표 헤더 노출 순서(합계 제외) - gift의 GIFT_CATEGORY 공통코드 ordering과도 같다. */
    private static final List<String> CATEGORY_CODES =
            List.of("TOUR", "AGRI", "SEAFOOD", "PROCESSED", "LIVING", "VOUCHER");

    private final OrderAdminClient orderAdminClient;
    private final LocgovClient locgovClient;
    private final GiftClient giftClient;

    public Result stats(Manager manager, String year, String month, String upperLocgovCode, String locgovCode) {
        List<LocgovClient.LocgovInfo> scopedLocgovs = scopedLocgovs(upperLocgovCode, locgovCode);

        Map<String, List<GiftClient.ItemSnapshot>> liveItemsByLocgov = liveItems().stream()
                .filter(i -> i.locgovCode() != null)
                .collect(java.util.stream.Collectors.groupingBy(GiftClient.ItemSnapshot::locgovCode));

        String ym = year + month;
        List<OrderAdminClient.GroupRow> allOrderRows = orderAdminClient.locgovGroupStats(
                manager, "ITEM_ALL", ym, ym, null, null);
        Map<String, List<OrderAdminClient.GroupRow>> orderRowsByLocgov = allOrderRows.stream()
                .collect(java.util.stream.Collectors.groupingBy(OrderAdminClient.GroupRow::locgovCode));

        List<LocgovRow> rows = new ArrayList<>();
        List<OrderAdminClient.GroupRow> scopedOrderRows = new ArrayList<>();
        for (LocgovClient.LocgovInfo l : scopedLocgovs) {
            List<GiftClient.ItemSnapshot> catalog = liveItemsByLocgov.getOrDefault(l.locgovCode(), List.of());
            List<OrderAdminClient.GroupRow> orderRows = orderRowsByLocgov.getOrDefault(l.locgovCode(), List.of());
            scopedOrderRows.addAll(orderRows);

            CategoryCounts counts = CategoryCounts.of(catalog);
            long orderCount = orderRows.stream().mapToLong(OrderAdminClient.GroupRow::saleCount).sum();
            long saleAmount = orderRows.stream().mapToLong(OrderAdminClient.GroupRow::saleAmount).sum();
            String displayName = (l.upperLocgovNm() != null ? l.upperLocgovNm() : "") ;
            rows.add(new LocgovRow(l.locgovCode(), displayName, l.locgovNm(), counts, orderCount, saleAmount,
                    topN(orderRows, 3)));
        }
        rows.sort(Comparator.comparing(LocgovRow::locgovCode));

        CategoryCounts totalCounts = CategoryCounts.sum(rows.stream().map(LocgovRow::categoryCounts).toList());
        long totalOrderCount = rows.stream().mapToLong(LocgovRow::orderCount).sum();
        long totalSaleAmount = rows.stream().mapToLong(LocgovRow::saleAmount).sum();
        LocgovRow grandTotal = new LocgovRow(null, "합계(전체)", "", totalCounts, totalOrderCount, totalSaleAmount,
                topN(scopedOrderRows, 3));

        return new Result(grandTotal, rows);
    }

    private List<LocgovClient.LocgovInfo> scopedLocgovs(String upperLocgovCode, String locgovCode) {
        List<LocgovClient.LocgovInfo> all = locgovClient.allLocgovs();
        if (hasText(locgovCode)) {
            return all.stream().filter(l -> locgovCode.equals(l.locgovCode())).toList();
        }
        if (hasText(upperLocgovCode)) {
            return all.stream().filter(l -> upperLocgovCode.equals(l.upperLocgovCode())).toList();
        }
        return all;
    }

    private List<GiftClient.ItemSnapshot> liveItems() {
        return giftClient.allForResync().stream()
                .filter(i -> STATUS_APPROVED.equals(i.dataStatusCode()) && DISPLAY_ON.equals(i.displayFlag()))
                .toList();
    }

    /** AS-IS DENSE_RANK + GROUP_CONCAT(동률이면 이름을 줄바꿈으로 묶는다) 재현 - 건수가 0인
     *  품목은 애초에 orderRows에 없으므로(그 달 주문이 없으면 행 자체가 안 생긴다) 별도 제외가
     *  필요 없다. */
    private static List<TopItem> topN(List<OrderAdminClient.GroupRow> items, int n) {
        List<Long> distinctCounts = items.stream()
                .map(OrderAdminClient.GroupRow::saleCount)
                .distinct()
                .sorted(Comparator.reverseOrder())
                .toList();
        List<TopItem> result = new ArrayList<>();
        for (int rank = 0; rank < Math.min(n, distinctCounts.size()); rank++) {
            long cnt = distinctCounts.get(rank);
            List<String> names = items.stream()
                    .filter(r -> r.saleCount() == cnt)
                    .map(OrderAdminClient.GroupRow::itemName)
                    .toList();
            result.add(new TopItem(String.join("\n", names), cnt));
        }
        while (result.size() < n) {
            result.add(new TopItem("", 0));
        }
        return result;
    }

    private static boolean hasText(String s) {
        return s != null && !s.isBlank();
    }

    public record Result(LocgovRow grandTotal, List<LocgovRow> rows) {
    }

    public record LocgovRow(String locgovCode, String upperLocgovNm, String locgovNm, CategoryCounts categoryCounts,
                             long orderCount, long saleAmount, List<TopItem> top3) {
    }

    public record TopItem(String name, long count) {
    }

    /** 합계(total) + AS-IS 표시 순서대로의 6개 카테고리 칸. */
    public record CategoryCounts(long total, long tour, long agri, long seafood, long processed, long living,
                                  long voucher) {

        static CategoryCounts of(List<GiftClient.ItemSnapshot> catalog) {
            Map<String, Long> byCode = catalog.stream()
                    .filter(i -> CATEGORY_CODES.contains(i.categoryCode()))
                    .collect(java.util.stream.Collectors.groupingBy(
                            GiftClient.ItemSnapshot::categoryCode, LinkedHashMap::new,
                            java.util.stream.Collectors.counting()));
            long tour = byCode.getOrDefault("TOUR", 0L);
            long agri = byCode.getOrDefault("AGRI", 0L);
            long seafood = byCode.getOrDefault("SEAFOOD", 0L);
            long processed = byCode.getOrDefault("PROCESSED", 0L);
            long living = byCode.getOrDefault("LIVING", 0L);
            long voucher = byCode.getOrDefault("VOUCHER", 0L);
            return new CategoryCounts(tour + agri + seafood + processed + living + voucher,
                    tour, agri, seafood, processed, living, voucher);
        }

        static CategoryCounts sum(List<CategoryCounts> list) {
            long total = 0, tour = 0, agri = 0, seafood = 0, processed = 0, living = 0, voucher = 0;
            for (CategoryCounts c : list) {
                total += c.total();
                tour += c.tour();
                agri += c.agri();
                seafood += c.seafood();
                processed += c.processed();
                living += c.living();
                voucher += c.voucher();
            }
            return new CategoryCounts(total, tour, agri, seafood, processed, living, voucher);
        }
    }
}
