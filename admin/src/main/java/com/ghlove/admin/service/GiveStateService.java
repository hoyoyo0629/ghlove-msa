package com.ghlove.admin.service;

import com.ghlove.admin.domain.DonationLedger;
import com.ghlove.admin.repository.DonationLedgerRepository;
import com.ghlove.admin.repository.PointLedgerStatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 기부금 모금현황 (AS-IS opmanager/give/give-state - GiveStateManagerController.list()).
 * AS-IS는 년도×지자체로 그룹핑된 집계(기부모금액/기부건수/기부인원/발생포인트/답례품금액)를
 * 보여준다. admin은 donation/point 서비스의 DB를 직접 읽을 수 없어(DB per Service) 이미
 * 있는 통계 ReadModel(StatsService와 동일한 STAT_DONATION_LEDGER/STAT_POINT_LEDGER)을
 * 그대로 재사용해 집계한다 - 원장을 새로 쌓지 않는다.
 */
@Service
@RequiredArgsConstructor
public class GiveStateService {

    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String TXN_EARN = "EARN";
    private static final String TXN_USE = "USE";

    private final DonationLedgerRepository donationLedgerRepository;
    private final PointLedgerStatRepository pointLedgerStatRepository;
    private final LocgovClient locgovClient;

    public List<GiveStateRow> search(String year, String upperLocgovCode, String locgovCode) {
        Map<String, LocgovClient.LocgovInfo> locgovsByCode = new LinkedHashMap<>();
        locgovClient.allLocgovs().forEach(l -> locgovsByCode.put(l.locgovCode(), l));

        Map<Key, List<DonationLedger>> donationsByKey = donationLedgerRepository.findByStatus(STATUS_COMPLETED).stream()
                .filter(d -> d.getEventDate() != null && d.getEventDate().length() >= 4 && d.getLocgovCode() != null)
                .collect(Collectors.groupingBy(d -> new Key(d.getEventDate().substring(0, 4), d.getLocgovCode())));

        Map<Key, Long> earnedByKey = sumPointsByKey(TXN_EARN, false);
        Map<Key, Long> usedByKey = sumPointsByKey(TXN_USE, true);

        List<GiveStateRow> rows = new ArrayList<>();
        for (Map.Entry<Key, List<DonationLedger>> entry : donationsByKey.entrySet()) {
            Key key = entry.getKey();
            if (year != null && !year.isBlank() && !year.equals(key.year())) {
                continue;
            }
            LocgovClient.LocgovInfo info = locgovsByCode.get(key.locgovCode());
            if (locgovCode != null && !locgovCode.isBlank() && !locgovCode.equals(key.locgovCode())) {
                continue;
            }
            if (upperLocgovCode != null && !upperLocgovCode.isBlank()
                    && (info == null || !upperLocgovCode.equals(info.upperLocgovCode()))) {
                continue;
            }

            List<DonationLedger> list = entry.getValue();
            BigDecimal amount = list.stream().map(DonationLedger::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            long persons = list.stream().map(DonationLedger::getUserId).distinct().count();

            rows.add(new GiveStateRow(
                    key.year(),
                    info != null ? info.upperLocgovCode() : null,
                    info != null ? info.upperLocgovNm() : key.locgovCode(),
                    key.locgovCode(),
                    info != null ? info.locgovNm() : key.locgovCode(),
                    amount, list.size(), persons,
                    earnedByKey.getOrDefault(key, 0L),
                    usedByKey.getOrDefault(key, 0L)));
        }
        rows.sort(Comparator.comparing(GiveStateRow::cntrYear, Comparator.reverseOrder())
                .thenComparing(r -> r.cntrAmt(), Comparator.reverseOrder()));
        return rows;
    }

    /** 년도 필터 드롭다운 - 실제 데이터가 있는 연도만. */
    public List<String> availableYears() {
        return donationLedgerRepository.findByStatus(STATUS_COMPLETED).stream()
                .filter(d -> d.getEventDate() != null && d.getEventDate().length() >= 4)
                .map(d -> d.getEventDate().substring(0, 4))
                .distinct()
                .sorted(Comparator.reverseOrder())
                .toList();
    }

    private Map<Key, Long> sumPointsByKey(String txnType, boolean useAbsoluteValue) {
        return pointLedgerStatRepository.findAll().stream()
                .filter(p -> txnType.equals(p.getTxnType()) && p.getLocgovCode() != null && p.getCreatedDate() != null)
                .collect(Collectors.groupingBy(
                        p -> new Key(String.valueOf(p.getCreatedDate().getYear()), p.getLocgovCode()),
                        Collectors.summingLong(p -> {
                            long v = p.getPointAmount() != null ? p.getPointAmount() : 0L;
                            return useAbsoluteValue ? Math.abs(v) : v;
                        })));
    }

    private record Key(String year, String locgovCode) {
    }

    /** 년도/광역지자체/기부지자체 단위 집계 1행. */
    public record GiveStateRow(String cntrYear, String upperLocgovCode, String upperLocgovNm,
                                String locgovCode, String locgovNm,
                                BigDecimal cntrAmt, long giveCnt, long givePersons,
                                long cntrPoint, long cntrUsePoint) {
    }
}
