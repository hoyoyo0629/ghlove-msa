package com.ghlove.point.web;

import com.ghlove.point.domain.PointLedger;
import com.ghlove.point.event.DonationCompletedEvent;
import com.ghlove.point.repository.PointLedgerRepository;
import com.ghlove.point.service.LocgovClient;
import com.ghlove.point.service.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Read-only JSON API for other services (member 마이페이지 "기부포인트 조회", "회원탈퇴") - not part of any write path. */
@RestController
@RequiredArgsConstructor
public class PointApiController {

    private static final String TXN_EARN = "EARN";
    private static final String TXN_USE = "USE";

    private final PointService pointService;
    private final PointLedgerRepository pointLedgerRepository;
    private final LocgovClient locgovClient;

    @GetMapping("/api/balance")
    public PointSummaryDto balance(@RequestParam Long userId) {
        return new PointSummaryDto(pointService.balanceOf(userId));
    }

    /** order의 장바구니 화면 "지자체별 잔여 포인트" 표시/주문가능여부 판정용. */
    @GetMapping("/api/balance-by-locgov")
    public PointSummaryDto balanceByLocgov(@RequestParam Long userId, @RequestParam String locgovCode) {
        return new PointSummaryDto(pointService.balanceByLocgov(userId, locgovCode));
    }

    /** donation 기부하기 화면의 "포인트 적립예상"용 - 지자체별 적립률은 지자체 재량이라
     *  고정값이 아니고, 미설정 지자체는 SYSTEM_CONFIG의 기본률로 떨어진다. */
    @GetMapping("/api/point-rate")
    public Map<String, java.math.BigDecimal> pointRate(@RequestParam String locgovCode) {
        return Map.of("rate", pointService.currentPointRateOf(locgovCode));
    }

    /** donation 마이페이지 "기부내역 조회"의 "기부포인트" 컬럼용 - CNTR_SN당 적립된 포인트. */
    @GetMapping("/api/ledger/earned")
    public Map<String, Long> earnedByCntrSn(@RequestParam List<String> cntrSns) {
        return pointLedgerRepository.findByRefKeyInAndTxnType(cntrSns, TXN_EARN).stream()
                .collect(Collectors.toMap(PointLedger::getRefKey, PointLedger::getPointAmount, (a, b) -> a));
    }

    /** admin "기부포인트 상세현황"(AS-IS give-point/form.jsp)의 건별 발생/사용/잔액 컬럼용.
     *  REMAINING_AMOUNT는 이 CNTR_SN으로 적립된 lot이 FIFO로 얼마나 소진됐는지를 그대로
     *  담고 있어 - 사용포인트는 다른 지자체 기부와 섞이지 않는 이 lot 단위 계산이라 정확하다. */
    @GetMapping("/api/ledger/earned-lots")
    public List<EarnLotDto> earnedLots(@RequestParam List<String> cntrSns) {
        return pointLedgerRepository.findByRefKeyInAndTxnType(cntrSns, TXN_EARN).stream()
                .map(l -> new EarnLotDto(l.getRefKey(), l.getPointAmount(), l.getRemainingAmount()))
                .toList();
    }

    public record EarnLotDto(String cntrSn, Long earnedAmount, Long remainingAmount) {
    }

    /** admin "주문금액-포인트사용 대사"(AS-IS view_rc_order_amt_vs_point_use_check 재구현)용 -
     *  주문ID당 실제 차감된 포인트 절대값. PointService.deductForOrder()는 POINT_AMOUNT를
     *  음수로 저장하므로(-pointAmount) 절대값으로 뒤집어 order.pointAmount와 직접 비교 가능하게 한다. */
    @GetMapping("/api/ledger/used")
    public Map<String, Long> usedByOrderId(@RequestParam List<String> orderIds) {
        return pointLedgerRepository.findByRefKeyInAndTxnType(orderIds, TXN_USE).stream()
                .collect(Collectors.groupingBy(PointLedger::getRefKey,
                        Collectors.summingLong(l -> -l.getPointAmount())));
    }

    /** admin StatsService ReadModel 재동기화용 (SFR-009 "원장 복구·재동기화 절차") - 원장
     *  전체 스냅샷. */
    @GetMapping("/api/admin/ledger/all")
    public List<LedgerSnapshotDto> allForResync() {
        return pointLedgerRepository.findAll().stream()
                .map(l -> new LedgerSnapshotDto(l.getLedgerId(), l.getUserId(), l.getLocgovCode(),
                        l.getTxnType(), l.getPointAmount(), l.getCreatedDate()))
                .toList();
    }

    public record LedgerSnapshotDto(Long ledgerId, Long userId, String locgovCode, String txnType,
                                     Long pointAmount, LocalDateTime createdDate) {
    }

    /** admin "기부금 변경신청 관리"(AS-IS give-reqmng, 요청분류=포인트생성) 승인 처리용.
     *  creditForDonation()이 REF_KEY+EARN 조합으로 이미 자체적으로 멱등 처리하므로, 정상
     *  적립경로(Kafka)로 이미 포인트를 받은 기부건에 대해 호출해도 안전하게 no-op된다 -
     *  이 엔드포인트는 그 사전 체크 결과를 그대로 admin에 알려준다. */
    @PostMapping("/api/admin/credit-for-donation")
    public Map<String, Object> creditForDonationAdmin(@RequestBody CreditRequest req) {
        boolean alreadyCredited = pointLedgerRepository.existsByRefKeyAndTxnType(req.cntrSn(), TXN_EARN);
        if (alreadyCredited) {
            return Map.of("credited", false, "message", "이미 포인트가 적립되어 있어 재생성이 필요하지 않습니다.");
        }
        pointService.creditForDonation(new DonationCompletedEvent(req.cntrSn(), req.userId(), req.locgovCode(),
                req.cntrAmt(), req.cntrDe(), LocalDateTime.now()));
        return Map.of("credited", true);
    }

    public record CreditRequest(String cntrSn, Long userId, String locgovCode, BigDecimal cntrAmt, String cntrDe) {
    }

    /** member 마이페이지 "회원탈퇴" 화면의 "잔여포인트" 표(지자체별) - PointController.myPoints()와
     *  동일한 지자체명 보강 로직. */
    @GetMapping("/api/locgov-point-summary")
    public List<LocgovPointSummaryDto> locgovPointSummary(@RequestParam Long userId) {
        Map<String, LocgovClient.LocgovInfo> locgovsByCode = new LinkedHashMap<>();
        locgovClient.allLocgovs().forEach(l -> locgovsByCode.put(l.locgovCode(), l));
        return pointService.ledgerSummaryByLocgov(userId).stream()
                .filter(s -> s.remaining() > 0)
                .map(s -> {
                    LocgovClient.LocgovInfo l = locgovsByCode.get(s.locgovCode());
                    String upperNm = l != null ? l.upperLocgovNm() : null;
                    String nm = l != null ? l.locgovNm() : s.locgovCode();
                    return new LocgovPointSummaryDto(s.locgovCode(), upperNm, nm, s.remaining());
                })
                .toList();
    }

    public record LocgovPointSummaryDto(String locgovCode, String upperLocgovNm, String locgovNm, long remaining) {
    }

    /** admin 지자체관리 화면 "포인트 지급률 변경이력" 팝업용 - 연도별 행 자체가 이력. */
    @GetMapping("/api/admin/locgov-point-rate")
    public List<LocgovPointRateDto> locgovPointRateHistory(@RequestParam String locgovCode) {
        return pointService.locgovPointRateHistory(locgovCode).stream()
                .map(r -> new LocgovPointRateDto(r.getStdrYear(), r.getLocgovCode(), r.getPointRate(),
                        r.getLastUpdtPnttm(), r.getLastUpdusrNm()))
                .toList();
    }

    /** admin 지자체관리 화면 "포인트 지급률 등록/수정"(AS-IS user/locgov/edit.jsp) - 연도+지자체
     *  단위 upsert. */
    @PostMapping("/api/admin/locgov-point-rate")
    public LocgovPointRateDto upsertLocgovPointRate(@RequestBody LocgovPointRateUpsertRequest req) {
        var saved = pointService.upsertLocgovPointRate(req.stdrYear(), req.locgovCode(), req.pointRate(), req.managerName());
        return new LocgovPointRateDto(saved.getStdrYear(), saved.getLocgovCode(), saved.getPointRate(),
                saved.getLastUpdtPnttm(), saved.getLastUpdusrNm());
    }

    public record LocgovPointRateDto(String stdrYear, String locgovCode, BigDecimal pointRate,
                                      LocalDateTime lastUpdtPnttm, String lastUpdusrNm) {
    }

    public record LocgovPointRateUpsertRequest(String stdrYear, String locgovCode, BigDecimal pointRate,
                                                 String managerName) {
    }
}
