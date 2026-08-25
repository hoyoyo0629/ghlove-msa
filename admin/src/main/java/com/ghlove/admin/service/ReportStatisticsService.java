package com.ghlove.admin.service;

import com.ghlove.admin.domain.DonationLedger;
import com.ghlove.admin.domain.GiftLedgerStat;
import com.ghlove.admin.repository.DonationLedgerRepository;
import com.ghlove.admin.repository.GiftLedgerStatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AS-IS opmanager/shop-statistics/report 스위트 재구현 (총괄/총괄누계/지자체별(광역·기초)/연간).
 * admin은 각 원장서비스 DB를 직접 못 읽으므로(DB per Service) StatsService의 ReadModel
 * (DonationLedger/GiftLedgerStat)과 member/donation의 크로스서비스 스냅샷을 그대로 쓴다.
 *
 * "2000만원 누계" 탭들은 AS-IS가 이미 이 프로젝트의 SYSTEM_CONFIG.ANNUAL_TOTAL_LIMIT(2천만원,
 * 고향사랑기부금법상 연간 개인기부한도)과 동일한 값을 쓰는 공식 한도 준수 집계다 - 회원별로
 * 그 해 기부를 등록순(cntrSn 오름차순 - 실제로는 접수순)으로 훑으며 잔여한도를 깎아, 한도를
 * 넘는 초과분은 실적에서 제외한다.
 */
@Service
@RequiredArgsConstructor
public class ReportStatisticsService {

    private static final BigDecimal ANNUAL_LIMIT = BigDecimal.valueOf(20_000_000L);
    private static final String STATUS_COMPLETED = "COMPLETED";

    private final DonationLedgerRepository donationLedgerRepository;
    private final GiftLedgerStatRepository giftLedgerStatRepository;
    private final MemberClient memberClient;
    private final LocgovClient locgovClient;

    // ---- 공용 헬퍼 ----

    private List<DonationLedger> completedDonations() {
        return donationLedgerRepository.findByStatus(STATUS_COMPLETED);
    }

    private int month(String eventDate) {
        return eventDate != null && eventDate.length() >= 6 ? Integer.parseInt(eventDate.substring(4, 6)) : 0;
    }

    private int year(String eventDate) {
        return eventDate != null && eventDate.length() >= 4 ? Integer.parseInt(eventDate.substring(0, 4)) : 0;
    }

    /** 회원별·연도별로 한도(2천만원)를 순차 차감해, 각 기부의 "한도내 인정액"을 계산한다. */
    private Map<String, BigDecimal> cappedAmountsByCntrSn(List<DonationLedger> donations) {
        Map<String, BigDecimal> result = new LinkedHashMap<>();
        Map<String, List<DonationLedger>> byUserYear = donations.stream()
                .collect(Collectors.groupingBy(d -> d.getUserId() + "_" + year(d.getEventDate())));
        for (List<DonationLedger> group : byUserYear.values()) {
            group.sort(Comparator.comparing(DonationLedger::getCntrSn));
            BigDecimal remaining = ANNUAL_LIMIT;
            for (DonationLedger d : group) {
                BigDecimal amt = d.getAmount() != null ? d.getAmount() : BigDecimal.ZERO;
                BigDecimal recognized = amt.min(remaining).max(BigDecimal.ZERO);
                result.put(d.getCntrSn(), recognized);
                remaining = remaining.subtract(recognized).max(BigDecimal.ZERO);
            }
        }
        return result;
    }

    // ---- report/general: 총괄 현황 (당해 월별, 4개 탭) ----

    public record MonthlyComparison(int month, long currentYear, long priorYear) {
    }

    public List<MonthlyComparison> memberCountsByMonth(int targetYear, int throughMonth) {
        List<MemberClient.MemberSnapshot> members = memberClient.allForResync();
        Map<Integer, Long> current = countByMonth(members, targetYear);
        Map<Integer, Long> prior = countByMonth(members, targetYear - 1);
        return buildComparison(current, prior, throughMonth);
    }

    private Map<Integer, Long> countByMonth(List<MemberClient.MemberSnapshot> members, int y) {
        return members.stream()
                .filter(m -> m.createdDate() != null && m.createdDate().getYear() == y)
                .collect(Collectors.groupingBy(m -> m.createdDate().getMonthValue(), Collectors.counting()));
    }

    public List<MonthlyComparison> donationCountsByMonth(int targetYear, int throughMonth, boolean cappedOnly) {
        List<DonationLedger> all = completedDonations();
        Map<String, BigDecimal> capped = cappedOnly ? cappedAmountsByCntrSn(all) : null;
        Map<Integer, Long> current = donationMonthCounts(all, targetYear, capped);
        Map<Integer, Long> prior = donationMonthCounts(all, targetYear - 1, capped);
        return buildComparison(current, prior, throughMonth);
    }

    private Map<Integer, Long> donationMonthCounts(List<DonationLedger> all, int y, Map<String, BigDecimal> capped) {
        return all.stream()
                .filter(d -> year(d.getEventDate()) == y)
                .filter(d -> capped == null || capped.getOrDefault(d.getCntrSn(), BigDecimal.ZERO).signum() > 0)
                .collect(Collectors.groupingBy(d -> month(d.getEventDate()), Collectors.counting()));
    }

    public List<MonthlyComparison> giftCountsByMonth(int targetYear, int throughMonth) {
        List<GiftLedgerStat> all = giftLedgerStatRepository.findAll();
        Map<Integer, Long> current = giftMonthCounts(all, targetYear);
        Map<Integer, Long> prior = giftMonthCounts(all, targetYear - 1);
        return buildComparison(current, prior, throughMonth);
    }

    private Map<Integer, Long> giftMonthCounts(List<GiftLedgerStat> all, int y) {
        return all.stream()
                .filter(g -> g.getUpdatedDate() != null && g.getUpdatedDate().getYear() == y)
                .collect(Collectors.groupingBy(g -> g.getUpdatedDate().getMonthValue(), Collectors.counting()));
    }

    private List<MonthlyComparison> buildComparison(Map<Integer, Long> current, Map<Integer, Long> prior, int throughMonth) {
        List<MonthlyComparison> rows = new ArrayList<>();
        for (int m = 1; m <= throughMonth; m++) {
            rows.add(new MonthlyComparison(m, current.getOrDefault(m, 0L), prior.getOrDefault(m, 0L)));
        }
        return rows;
    }

    // ---- report/generalTotal: 총괄 누계 현황 (전기간 누계, 4개 탭) ----

    public record BreakdownRow(String key, String label, long count, BigDecimal amount) {
    }

    /** AS-IS 공통코드 CNTR_PATH 실제값(09.공통코드 목록.xlsx) - 100:온라인, 200:오프라인. */
    private static final Map<String, String> CNTR_PATH_LABELS = Map.of("100", "온라인", "200", "오프라인");

    /** 탭1: 기부방법별 기부현황(누계) - CNTR_PATH_CODE. */
    public List<BreakdownRow> donationsByPathCode() {
        return groupDonations(DonationLedger::getCntrPathCode).stream()
                .map(r -> new BreakdownRow(r.key(), CNTR_PATH_LABELS.getOrDefault(r.key(), r.key()), r.count(), r.amount()))
                .toList();
    }

    /** 탭2: 금액별 기부건수현황(누계) - 금액 구간. */
    public List<BreakdownRow> donationsByAmountBracket() {
        return groupDonations(d -> amountBracketOf(d.getAmount()));
    }

    private String amountBracketOf(BigDecimal amount) {
        if (amount == null) {
            return "미상";
        }
        long amt = amount.longValue();
        if (amt < 100_000) return "10만원 미만";
        if (amt < 500_000) return "10~50만원";
        if (amt < 1_000_000) return "50~100만원";
        if (amt < 5_000_000) return "100~500만원";
        if (amt < 10_000_000) return "500~1000만원";
        return "1000만원 이상";
    }

    /** 탭3: 연령별 기부건수현황(누계) - 기부 시점 만나이 10살 단위. birthday=yyyyMMdd. */
    public List<BreakdownRow> donationsByAgeBracket() {
        Map<Long, String> birthdayByUserId = memberClient.allForResync().stream()
                .filter(m -> m.birthday() != null && m.birthday().length() >= 4)
                .collect(Collectors.toMap(MemberClient.MemberSnapshot::userId, MemberClient.MemberSnapshot::birthday, (a, b) -> a));
        return groupDonations(d -> ageBracketOf(birthdayByUserId.get(d.getUserId()), d.getEventDate()));
    }

    private String ageBracketOf(String birthday, String eventDate) {
        if (birthday == null || birthday.length() < 4 || eventDate == null || eventDate.length() < 4) {
            return "미상";
        }
        try {
            int birthYear = Integer.parseInt(birthday.substring(0, 4));
            int donationYear = Integer.parseInt(eventDate.substring(0, 4));
            int age = donationYear - birthYear;
            int bracket = Math.max(0, age / 10) * 10;
            return bracket >= 70 ? "70대 이상" : bracket + "대";
        } catch (NumberFormatException e) {
            return "미상";
        }
    }

    /** 탭4: 거주지역->기부지역 건수현황(누계) - PSITN_LOCGOV_CODE(거주) -> LOCGOV_CODE(기부처) 광역 매트릭스. */
    public List<ResidenceToDonationRow> donationsByResidenceToDonation() {
        Map<String, String> mctpvNameByLocgov = mctpvNameByLocgovCode();
        Map<String, Map<String, Long>> matrix = new LinkedHashMap<>();
        for (DonationLedger d : completedDonations()) {
            String from = mctpvNameByLocgov.getOrDefault(d.getPsitnLocgovCode(), "미상");
            String to = mctpvNameByLocgov.getOrDefault(d.getLocgovCode(), "미상");
            matrix.computeIfAbsent(from, k -> new LinkedHashMap<>()).merge(to, 1L, Long::sum);
        }
        List<ResidenceToDonationRow> rows = new ArrayList<>();
        matrix.forEach((from, tos) -> tos.forEach((to, count) -> rows.add(new ResidenceToDonationRow(from, to, count))));
        rows.sort(Comparator.comparing(ResidenceToDonationRow::count).reversed());
        return rows;
    }

    public record ResidenceToDonationRow(String fromMctpv, String toMctpv, long count) {
    }

    private List<BreakdownRow> groupDonations(java.util.function.Function<DonationLedger, String> keyFn) {
        Map<String, List<DonationLedger>> grouped = completedDonations().stream()
                .collect(Collectors.groupingBy(d -> {
                    String k = keyFn.apply(d);
                    return k != null ? k : "미상";
                }));
        List<BreakdownRow> rows = new ArrayList<>();
        grouped.forEach((key, list) -> rows.add(new BreakdownRow(key, key, list.size(),
                list.stream().map(DonationLedger::getAmount).filter(java.util.Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add))));
        rows.sort(Comparator.comparing(BreakdownRow::amount).reversed());
        return rows;
    }

    // ---- report/mctpv, report/lclgv: 지자체별 현황 (광역/기초, 월별 당해-전년 비교, 3개 탭) ----

    public record LocgovMonthlyRow(String code, String name, Map<Integer, Long> currentCount,
                                    Map<Integer, Long> priorCount, Map<Integer, BigDecimal> currentAmount,
                                    Map<Integer, BigDecimal> priorAmount) {
    }

    /** mctpv=true면 광역시도 단위로 묶고(report/mctpv), false면 기초지자체 단위 그대로(report/lclgv). */
    public List<LocgovMonthlyRow> donationStatsByLocgov(int targetYear, int throughMonth, boolean cappedOnly, boolean byMctpv) {
        List<DonationLedger> all = completedDonations();
        Map<String, BigDecimal> capped = cappedOnly ? cappedAmountsByCntrSn(all) : null;
        Map<String, String> nameByCode = byMctpv ? mctpvNameByCode() : locgovNameByCode();
        java.util.function.Function<DonationLedger, String> keyFn = byMctpv
                ? d -> mctpvCodeOf(d.getLocgovCode())
                : DonationLedger::getLocgovCode;

        return buildLocgovMonthlyRows(all, targetYear, throughMonth, nameByCode, keyFn, capped);
    }

    private List<LocgovMonthlyRow> buildLocgovMonthlyRows(List<DonationLedger> all, int targetYear, int throughMonth,
                                                            Map<String, String> nameByCode,
                                                            java.util.function.Function<DonationLedger, String> keyFn,
                                                            Map<String, BigDecimal> capped) {
        Map<String, Map<Integer, Long>> countCurrent = new LinkedHashMap<>();
        Map<String, Map<Integer, Long>> countPrior = new LinkedHashMap<>();
        Map<String, Map<Integer, BigDecimal>> amtCurrent = new LinkedHashMap<>();
        Map<String, Map<Integer, BigDecimal>> amtPrior = new LinkedHashMap<>();

        for (DonationLedger d : all) {
            String key = keyFn.apply(d);
            if (key == null) {
                continue;
            }
            int y = year(d.getEventDate());
            int m = month(d.getEventDate());
            BigDecimal amt = capped != null ? capped.getOrDefault(d.getCntrSn(), BigDecimal.ZERO) : d.getAmount();
            if (amt == null || (capped != null && amt.signum() == 0)) {
                continue;
            }
            if (y == targetYear) {
                countCurrent.computeIfAbsent(key, k -> new LinkedHashMap<>()).merge(m, 1L, Long::sum);
                amtCurrent.computeIfAbsent(key, k -> new LinkedHashMap<>()).merge(m, amt, BigDecimal::add);
            } else if (y == targetYear - 1) {
                countPrior.computeIfAbsent(key, k -> new LinkedHashMap<>()).merge(m, 1L, Long::sum);
                amtPrior.computeIfAbsent(key, k -> new LinkedHashMap<>()).merge(m, amt, BigDecimal::add);
            }
        }

        java.util.Set<String> keys = new java.util.TreeSet<>();
        keys.addAll(countCurrent.keySet());
        keys.addAll(countPrior.keySet());
        List<LocgovMonthlyRow> rows = new ArrayList<>();
        for (String key : keys) {
            rows.add(new LocgovMonthlyRow(key, nameByCode.getOrDefault(key, key),
                    countCurrent.getOrDefault(key, Map.of()), countPrior.getOrDefault(key, Map.of()),
                    amtCurrent.getOrDefault(key, Map.of()), amtPrior.getOrDefault(key, Map.of())));
        }
        return rows;
    }

    /** 3번째 탭: 지자체별 답례품 제공현황 (건수만, 등록일 기준). */
    public List<LocgovMonthlyRow> giftStatsByLocgov(int targetYear, int throughMonth, boolean byMctpv) {
        List<GiftLedgerStat> all = giftLedgerStatRepository.findAll();
        Map<String, String> nameByCode = byMctpv ? mctpvNameByCode() : locgovNameByCode();
        Map<String, Map<Integer, Long>> countCurrent = new LinkedHashMap<>();
        Map<String, Map<Integer, Long>> countPrior = new LinkedHashMap<>();
        for (GiftLedgerStat g : all) {
            if (g.getUpdatedDate() == null) {
                continue;
            }
            String key = byMctpv ? mctpvCodeOf(g.getLocgovCode()) : g.getLocgovCode();
            if (key == null) {
                continue;
            }
            int y = g.getUpdatedDate().getYear();
            int m = g.getUpdatedDate().getMonthValue();
            if (y == targetYear) {
                countCurrent.computeIfAbsent(key, k -> new LinkedHashMap<>()).merge(m, 1L, Long::sum);
            } else if (y == targetYear - 1) {
                countPrior.computeIfAbsent(key, k -> new LinkedHashMap<>()).merge(m, 1L, Long::sum);
            }
        }
        java.util.Set<String> keys = new java.util.TreeSet<>();
        keys.addAll(countCurrent.keySet());
        keys.addAll(countPrior.keySet());
        List<LocgovMonthlyRow> rows = new ArrayList<>();
        for (String key : keys) {
            rows.add(new LocgovMonthlyRow(key, nameByCode.getOrDefault(key, key),
                    countCurrent.getOrDefault(key, Map.of()), countPrior.getOrDefault(key, Map.of()),
                    Map.of(), Map.of()));
        }
        return rows;
    }

    // ---- report/year: 연간통계 현황 ----

    public record YearlyStat(int year, long donationCount, BigDecimal donationAmount, long giftCount) {
    }

    public List<YearlyStat> yearlyTrend(int fromYear, int toYear) {
        List<DonationLedger> donations = completedDonations();
        List<GiftLedgerStat> gifts = giftLedgerStatRepository.findAll();
        List<YearlyStat> rows = new ArrayList<>();
        for (int y = fromYear; y <= toYear; y++) {
            final int yy = y;
            List<DonationLedger> yearDonations = donations.stream().filter(d -> year(d.getEventDate()) == yy).toList();
            long giftCount = gifts.stream().filter(g -> g.getUpdatedDate() != null && g.getUpdatedDate().getYear() == yy).count();
            BigDecimal amount = yearDonations.stream().map(DonationLedger::getAmount)
                    .filter(java.util.Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
            rows.add(new YearlyStat(y, yearDonations.size(), amount, giftCount));
        }
        return rows;
    }

    // ---- 지자체/광역시도 코드-이름 매핑 ----

    private Map<String, String> locgovNameByCode() {
        return locgovClient.allLocgovs().stream()
                .collect(Collectors.toMap(LocgovClient.LocgovInfo::locgovCode,
                        l -> (l.upperLocgovNm() != null ? l.upperLocgovNm() + " " : "") + l.locgovNm(), (a, b) -> a));
    }

    private Map<String, String> mctpvNameByLocgovCode() {
        return locgovClient.allLocgovs().stream()
                .collect(Collectors.toMap(LocgovClient.LocgovInfo::locgovCode,
                        l -> l.upperLocgovNm() != null ? l.upperLocgovNm() : l.locgovNm(), (a, b) -> a));
    }

    private Map<String, String> mctpvNameByCode() {
        return locgovClient.allLocgovs().stream()
                .filter(l -> l.upperLocgovCode() != null)
                .collect(Collectors.toMap(LocgovClient.LocgovInfo::upperLocgovCode,
                        l -> l.upperLocgovNm() != null ? l.upperLocgovNm() : l.upperLocgovCode(), (a, b) -> a));
    }

    private Map<String, String> mctpvCodeByLocgovCode() {
        return locgovClient.allLocgovs().stream()
                .collect(Collectors.toMap(LocgovClient.LocgovInfo::locgovCode, LocgovClient.LocgovInfo::upperLocgovCode, (a, b) -> a));
    }

    private String mctpvCodeOf(String locgovCode) {
        return locgovCode != null ? mctpvCodeByLocgovCode().get(locgovCode) : null;
    }

    public int currentYear() {
        return Year.now().getValue();
    }

    public int currentMonth() {
        return LocalDate.now().getMonthValue();
    }
}
