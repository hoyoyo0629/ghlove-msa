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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private static final int DEFAULT_VALID_DAYS_FALLBACK = 1825;
    private static final int DEFAULT_EXPIRY_NOTICE_DAYS_FALLBACK = 30;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final PointLedgerRepository pointLedgerRepository;
    private final PointBalanceRepository pointBalanceRepository;
    private final LocgovPointRateRepository locgovPointRateRepository;
    private final CommonCodeRepository commonCodeRepository;
    private final PointReservationRepository pointReservationRepository;
    private final PointLedgerPublisher pointLedgerPublisher;

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
     * PT_POINT_LEDGER.POINT_AMOUNT는 부호 있는 값(+적립 EARN/RESTORE, -사용 USE/REVERSE/
     * EXPIRE)이라 그냥 합산하면 잔여가 나온다 - 별도 잔액 컬럼을 새로 만들 필요가 없다.
     */
    public List<LocgovPointSummary> ledgerSummaryByLocgov(Long userId) {
        Map<String, List<PointLedger>> byLocgov = ledgerOf(userId).stream()
                .filter(l -> l.getLocgovCode() != null)
                .collect(Collectors.groupingBy(PointLedger::getLocgovCode, LinkedHashMap::new, Collectors.toList()));
        return byLocgov.entrySet().stream()
                .map(e -> {
                    long earned = e.getValue().stream().mapToLong(PointLedger::getPointAmount).filter(a -> a > 0).sum();
                    long used = e.getValue().stream().mapToLong(PointLedger::getPointAmount).filter(a -> a < 0).map(a -> -a).sum();
                    return new LocgovPointSummary(e.getKey(), earned, used, earned - used);
                })
                .toList();
    }

    public record LocgovPointSummary(String locgovCode, long earned, long used, long remaining) {
    }

    public long totalEarnedOf(Long userId) {
        return ledgerOf(userId).stream().mapToLong(PointLedger::getPointAmount).filter(a -> a > 0).sum();
    }

    public long totalUsedOf(Long userId) {
        return ledgerOf(userId).stream().mapToLong(PointLedger::getPointAmount).filter(a -> a < 0).map(a -> -a).sum();
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
        long reverseAmount = -original.getPointAmount();

        PointLedger ledger = new PointLedger();
        ledger.setUserId(event.userId());
        ledger.setLocgovCode(event.cntrLocgovCode());
        ledger.setTxnType(TXN_REVERSE);
        ledger.setPointAmount(reverseAmount);
        ledger.setReason("기부 취소에 따른 포인트 회수");
        ledger.setRefKey(event.cntrSn());
        ledger.setCreatedDate(LocalDateTime.now());
        pointLedgerRepository.save(ledger);
        pointLedgerPublisher.publish(ledger);

        consumeLots(event.userId(), original.getPointAmount());
        adjustBalance(event.userId(), reverseAmount);
        log.info("Reversed {} points from userId={} for cancelled donation {}",
                original.getPointAmount(), event.userId(), event.cntrSn());
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

    /** 포인트 사용(차감) 수동 테스트 기능 (주문 외 임의 사용 시나리오 확인용). */
    @Transactional
    public void usePoints(Long userId, long amount, String orderCode) {
        if (userId == null || userId <= 0) {
            throw new PointException("회원 ID를 입력해 주세요.");
        }
        if (amount <= 0) {
            throw new PointException("사용 포인트는 0보다 커야 합니다.");
        }
        if (balanceOf(userId) < amount) {
            throw new PointException("보유 포인트가 부족합니다. (잔액: " + balanceOf(userId) + "P)");
        }

        PointLedger ledger = new PointLedger();
        ledger.setUserId(userId);
        ledger.setTxnType(TXN_USE);
        ledger.setPointAmount(-amount);
        ledger.setReason("포인트 사용");
        ledger.setRefKey(orderCode);
        ledger.setCreatedDate(LocalDateTime.now());
        pointLedgerRepository.save(ledger);
        pointLedgerPublisher.publish(ledger);

        consumeLots(userId, amount);
        adjustBalance(userId, -amount);
    }

    public long reservedAmountOf(Long userId) {
        return pointReservationRepository.sumReservedByUserId(userId);
    }

    /** 가용 잔액 = 보유잔액 - 활성 예약 합계. 새 예약/사용 가능 여부 판단에 쓰인다. */
    public long availableBalanceOf(Long userId) {
        return balanceOf(userId) - reservedAmountOf(userId);
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
     * 확정 전 "일단 잡아두기" 단계). 예약 가능 여부는 가용잔액 기준으로 판단한다.
     */
    @Transactional
    public PointReservation reserve(Long userId, long amount, String refKey, String reason) {
        if (userId == null || userId <= 0) {
            throw new PointException("회원 ID를 입력해 주세요.");
        }
        if (amount <= 0) {
            throw new PointException("예약 포인트는 0보다 커야 합니다.");
        }
        if (availableBalanceOf(userId) < amount) {
            throw new PointException("예약 가능한 포인트가 부족합니다. (가용잔액: " + availableBalanceOf(userId) + "P)");
        }

        PointReservation reservation = new PointReservation();
        reservation.setUserId(userId);
        reservation.setAmount(amount);
        reservation.setRefKey(refKey);
        reservation.setReason(reason);
        reservation.setStatus(RESV_RESERVED);
        reservation.setCreatedDate(LocalDateTime.now());
        return pointReservationRepository.save(reservation);
    }

    /** 예약 확정 - 이 시점에 비로소 실제 원장(USE)행과 잔액 차감이 발생한다. */
    @Transactional
    public PointReservation confirmReservation(Long reservationId) {
        PointReservation reservation = getReservationOrThrow(reservationId);
        if (!RESV_RESERVED.equals(reservation.getStatus())) {
            throw new PointException("예약중 상태인 건만 확정할 수 있습니다.");
        }

        PointLedger ledger = new PointLedger();
        ledger.setUserId(reservation.getUserId());
        ledger.setTxnType(TXN_USE);
        ledger.setPointAmount(-reservation.getAmount());
        ledger.setReason("포인트 예약 확정" + (reservation.getReason() != null ? " (" + reservation.getReason() + ")" : ""));
        ledger.setRefKey(reservation.getRefKey() != null ? reservation.getRefKey() : "RESV-" + reservation.getReservationId());
        ledger.setCreatedDate(LocalDateTime.now());
        pointLedgerRepository.save(ledger);
        pointLedgerPublisher.publish(ledger);

        consumeLots(reservation.getUserId(), reservation.getAmount());
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
        return pointReservationRepository.save(reservation);
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
     * 소멸 처리 배치 (SFR-004 "소멸 처리"). 실시간 스케줄러가 없어 수동으로 트리거하는
     * 실행 엔드포인트를 통해 호출된다. 만료일이 지났는데 아직 안 쓰인 lot(EARN/RESTORE
     * 잔여분)을 모두 찾아 EXPIRE 원장 행으로 소멸시키고 잔액에서 차감한다.
     *
     * @return 소멸 처리된 lot 개수
     */
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

    /** donation 기부하기 화면의 "포인트 적립예상" 표시용 - 올해 기준 해당 지자체 적립률(%). */
    public BigDecimal currentPointRateOf(String locgovCode) {
        return pointRateOf(String.valueOf(java.time.LocalDate.now().getYear()), locgovCode);
    }

    private BigDecimal pointRateOf(String year, String locgovCode) {
        return locgovPointRateRepository.findByStdrYearAndLocgovCode(year, locgovCode)
                .map(com.ghlove.point.domain.LocgovPointRate::getPointRate)
                .orElseGet(this::defaultPointRate);
    }

    private BigDecimal defaultPointRate() {
        return commonCodeRepository.findById(new CommonCodeId("SYSTEM_CONFIG", "ko", "DEFAULT_POINT_RATE"))
                .map(com.ghlove.point.domain.CommonCode::getCodeValue)
                .filter(v -> v != null && !v.isBlank())
                .map(BigDecimal::new)
                .orElse(DEFAULT_RATE_FALLBACK);
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

    /** 차감액을 만료 임박한 lot부터 순서대로 소비한다 (FIFO, 지자체 구분 없이 - 기부취소
     * 회수/수동사용/예약확정처럼 "어느 답례품을 샀는지"가 없는 호출부용). */
    private void consumeLots(Long userId, long debitAmount) {
        consumeLots(userId, debitAmount, null);
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
