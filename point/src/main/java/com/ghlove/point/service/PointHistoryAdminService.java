package com.ghlove.point.service;

import com.ghlove.point.domain.PointLedger;
import com.ghlove.point.repository.PointLedgerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * admin 콘솔 "포인트 사용내역"/"일별 포인트 현황" 화면(AS-IS opmanager/point -
 * PointManagerController, B2)용. 기존 GivePointController(admin)는 기부 건별(CNTR_SN)
 * 발생-사용-잔액만 다루지만, AS-IS는 이와 별개로 PT_POINT_LEDGER 전체 트랜잭션(EARN/USE/
 * REVERSE/RESTORE/EXPIRE, 지자체 무관 cross-locgov)을 그대로 훑어보는 로그 화면과, 그걸
 * 일자별로 발생/사용 합계로 집계한 대시보드가 따로 있다.
 */
@Service
@RequiredArgsConstructor
public class PointHistoryAdminService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final int HISTORY_LIMIT = 500;

    /** 적립성(양수, 발행) 트랜잭션 - 소멸(EXPIRE)은 발행이 아니라 소진이라 사용 쪽으로 집계한다. */
    private static final java.util.Set<String> EARN_TYPES = java.util.Set.of("EARN", "RESTORE");
    private static final java.util.Set<String> USE_TYPES = java.util.Set.of("USE", "REVERSE");
    private static final String EXPIRE_TYPE = "EXPIRE";

    private final PointLedgerRepository pointLedgerRepository;

    /** 전체 사용내역 로그 - 최신순 최대 500건, 필터는 메모리에서(다른 admin 로그 화면과 동일한
     *  "top N + in-memory filter" 관례, 로컬/운영 규모상 문제 없음). */
    public List<HistoryRow> search(Long userId, String locgovCode, String txnType,
                                    String fromDate, String toDate) {
        LocalDateTime from = parseStart(fromDate);
        LocalDateTime to = parseEnd(toDate);
        return pointLedgerRepository.findAll().stream()
                .filter(l -> userId == null || userId.equals(l.getUserId()))
                .filter(l -> locgovCode == null || locgovCode.isBlank() || locgovCode.equals(l.getLocgovCode()))
                .filter(l -> txnType == null || txnType.isBlank() || txnType.equals(l.getTxnType()))
                .filter(l -> from == null || l.getCreatedDate() == null || !l.getCreatedDate().isBefore(from))
                .filter(l -> to == null || l.getCreatedDate() == null || !l.getCreatedDate().isAfter(to))
                .sorted(Comparator.comparing(PointLedger::getCreatedDate,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(HISTORY_LIMIT)
                .map(this::toRow)
                .toList();
    }

    private HistoryRow toRow(PointLedger l) {
        return new HistoryRow(l.getLedgerId(), l.getUserId(), l.getLocgovCode(), l.getTxnType(),
                l.getPointAmount(), l.getReason(), l.getRefKey(), l.getCreatedDate(),
                l.getRemainingAmount(), l.getExpirationDate());
    }

    public record HistoryRow(Long ledgerId, Long userId, String locgovCode, String txnType, Long pointAmount,
                              String reason, String refKey, LocalDateTime createdDate,
                              Long remainingAmount, String expirationDate) {
    }

    /** 일별 발생/사용/소멸 현황 집계. */
    public List<DailyStat> dailyStats(String fromDate, String toDate, String locgovCode) {
        LocalDateTime from = parseStart(fromDate);
        LocalDateTime to = parseEnd(toDate);
        List<PointLedger> rows = pointLedgerRepository.findAll().stream()
                .filter(l -> l.getCreatedDate() != null)
                .filter(l -> locgovCode == null || locgovCode.isBlank() || locgovCode.equals(l.getLocgovCode()))
                .filter(l -> from == null || !l.getCreatedDate().isBefore(from))
                .filter(l -> to == null || !l.getCreatedDate().isAfter(to))
                .toList();

        Map<String, long[]> byDate = new LinkedHashMap<>(); // [earnCount, earnAmount, useCount, useAmount, expireCount, expireAmount]
        for (PointLedger l : rows) {
            String date = l.getCreatedDate().toLocalDate().format(DATE_FMT);
            long[] agg = byDate.computeIfAbsent(date, d -> new long[6]);
            long amount = l.getPointAmount() != null ? Math.abs(l.getPointAmount()) : 0L;
            if (EARN_TYPES.contains(l.getTxnType())) {
                agg[0]++;
                agg[1] += amount;
            } else if (USE_TYPES.contains(l.getTxnType())) {
                agg[2]++;
                agg[3] += amount;
            } else if (EXPIRE_TYPE.equals(l.getTxnType())) {
                agg[4]++;
                agg[5] += amount;
            }
        }
        return byDate.entrySet().stream()
                .sorted(Map.Entry.<String, long[]>comparingByKey().reversed())
                .map(e -> new DailyStat(e.getKey(), e.getValue()[0], e.getValue()[1],
                        e.getValue()[2], e.getValue()[3], e.getValue()[4], e.getValue()[5]))
                .toList();
    }

    private LocalDateTime parseStart(String date) {
        if (date == null || date.isBlank()) {
            return null;
        }
        return LocalDate.parse(date).atStartOfDay();
    }

    private LocalDateTime parseEnd(String date) {
        if (date == null || date.isBlank()) {
            return null;
        }
        return LocalDate.parse(date).atTime(23, 59, 59);
    }

    public record DailyStat(String date, long earnCount, long earnAmount,
                             long useCount, long useAmount, long expireCount, long expireAmount) {
    }
}
