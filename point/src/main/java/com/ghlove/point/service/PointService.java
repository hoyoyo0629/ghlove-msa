package com.ghlove.point.service;

import com.ghlove.point.domain.CommonCodeId;
import com.ghlove.point.domain.PointBalance;
import com.ghlove.point.domain.PointLedger;
import com.ghlove.point.domain.PointReservation;
import com.ghlove.point.event.DonationCancelledEvent;
import com.ghlove.point.event.DonationCompletedEvent;
import com.ghlove.point.event.PointLedgerPublisher;
import com.ghlove.point.repository.CommonCodeRepository;
import com.ghlove.point.repository.LocgovPointRateRepository;
import com.ghlove.point.repository.PointBalanceRepository;
import com.ghlove.point.repository.PointLedgerRepository;
import com.ghlove.point.repository.PointReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PointService {

    private static final String TXN_EARN = "EARN";
    private static final String TXN_USE = "USE";
    private static final String TXN_REVERSE = "REVERSE";
    private static final String TXN_RESTORE = "RESTORE";
    private static final String TXN_EXPIRE = "EXPIRE";
    private static final String RESV_RESERVED = "RESERVED";
    private static final String RESV_CONFIRMED = "CONFIRMED";
    private static final String RESV_RELEASED = "RELEASED";
    private static final BigDecimal DEFAULT_RATE_FALLBACK = BigDecimal.valueOf(30);
    /** 고향사랑 기부금법 제8조 답례품(포인트) 제공한도 - SYSTEM_CONFIG/MAX_POINT_RATE 미설정 시 기본값. */
    private static final BigDecimal MAX_RATE_FALLBACK = BigDecimal.valueOf(30);
    private static final int DEFAULT_VALID_DAYS_FALLBACK = 1825;
    private static final int DEFAULT_EXPIRY_NOTICE_DAYS_FALLBACK = 30;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final PointLedgerRepository pointLedgerRepository;
    private final PointBalanceRepository pointBalanceRepository;
    private final LocgovPointRateRepository locgovPointRateRepository;
    private final CommonCodeRepository commonCodeRepository;
    private final PointReservationRepository pointReservationRepository;
    private final PointLedgerPublisher pointLedgerPublisher;
    private final com.ghlove.point.event.PointReservationPublisher pointReservationPublisher;

    public Map<String, String> codesOf(String codeType) {
        return commonCodeRepository.findByCodeTypeAndLanguageAndUseYnOrderByOrdering(codeType, "ko", "Y").stream()
                .collect(Collectors.toMap(
                        com.ghlove.point.domain.CommonCode::getId,
                        com.ghlove.point.domain.CommonCode::getLabel,
                        (a, b) -> a, java.util.LinkedHashMap::new));
    }

    public long balanceOf(Long userId) {
        return pointBalanceRepository.findById(userId).map(PointBalance::getBalance).orElse(0L);
    }

    /**
     * 지자체별 사용 가능 포인트 (SFR-004 - 기부 포인트는 기부한 지자체 답례품에만 쓸 수
     * 있다). PT_POINT_BALANCE는 전체 합계만 갖고 있어서, 아직 소진되지 않은 EARN/RESTORE
     * lot(PT_POINT_LEDGER.REMAINING_AMOUNT > 0)을 LOCGOV_CODE로 걸러 합산한다.
     */
    public long balanceByLocgov(Long userId, String locgovCode) {
        return pointLedgerRepository.findByUserIdAndRemainingAmountGreaterThanOrderByExpirationDateAsc(userId, 0L)
                .stream()
                .filter(lot -> locgovCode.equals(lot.getLocgovCode()))
                .mapToLong(PointLedger::getRemainingAmount)
                .sum();
    }

    public List<PointLedger> ledgerOf(Long userId) {
        return pointLedgerRepository.findByUserIdOrderByCreatedDateDesc(userId);
    }

    /**
     * 마이페이지 "기부포인트 조회" - 지자체별 적립/사용/잔여 (AS-IS mypage/cntrPoint.html).
     * 적립·사용의 집계 의미는 {@link #earnedOf}/{@link #usedOf} 참고 - 부호가 아니라
     * 거래유형으로 갈라서 취소분(REVERSE/RESTORE)을 각각 상계한다.
     */
    public List<LocgovPointSummary> ledgerSummaryByLocgov(Long userId) {
        Map<String, List<PointLedger>> byLocgov = ledgerOf(userId).stream()
                .filter(l -> l.getLocgovCode() != null)
                .collect(Collectors.groupingBy(PointLedger::getLocgovCode, LinkedHashMap::new, Collectors.toList()));
        return byLocgov.entrySet().stream()
                .map(e -> {
                    long earned = earnedOf(e.getValue());
                    long used = usedOf(e.getValue());
                    return new LocgovPointSummary(e.getKey(), earned, used, earned - used);
                })
                .toList();
    }

    public record LocgovPointSummary(String locgovCode, long earned, long used, long remaining) {
    }

    /**
     * 마이페이지 "기부포인트 조회" 목록 - AS-IS give-point-mapper.getGivePointList와 같은
     * 단위로 **기부연도 + 지자체**로 묶고 연도 내림차순으로 준다. 연도 범위(from~to)를 주면
     * 그 안만 본다(AS-IS shCntrYearStart/shCntrYearEnd).
     *
     * <p>지자체로만 묶는 {@link #ledgerSummaryByLocgov}는 예약/장바구니처럼 "지금 이 지자체에
     * 얼마 남았나"를 보는 쪽이 쓴다 - 그쪽은 연도를 나눠선 안 되므로 별도로 둔다.
     */
    public List<YearLocgovPointSummary> ledgerSummaryByYearAndLocgov(Long userId, String yearFrom, String yearTo) {
        return ledgerOf(userId).stream()
                .filter(l -> l.getLocgovCode() != null)
                .filter(l -> withinYear(l.getStdrYear(), yearFrom, yearTo))
                .collect(Collectors.groupingBy(
                        l -> new YearLocgovKey(yearOf(l), l.getLocgovCode()),
                        LinkedHashMap::new, Collectors.toList()))
                .entrySet().stream()
                .map(e -> {
                    List<PointLedger> rows = e.getValue();
                    long earned = earnedOf(rows);
                    long used = usedOf(rows);
                    return new YearLocgovPointSummary(e.getKey().year(), e.getKey().locgovCode(),
                            donatedAmountOf(rows), earned, used, earned - used);
                })
                .sorted(java.util.Comparator.comparing(YearLocgovPointSummary::year).reversed()
                        .thenComparing(YearLocgovPointSummary::locgovCode))
                .toList();
    }

    /**
     * 원장 행들의 **적립포인트** 합 - AS-IS give-point-mapper의 CNTR_POINT에 해당한다.
     *
     * <p>부호로 가르면 안 된다. 이 원장은 append-only라 취소가 원본 행을 지우지 않고 반대
     * 부호의 행을 덧붙이는데, 그러면 주문취소 복원(RESTORE, 양수)이 적립으로 둔갑하고
     * 기부취소 회수(REVERSE, 음수)가 사용으로 둔갑한다. 실제로 30,000P를 쓰고 주문을 취소하면
     * 적립·사용이 나란히 30,000P씩 부풀었다(잔여만 우연히 맞았다).
     *
     * <p>AS-IS는 취소된 기부(CNTR_STTUS_CODE != '200')를 집계에서 빼고 취소된 사용내역은
     * G_CNTR_USE_POINT 행 자체를 지운다(deleteGiveUsePoint). 즉 **취소분이 양쪽에서 사라지는
     * 것이 AS-IS 의미**이고, append-only 원장에서 같은 결과를 내려면 거래유형으로 상계해야 한다.
     */
    public static long earnedOf(Collection<PointLedger> rows) {
        return sumOf(rows, TXN_EARN, TXN_REVERSE);
    }

    /**
     * 원장 행들의 **사용포인트** 합(양수). USE에서 주문취소 복원(RESTORE)을 상계한다.
     * 소멸(EXPIRE)은 발행이 아니라 소진이라 사용 쪽에 넣는다 - 화면에 소멸 칸이 따로 없는데
     * 여기서 빼면 "적립 - 사용"이 실제 잔액과 어긋난다(admin 일별집계도 같은 처리를 한다).
     */
    public static long usedOf(Collection<PointLedger> rows) {
        return -sumOf(rows, TXN_USE, TXN_RESTORE, TXN_EXPIRE);
    }

    /**
     * 기부금액 합 - 적립 행에만 실려 있다. 취소된 기부(REVERSE 행이 달린 CNTR_SN)의 적립 행은
     * 제외한다: AS-IS가 CNTR_STTUS_CODE='200'만 세는 것과 같은 의미다.
     * 이 컬럼을 넣기 전에 적립된 행은 값이 NULL이라 0으로 집계된다.
     */
    private static long donatedAmountOf(Collection<PointLedger> rows) {
        java.util.Set<String> reversed = rows.stream()
                .filter(l -> TXN_REVERSE.equals(l.getTxnType()))
                .map(PointLedger::getRefKey)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
        return rows.stream()
                .filter(l -> l.getCntrAmt() != null)
                .filter(l -> l.getRefKey() == null || !reversed.contains(l.getRefKey()))
                .mapToLong(l -> l.getCntrAmt().longValue())
                .sum();
    }

    private static long sumOf(Collection<PointLedger> rows, String... txnTypes) {
        java.util.Set<String> types = java.util.Set.of(txnTypes);
        return rows.stream()
                .filter(l -> types.contains(l.getTxnType()))
                .filter(l -> l.getPointAmount() != null)
                .mapToLong(PointLedger::getPointAmount)
                .sum();
    }

    /**
     * 마이페이지 "기부포인트 현황"(AS-IS mypage/cntrPointDetail.html) - 한 지자체(+연도)의
     * 원장 행을 발생 순서대로 준다. 화면은 이 행들로 발생일자/적립/사용/잔여/참조를 그린다.
     */
    public List<PointLedger> ledgerDetail(Long userId, String locgovCode, String year) {
        return ledgerOf(userId).stream()
                .filter(l -> locgovCode == null || locgovCode.isBlank() || locgovCode.equals(l.getLocgovCode()))
                .filter(l -> year == null || year.isBlank() || year.equals(yearOf(l)))
                .sorted(java.util.Comparator.comparing(PointLedger::getCreatedDate,
                        java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())))
                .toList();
    }

    /** 회원이 실제로 갖고 있는 기부연도 목록 (검색 셀렉트박스용, 내림차순). */
    public List<String> ledgerYearsOf(Long userId) {
        return ledgerOf(userId).stream()
                .map(this::yearOf)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .sorted(java.util.Comparator.reverseOrder())
                .toList();
    }

    private String yearOf(PointLedger ledger) {
        if (ledger.getStdrYear() != null && !ledger.getStdrYear().isBlank()) {
            return ledger.getStdrYear();
        }
        return ledger.getCreatedDate() != null ? String.valueOf(ledger.getCreatedDate().getYear()) : null;
    }

    private boolean withinYear(String year, String from, String to) {
        String value = year == null || year.isBlank() ? null : year;
        if (value == null) {
            return from == null || from.isBlank();
        }
        if (from != null && !from.isBlank() && value.compareTo(from) < 0) {
            return false;
        }
        return to == null || to.isBlank() || value.compareTo(to) <= 0;
    }

    private record YearLocgovKey(String year, String locgovCode) {
    }

    /** AS-IS getGivePointList 한 행 - 기부연도/지자체/기부금액/적립/사용/잔여. */
    public record YearLocgovPointSummary(String year, String locgovCode, long cntrAmt,
                                          long earned, long used, long remaining) {
    }

    /** 마이페이지 "총 적립 포인트" 카드. 집계 의미는 {@link #earnedOf} 참고. */
    public long totalEarnedOf(Long userId) {
        return earnedOf(ledgerOf(userId));
    }

    /** 마이페이지 "총 사용 포인트" 카드. 집계 의미는 {@link #usedOf} 참고. */
    public long totalUsedOf(Long userId) {
        return usedOf(ledgerOf(userId));
    }

    /**
     * 기부완료 이벤트 소비 -> 포인트 적립 (SFR-003/SFR-004 이벤트 기반 연계).
     * REF_KEY(CNTR_SN)+EARN 조합으로 이미 처리된 이벤트인지 확인해 중복 적립을 막는다
     * (Kafka는 최소 1회 전달을 보장하므로 재전달/컨슈머 재시작에도 안전해야 함).
     */
    @Transactional
    public void creditForDonation(DonationCompletedEvent event) {
        if (pointLedgerRepository.existsByRefKeyAndTxnType(event.cntrSn(), TXN_EARN)) {
            log.info("Donation {} already credited - skipping duplicate event", event.cntrSn());
            return;
        }

        String year = event.cntrDe() != null && event.cntrDe().length() >= 4
                ? event.cntrDe().substring(0, 4)
                : String.valueOf(LocalDateTime.now().getYear());
        BigDecimal rate = pointRateOf(year, event.cntrLocgovCode());
        long pointAmount = event.cntrAmt()
                .multiply(rate)
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.FLOOR)
                .longValueExact();

        PointLedger ledger = new PointLedger();
        ledger.setUserId(event.userId());
        ledger.setLocgovCode(event.cntrLocgovCode());
        ledger.setTxnType(TXN_EARN);
        ledger.setPointAmount(pointAmount);
        ledger.setReason("기부포인트 적립 (" + rate + "%)");
        // AS-IS 요약이 기부연도·기부금액 단위라, 조회 때 donation을 다시 부르지 않도록
        // 이벤트가 실어온 값을 적립 행에 그대로 남긴다.
        ledger.setStdrYear(year);
        ledger.setCntrAmt(event.cntrAmt());
        ledger.setRefKey(event.cntrSn());
        ledger.setCreatedDate(LocalDateTime.now());
        openLot(ledger, pointAmount);
        pointLedgerRepository.save(ledger);
        pointLedgerPublisher.publish(ledger);

        adjustBalance(event.userId(), pointAmount);
        log.info("Credited {} points to userId={} for donation {}", pointAmount, event.userId(), event.cntrSn());
    }

    /**
     * 기부취소 이벤트 소비 -> 포인트 회수 (SAGA 보상 트랜잭션). 원래 적립(EARN) 건이
     * 있어야만 회수하며, 이미 회수했다면 다시 처리하지 않는다. 사용자가 이미 포인트를
     * 사용해 잔액이 부족하더라도 회수는 그대로 반영한다 (잔액이 음수가 될 수 있음 -
     * 실제 서비스라면 별도 채권/정산 처리로 이어질 부분).
     */
    @Transactional
    public void reverseForDonation(DonationCancelledEvent event) {
        if (!pointLedgerRepository.existsByRefKeyAndTxnType(event.cntrSn(), TXN_EARN)) {
            log.info("Donation {} was never credited (cancelled while still REQUESTED) - nothing to reverse",
                    event.cntrSn());
            return;
        }
        if (pointLedgerRepository.existsByRefKeyAndTxnType(event.cntrSn(), TXN_REVERSE)) {
            log.info("Donation {} already reversed - skipping duplicate event", event.cntrSn());
            return;
        }

        PointLedger original = pointLedgerRepository.findFirstByRefKeyAndTxnType(event.cntrSn(), TXN_EARN)
                .orElseThrow();
        long earned = original.getPointAmount();
        // 이미 사용된 포인트는 회수할 수 없다 - 적립 lot의 미사용 잔량(remainingAmount)만 회수한다.
        // (예전 버그: 사용 여부와 무관하게 적립 전액을 차감해 잔액이 음수가 됐다. 포인트는 화폐성
        //  자원이므로 잔액이 음수가 되면 안 된다.) 사용된 부분은 그 주문/사용 트랜잭션이 별도로 책임진다.
        long reversible = original.getRemainingAmount() != null ? Math.max(0L, original.getRemainingAmount()) : 0L;

        PointLedger ledger = new PointLedger();
        ledger.setUserId(event.userId());
        ledger.setLocgovCode(event.cntrLocgovCode());
        ledger.setTxnType(TXN_REVERSE);
        ledger.setPointAmount(-reversible);
        ledger.setReason(reversible < earned
                ? "기부 취소에 따른 포인트 회수 (일부는 이미 사용되어 미사용분 " + reversible + "P만 회수)"
                : "기부 취소에 따른 포인트 회수");
        ledger.setRefKey(event.cntrSn());
        ledger.setCreatedDate(LocalDateTime.now());
        pointLedgerRepository.save(ledger);
        pointLedgerPublisher.publish(ledger);

        // 이 적립 lot의 미사용 잔량을 소멸시키고, 딱 그만큼만 잔액에서 차감한다.
        original.setRemainingAmount(0L);
        pointLedgerRepository.save(original);
        adjustBalance(event.userId(), -reversible);
        if (reversible < earned) {
            log.warn("Donation {} reversal: earned={} but only {} was unused - {}P already spent, not clawed back",
                    event.cntrSn(), earned, reversible, earned - reversible);
        }
        log.info("Reversed {} points from userId={} for cancelled donation {}",
                reversible, event.userId(), event.cntrSn());
    }

    /**
     * order.saga의 ORDER_CREATED 이벤트에 반응해 포인트를 차감한다. 기부 포인트는 기부한
     * 지자체 답례품에만 쓸 수 있으므로(장바구니 화면의 "지자체별 잔여 포인트"), 전체
     * 잔액이 아니라 이 주문의 지자체(locgovCode)로 적립된 lot만 확인/소비한다. 잔액이
     * 부족하면 차감하지 않고 false를 반환한다 (호출자가 POINT_DEDUCT_FAILED를 발행).
     * REF_KEY(orderId)+USE 조합으로 이미 처리된 이벤트인지 확인해 중복 차감을 막는다.
     */
    @Transactional
    public boolean deductForOrder(String orderId, Long userId, long pointAmount, String locgovCode) {
        if (pointLedgerRepository.existsByRefKeyAndTxnType(orderId, TXN_USE)) {
            log.info("Order {} already processed for point deduction - skipping duplicate event", orderId);
            return true;
        }
        if (balanceByLocgov(userId, locgovCode) < pointAmount) {
            return false;
        }

        PointLedger ledger = new PointLedger();
        ledger.setUserId(userId);
        ledger.setLocgovCode(locgovCode);
        ledger.setTxnType(TXN_USE);
        ledger.setPointAmount(-pointAmount);
        ledger.setReason("주문 결제");
        ledger.setRefKey(orderId);
        ledger.setCreatedDate(LocalDateTime.now());
        pointLedgerRepository.save(ledger);
        pointLedgerPublisher.publish(ledger);

        consumeLots(userId, pointAmount, locgovCode);
        adjustBalance(userId, -pointAmount);
        return true;
    }

    /** order.saga의 ORDER_CANCELLED에 반응해, 이 서비스가 실제로 차감했던 포인트만 복원한다. */
    @Transactional
    public void restoreForOrder(String orderId) {
        if (!pointLedgerRepository.existsByRefKeyAndTxnType(orderId, TXN_USE)) {
            log.info("Order {} was never deducted here - nothing to restore", orderId);
            return;
        }
        if (pointLedgerRepository.existsByRefKeyAndTxnType(orderId, TXN_RESTORE)) {
            log.info("Order {} already restored - skipping duplicate event", orderId);
            return;
        }

        PointLedger original = pointLedgerRepository.findFirstByRefKeyAndTxnType(orderId, TXN_USE).orElseThrow();
        long restoreAmount = -original.getPointAmount();

        PointLedger ledger = new PointLedger();
        ledger.setUserId(original.getUserId());
        ledger.setLocgovCode(original.getLocgovCode());
        ledger.setTxnType(TXN_RESTORE);
        ledger.setPointAmount(restoreAmount);
        ledger.setReason("주문취소 복원");
        ledger.setRefKey(orderId);
        ledger.setCreatedDate(LocalDateTime.now());
        openLot(ledger, restoreAmount);
        pointLedgerRepository.save(ledger);
        pointLedgerPublisher.publish(ledger);

        adjustBalance(original.getUserId(), restoreAmount);
    }

    /**
     * 포인트 사용(차감) 수동 테스트 기능 (주문 외 임의 사용 시나리오 확인용).
     *
     * <p>지자체를 반드시 받는다 - SFR-004상 기부 포인트는 기부한 지자체 답례품에만 쓸 수
     * 있고, AS-IS도 사용이력(G_CNTR_USE_POINT)에 CNTR_LOCGOV_CODE를 항상 남긴다. 예전에는
     * 이 경로가 지자체를 모른 채 전역 잔액만 보고 차감했는데, 그러면 원장행은 지자체가
     * 비어 있는데 lot은 아무 지자체 것이나 소진돼서 "마이페이지 기부포인트 조회(원장 합계)"와
     * "장바구니 잔여 포인트(lot 합계)"가 영구히 어긋났다.
     */
    @Transactional
    public void usePoints(Long userId, long amount, String locgovCode, String orderCode) {
        if (userId == null || userId <= 0) {
            throw new PointException("회원 ID를 입력해 주세요.");
        }
        if (locgovCode == null || locgovCode.isBlank()) {
            throw new PointException("포인트를 사용할 지자체를 선택해 주세요.");
        }
        if (amount <= 0) {
            throw new PointException("사용 포인트는 0보다 커야 합니다.");
        }
        long available = availableBalanceOf(userId, locgovCode);
        if (available < amount) {
            throw new PointException("해당 지자체의 사용 가능 포인트가 부족합니다. (가용잔액: " + available + "P)");
        }

        PointLedger ledger = new PointLedger();
        ledger.setUserId(userId);
        ledger.setLocgovCode(locgovCode);
        ledger.setTxnType(TXN_USE);
        ledger.setPointAmount(-amount);
        ledger.setReason("포인트 사용");
        ledger.setRefKey(orderCode);
        ledger.setCreatedDate(LocalDateTime.now());
        pointLedgerRepository.save(ledger);
        pointLedgerPublisher.publish(ledger);

        consumeLots(userId, amount, locgovCode);
        adjustBalance(userId, -amount);
    }

    public long reservedAmountOf(Long userId) {
        return pointReservationRepository.sumReservedByUserId(userId);
    }

    public long reservedAmountOf(Long userId, String locgovCode) {
        return pointReservationRepository.sumReservedByUserIdAndLocgov(userId, locgovCode);
    }

    /** 가용 잔액 = 보유잔액 - 활성 예약 합계. 전체 합계 표시용. */
    public long availableBalanceOf(Long userId) {
        return balanceOf(userId) - reservedAmountOf(userId);
    }

    /**
     * 지자체별 가용 잔액 = 그 지자체 lot 잔여합 - 그 지자체 활성 예약 합계.
     * 예약/사용 가능 여부는 반드시 이 값으로 판단해야 한다 - 전역 잔액으로 판단하면
     * 다른 지자체가 적립해 준 포인트를 쓸 수 있게 된다.
     */
    public long availableBalanceOf(Long userId, String locgovCode) {
        return balanceByLocgov(userId, locgovCode) - reservedAmountOf(userId, locgovCode);
    }

    public List<PointReservation> reservationsOf(Long userId) {
        return pointReservationRepository.findByUserIdOrderByCreatedDateDesc(userId);
    }

    /** 컨트롤러의 소유자 확인용 (SFR-010 - 그 예약이 실제 로그인한 회원 것인지 검증). */
    public boolean isReservationOwner(Long reservationId, Long userId) {
        return pointReservationRepository.findById(reservationId)
                .map(PointReservation::getUserId).map(userId::equals).orElse(false);
    }

    /**
     * 포인트 예약(hold) - SFR-004. 원장/잔액은 건드리지 않고 예약 행만 남긴다 (결제
     * 확정 전 "일단 잡아두기" 단계). 예약 가능 여부는 <b>지자체별</b> 가용잔액 기준으로
     * 판단한다.
     *
     * <p><b>실제 주문결제 SAGA({@link #deductForOrder})와는 의도적으로 분리된 별개
     * 기능이다</b> - 마이페이지 "포인트 예약 관리" 화면에서 회원이 직접 호출하는 수동
     * 기능일 뿐, order 체크아웃이 이 경로를 타지 않는다(AS-IS에는 예약 개념 자체가 없는
     * 이 프로젝트 신규 기능이다).
     *
     * <p>예전에는 이 메서드와 {@link #confirmReservation}이 {@code locgovCode}를 전혀
     * 모른 채 전역 잔액만 봤는데, 확정 시 {@code consumeLots}가 만료일 순서로 아무 지자체
     * lot이나 소진해 버려서 두 가지가 동시에 깨졌다: (1) "다른 지자체가 적립한 포인트로
     * 이 지자체 답례품을 사는" SFR-004 위반, (2) 원장행에는 지자체가 비어 있는데 lot만
     * 줄어드니 마이페이지 기부포인트 조회(원장 합계)와 장바구니 잔여 포인트(lot 합계)가
     * 영구히 어긋남. 그래서 예약도 지자체 단위로 바꿨다.
     */
    @Transactional
    public PointReservation reserve(Long userId, long amount, String locgovCode, String refKey, String reason) {
        if (userId == null || userId <= 0) {
            throw new PointException("회원 ID를 입력해 주세요.");
        }
        if (locgovCode == null || locgovCode.isBlank()) {
            throw new PointException("예약할 지자체를 선택해 주세요.");
        }
        if (amount <= 0) {
            throw new PointException("예약 포인트는 0보다 커야 합니다.");
        }
        long available = availableBalanceOf(userId, locgovCode);
        if (available < amount) {
            throw new PointException("해당 지자체의 예약 가능한 포인트가 부족합니다. (가용잔액: " + available + "P)");
        }

        PointReservation reservation = new PointReservation();
        reservation.setUserId(userId);
        reservation.setLocgovCode(locgovCode);
        reservation.setAmount(amount);
        reservation.setRefKey(refKey);
        reservation.setReason(reason);
        reservation.setStatus(RESV_RESERVED);
        reservation.setCreatedDate(LocalDateTime.now());
        PointReservation saved = pointReservationRepository.save(reservation);
        // ISP 도메인 이벤트 "포인트예약됨" - 예약은 원장을 건드리지 않아 PointLedgerEvent가 안 나간다.
        pointReservationPublisher.publishReserved(saved);
        return saved;
    }

    /** 예약 확정 - 이 시점에 비로소 실제 원장(USE)행과 잔액 차감이 발생한다. */
    @Transactional
    public PointReservation confirmReservation(Long reservationId) {
        PointReservation reservation = getReservationOrThrow(reservationId);
        if (!RESV_RESERVED.equals(reservation.getStatus())) {
            throw new PointException("예약중 상태인 건만 확정할 수 있습니다.");
        }
        String locgovCode = reservation.getLocgovCode();
        if (locgovCode == null || locgovCode.isBlank()) {
            // 지자체 없이 만들어진 옛 예약행 - 확정하면 다른 지자체 lot을 잠식하므로 막는다.
            throw new PointException("지자체 정보가 없는 예약은 확정할 수 없습니다. 해제 후 다시 예약해 주세요.");
        }
        // 예약 후 그 지자체 포인트가 다른 경로로 빠져나갔을 수 있다 - 확정 시점에 다시 본다
        // (자기 예약분은 아직 원장에 없으므로 lot 잔여합과 직접 비교한다).
        long lotBalance = balanceByLocgov(reservation.getUserId(), locgovCode);
        if (lotBalance < reservation.getAmount()) {
            throw new PointException("해당 지자체의 포인트가 부족해 확정할 수 없습니다. (잔여: " + lotBalance + "P)");
        }

        PointLedger ledger = new PointLedger();
        ledger.setUserId(reservation.getUserId());
        ledger.setLocgovCode(locgovCode);
        ledger.setTxnType(TXN_USE);
        ledger.setPointAmount(-reservation.getAmount());
        ledger.setReason("포인트 예약 확정" + (reservation.getReason() != null ? " (" + reservation.getReason() + ")" : ""));
        ledger.setRefKey(reservation.getRefKey() != null ? reservation.getRefKey() : "RESV-" + reservation.getReservationId());
        ledger.setCreatedDate(LocalDateTime.now());
        pointLedgerRepository.save(ledger);
        pointLedgerPublisher.publish(ledger);

        consumeLots(reservation.getUserId(), reservation.getAmount(), locgovCode);
        adjustBalance(reservation.getUserId(), -reservation.getAmount());

        reservation.setStatus(RESV_CONFIRMED);
        reservation.setResolvedDate(LocalDateTime.now());
        return pointReservationRepository.save(reservation);
    }

    /** 예약 해제 - 원장/잔액을 애초에 건드리지 않았으므로 상태만 되돌리면 끝난다. */
    @Transactional
    public PointReservation releaseReservation(Long reservationId) {
        PointReservation reservation = getReservationOrThrow(reservationId);
        if (!RESV_RESERVED.equals(reservation.getStatus())) {
            throw new PointException("예약중 상태인 건만 해제할 수 있습니다.");
        }
        reservation.setStatus(RESV_RELEASED);
        reservation.setResolvedDate(LocalDateTime.now());
        PointReservation released = pointReservationRepository.save(reservation);
        // ISP 도메인 이벤트 "포인트해제됨".
        pointReservationPublisher.publishReleased(released);
        return released;
    }

    private PointReservation getReservationOrThrow(Long reservationId) {
        return pointReservationRepository.findById(reservationId)
                .orElseThrow(() -> new PointException("예약 내역을 찾을 수 없습니다."));
    }

    /** 만료 예정 포인트 안내 (SFR-004) - 아직 만료되지 않았지만 안내 기준일 이내에 만료될 lot들. */
    public List<PointLedger> upcomingExpirations(Long userId) {
        String today = DATE_FORMAT.format(LocalDate.now());
        String noticeLimit = DATE_FORMAT.format(LocalDate.now().plusDays(expiryNoticeDays()));
        return pointLedgerRepository.findByUserIdAndRemainingAmountGreaterThanOrderByExpirationDateAsc(userId, 0L)
                .stream()
                .filter(lot -> lot.getExpirationDate() != null
                        && lot.getExpirationDate().compareTo(today) >= 0
                        && lot.getExpirationDate().compareTo(noticeLimit) <= 0)
                .toList();
    }

    private int expiryNoticeDays() {
        return commonCodeRepository.findById(new CommonCodeId("SYSTEM_CONFIG", "ko", "POINT_EXPIRY_NOTICE_DAYS"))
                .map(com.ghlove.point.domain.CommonCode::getCodeValue)
                .filter(v -> v != null && !v.isBlank())
                .map(Integer::parseInt)
                .orElse(DEFAULT_EXPIRY_NOTICE_DAYS_FALLBACK);
    }

    /**
     * 소멸 처리 배치 (SFR-004 "소멸 처리"). 매일 자동 실행(donation의
     * {@code DesignatedAdminService.closeExpiredProjects()}와 동일한 "기간이 그냥
     * 흘러서 발생하는 전환이라 어떤 이벤트에도 안 걸린다" 성격 - SFR-004 재검토
     * 라운드에서 수동 트리거뿐이던 걸 자동화로 전환) - 운영자가 즉시 실행해보고 싶을
     * 때를 위한 수동 실행 엔드포인트(`/batch/expire`)도 그대로 남겨둔다. 만료일이
     * 지났는데 아직 안 쓰인 lot(EARN/RESTORE 잔여분)을 모두 찾아 EXPIRE 원장 행으로
     * 소멸시키고 잔액에서 차감한다.
     *
     * @return 소멸 처리된 lot 개수
     */
    @Scheduled(cron = "0 30 1 * * *")
    @Transactional
    public int runExpirationBatch() {
        String today = DATE_FORMAT.format(LocalDate.now());
        List<PointLedger> expiredLots = pointLedgerRepository
                .findByRemainingAmountGreaterThanAndExpirationDateLessThan(0L, today);

        for (PointLedger lot : expiredLots) {
            long expiredAmount = lot.getRemainingAmount();

            PointLedger expireLedger = new PointLedger();
            expireLedger.setUserId(lot.getUserId());
            expireLedger.setLocgovCode(lot.getLocgovCode());
            expireLedger.setTxnType(TXN_EXPIRE);
            expireLedger.setPointAmount(-expiredAmount);
            expireLedger.setReason("유효기간 만료 소멸 (원 적립: 원장 #" + lot.getLedgerId() + ", 만료일 " + lot.getExpirationDate() + ")");
            expireLedger.setRefKey(String.valueOf(lot.getLedgerId()));
            expireLedger.setCreatedDate(LocalDateTime.now());
            pointLedgerRepository.save(expireLedger);
            pointLedgerPublisher.publish(expireLedger);

            lot.setRemainingAmount(0L);
            pointLedgerRepository.save(lot);

            adjustBalance(lot.getUserId(), -expiredAmount);
            log.info("Expired {} points from userId={} (lot ledgerId={}, expired {})",
                    expiredAmount, lot.getUserId(), lot.getLedgerId(), lot.getExpirationDate());
        }
        return expiredLots.size();
    }

    /**
     * 회원 탈퇴 시 잔여 기부포인트 전량 소멸 (AS-IS `GeneralCustomerServiceImpl:286~296`).
     *
     * <p>AS-IS는 탈퇴 처리 중 `getCntrBlcePointList(userId)`로 잔여 기부포인트를 뽑아
     * `updateCntrBlcePointDel` + `insertCntrUsePoint`를 돈다 — <b>만료 배치와 완전히 같은
     * 처리</b>다(소멸분을 사용 이력으로 남긴다). 그래서 여기서도 만료 배치와 같은 모양으로
     * `EXPIRE` 원장행을 남기고 잔액을 깎는다. 사유 문구만 달리해 나중에 구분할 수 있게 한다.
     *
     * <p>탈퇴는 되돌릴 수 있는 처리가 아니므로 <b>실패하면 예외를 던진다</b> — member가 이
     * 호출에 실패하면 탈퇴를 진행하지 말아야 한다. 소멸시키지 않은 포인트가 원장에 남으면
     * 탈퇴 회원의 잔액이 살아 있는 상태가 된다.
     *
     * @return 소멸된 총 포인트
     */
    @Transactional
    public long expireAllOnWithdrawal(Long userId) {
        List<PointLedger> lots = pointLedgerRepository
                .findByUserIdAndRemainingAmountGreaterThanOrderByExpirationDateAsc(userId, 0L);
        long total = 0L;
        for (PointLedger lot : lots) {
            long remaining = lot.getRemainingAmount();

            PointLedger expireLedger = new PointLedger();
            expireLedger.setUserId(lot.getUserId());
            expireLedger.setLocgovCode(lot.getLocgovCode());
            expireLedger.setTxnType(TXN_EXPIRE);
            expireLedger.setPointAmount(-remaining);
            expireLedger.setReason("회원 탈퇴에 따른 소멸 (원 적립: 원장 #" + lot.getLedgerId() + ")");
            expireLedger.setRefKey(String.valueOf(lot.getLedgerId()));
            expireLedger.setCreatedDate(LocalDateTime.now());
            pointLedgerRepository.save(expireLedger);
            pointLedgerPublisher.publish(expireLedger);

            lot.setRemainingAmount(0L);
            pointLedgerRepository.save(lot);

            adjustBalance(lot.getUserId(), -remaining);
            total += remaining;
        }
        if (total > 0) {
            log.info("Expired {} points on withdrawal (userId={}, lots={})", total, userId, lots.size());
        }
        return total;
    }

    /** donation 기부하기 화면의 "포인트 적립예상" 표시용 - 올해 기준 해당 지자체 적립률(%). */
    public BigDecimal currentPointRateOf(String locgovCode) {
        return pointRateOf(String.valueOf(java.time.LocalDate.now().getYear()), locgovCode);
    }

    private BigDecimal pointRateOf(String year, String locgovCode) {
        BigDecimal rate = locgovPointRateRepository.findByStdrYearAndLocgovCode(year, locgovCode)
                .map(com.ghlove.point.domain.LocgovPointRate::getPointRate)
                .orElseGet(this::defaultPointRate);
        // 법정 상한(고향사랑 기부금법 제8조 답례품 제공한도, 기본 30%)을 적립 시점에도 클램프한다.
        // 저장 시 가드가 도입되기 전 들어온 초과 데이터(예: 40%)로 상한을 넘겨 적립되는 것을 막는다.
        BigDecimal maxRate = maxPointRate();
        return rate != null && rate.compareTo(maxRate) > 0 ? maxRate : rate;
    }

    private BigDecimal defaultPointRate() {
        return commonCodeRepository.findById(new CommonCodeId("SYSTEM_CONFIG", "ko", "DEFAULT_POINT_RATE"))
                .map(com.ghlove.point.domain.CommonCode::getCodeValue)
                .filter(v -> v != null && !v.isBlank())
                .map(BigDecimal::new)
                .orElse(DEFAULT_RATE_FALLBACK);
    }

    private BigDecimal maxPointRate() {
        return commonCodeRepository.findById(new CommonCodeId("SYSTEM_CONFIG", "ko", "MAX_POINT_RATE"))
                .map(com.ghlove.point.domain.CommonCode::getCodeValue)
                .filter(v -> v != null && !v.isBlank())
                .map(BigDecimal::new)
                .orElse(MAX_RATE_FALLBACK);
    }

    /** admin 지자체관리 화면 "포인트 지급률 등록/수정"(AS-IS user/locgov/edit.jsp "포인트
     *  지급률") - 연도+지자체 단위로 upsert한다. 이 값이 바로 {@link #currentPointRateOf}가
     *  읽는 실제 적립률이다. 고향사랑 기부금법 제8조상 답례품(포인트) 제공한도인 기부금액의
     *  30%를 넘게 저장할 수 없다(SFR-003 재검토 라운드에서 발견한 gap - 지금까지는 관리자가
     *  얼마든 자유 입력해도 그대로 저장됐다). 한도 자체는 법 개정 가능성을 고려해
     *  SYSTEM_CONFIG(MAX_POINT_RATE)로 관리하고 하드코딩하지 않는다. */
    @org.springframework.transaction.annotation.Transactional
    public com.ghlove.point.domain.LocgovPointRate upsertLocgovPointRate(String stdrYear, String locgovCode,
                                                                           BigDecimal pointRate, String managerName) {
        BigDecimal maxRate = maxPointRate();
        if (pointRate.compareTo(maxRate) > 0) {
            throw new PointException("포인트 지급률은 " + maxRate.stripTrailingZeros().toPlainString()
                    + "%를 초과할 수 없습니다(고향사랑 기부금법 제8조 답례품 제공한도).");
        }
        if (pointRate.compareTo(BigDecimal.ZERO) < 0) {
            throw new PointException("포인트 지급률은 0% 이상이어야 합니다.");
        }
        com.ghlove.point.domain.LocgovPointRate entity = locgovPointRateRepository
                .findByStdrYearAndLocgovCode(stdrYear, locgovCode)
                .orElseGet(() -> {
                    com.ghlove.point.domain.LocgovPointRate e = new com.ghlove.point.domain.LocgovPointRate();
                    e.setStdrYear(stdrYear);
                    e.setLocgovCode(locgovCode);
                    return e;
                });
        entity.setPointRate(pointRate);
        entity.setLastUpdtPnttm(java.time.LocalDateTime.now());
        entity.setLastUpdusrNm(managerName);
        return locgovPointRateRepository.save(entity);
    }

    /** admin 지자체관리 "포인트 지급률 변경이력" 팝업(AS-IS locgov-point-list.jsp)용. */
    public java.util.List<com.ghlove.point.domain.LocgovPointRate> locgovPointRateHistory(String locgovCode) {
        return locgovPointRateRepository.findByLocgovCodeOrderByStdrYearDesc(locgovCode);
    }

    private int pointValidDays() {
        return commonCodeRepository.findById(new CommonCodeId("SYSTEM_CONFIG", "ko", "POINT_VALID_DAYS"))
                .map(com.ghlove.point.domain.CommonCode::getCodeValue)
                .filter(v -> v != null && !v.isBlank())
                .map(Integer::parseInt)
                .orElse(DEFAULT_VALID_DAYS_FALLBACK);
    }

    /** EARN/RESTORE 원장 행을 새 FIFO lot으로 연다 (잔여량 = 발생액, 만료일 = 오늘 + 유효기간). */
    private void openLot(PointLedger ledger, long amount) {
        ledger.setRemainingAmount(amount);
        ledger.setExpirationDate(DATE_FORMAT.format(LocalDate.now().plusDays(pointValidDays())));
    }

    /** 차감액을 만료 임박한 lot부터 순서대로 소비한다 (FIFO). locgovCode가 있으면 그
     * 지자체로 적립된 lot만 대상으로 한다 - 다른 지자체 기부 포인트로는 소비되지 않는다. */
    private void consumeLots(Long userId, long debitAmount, String locgovCode) {
        long remaining = debitAmount;
        List<PointLedger> lots = pointLedgerRepository
                .findByUserIdAndRemainingAmountGreaterThanOrderByExpirationDateAsc(userId, 0L);

        for (PointLedger lot : lots) {
            if (remaining <= 0) {
                break;
            }
            if (locgovCode != null && !locgovCode.equals(lot.getLocgovCode())) {
                continue;
            }
            long consumed = Math.min(remaining, lot.getRemainingAmount());
            lot.setRemainingAmount(lot.getRemainingAmount() - consumed);
            pointLedgerRepository.save(lot);
            remaining -= consumed;
        }
        // 잔여 lot보다 차감액이 큰 경우(과거 미추적 lot 등) 남은 만큼은 조용히 무시한다 -
        // 잔액(PT_POINT_BALANCE) 자체는 항상 정확하게 별도로 갱신되므로 사용자 잔액에는 영향 없음.
    }

    private void adjustBalance(Long userId, long delta) {
        PointBalance balance = pointBalanceRepository.findById(userId).orElseGet(() -> {
            PointBalance b = new PointBalance();
            b.setUserId(userId);
            b.setBalance(0L);
            return b;
        });
        balance.setBalance(balance.getBalance() + delta);
        balance.setUpdatedDate(LocalDateTime.now());
        pointBalanceRepository.save(balance);
    }
}
