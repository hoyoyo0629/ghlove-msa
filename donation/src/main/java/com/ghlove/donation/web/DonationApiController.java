package com.ghlove.donation.web;

import com.ghlove.donation.domain.DesignatedProject;
import com.ghlove.donation.domain.DonationLevy;
import com.ghlove.donation.domain.Locgov;
import com.ghlove.donation.repository.DonationLevyRepository;
import com.ghlove.donation.repository.DonationRepository;
import com.ghlove.donation.repository.LocgovRepository;
import com.ghlove.donation.repository.RelayLogRepository;
import com.ghlove.donation.service.DonationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/** Read-only JSON API for other services (member's 메인 화면 "특정사업에 기부하기") - not part of any write path. */
@RestController
@RequiredArgsConstructor
public class DonationApiController {

    private static final Set<String> STATS_STATUSES = Set.of("COMPLETED", "CANCELLED");

    private final DonationService donationService;
    private final LocgovRepository locgovRepository;
    private final DonationRepository donationRepository;
    private final DonationLevyRepository donationLevyRepository;
    private final RelayLogRepository relayLogRepository;

    @GetMapping("/api/designated-projects")
    public List<DesignatedProjectDto> openProjects() {
        return donationService.openProjects().stream()
                .map(this::toDto)
                .toList();
    }

    /** 메인화면 "총 기부금" 위젯용 (AS-IS GET /api/common/getGiveState 재현). */
    @GetMapping("/api/give-state")
    public com.ghlove.donation.service.GiveState giveState() {
        return donationService.giveState();
    }

    /** admin StatsService ReadModel 재동기화용 - Kafka 컨슈머 오프셋/보존기간과 무관하게
     *  현재 실제 상태(COMPLETED/CANCELLED)를 그대로 스냅샷으로 넘긴다(SFR-009 "원장 복구·
     *  재동기화 절차"). */
    @GetMapping("/api/admin/donations/all")
    public List<DonationSnapshotDto> allForResync() {
        return donationRepository.findAll().stream()
                .filter(d -> STATS_STATUSES.contains(d.getCntrSttusCode()))
                .map(d -> new DonationSnapshotDto(d.getCntrSn(), d.getUserId(), d.getCntrLocgovCode(),
                        d.getCntrAmt(), d.getCntrDe(), d.getCntrSttusCode(), d.getCntrPathCode(), d.getPsitnLocgovCode()))
                .toList();
    }

    public record DonationSnapshotDto(String cntrSn, Long userId, String cntrLocgovCode, BigDecimal cntrAmt,
                                       String cntrDe, String cntrSttusCode, String cntrPathCode, String psitnLocgovCode) {
    }

    /** admin "연계 로그 관리"(AS-IS opmanager/log gif-stnd-buga/sunap, gif-seoul-buga/sunap 재구현)용 -
     *  이 프로젝트는 세외수입 부과/수납 연계가 표준/서울시로 나뉘지 않고 하나의 흐름(LocalTaxClient)
     *  이라 DONATION_LEVY 전체를 그대로 노출한다. */
    @GetMapping("/api/admin/donation-levy")
    public List<DonationLevyDto> levyLogs() {
        return donationLevyRepository.findAll().stream()
                .sorted(Comparator.comparing(DonationLevy::getBugaDate, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(l -> new DonationLevyDto(l.getCntrSn(), l.getBugaNo(), l.getBugaDate(), l.getSunapYn(),
                        l.getSunapDate(), l.getNtsStatus(), l.getNtsReceiptNo(), l.getNtsRegisteredDate(), l.getNtsErrorMessage()))
                .toList();
    }

    public record DonationLevyDto(String cntrSn, String bugaNo, LocalDateTime bugaDate, String sunapYn,
                                   LocalDateTime sunapDate, String ntsStatus, String ntsReceiptNo,
                                   LocalDateTime ntsRegisteredDate, String ntsErrorMessage) {
    }

    /** admin "연계 로그 관리" 화면의 "이력 백필" 버튼용 - completeDonation()에 부과/수납
     *  연계 로직이 추가되기 전에 이미 COMPLETED된 기부 건의 DONATION_LEVY 누락을 채운다. */
    @org.springframework.web.bind.annotation.PostMapping("/api/admin/donation-levy/backfill")
    public DonationService.LevyBackfillResult backfillLevy() {
        return donationService.backfillLevy();
    }

    /** admin "연계 로그 관리" 화면의 시도 이력(성공/실패 모두) 조회용 - DONATION_LEVY(현재
     *  상태)와 달리 G_RELAY_LOG는 호출마다 append되는 감사 이력이다. */
    @GetMapping("/api/admin/relay-log")
    public List<RelayLogDto> relayLog() {
        return relayLogRepository.findAllByOrderByFrstRegistPnttmDesc().stream()
                .map(r -> new RelayLogDto(r.getRelayLogId(), r.getUserId(), r.getRelayType(),
                        r.getCntrLocgovCode(), r.getCntrSn(), r.getRelayResultCode(), r.getFrstRegistPnttm()))
                .toList();
    }

    public record RelayLogDto(Integer relayLogId, Long userId, String relayType, String cntrLocgovCode,
                               String cntrSn, String relayResultCode, LocalDateTime frstRegistPnttm) {
    }

    /** member 마이페이지 "기부내역 조회" 카드 + 메인화면 로그인 계정 박스용. */
    @GetMapping("/api/my-summary")
    public MySummaryDto mySummary(@RequestParam Long userId) {
        int thisYear = java.time.Year.now().getValue();
        return new MySummaryDto(donationService.myCompletedTotal(userId),
                donationService.myCompletedTotalOfYear(userId, thisYear));
    }

    /** order의 장바구니 화면이 지자체명을 표시하기 위한 조회 (gift/point는 코드만 갖고 이름은 없음). */
    @GetMapping("/api/locgovs/{locgovCode}")
    public LocgovNameDto locgovName(@org.springframework.web.bind.annotation.PathVariable String locgovCode) {
        Locgov locgov = locgovRepository.findById(locgovCode).orElse(null);
        String displayName = locgov != null
                ? (locgov.getUpperLocgovNm() != null ? locgov.getUpperLocgovNm() + " " : "") + locgov.getLocgovNm()
                : locgovCode;
        return new LocgovNameDto(locgovCode, displayName);
    }

    /** order의 장바구니 GNB "지자체몰 선택하기" 지도 팝업용 - 시/도별 시/군/구 전체 목록. */
    @GetMapping("/api/locgovs")
    public List<LocgovDto> allLocgovs() {
        return locgovRepository.findByUseAtOrderByLocgovNm("Y").stream()
                .map(l -> new LocgovDto(l.getLocgovCode(), l.getLocgovNm(), l.getUpperLocgovCode(), l.getUpperLocgovNm()))
                .toList();
    }

    /** member "회원 정보 수정" 화면이 현재 등록된 관심지자체 칩 목록을 보여주기 위한 조회. */
    @GetMapping("/api/interest-locgovs")
    public List<InterestLocgovDto> interestLocgovs(@RequestParam Long userId) {
        return donationService.interestLocgovsOf(userId).stream()
                .map(r -> new InterestLocgovDto(r.locgovCode(), r.locgovName()))
                .toList();
    }

    private DesignatedProjectDto toDto(DesignatedProject p) {
        BigDecimal raised = donationService.raisedAmount(p.getDsgnDntnBizId());
        BigDecimal goal = p.getGoalAmt() != null ? BigDecimal.valueOf(p.getGoalAmt()) : BigDecimal.ZERO;
        BigDecimal percent = goal.compareTo(BigDecimal.ZERO) > 0
                ? raised.multiply(BigDecimal.valueOf(100)).divide(goal, 1, RoundingMode.HALF_UP).min(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;
        Locgov locgov = locgovRepository.findById(p.getLclgvCd()).orElse(null);
        String locgovName = locgov != null
                ? (locgov.getUpperLocgovNm() != null ? locgov.getUpperLocgovNm() + " " : "") + locgov.getLocgovNm()
                : p.getLclgvCd();

        return new DesignatedProjectDto(p.getDsgnDntnBizId(), p.getDsgnDntnBizTtl(), p.getDsgnDntnBizCn(),
                p.getLclgvCd(), locgovName, p.getGoalAmt(), raised, percent,
                p.getDsgnDntnBizBgngYmd(), p.getDsgnDntnBizEndYmd(), p.getImageUrl());
    }
}
