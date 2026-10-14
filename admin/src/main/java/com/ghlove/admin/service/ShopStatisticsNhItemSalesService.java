package com.ghlove.admin.service;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.repository.NhStatDataTempRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 통계 > 답례품 구매현황 > 농협 사업자 답례품 현황 (AS-IS {@code getNhItemSalesStats}).
 *
 * <p>AS-IS는 {@code OP_SELLER.BUSINESS_NUMBER}를 {@code G_NH_STAT_DATA_TEMP}(외부에서 받은
 * 농협 사업자번호 목록, AS-IS 6,779건)와 매칭해 "농협 소속 판매자"를 가린다 - <b>은행명이
 * "농협"인 판매자가 아니다</b>(이전 구현은 bankName.contains("농협") 휴리스틱으로 잘못
 * 짚었던 것, 2026-10-14 바로잡음).
 *
 * <p>품목 목록은 <b>카탈로그 전체</b>다(AS-IS 베이스 서브쿼리에 승인·노출 필터가 없다 -
 * {@link ShopStatisticsMonthLocgovService}의 카탈로그 스냅샷과 다른 점). 주문 통계만 선택한
 * 년/월로 좁혀지고, 주문이 없는 품목도 0으로 한 행 나온다(LEFT JOIN).
 *
 * <p>"하위카테고리"(AS-IS {@code OP_CATEGORY} 세부분류)·"상품코드"(AS-IS
 * {@code ITEM_USER_CODE})는 TO-BE 답례품 모델에 대응 필드가 없다 - 지자체별 화면의 "답례품
 * 코드" 때와 같은 사정으로 "-"를 그대로 둔다.
 */
@Component
@RequiredArgsConstructor
public class ShopStatisticsNhItemSalesService {

    /** [[admin-sales-day-statistics-port]] 월별 지자체별 답례품 현황과 같은 GIFT_CATEGORY 라벨. */
    private static final Map<String, String> CATEGORY_LABELS = Map.of(
            "TOUR", "관광서비스", "AGRI", "농축산물", "SEAFOOD", "수산물",
            "PROCESSED", "가공식품", "LIVING", "생활용품", "VOUCHER", "지역상품권");

    private static final String NO_DATA = "-";

    private final NhStatDataTempRepository nhStatDataTempRepository;
    private final OrderAdminClient orderAdminClient;
    private final LocgovClient locgovClient;
    private final GiftClient giftClient;

    public SearchResult search(Manager manager, SearchParam param) {
        Set<String> nhBusinessNumbers = nhStatDataTempRepository.findAll().stream()
                .map(com.ghlove.admin.domain.NhStatDataTemp::getBusinessNo)
                .collect(java.util.stream.Collectors.toSet());

        Map<Long, GiftClient.SellerDetail> nhSellersById = giftClient.sellers().stream()
                .filter(s -> s.businessNumber() != null && nhBusinessNumbers.contains(s.businessNumber()))
                .collect(java.util.stream.Collectors.toMap(GiftClient.SellerDetail::sellerId, s -> s, (a, b) -> a));

        Map<String, LocgovClient.LocgovInfo> locgovsByCode = locgovClient.allLocgovs().stream()
                .collect(java.util.stream.Collectors.toMap(LocgovClient.LocgovInfo::locgovCode, l -> l, (a, b) -> a));

        String ym = param.searchYear() + param.searchMonth();
        Map<Long, OrderAdminClient.NhItemSaleRow> saleByItem = orderAdminClient
                .nhItemSaleStats(manager, ym, ym).stream()
                .collect(java.util.stream.Collectors.toMap(OrderAdminClient.NhItemSaleRow::itemId, r -> r, (a, b) -> a));

        List<NhItemRow> rows = new ArrayList<>();
        for (GiftClient.ItemSnapshot item : giftClient.allForResync()) {
            GiftClient.SellerDetail seller = nhSellersById.get(item.sellerId());
            if (seller == null) {
                continue;
            }
            if (!matchesLocgov(item.locgovCode(), param)) {
                continue;
            }
            if (!matchesQuery(seller, item, param)) {
                continue;
            }
            LocgovClient.LocgovInfo locgov = locgovsByCode.get(item.locgovCode());
            OrderAdminClient.NhItemSaleRow sale = saleByItem.get(item.itemId());
            rows.add(new NhItemRow(
                    seller.businessNumber(), seller.loginId(), seller.companyName(),
                    locgov != null ? locgov.upperLocgovNm() : "", locgov != null ? locgov.locgovNm() : "",
                    item.itemName(), CATEGORY_LABELS.getOrDefault(item.categoryCode(), NO_DATA), NO_DATA,
                    item.salePrice() != null ? item.salePrice() : 0,
                    sale != null ? sale.orderCount() : 0, sale != null ? sale.saleQuantity() : 0,
                    sale != null ? sale.saleAmount() : 0, NO_DATA,
                    nullToEmpty(item.locgovCode()), item.sellerId()));
        }

        rows.sort(Comparator.comparing((NhItemRow r) -> nullToEmpty(r.businessNumber()))
                .thenComparing(r -> nullToEmpty(r.locgovCodeForSort()))
                .thenComparingLong(NhItemRow::sellerIdForSort));

        int total = rows.size();
        int from = Math.min(param.startRow(), total);
        int to = Math.min(from + param.itemsPerPage(), total);
        return new SearchResult(total, rows.subList(from, to));
    }

    private boolean matchesLocgov(String itemLocgovCode, SearchParam param) {
        if (hasText(param.locgovCode())) {
            return param.locgovCode().equals(itemLocgovCode);
        }
        if (hasText(param.upperLocgovCode())) {
            LocgovClient.LocgovInfo info = itemLocgovCode == null ? null
                    : locgovClient.allLocgovs().stream()
                            .filter(l -> l.locgovCode().equals(itemLocgovCode)).findFirst().orElse(null);
            return info != null && param.upperLocgovCode().equals(info.upperLocgovCode());
        }
        return true;
    }

    private boolean matchesQuery(GiftClient.SellerDetail seller, GiftClient.ItemSnapshot item, SearchParam param) {
        if (!hasText(param.query()) || !hasText(param.where())) {
            return true;
        }
        String target = switch (param.where()) {
            case "BUSINESS_NUMBER" -> seller.businessNumber();
            case "LOGIN_ID" -> seller.loginId();
            case "COMPANY_NAME" -> seller.companyName();
            case "ITEM_NAME" -> item.itemName();
            default -> null;
        };
        return target != null && target.contains(param.query());
    }

    private static boolean hasText(String s) {
        return s != null && !s.isBlank();
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    public record SearchResult(int totalCount, List<NhItemRow> rows) {
    }

    /** 정렬용 locgovCode/sellerId를 따로 들고 다닌다(표에는 지자체명/사업자번호만 보인다). */
    public record NhItemRow(String businessNumber, String loginId, String companyName,
                             String upperLocgovNm, String locgovNm, String itemName,
                             String categoryNm, String categoryDetailNm, long price,
                             long orderCnt, long saleCnt, long totalSalePrice, String itemUserCode,
                             String locgovCodeForSort, Long sellerIdForSort) {
    }

    /** AS-IS StatisticsParam 중 이 화면이 쓰는 것만 - 컨트롤러가 기본값까지 다 채워 넘긴다. */
    public record SearchParam(String upperLocgovCode, String locgovCode, String where, String query,
                               String searchYear, String searchMonth, int page, int itemsPerPage) {
        public int startRow() {
            return (Math.max(page, 1) - 1) * itemsPerPage;
        }
    }
}
