package com.ghlove.admin.service;

import com.ghlove.admin.domain.OrderLedger;
import com.ghlove.admin.repository.OrderLedgerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * AS-IS opmanager/shop-statistics의 "판매(sales)/대시보드/선호도" 스위트 재구현 (Tier B - report
 * 스위트는 {@link ReportStatisticsService}가 별개로 담당). admin은 order 서비스 DB를 직접 못 읽으므로
 * (DB per Service) StatsService의 ReadModel(OrderLedger)과 gift/member의 크로스서비스 스냅샷을 조인한다.
 *
 * 이 프로젝트는 실결제(PG)가 아니라 전액 포인트 결제이므로 AS-IS의 "매출액(원)"은 전부 "포인트"로
 * 대체된다 - {@link com.ghlove.admin.domain.OrderLedger#getPointAmount()}가 AS-IS PayAmount에 대응.
 * OrderLedger는 취소일자를 별도로 갖지 않고(주문 1건이 상태만 CONFIRMED->CANCELLED로 바뀌는 구조)
 * 취소 통계도 createdDate(최초 주문일) 기준으로 집계한다 - AS-IS처럼 취소 시점 기준 별도 집계는
 * 이 프로젝트의 ReadModel에 없는 데이터라 재현 불가.
 */
@Service
@RequiredArgsConstructor
public class ShopStatisticsTierBService {

    private static final String STATUS_CONFIRMED = "CONFIRMED";
    private static final String STATUS_CANCELLED = "CANCELLED";
    private static final DateTimeFormatter YMD = DateTimeFormatter.BASIC_ISO_DATE;

    private final OrderLedgerRepository orderLedgerRepository;
    private final GiftClient giftClient;
    private final MemberClient memberClient;
    private final LocgovClient locgovClient;

    // ---- 공용 조회/조인 헬퍼 ----

    private List<OrderLedger> allOrders() {
        return orderLedgerRepository.findAll();
    }

    private Map<Long, GiftClient.ItemSnapshot> itemsById() {
        return giftClient.allForResync().stream()
                .collect(Collectors.toMap(GiftClient.ItemSnapshot::itemId, i -> i, (a, b) -> a));
    }

    private Map<Long, GiftClient.SellerDetail> sellersById() {
        return giftClient.sellers().stream()
                .collect(Collectors.toMap(GiftClient.SellerDetail::sellerId, s -> s, (a, b) -> a));
    }

    private Map<Integer, GiftClient.BrandDetail> brandsById() {
        return giftClient.brands().stream()
                .collect(Collectors.toMap(GiftClient.BrandDetail::brandId, b -> b, (a, b) -> a));
    }

    private Map<String, String> locgovNameByCode() {
        return locgovClient.allLocgovs().stream()
                .collect(Collectors.toMap(LocgovClient.LocgovInfo::locgovCode,
                        l -> (l.upperLocgovNm() != null ? l.upperLocgovNm() + " " : "") + l.locgovNm(), (a, b) -> a));
    }

    private boolean isPay(OrderLedger o) {
        return STATUS_CONFIRMED.equals(o.getStatus());
    }

    private boolean isCancel(OrderLedger o) {
        return STATUS_CANCELLED.equals(o.getStatus());
    }

    private long amt(OrderLedger o) {
        return o.getPointAmount() != null ? o.getPointAmount() : 0L;
    }

    private String ymd(LocalDateTime dt) {
        return dt.format(YMD);
    }

    private String defaultStart(String s) {
        return (s != null && !s.isBlank()) ? s : LocalDate.now().minusDays(29).format(YMD);
    }

    private String defaultEnd(String s) {
        return (s != null && !s.isBlank()) ? s : LocalDate.now().format(YMD);
    }

    /** 상세화면 공용 - 회원별/브랜드별/지역별/상품별 드릴다운의 주문 원본 목록. */
    public record OrderRow(String orderId, Long itemId, String itemName, Integer quantity, Long pointAmount,
                            String status, LocalDateTime createdDate) {
    }

    private List<OrderRow> toOrderRows(List<OrderLedger> orders, Map<Long, GiftClient.ItemSnapshot> items) {
        return orders.stream()
                .sorted(Comparator.comparing(OrderLedger::getCreatedDate, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(o -> new OrderRow(o.getOrderId(), o.getItemId(),
                        Optional.ofNullable(items.get(o.getItemId())).map(GiftClient.ItemSnapshot::itemName).orElse("상품#" + o.getItemId()),
                        o.getQuantity(), o.getPointAmount(), o.getStatus(), o.getCreatedDate()))
                .toList();
    }

    // ---- A. 기간별 매출 통계 (day/month/year) ----

    public record PeriodStat(String period, long payCount, long payAmount, long cancelCount, long cancelAmount) {
    }

    public List<PeriodStat> periodStats(String type, String startDate, String endDate) {
        String start = defaultStart(startDate);
        String end = defaultEnd(endDate);
        Map<String, long[]> buckets = new TreeMap<>();
        for (OrderLedger o : allOrders()) {
            if (o.getCreatedDate() == null) {
                continue;
            }
            String day = ymd(o.getCreatedDate());
            if (day.compareTo(start) < 0 || day.compareTo(end) > 0) {
                continue;
            }
            String key = switch (type) {
                case "year" -> day.substring(0, 4);
                case "month" -> day.substring(0, 6);
                default -> day;
            };
            long[] b = buckets.computeIfAbsent(key, k -> new long[4]);
            if (isPay(o)) {
                b[0]++;
                b[1] += amt(o);
            } else if (isCancel(o)) {
                b[2]++;
                b[3] += amt(o);
            }
        }
        return buckets.entrySet().stream()
                .map(e -> new PeriodStat(e.getKey(), e.getValue()[0], e.getValue()[1], e.getValue()[2], e.getValue()[3]))
                .toList();
    }

    // ---- B. 답례품 구매현황 전체 (연+월 결합) ----

    public record AllSalesSummary(String year, PeriodStat yearTotal, List<PeriodStat> monthly) {
    }

    public AllSalesSummary allSales(String year) {
        String y = (year != null && !year.isBlank()) ? year : String.valueOf(LocalDate.now().getYear());
        Map<String, PeriodStat> byMonth = periodStats("month", y + "0101", y + "1231").stream()
                .collect(Collectors.toMap(PeriodStat::period, p -> p));
        List<PeriodStat> full = new ArrayList<>();
        long tPay = 0, tPayAmt = 0, tCancel = 0, tCancelAmt = 0;
        for (int m = 1; m <= 12; m++) {
            String key = y + String.format("%02d", m);
            PeriodStat p = byMonth.getOrDefault(key, new PeriodStat(key, 0, 0, 0, 0));
            full.add(p);
            tPay += p.payCount();
            tPayAmt += p.payAmount();
            tCancel += p.cancelCount();
            tCancelAmt += p.cancelAmount();
        }
        return new AllSalesSummary(y, new PeriodStat(y, tPay, tPayAmt, tCancel, tCancelAmt), full);
    }

    // ---- C. 결제타입별 매출 통계 - 이 프로젝트는 전액 포인트결제라 단일 유형. ----

    public record PaymentTypeStat(String payType, List<PeriodStat> byDay) {
    }

    public List<PaymentTypeStat> paymentStats(String startDate, String endDate) {
        return List.of(new PaymentTypeStat("포인트", periodStats("day", startDate, endDate)));
    }

    // ---- D. 회원별 매출 통계 ----

    public record UserStat(Long userId, String userName, long payCount, long payAmount) {
    }

    public List<UserStat> userStats() {
        Map<Long, long[]> byUser = new LinkedHashMap<>();
        for (OrderLedger o : allOrders()) {
            if (!isPay(o) || o.getUserId() == null) {
                continue;
            }
            long[] b = byUser.computeIfAbsent(o.getUserId(), k -> new long[2]);
            b[0]++;
            b[1] += amt(o);
        }
        List<UserStat> rows = new ArrayList<>();
        byUser.forEach((id, b) -> {
            MemberClient.MemberInfo info = memberClient.fetchOrNull(id);
            rows.add(new UserStat(id, info != null && info.userName() != null ? info.userName() : ("회원#" + id), b[0], b[1]));
        });
        rows.sort(Comparator.comparingLong(UserStat::payAmount).reversed());
        return rows;
    }

    public List<OrderRow> userOrders(Long userId) {
        Map<Long, GiftClient.ItemSnapshot> items = itemsById();
        List<OrderLedger> orders = allOrders().stream().filter(o -> userId.equals(o.getUserId())).toList();
        return toOrderRows(orders, items);
    }

    // ---- E. 판매자별 통계 ----

    public record SellerStat(Long sellerId, String sellerName, long payCount, long payAmount, long cancelCount) {
    }

    public List<SellerStat> sellerStats() {
        Map<Long, GiftClient.SellerDetail> sellers = sellersById();
        Map<Long, long[]> byS = new LinkedHashMap<>();
        for (OrderLedger o : allOrders()) {
            if (o.getSellerId() == null) {
                continue;
            }
            long[] b = byS.computeIfAbsent(o.getSellerId(), k -> new long[3]);
            if (isPay(o)) {
                b[0]++;
                b[1] += amt(o);
            } else if (isCancel(o)) {
                b[2]++;
            }
        }
        List<SellerStat> rows = new ArrayList<>();
        byS.forEach((id, b) -> rows.add(new SellerStat(id,
                Optional.ofNullable(sellers.get(id)).map(GiftClient.SellerDetail::sellerName).orElse("판매자#" + id),
                b[0], b[1], b[2])));
        rows.sort(Comparator.comparingLong(SellerStat::payAmount).reversed());
        return rows;
    }

    // ---- F. 브랜드별 통계 ----

    public record BrandStat(Integer brandId, String brandName, long payCount, long payAmount) {
    }

    private static final Integer NO_BRAND = -1;

    public List<BrandStat> brandStats() {
        Map<Long, GiftClient.ItemSnapshot> items = itemsById();
        Map<Integer, GiftClient.BrandDetail> brands = brandsById();
        Map<Integer, long[]> byB = new LinkedHashMap<>();
        for (OrderLedger o : allOrders()) {
            if (!isPay(o)) {
                continue;
            }
            GiftClient.ItemSnapshot item = items.get(o.getItemId());
            Integer brandId = item != null && item.brandId() != null ? item.brandId() : NO_BRAND;
            long[] b = byB.computeIfAbsent(brandId, k -> new long[2]);
            b[0]++;
            b[1] += amt(o);
        }
        List<BrandStat> rows = new ArrayList<>();
        byB.forEach((id, b) -> {
            String name = NO_BRAND.equals(id) ? "자사(브랜드없음)"
                    : Optional.ofNullable(brands.get(id)).map(GiftClient.BrandDetail::brandName).orElse("브랜드#" + id);
            rows.add(new BrandStat(NO_BRAND.equals(id) ? null : id, name, b[0], b[1]));
        });
        rows.sort(Comparator.comparingLong(BrandStat::payAmount).reversed());
        return rows;
    }

    public List<OrderRow> brandOrders(Integer brandId) {
        Map<Long, GiftClient.ItemSnapshot> items = itemsById();
        Set<Long> itemIds = items.values().stream()
                .filter(i -> brandId == null ? i.brandId() == null : brandId.equals(i.brandId()))
                .map(GiftClient.ItemSnapshot::itemId)
                .collect(Collectors.toSet());
        List<OrderLedger> orders = allOrders().stream().filter(o -> itemIds.contains(o.getItemId())).toList();
        return toOrderRows(orders, items);
    }

    // ---- G. 상품별 판매 통계 ----

    public record ItemStat(Long itemId, String itemName, long payCount, long payAmount) {
    }

    public List<ItemStat> itemStats() {
        return itemStatsInRange(null, null);
    }

    /** /sales/item/day - 조회기간 내 상품별 판매 랭킹(기간 미지정 시 최근 30일). */
    public List<ItemStat> itemStatsByPeriod(String startDate, String endDate) {
        return itemStatsInRange(defaultStart(startDate), defaultEnd(endDate));
    }

    private List<ItemStat> itemStatsInRange(String start, String end) {
        Map<Long, GiftClient.ItemSnapshot> items = itemsById();
        Map<Long, long[]> byI = new LinkedHashMap<>();
        for (OrderLedger o : allOrders()) {
            if (!isPay(o) || o.getItemId() == null) {
                continue;
            }
            if (start != null) {
                if (o.getCreatedDate() == null) {
                    continue;
                }
                String day = ymd(o.getCreatedDate());
                if (day.compareTo(start) < 0 || day.compareTo(end) > 0) {
                    continue;
                }
            }
            long[] b = byI.computeIfAbsent(o.getItemId(), k -> new long[2]);
            b[0]++;
            b[1] += amt(o);
        }
        List<ItemStat> rows = new ArrayList<>();
        byI.forEach((id, b) -> rows.add(new ItemStat(id,
                Optional.ofNullable(items.get(id)).map(GiftClient.ItemSnapshot::itemName).orElse("상품#" + id), b[0], b[1])));
        rows.sort(Comparator.comparingLong(ItemStat::payAmount).reversed());
        return rows;
    }

    public List<OrderRow> itemOrders(Long itemId) {
        Map<Long, GiftClient.ItemSnapshot> items = itemsById();
        List<OrderLedger> orders = allOrders().stream().filter(o -> itemId.equals(o.getItemId())).toList();
        return toOrderRows(orders, items);
    }

    // ---- H. 카테고리별 통계 (AS-IS /sales/category는 미사용 코드주석 - /sales/categories로 통합) ----

    public record CategoryStat(String categoryCode, long payCount, long payAmount) {
    }

    public List<CategoryStat> categoryStats() {
        Map<Long, GiftClient.ItemSnapshot> items = itemsById();
        Map<String, long[]> byC = new LinkedHashMap<>();
        for (OrderLedger o : allOrders()) {
            if (!isPay(o)) {
                continue;
            }
            GiftClient.ItemSnapshot item = items.get(o.getItemId());
            String cat = item != null && item.categoryCode() != null ? item.categoryCode() : "미분류";
            long[] b = byC.computeIfAbsent(cat, k -> new long[2]);
            b[0]++;
            b[1] += amt(o);
        }
        List<CategoryStat> rows = new ArrayList<>();
        byC.forEach((code, b) -> rows.add(new CategoryStat(code, b[0], b[1])));
        rows.sort(Comparator.comparingLong(CategoryStat::payAmount).reversed());
        return rows;
    }

    // ---- I. 지역별 통계 (답례품 등록 지자체 기준) ----

    public record AreaStat(String locgovCode, String locgovName, long payCount, long payAmount) {
    }

    public List<AreaStat> areaStats() {
        Map<Long, GiftClient.ItemSnapshot> items = itemsById();
        Map<String, String> names = locgovNameByCode();
        Map<String, long[]> byA = new LinkedHashMap<>();
        for (OrderLedger o : allOrders()) {
            if (!isPay(o)) {
                continue;
            }
            GiftClient.ItemSnapshot item = items.get(o.getItemId());
            String code = item != null && item.locgovCode() != null ? item.locgovCode() : "미상";
            long[] b = byA.computeIfAbsent(code, k -> new long[2]);
            b[0]++;
            b[1] += amt(o);
        }
        List<AreaStat> rows = new ArrayList<>();
        byA.forEach((code, b) -> rows.add(new AreaStat(code, names.getOrDefault(code, code), b[0], b[1])));
        rows.sort(Comparator.comparingLong(AreaStat::payAmount).reversed());
        return rows;
    }

    public List<OrderRow> areaOrders(String locgovCode) {
        Map<Long, GiftClient.ItemSnapshot> items = itemsById();
        Set<Long> itemIds = items.values().stream()
                .filter(i -> locgovCode.equals(i.locgovCode()))
                .map(GiftClient.ItemSnapshot::itemId)
                .collect(Collectors.toSet());
        List<OrderLedger> orders = allOrders().stream().filter(o -> itemIds.contains(o.getItemId())).toList();
        return toOrderRows(orders, items);
    }

    // ---- J. 매출 제로 상품 내역 ----

    public record NoSaleItem(Long itemId, String itemName, Long sellerId, String sellerName, Integer salePrice) {
    }

    public List<NoSaleItem> noSaleItems() {
        Set<Long> soldItemIds = allOrders().stream().filter(this::isPay).map(OrderLedger::getItemId).collect(Collectors.toSet());
        Map<Long, GiftClient.SellerDetail> sellers = sellersById();
        return giftClient.allForResync().stream()
                .filter(i -> !soldItemIds.contains(i.itemId()))
                .map(i -> new NoSaleItem(i.itemId(), i.itemName(), i.sellerId(),
                        Optional.ofNullable(sellers.get(i.sellerId())).map(GiftClient.SellerDetail::sellerName).orElse("-"),
                        i.salePrice()))
                .toList();
    }

    // ---- K. 미이용자 목록 (구매이력 없는 회원, 최근 가입순 최대 200명) ----

    public record NoUserEntry(Long userId, String userName, String joinedDate) {
    }

    public List<NoUserEntry> noUsers() {
        Set<Long> buyers = allOrders().stream().filter(this::isPay).map(OrderLedger::getUserId).collect(Collectors.toSet());
        return memberClient.allForResync().stream()
                .filter(m -> !buyers.contains(m.userId()))
                .sorted(Comparator.comparing(MemberClient.MemberSnapshot::createdDate,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(200)
                .map(m -> {
                    MemberClient.MemberInfo info = memberClient.fetchOrNull(m.userId());
                    return new NoUserEntry(m.userId(), info != null && info.userName() != null ? info.userName() : ("회원#" + m.userId()),
                            m.createdDate() != null ? m.createdDate().toLocalDate().toString() : "-");
                })
                .toList();
    }

    // ---- L. 월별 지자체별 답례품 현황 (지자체별 당월 판매 TOP3 상품) ----

    public record LocgovTop3Row(String locgovCode, String locgovName, long orderCount, long orderAmount,
                                 String top1, String top2, String top3) {
    }

    public List<LocgovTop3Row> monthLocgovTop3(int year, int month) {
        Map<Long, GiftClient.ItemSnapshot> items = itemsById();
        Map<String, String> names = locgovNameByCode();
        Map<String, List<OrderLedger>> byLocgov = new LinkedHashMap<>();
        for (OrderLedger o : allOrders()) {
            if (!isPay(o) || o.getCreatedDate() == null) {
                continue;
            }
            if (o.getCreatedDate().getYear() != year || o.getCreatedDate().getMonthValue() != month) {
                continue;
            }
            GiftClient.ItemSnapshot item = items.get(o.getItemId());
            String code = item != null && item.locgovCode() != null ? item.locgovCode() : "미상";
            byLocgov.computeIfAbsent(code, k -> new ArrayList<>()).add(o);
        }
        List<LocgovTop3Row> rows = new ArrayList<>();
        byLocgov.forEach((code, orders) -> {
            Map<Long, Long> countByItem = orders.stream()
                    .collect(Collectors.groupingBy(OrderLedger::getItemId, Collectors.counting()));
            List<String> top = countByItem.entrySet().stream()
                    .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                    .limit(3)
                    .map(e -> Optional.ofNullable(items.get(e.getKey())).map(GiftClient.ItemSnapshot::itemName).orElse("상품#" + e.getKey()))
                    .toList();
            long orderCount = orders.size();
            long orderAmount = orders.stream().mapToLong(this::amt).sum();
            rows.add(new LocgovTop3Row(code, names.getOrDefault(code, code), orderCount, orderAmount,
                    top.size() > 0 ? top.get(0) : "-", top.size() > 1 ? top.get(1) : "-", top.size() > 2 ? top.get(2) : "-"));
        });
        rows.sort(Comparator.comparingLong(LocgovTop3Row::orderAmount).reversed());
        return rows;
    }

    // ---- M. 지자체별 구매현황 ----

    public record LocgovStat(String locgovCode, String locgovName, long payCount, long payAmount) {
    }

    public List<LocgovStat> locgovStats(int year) {
        Map<Long, GiftClient.ItemSnapshot> items = itemsById();
        Map<String, String> names = locgovNameByCode();
        Map<String, long[]> byL = new LinkedHashMap<>();
        for (OrderLedger o : allOrders()) {
            if (!isPay(o) || o.getCreatedDate() == null || o.getCreatedDate().getYear() != year) {
                continue;
            }
            GiftClient.ItemSnapshot item = items.get(o.getItemId());
            String code = item != null && item.locgovCode() != null ? item.locgovCode() : "미상";
            long[] b = byL.computeIfAbsent(code, k -> new long[2]);
            b[0]++;
            b[1] += amt(o);
        }
        List<LocgovStat> rows = new ArrayList<>();
        byL.forEach((code, b) -> rows.add(new LocgovStat(code, names.getOrDefault(code, code), b[0], b[1])));
        rows.sort(Comparator.comparingLong(LocgovStat::payAmount).reversed());
        return rows;
    }

    public List<UserStat> locgovUserStats(String locgovCode, int year) {
        Map<Long, GiftClient.ItemSnapshot> items = itemsById();
        Map<Long, long[]> byUser = new LinkedHashMap<>();
        for (OrderLedger o : allOrders()) {
            if (!isPay(o) || o.getCreatedDate() == null || o.getCreatedDate().getYear() != year || o.getUserId() == null) {
                continue;
            }
            GiftClient.ItemSnapshot item = items.get(o.getItemId());
            String code = item != null ? item.locgovCode() : null;
            if (!locgovCode.equals(code)) {
                continue;
            }
            long[] b = byUser.computeIfAbsent(o.getUserId(), k -> new long[2]);
            b[0]++;
            b[1] += amt(o);
        }
        List<UserStat> rows = new ArrayList<>();
        byUser.forEach((id, b) -> {
            MemberClient.MemberInfo info = memberClient.fetchOrNull(id);
            rows.add(new UserStat(id, info != null && info.userName() != null ? info.userName() : ("회원#" + id), b[0], b[1]));
        });
        rows.sort(Comparator.comparingLong(UserStat::payAmount).reversed());
        return rows;
    }

    // ---- N. 답례품 선호도 (관심답례품/찜) ----

    public record WishStat(Long itemId, String itemName, String locgovCode, String locgovName, long wishCount) {
    }

    public List<WishStat> wishStats() {
        Map<Long, GiftClient.ItemSnapshot> items = itemsById();
        Map<String, String> names = locgovNameByCode();
        Map<Long, Long> counts = giftClient.wishlistAll().stream()
                .collect(Collectors.groupingBy(GiftClient.WishlistEntry::itemId, Collectors.counting()));
        List<WishStat> rows = new ArrayList<>();
        counts.forEach((itemId, count) -> {
            GiftClient.ItemSnapshot item = items.get(itemId);
            String locgov = item != null ? item.locgovCode() : null;
            rows.add(new WishStat(itemId, item != null ? item.itemName() : ("상품#" + itemId),
                    locgov, names.getOrDefault(locgov, "미상"), count));
        });
        rows.sort(Comparator.comparingLong(WishStat::wishCount).reversed());
        return rows;
    }

    // ---- O/P. 일일/월별 현황 대시보드 ----

    public record DashboardDay(String date, long payCount, long payAmount, long cancelCount, long newUserCount) {
    }

    public DashboardDay dashboardDay(String date) {
        String d = defaultEnd(date);
        long payCount = 0, payAmount = 0, cancelCount = 0;
        for (OrderLedger o : allOrders()) {
            if (o.getCreatedDate() == null || !ymd(o.getCreatedDate()).equals(d)) {
                continue;
            }
            if (isPay(o)) {
                payCount++;
                payAmount += amt(o);
            } else if (isCancel(o)) {
                cancelCount++;
            }
        }
        long newUsers = memberClient.allForResync().stream()
                .filter(m -> m.createdDate() != null && ymd(m.createdDate()).equals(d)).count();
        return new DashboardDay(d, payCount, payAmount, cancelCount, newUsers);
    }

    public record DashboardMonth(String yearMonth, List<PeriodStat> dailyBreakdown, long totalPayCount, long totalPayAmount) {
    }

    public DashboardMonth dashboardMonth(String yearMonth) {
        String ym = (yearMonth != null && !yearMonth.isBlank()) ? yearMonth
                : LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        List<PeriodStat> daily = periodStats("day", ym + "01", ym + "31");
        long totalCount = daily.stream().mapToLong(PeriodStat::payCount).sum();
        long totalAmount = daily.stream().mapToLong(PeriodStat::payAmount).sum();
        return new DashboardMonth(ym, daily, totalCount, totalAmount);
    }

    // ---- Q. 농협 사업자 답례품 현황 (은행명에 "농협" 포함된 판매자) ----

    public List<SellerStat> nhItemSales() {
        Map<Long, GiftClient.SellerDetail> sellers = sellersById();
        Set<Long> nhSellerIds = sellers.values().stream()
                .filter(s -> s.bankName() != null && s.bankName().contains("농협"))
                .map(GiftClient.SellerDetail::sellerId)
                .collect(Collectors.toSet());
        return sellerStats().stream().filter(s -> nhSellerIds.contains(s.sellerId())).toList();
    }

    public int currentYear() {
        return LocalDate.now().getYear();
    }

    public int currentMonth() {
        return LocalDate.now().getMonthValue();
    }
}
