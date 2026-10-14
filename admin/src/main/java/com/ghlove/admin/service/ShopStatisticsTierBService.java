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

    // ---- B. 답례품 구매현황 전체 → ShopStatisticsTierBController 로 옮겼다(AS-IS all.jsp
    //         사양으로 재구현, OrderAdminClient.dateStats 재사용). 옛 allSales()/AllSalesSummary
    //         (이 화면 전용, periodStats 기반 발명된 표)는 호출부가 사라져 제거했다.

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

    // ---- L. 월별 지자체별 답례품 현황 → ShopStatisticsMonthLocgovService로 옮겼다(AS-IS
    //         getMonthLocgovItemStat 전체 재구현 - 7칸 카테고리 선정/등록 현황·동률 TOP3·엑셀,
    //         2026-10-14). 옛 monthLocgovTop3/LocgovTop3Row(지자체 TOP3 주문금액 순위만 보여주던
    //         축소판)는 제거했다.

    // ---- M. 지자체별 구매현황 → ShopStatisticsLocgovService로 옮겼다(AS-IS mois/locgov 역할
    //         분기·지자체명/상호명/회원명 cross-service enrichment·정렬·엑셀 전체 재구현,
    //         2026-10-14). 옛 locgovStats/locgovUserStats(연도만 받아 정렬 하나뿐이던 축소판)는
    //         제거했다 - LocgovStat 레코드도 그 둘만 썼으므로 같이 지웠다(UserStat은 D 섹션이
    //         아직 쓰므로 남긴다).


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

    // ---- Q. 농협 사업자 답례품 현황 → ShopStatisticsNhItemSalesService로 옮겼다(AS-IS는
    //         bankName이 아니라 G_NH_STAT_DATA_TEMP 사업자번호 매칭이었다, 2026-10-14 바로잡음).

    // ---- R. 대시보드 텍스트 리포트 (AS-IS opmanager/report - ReportController, part1 3b/B10).
    // 행안부 일일/주간보고용 - 이미 계산된 대시보드 데이터를 클립보드복사에 알맞은 정형 텍스트로
    // 포맷팅만 한다(별도 신규 데이터소스 없음). ----

    public String dashboardDayReportText(DashboardDay d) {
        StringBuilder sb = new StringBuilder();
        sb.append("[고향사랑e음 일일 현황 보고]\n");
        sb.append("일자 : ").append(d.date()).append('\n');
        sb.append("결제건수 : ").append(d.payCount()).append("건\n");
        sb.append("결제포인트 : ").append(formatComma(d.payAmount())).append("P\n");
        sb.append("취소건수 : ").append(d.cancelCount()).append("건\n");
        sb.append("신규가입자수 : ").append(d.newUserCount()).append("명\n");
        return sb.toString();
    }

    public String dashboardMonthReportText(DashboardMonth d) {
        StringBuilder sb = new StringBuilder();
        sb.append("[고향사랑e음 월별 현황 보고]\n");
        sb.append("대상월 : ").append(d.yearMonth()).append('\n');
        sb.append("합계 결제건수 : ").append(d.totalPayCount()).append("건\n");
        sb.append("합계 결제포인트 : ").append(formatComma(d.totalPayAmount())).append("P\n");
        sb.append("-- 일자별 내역 --\n");
        for (PeriodStat p : d.dailyBreakdown()) {
            sb.append(p.period()).append('\t')
                    .append(p.payCount()).append("건\t")
                    .append(formatComma(p.payAmount())).append("P\t취소 ")
                    .append(p.cancelCount()).append("건\n");
        }
        return sb.toString();
    }

    private static String formatComma(long n) {
        return String.format("%,d", n);
    }
}
