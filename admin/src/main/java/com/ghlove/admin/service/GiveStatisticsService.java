package com.ghlove.admin.service;

import com.ghlove.admin.domain.DonationLedger;
import com.ghlove.admin.repository.DonationLedgerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 기부 통계 (AS-IS opmanager/give/statistics - 전체/기부인원/기부금액/기부건수/개인별/일자별
 * + 지자체별/운영통계 8개+ 화면). 지자체별(locgov)과 운영통계(operate)는 이미 만든 기능과
 * 사실상 중복이라(give-state=지자체별 실적, give-operation=지출/운용현황, /stats=사이트
 * 전체 지표) 재구현하지 않는다 - 제안요청서 SFR-007도 "지자체별 실적 분석"을 화면 단위가
 * 아니라 분석 내용으로 요구할 뿐이다. 이 서비스는 AS-IS의 person/amount/number(같은
 * 데이터를 세 화면으로 쪼갠 것)를 하나로 합치고, 실제로 새로운 분석 관점인 월별 추이/
 * 연령대별/금액구간별/개인별 순위를 제공한다. 원 데이터는 GiveStateService와 동일하게
 * STAT_DONATION_LEDGER ReadModel(admin의 CQRS 사본)에서 가져온다 - 새 원장을 쌓지 않는다.
 */
@Service
@RequiredArgsConstructor
public class GiveStatisticsService {

    private static final String STATUS_COMPLETED = "COMPLETED";

    /** SFR-007 "분담금 산정 리포트" - 실제 산정 기준이 AS-IS/제안요청서 어디에도 없어
     *  사용자 확인 하에 임시로 채택한 가정치(지자체가 유치한 기부금액의 1%를 플랫폼
     *  운영 분담금으로 산정). 실제 정책이 확정되면 이 상수만 교체하면 된다. */
    static final BigDecimal LEVY_RATE = new BigDecimal("0.01");

    private final DonationLedgerRepository donationLedgerRepository;
    private final MemberClient memberClient;
    private final LocgovClient locgovClient;

    private List<DonationLedger> filtered(String year, String upperLocgovCode, String locgovCode) {
        Map<String, LocgovClient.LocgovInfo> locgovsByCode = new LinkedHashMap<>();
        locgovClient.allLocgovs().forEach(l -> locgovsByCode.put(l.locgovCode(), l));

        return donationLedgerRepository.findByStatus(STATUS_COMPLETED).stream()
                .filter(d -> d.getEventDate() != null && d.getEventDate().length() >= 8)
                .filter(d -> year == null || year.isBlank() || d.getEventDate().startsWith(year))
                .filter(d -> locgovCode == null || locgovCode.isBlank() || locgovCode.equals(d.getLocgovCode()))
                .filter(d -> {
                    if (upperLocgovCode == null || upperLocgovCode.isBlank()) {
                        return true;
                    }
                    LocgovClient.LocgovInfo info = locgovsByCode.get(d.getLocgovCode());
                    return info != null && upperLocgovCode.equals(info.upperLocgovCode());
                })
                .toList();
    }

    /** 월별 추이 - AS-IS all/{year}/month. */
    public List<MonthRow> monthlyTrend(String year, String upperLocgovCode, String locgovCode) {
        Map<String, List<DonationLedger>> byMonth = filtered(year, upperLocgovCode, locgovCode).stream()
                .collect(Collectors.groupingBy(d -> d.getEventDate().substring(4, 6)));

        List<MonthRow> rows = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            String key = String.format("%02d", m);
            List<DonationLedger> list = byMonth.getOrDefault(key, List.of());
            BigDecimal amount = list.stream().map(DonationLedger::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            rows.add(new MonthRow(key, amount, list.size()));
        }
        return rows;
    }

    /** 연령대별 분포 - AS-IS all/{year}/age. 10년 단위 브라켓, 생년월일 조회 실패 시 "미상". */
    public List<AgeBracketRow> ageBracket(String year, String upperLocgovCode, String locgovCode) {
        List<DonationLedger> list = filtered(year, upperLocgovCode, locgovCode);
        Map<Long, MemberClient.MemberInfo> memberByUserId = new LinkedHashMap<>();
        for (DonationLedger d : list) {
            memberByUserId.computeIfAbsent(d.getUserId(), memberClient::fetchOrNull);
        }

        Map<String, List<DonationLedger>> byBracket = list.stream()
                .collect(Collectors.groupingBy(d -> ageBracketOf(memberByUserId.get(d.getUserId()))));

        List<String> order = List.of("20대 이하", "30대", "40대", "50대", "60대 이상", "미상");
        List<AgeBracketRow> rows = new ArrayList<>();
        for (String bracket : order) {
            List<DonationLedger> bucket = byBracket.getOrDefault(bracket, List.of());
            BigDecimal amount = bucket.stream().map(DonationLedger::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            rows.add(new AgeBracketRow(bracket, amount, bucket.size()));
        }
        return rows;
    }

    private static String ageBracketOf(MemberClient.MemberInfo member) {
        if (member == null || member.birthday() == null || member.birthday().length() < 4) {
            return "미상";
        }
        try {
            int birthYear = Integer.parseInt(member.birthday().substring(0, 4));
            int age = LocalDate.now().getYear() - birthYear;
            if (age < 30) return "20대 이하";
            if (age < 40) return "30대";
            if (age < 50) return "40대";
            if (age < 60) return "50대";
            return "60대 이상";
        } catch (NumberFormatException e) {
            return "미상";
        }
    }

    /** 금액구간별 분포 - AS-IS amount/number를 하나로 합친 것(같은 데이터를 금액 축으로). */
    public List<AmountBracketRow> amountBracket(String year, String upperLocgovCode, String locgovCode) {
        List<DonationLedger> list = filtered(year, upperLocgovCode, locgovCode);
        List<Bracket> brackets = List.of(
                new Bracket("1만원 미만", BigDecimal.ZERO, new BigDecimal("10000")),
                new Bracket("1만원~5만원", new BigDecimal("10000"), new BigDecimal("50000")),
                new Bracket("5만원~10만원", new BigDecimal("50000"), new BigDecimal("100000")),
                new Bracket("10만원~50만원", new BigDecimal("100000"), new BigDecimal("500000")),
                new Bracket("50만원~100만원", new BigDecimal("500000"), new BigDecimal("1000000")),
                new Bracket("100만원 이상", new BigDecimal("1000000"), null));

        List<AmountBracketRow> rows = new ArrayList<>();
        for (Bracket b : brackets) {
            List<DonationLedger> bucket = list.stream()
                    .filter(d -> d.getAmount().compareTo(b.from()) >= 0
                            && (b.to() == null || d.getAmount().compareTo(b.to()) < 0))
                    .toList();
            BigDecimal amount = bucket.stream().map(DonationLedger::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            rows.add(new AmountBracketRow(b.label(), amount, bucket.size()));
        }
        return rows;
    }

    /** 개인별 순위 - AS-IS personal (사용자 단위 합산 후 정렬). admin은 실명/아이디를
     *  마스킹 없이 그대로 보여준다([[admin-pii-display-no-masking]]). */
    public List<PersonalRow> personalRanking(String year, String upperLocgovCode, String locgovCode, int topN) {
        List<DonationLedger> list = filtered(year, upperLocgovCode, locgovCode);
        Map<Long, List<DonationLedger>> byUser = list.stream().collect(Collectors.groupingBy(DonationLedger::getUserId));

        Map<Long, MemberClient.MemberInfo> memberByUserId = new LinkedHashMap<>();
        for (Long userId : byUser.keySet()) {
            memberByUserId.computeIfAbsent(userId, memberClient::fetchOrNull);
        }

        return byUser.entrySet().stream()
                .map(e -> {
                    BigDecimal amount = e.getValue().stream().map(DonationLedger::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                    MemberClient.MemberInfo member = memberByUserId.get(e.getKey());
                    return new PersonalRow(e.getKey(), member != null ? member.userName() : "-",
                            member != null ? member.loginId() : "-", amount, e.getValue().size());
                })
                .sorted(Comparator.comparing(PersonalRow::amount).reversed())
                .limit(topN)
                .toList();
    }

    /** 분담금 산정 리포트 - LEVY_RATE 가정치 적용, 지자체별. */
    public List<LevyRow> levyReport(String year, String upperLocgovCode, String locgovCode) {
        Map<String, LocgovClient.LocgovInfo> locgovsByCode = new LinkedHashMap<>();
        locgovClient.allLocgovs().forEach(l -> locgovsByCode.put(l.locgovCode(), l));

        Map<String, List<DonationLedger>> byLocgov = filtered(year, upperLocgovCode, locgovCode).stream()
                .collect(Collectors.groupingBy(DonationLedger::getLocgovCode));

        List<LevyRow> rows = new ArrayList<>();
        for (Map.Entry<String, List<DonationLedger>> e : byLocgov.entrySet()) {
            BigDecimal amount = e.getValue().stream().map(DonationLedger::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            LocgovClient.LocgovInfo info = locgovsByCode.get(e.getKey());
            BigDecimal levy = amount.multiply(LEVY_RATE).setScale(0, java.math.RoundingMode.FLOOR);
            rows.add(new LevyRow(e.getKey(), info != null ? info.upperLocgovNm() : e.getKey(),
                    info != null ? info.locgovNm() : e.getKey(), amount, levy));
        }
        rows.sort(Comparator.comparing(LevyRow::cntrAmt).reversed());
        return rows;
    }

    private record Bracket(String label, BigDecimal from, BigDecimal to) {
    }

    public record MonthRow(String month, BigDecimal amount, long count) {
    }

    public record AgeBracketRow(String bracket, BigDecimal amount, long count) {
    }

    public record AmountBracketRow(String bracket, BigDecimal amount, long count) {
    }

    public record PersonalRow(Long userId, String userName, String loginId, BigDecimal amount, long count) {
    }

    public record LevyRow(String locgovCode, String upperLocgovNm, String locgovNm, BigDecimal cntrAmt, BigDecimal levyAmt) {
    }
}
