package com.ghlove.donation.service;

import com.ghlove.donation.domain.CommonCodeId;
import com.ghlove.donation.domain.CtbnySetup;
import com.ghlove.donation.domain.DesignatedProject;
import com.ghlove.donation.domain.Donation;
import com.ghlove.donation.domain.DonationLevy;
import com.ghlove.donation.domain.HonorBenefit;
import com.ghlove.donation.domain.HonorCntrbtr;
import com.ghlove.donation.domain.InterestLocgov;
import com.ghlove.donation.domain.Locgov;
import com.ghlove.donation.domain.PrjNotice;
import com.ghlove.donation.domain.RelayLog;
import com.ghlove.donation.domain.SpecialDisasterZone;
import com.ghlove.donation.event.DonationEventPublisher;
import com.ghlove.donation.repository.CommonCodeRepository;
import com.ghlove.donation.repository.CtbnySetupRepository;
import com.ghlove.donation.repository.DesignatedProjectRepository;
import com.ghlove.donation.repository.DonationLevyRepository;
import com.ghlove.donation.repository.DonationRepository;
import com.ghlove.donation.repository.HonorBenefitRepository;
import com.ghlove.donation.repository.HonorCntrbtrRepository;
import com.ghlove.donation.repository.InterestLocgovRepository;
import com.ghlove.donation.repository.LocgovRepository;
import com.ghlove.donation.repository.SpecialDisasterZoneRepository;
import com.ghlove.donation.service.integration.LevyResult;
import com.ghlove.donation.service.integration.LocalTaxClient;
import com.ghlove.donation.service.integration.NtsClient;
import com.ghlove.donation.service.integration.NtsReceiptResult;
import com.ghlove.donation.service.integration.PaymentConfirmResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DonationService {

    private static final String STATUS_REQUESTED = "REQUESTED";
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String STATUS_CANCELLED = "CANCELLED";
    private static final String DSGN_STATUS_OPEN = "OPEN";
    private static final String DSGN_STATUS_CLOSED = "CLOSED";
    /** AS-IS 공통코드 CNTR_PATH 실제값(09.공통코드 목록.xlsx) - 온라인/오프라인. */
    private static final String PATH_ONLINE = "100";
    private static final String PATH_OFFLINE = "200";
    // 2026년 개정 고향사랑기부금 세액공제 - AS-IS 안내페이지(donation/guide1.html) 기준.
    // AS-IS의 실제 계산 코드(mypage-mapper.xml#getTaxRedutionEstimate)는 구법(10만원까지
    // 전액, 초과분 16.5% 단일)을 그대로 쓰고 있어 안내 문구와 어긋나 있다 - 이관 대상이 아닌
    // AS-IS 자체의 미반영 버그로 판단해, 개정 법령 기준(안내 문구)을 따랐다.
    private static final BigDecimal TAX_CREDIT_TIER1_LIMIT = BigDecimal.valueOf(100_000);
    private static final BigDecimal TAX_CREDIT_TIER2_LIMIT = BigDecimal.valueOf(200_000);
    private static final BigDecimal TAX_CREDIT_TIER1_RATE = BigDecimal.ONE;
    private static final BigDecimal TAX_CREDIT_TIER2_RATE = BigDecimal.valueOf(0.44);
    private static final BigDecimal TAX_CREDIT_TIER3_RATE = BigDecimal.valueOf(0.165);
    private static final BigDecimal TAX_CREDIT_TIER3_DISASTER_RATE = BigDecimal.valueOf(0.33);
    private static final String NTS_PENDING = "PENDING";
    private static final String NTS_REGISTERED = "REGISTERED";
    private static final String NTS_FAILED = "FAILED";
    private static final String RELAY_TYPE_BUGA = "BUGA";
    private static final String RELAY_TYPE_SUNAP = "SUNAP";
    private static final String RELAY_TYPE_NTS_RECEIPT = "NTS_RECEIPT";
    private static final String RELAY_RESULT_SUCCESS = "SUCCESS";
    private static final String RELAY_RESULT_FAIL = "FAIL";
    private static final DateTimeFormatter DE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter PNTTM_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final SecureRandom RANDOM = new SecureRandom();

    private final LocgovRepository locgovRepository;
    private final DonationRepository donationRepository;
    private final CommonCodeRepository commonCodeRepository;
    private final CtbnySetupRepository ctbnySetupRepository;
    private final DesignatedProjectRepository designatedProjectRepository;
    private final DonationEventPublisher donationEventPublisher;
    private final DonationLevyRepository donationLevyRepository;
    private final com.ghlove.donation.repository.RelayLogRepository relayLogRepository;
    private final LocalTaxClient localTaxClient;
    private final NtsClient ntsClient;
    private final MemberClient memberClient;
    private final HonorBenefitRepository honorBenefitRepository;
    private final SpecialDisasterZoneRepository specialDisasterZoneRepository;
    private final HonorCntrbtrRepository honorCntrbtrRepository;
    private final InterestLocgovRepository interestLocgovRepository;
    private final com.ghlove.donation.repository.PrjNoticeRepository prjNoticeRepository;
    private final DesignatedAdminService designatedAdminService;

    public List<Locgov> activeLocgovs() {
        return locgovRepository.findByUseAtOrderByLocgovNm("Y");
    }

    /** No-hardcoding principle: code labels always come from OP_COMMON_CODE, never a Java enum/switch. */
    public Map<String, String> codesOf(String codeType) {
        return commonCodeRepository.findByCodeTypeAndLanguageAndUseYnOrderByOrdering(codeType, "ko", "Y").stream()
                .collect(Collectors.toMap(
                        com.ghlove.donation.domain.CommonCode::getId,
                        com.ghlove.donation.domain.CommonCode::getLabel,
                        (a, b) -> a, java.util.LinkedHashMap::new));
    }

    /** member 마이페이지 "기부내역 조회" 카드용 - 완료된 기부 누계 총액. */
    public BigDecimal myCompletedTotal(Long userId) {
        return sum(myDonations(userId).stream()
                .filter(d -> STATUS_COMPLETED.equals(d.getCntrSttusCode()))
                .toList());
    }

    /** member 메인화면 로그인 계정 박스 "올해 기부액"용 - 해당 연도 완료된 기부 누계 총액. */
    public BigDecimal myCompletedTotalOfYear(Long userId, int year) {
        return sum(donationRepository
                .findByUserIdAndCntrDeStartingWithAndCntrSttusCode(userId, String.valueOf(year), STATUS_COMPLETED));
    }

    public List<Donation> myDonations(Long userId) {
        return donationRepository.findByUserIdOrderByFrstRegistPnttmDesc(userId);
    }

    public Map<String, DonationLevy> leviesOf(List<Donation> donations) {
        return donationLevyRepository.findAllById(donations.stream().map(Donation::getCntrSn).toList())
                .stream().collect(Collectors.toMap(DonationLevy::getCntrSn, l -> l));
    }

    public List<DesignatedProject> openProjects() {
        return designatedProjectRepository.findByRlsYnAndDsgnDntnBizSttsCdOrderByDsgnDntnBizIdDesc("Y", DSGN_STATUS_OPEN);
    }

    public BigDecimal raisedAmount(Long dsgnDntnBizId) {
        return sum(donationRepository.findByDsgnDntnBizIdAndCntrSttusCode(dsgnDntnBizId, STATUS_COMPLETED));
    }

    /** 상세화면 "기부참여" 건수 (AS-IS prjCard.cntrCnt) - 완료된 기부 건수. */
    public int participantCountOf(Long dsgnDntnBizId) {
        return donationRepository.findByDsgnDntnBizIdAndCntrSttusCode(dsgnDntnBizId, STATUS_COMPLETED).size();
    }

    /** 상세화면 "응원메시지(기부내역)" 탭 - 완료된 기부만 보여준다(raisedAmount와 동일 기준).
     *  AS-IS는 이름을 "고**"처럼 마스킹해서 보여준다. */
    public List<CheerMessage> cheerMessagesOf(Long dsgnDntnBizId) {
        return donationRepository.findByDsgnDntnBizIdAndCntrSttusCode(dsgnDntnBizId, STATUS_COMPLETED).stream()
                .sorted(Comparator.comparing(Donation::getCntrDe).reversed())
                .map(d -> {
                    MemberInfo member = memberClient.fetchOrNull(d.getUserId());
                    return new CheerMessage(maskName(member), maskLoginId(member), formatCntrDe(d.getCntrDe()),
                            d.getCntrAmt(), d.getCheerMsg());
                })
                .toList();
    }

    private static String maskName(MemberInfo member) {
        String name = member != null ? member.userName() : null;
        if (name == null || name.isBlank()) {
            return "익명";
        }
        return name.substring(0, 1) + "**";
    }

    /** AS-IS cntrList의 loginId 마스킹("hth23***"처럼 뒤 3자리를 가림)과 동일한 방식. */
    private static String maskLoginId(MemberInfo member) {
        String loginId = member != null ? member.loginId() : null;
        if (loginId == null || loginId.isBlank()) {
            return "-";
        }
        if (loginId.length() <= 3) {
            return loginId.charAt(0) + "**";
        }
        return loginId.substring(0, loginId.length() - 3) + "***";
    }

    private static String formatCntrDe(String cntrDe) {
        if (cntrDe == null || cntrDe.length() < 8) {
            return cntrDe;
        }
        return cntrDe.substring(0, 4) + "-" + cntrDe.substring(4, 6) + "-" + cntrDe.substring(6, 8);
    }

    /** 상세화면 "공지사항" 탭. */
    public List<PrjNotice> noticesOf(Long dsgnDntnBizId) {
        return prjNoticeRepository.findByDsgnDntnBizIdOrderByPrjNoticeIdDesc(dsgnDntnBizId);
    }

    /** /designated-donation 목록 화면. AS-IS index-main.html의 상태(진행중/종료)·정렬(최근등록순/
     *  참여금액순/모금율순/종료임박순)·사업구분·키워드·지자체 필터를 재현한다 - 시딩 규모
     *  (수백 건)에서는 필터를 인메모리로 처리하는 편이 파생 쿼리를 늘리는 것보다 간단하다. */
    public List<DesignatedProject> designatedProjects(String status, String bsnsType, String sort,
                                                        String keyword, String locgovCode) {
        String sttsCd = "CLOSED".equals(status) ? DSGN_STATUS_CLOSED : DSGN_STATUS_OPEN;
        List<DesignatedProject> projects = designatedProjectRepository
                .findByRlsYnAndDsgnDntnBizSttsCdOrderByDsgnDntnBizIdDesc("Y", sttsCd);

        List<DesignatedProject> filtered = (bsnsType == null || bsnsType.isBlank())
                ? projects
                : projects.stream().filter(p -> bsnsType.equals(p.getDsgnDntnBizSeCd())).toList();
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim();
            filtered = filtered.stream()
                    .filter(p -> p.getDsgnDntnBizTtl() != null && p.getDsgnDntnBizTtl().contains(kw))
                    .toList();
        }
        if (locgovCode != null && !locgovCode.isBlank()) {
            filtered = filtered.stream().filter(p -> locgovCode.equals(p.getLclgvCd())).toList();
        }

        Comparator<DesignatedProject> comparator = switch (sort == null ? "" : sort) {
            case "AMOUNT" -> Comparator.comparing((DesignatedProject p) -> raisedAmount(p.getDsgnDntnBizId())).reversed();
            case "RATE" -> Comparator.comparing(this::fundingRatePercent).reversed();
            case "ENDING" -> Comparator.comparing(p -> p.getDsgnDntnBizEndYmd() != null ? p.getDsgnDntnBizEndYmd() : "99999999");
            default -> Comparator.comparing(DesignatedProject::getDsgnDntnBizId).reversed(); // LATEST
        };
        return filtered.stream().sorted(comparator).toList();
    }

    public BigDecimal fundingRatePercent(DesignatedProject project) {
        BigDecimal goal = project.getGoalAmt() != null ? BigDecimal.valueOf(project.getGoalAmt()) : BigDecimal.ZERO;
        if (goal.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return raisedAmount(project.getDsgnDntnBizId()).multiply(BigDecimal.valueOf(100))
                .divide(goal, 1, RoundingMode.HALF_UP);
    }

    public Optional<DesignatedProject> findProject(Long dsgnDntnBizId) {
        return designatedProjectRepository.findById(dsgnDntnBizId)
                .filter(p -> "Y".equals(p.getRlsYn()));
    }

    /** 일반기부(무지정). 신청(REQUESTED) 상태로만 생성 - 결제완료 처리는 별도 단계. */
    @Transactional
    public Donation createGeneralDonation(Long userId, String locgovCode, BigDecimal amount) {
        return createGeneralDonation(userId, locgovCode, amount, null, null);
    }

    /**
     * 기부하기 화면(AS-IS donation-main.html)에서 들어오는 일반기부 - 거주지 확인 결과
     * (psitnLocgovCode)와 답례품 제공 여부(presentType)까지 함께 받는다. 본인 주소지
     * 지자체 기부 차단은 화면에서도 막지만, 화면을 우회한 요청도 막아야 하므로 여기서
     * 다시 검증한다.
     */
    @Transactional
    public Donation createGeneralDonation(Long userId, String locgovCode, BigDecimal amount,
                                           String psitnLocgovCode, String presentType) {
        requireUser(userId);
        requireAmount(amount);
        Locgov locgov = locgovRepository.findById(locgovCode)
                .filter(l -> "Y".equals(l.getUseAt()))
                .orElseThrow(() -> new DonationException("선택한 지자체를 찾을 수 없습니다."));

        if (isSelfResidence(locgov.getLocgovCode(), psitnLocgovCode)) {
            throw new DonationException("자신의 주민등록주소지의 지자체에는 기부를 하실 수 없습니다. 다른 지자체를 선택해 주세요.");
        }

        validateAnnualLimit(userId, locgov.getLocgovCode(), amount);
        Donation donation = saveRequested(userId, locgov.getLocgovCode(), amount, null);
        donation.setPsitnLocgovCode(psitnLocgovCode);
        donation.setRtnpsntReqstCode(presentType);
        donation.setInfoAgreAt("Y");
        return donationRepository.save(donation);
    }

    /** 지정기부. 사업이 진행중(OPEN)이고 공개(RLS_YN=Y)이며 신청 기간 내인 경우만 허용. */
    @Transactional
    public Donation createDesignatedDonation(Long userId, Long dsgnDntnBizId, BigDecimal amount) {
        return createDesignatedDonation(userId, dsgnDntnBizId, amount, null);
    }

    /** cheerMsg는 상세화면 "응원메시지(기부내역)" 탭에 표시되는 선택 입력 - AS-IS의 giveOrder=1
     *  (기부와 동시에 작성) 케이스만 지원한다(완료 후 수정하는 saveCheerMsg 플로우는 제외). */
    @Transactional
    public Donation createDesignatedDonation(Long userId, Long dsgnDntnBizId, BigDecimal amount, String cheerMsg) {
        requireUser(userId);
        requireAmount(amount);
        DesignatedProject project = designatedProjectRepository.findById(dsgnDntnBizId)
                .orElseThrow(() -> new DonationException("지정기부사업을 찾을 수 없습니다."));
        if (!"Y".equals(project.getRlsYn()) || !DSGN_STATUS_OPEN.equals(project.getDsgnDntnBizSttsCd())) {
            throw new DonationException("현재 기부를 받고 있지 않은 사업입니다.");
        }
        String today = DE_FORMAT.format(LocalDate.now());
        if (project.getDsgnDntnBizBgngYmd() != null && today.compareTo(project.getDsgnDntnBizBgngYmd()) < 0
                || project.getDsgnDntnBizEndYmd() != null && today.compareTo(project.getDsgnDntnBizEndYmd()) > 0) {
            throw new DonationException("기부 신청 기간이 아닙니다.");
        }

        validateAnnualLimit(userId, project.getLclgvCd(), amount);
        Donation donation = saveRequested(userId, project.getLclgvCd(), amount, project.getDsgnDntnBizId());
        if (cheerMsg != null && !cheerMsg.isBlank()) {
            donation.setCheerMsg(cheerMsg.length() > 100 ? cheerMsg.substring(0, 100) : cheerMsg);
            donation = donationRepository.save(donation);
        }
        return donation;
    }

    /**
     * 결제완료 처리. REQUESTED만 완료 처리 가능. 세외수입 부과/수납 연계(LocalTaxClient)를
     * 거쳐야 완료로 전환되고(실패 시 완료 처리 자체가 막힘), 이어서 국세청 전자기부금영수증
     * 등록(NtsClient)은 best-effort로 시도한다 - 실패해도 기부 완료는 이미 확정된 뒤라
     * DONATION_LEVY.NTS_STATUS=FAILED로 남기고 배치 재시도 대상으로 남겨둔다(AS-IS와 동일한
     * 성격). 두 연계 모두 ghlove.integrations.*.enabled=false(기본값, 방화벽 미개방)면
     * 모크 응답으로 즉시 성공 처리된다.
     */
    @Transactional
    public Donation completeDonation(String cntrSn) {
        Donation donation = getDonationOrThrow(cntrSn);
        if (!STATUS_REQUESTED.equals(donation.getCntrSttusCode())) {
            throw new DonationException("결제 대기중인 기부만 완료 처리할 수 있습니다.");
        }
        Locgov locgov = locgovRepository.findById(donation.getCntrLocgovCode()).orElse(null);

        LevyResult levy = localTaxClient.registerLevy(donation, locgov);
        logRelay(RELAY_TYPE_BUGA, donation, RELAY_RESULT_SUCCESS);
        PaymentConfirmResult sunap = localTaxClient.confirmPayment(donation, locgov, levy.bugaNo());
        if (!"Y".equals(sunap.sunapYn())) {
            logRelay(RELAY_TYPE_SUNAP, donation, RELAY_RESULT_FAIL);
            throw new DonationException("세외수입 수납 확인에 실패했습니다.");
        }
        logRelay(RELAY_TYPE_SUNAP, donation, RELAY_RESULT_SUCCESS);

        donation.setCntrSttusCode(STATUS_COMPLETED);
        Donation saved = donationRepository.save(donation);
        upsertHonorTier(saved, locgov);
        designatedAdminService.autoCloseIfGoalReached(saved.getDsgnDntnBizId());

        DonationLevy levyEntity = donationLevyRepository.findById(cntrSn).orElseGet(DonationLevy::new);
        levyEntity.setCntrSn(cntrSn);
        levyEntity.setBugaNo(levy.bugaNo());
        levyEntity.setBugaDate(levy.bugaDate());
        levyEntity.setSunapYn(sunap.sunapYn());
        levyEntity.setSunapDate(sunap.sunapDate());
        levyEntity.setNtsStatus(NTS_PENDING);
        donationLevyRepository.save(levyEntity);

        donationEventPublisher.publishCompleted(saved);

        try {
            MemberInfo member = memberClient.fetch(donation.getUserId());
            NtsReceiptResult receipt = ntsClient.registerReceipt(donation, locgov, member);
            levyEntity.setNtsStatus(NTS_REGISTERED);
            levyEntity.setNtsReceiptNo(receipt.receiptNo());
            levyEntity.setNtsRegisteredDate(LocalDateTime.now());
            logRelay(RELAY_TYPE_NTS_RECEIPT, donation, RELAY_RESULT_SUCCESS);
        } catch (RuntimeException e) {
            levyEntity.setNtsStatus(NTS_FAILED);
            levyEntity.setNtsErrorMessage(e.getMessage());
            logRelay(RELAY_TYPE_NTS_RECEIPT, donation, RELAY_RESULT_FAIL);
            log.warn("NTS e-receipt registration failed for cntrSn={}, will need retry", cntrSn, e);
        }
        donationLevyRepository.save(levyEntity);

        return saved;
    }

    /** 연계(부과/수납/국세청 등록) 시도 1건을 이력으로 남긴다(admin "연계 로그 관리" 화면). */
    private void logRelay(String relayType, Donation donation, String resultCode) {
        RelayLog entry = new RelayLog();
        entry.setUserId(donation.getUserId());
        entry.setRelayType(relayType);
        entry.setCntrLocgovCode(donation.getCntrLocgovCode());
        entry.setCntrSn(donation.getCntrSn());
        entry.setRelayResultCode(resultCode);
        entry.setFrstRegistPnttm(LocalDateTime.now());
        relayLogRepository.save(entry);
    }

    /**
     * DONATION_LEVY 백필 - completeDonation()이 이 연계 로직을 갖추기 전에 이미 COMPLETED된
     * 기부 건들은 부과/수납/국세청 등록 이력이 아예 없다(오늘 "연계 로그 관리" 화면 검증 중
     * 실제로 발견한 갭 - point 원장 누락과 같은 성격의 문제). 기부 완료 상태 자체를 다시
     * 건드리지 않고(이미 확정됨) DONATION_LEVY 행만 멱등하게 채워 넣는다 - 이미 있는 건은
     * 건너뛰므로 몇 번을 실행해도 안전하다.
     */
    @Transactional
    public LevyBackfillResult backfillLevy() {
        List<Donation> completed = donationRepository.findAll().stream()
                .filter(d -> STATUS_COMPLETED.equals(d.getCntrSttusCode()))
                .toList();
        int filled = 0;
        int alreadyOk = 0;
        int failed = 0;
        for (Donation donation : completed) {
            if (donationLevyRepository.existsById(donation.getCntrSn())) {
                alreadyOk++;
                continue;
            }
            try {
                Locgov locgov = locgovRepository.findById(donation.getCntrLocgovCode()).orElse(null);
                LevyResult levy = localTaxClient.registerLevy(donation, locgov);
                logRelay(RELAY_TYPE_BUGA, donation, RELAY_RESULT_SUCCESS);
                PaymentConfirmResult sunap = localTaxClient.confirmPayment(donation, locgov, levy.bugaNo());
                logRelay(RELAY_TYPE_SUNAP, donation, "Y".equals(sunap.sunapYn()) ? RELAY_RESULT_SUCCESS : RELAY_RESULT_FAIL);

                DonationLevy levyEntity = new DonationLevy();
                levyEntity.setCntrSn(donation.getCntrSn());
                levyEntity.setBugaNo(levy.bugaNo());
                levyEntity.setBugaDate(levy.bugaDate());
                levyEntity.setSunapYn(sunap.sunapYn());
                levyEntity.setSunapDate(sunap.sunapDate());
                levyEntity.setNtsStatus(NTS_PENDING);
                donationLevyRepository.save(levyEntity);

                try {
                    MemberInfo member = memberClient.fetch(donation.getUserId());
                    NtsReceiptResult receipt = ntsClient.registerReceipt(donation, locgov, member);
                    levyEntity.setNtsStatus(NTS_REGISTERED);
                    levyEntity.setNtsReceiptNo(receipt.receiptNo());
                    levyEntity.setNtsRegisteredDate(LocalDateTime.now());
                    logRelay(RELAY_TYPE_NTS_RECEIPT, donation, RELAY_RESULT_SUCCESS);
                } catch (RuntimeException e) {
                    levyEntity.setNtsStatus(NTS_FAILED);
                    levyEntity.setNtsErrorMessage(e.getMessage());
                    logRelay(RELAY_TYPE_NTS_RECEIPT, donation, RELAY_RESULT_FAIL);
                }
                donationLevyRepository.save(levyEntity);
                filled++;
            } catch (RuntimeException e) {
                log.warn("Levy backfill failed for cntrSn={}", donation.getCntrSn(), e);
                failed++;
            }
        }
        log.info("Donation levy backfill complete: filled={} alreadyOk={} failed={}", filled, alreadyOk, failed);
        return new LevyBackfillResult(filled, alreadyOk, failed);
    }

    public record LevyBackfillResult(int filled, int alreadyOk, int failed) {
    }

    /**
     * 마이페이지 "기부혜택증" (SFR-003 명예기부자). 별도 발급 신청 없이 완료된 기부가
     * 쌓일 때마다 해당 연도·지자체 누적 완료 금액을 G_LOCGOV.STDR_1/2/3LEVEL_AMT 구간과
     * 비교해 가장 높은 등급을 G_HONOR_CNTRBTR에 upsert한다 - AS-IS도 별도 "발급" 버튼
     * 없이 자동 산정되는 화면이다(mypage/honorList.html). 임계값 미달이면 기존에
     * 있던 등급 행도 건드리지 않는다(강등 없음 - 한 번 달성한 등급은 유지).
     */
    private void upsertHonorTier(Donation donation, Locgov locgov) {
        if (locgov == null || locgov.getStdr1LevelAmt() == null) {
            return;
        }
        int year = Integer.parseInt(donation.getCntrDe().substring(0, 4));
        BigDecimal cumulative = sum(donationRepository.findByUserIdAndCntrDeStartingWithAndCntrSttusCode(
                donation.getUserId(), String.valueOf(year), STATUS_COMPLETED).stream()
                .filter(d -> locgov.getLocgovCode().equals(d.getCntrLocgovCode()))
                .toList());

        // AS-IS 공통코드 HONOR_STD 실제값(09.공통코드 목록.xlsx) - 100:우수, 200:최우수, 300:특급.
        String level = null;
        if (locgov.getStdr3LevelAmt() != null && cumulative.compareTo(BigDecimal.valueOf(locgov.getStdr3LevelAmt())) >= 0) {
            level = "300";
        } else if (locgov.getStdr2LevelAmt() != null && cumulative.compareTo(BigDecimal.valueOf(locgov.getStdr2LevelAmt())) >= 0) {
            level = "200";
        } else if (cumulative.compareTo(BigDecimal.valueOf(locgov.getStdr1LevelAmt())) >= 0) {
            level = "100";
        }
        if (level == null) {
            return;
        }

        HonorCntrbtr entity = honorCntrbtrRepository
                .findByStdrYearAndLocgovCodeAndUserId(year, locgov.getLocgovCode(), donation.getUserId())
                .orElseGet(HonorCntrbtr::new);
        entity.setStdrYear(year);
        entity.setLocgovCode(locgov.getLocgovCode());
        entity.setUserId(donation.getUserId());
        entity.setHonorCntrbtrLevelCode(level);
        honorCntrbtrRepository.save(entity);
    }

    /** 마이페이지 "기부혜택증" 목록 - 지자체명 표시를 위해 G_LOCGOV와 합류한다. */
    public List<HonorCertificateRow> myHonorCertificates(Long userId) {
        List<HonorCntrbtr> certs = honorCntrbtrRepository.findByUserIdOrderByStdrYearDesc(userId);
        Map<String, Locgov> locgovsByCode = locgovRepository.findAllById(
                certs.stream().map(HonorCntrbtr::getLocgovCode).distinct().toList())
                .stream().collect(Collectors.toMap(Locgov::getLocgovCode, l -> l));
        return certs.stream()
                .map(c -> {
                    Locgov l = locgovsByCode.get(c.getLocgovCode());
                    String locgovName = l != null
                            ? (l.getUpperLocgovNm() != null ? l.getUpperLocgovNm() + " " : "") + l.getLocgovNm()
                            : c.getLocgovCode();
                    return new HonorCertificateRow(c.getStdrYear(), c.getLocgovCode(), locgovName, c.getHonorCntrbtrLevelCode());
                })
                .toList();
    }

    public record HonorCertificateRow(Integer stdrYear, String locgovCode, String locgovName, String levelCode) {
    }

    /** 마이페이지 "관심지자체" 목록 - 지자체명 + "나의 기부현황"(연도 무관 완료 기부 합산)까지 합류. */
    public List<InterestLocgovRow> interestLocgovsOf(Long userId) {
        return interestLocgovRepository.findByUserId(userId).stream()
                .map(i -> {
                    String locgovName = locgovNameOf(i.getLocgovCode());
                    BigDecimal myTotal = sum(donationRepository
                            .findByUserIdAndCntrLocgovCodeAndCntrSttusCode(userId, i.getLocgovCode(), STATUS_COMPLETED));
                    return new InterestLocgovRow(i.getLocgovCode(), locgovName, myTotal);
                })
                .toList();
    }

    public record InterestLocgovRow(String locgovCode, String locgovName, BigDecimal myTotal) {
    }

    private String locgovNameOf(String locgovCode) {
        Locgov l = locgovRepository.findById(locgovCode).orElse(null);
        return l != null
                ? (l.getUpperLocgovNm() != null ? l.getUpperLocgovNm() + " " : "") + l.getLocgovNm()
                : locgovCode;
    }

    /** @return 추가된(또는 이미 있던) 지자체의 표시용 이름 - member 프로필 화면이 AJAX로
     *  칩을 즉시 그리는 데 쓴다. */
    @Transactional
    public String addInterestLocgov(Long userId, String locgovCode) {
        if (interestLocgovRepository.existsByLocgovCodeAndUserId(locgovCode, userId)) {
            return locgovNameOf(locgovCode);
        }
        locgovRepository.findById(locgovCode)
                .orElseThrow(() -> new DonationException("선택한 지자체를 찾을 수 없습니다."));
        InterestLocgov entity = new InterestLocgov();
        entity.setUserId(userId);
        entity.setLocgovCode(locgovCode);
        entity.setRegistDe(DE_FORMAT.format(LocalDate.now()));
        interestLocgovRepository.save(entity);
        return locgovNameOf(locgovCode);
    }

    @Transactional
    public void removeInterestLocgov(Long userId, String locgovCode) {
        interestLocgovRepository.deleteByLocgovCodeAndUserId(locgovCode, userId);
    }

    /** AS-IS mypage/intrstLocGov.html의 "선택삭제" - 체크박스로 고른 여러 지자체를 한 번에 삭제. */
    @Transactional
    public void removeInterestLocgovs(Long userId, List<String> locgovCodes) {
        interestLocgovRepository.deleteByLocgovCodeInAndUserId(locgovCodes, userId);
    }

    /**
     * 기부 취소. REQUESTED(신청 취소)와 COMPLETED(환불) 모두 취소 가능. COMPLETED 상태였던
     * 건만 취소 이벤트를 발행한다 - REQUESTED는 애초에 완료 이벤트가 발행된 적이 없어
     * 포인트 서비스가 보상할 대상이 없기 때문.
     */
    @Transactional
    public Donation cancelDonation(String cntrSn) {
        Donation donation = getDonationOrThrow(cntrSn);
        String previousStatus = donation.getCntrSttusCode();
        if (STATUS_CANCELLED.equals(previousStatus)) {
            throw new DonationException("이미 취소된 기부입니다.");
        }
        donation.setCntrSttusCode(STATUS_CANCELLED);
        Donation saved = donationRepository.save(donation);
        if (STATUS_COMPLETED.equals(previousStatus)) {
            donationEventPublisher.publishCancelled(saved);
        }
        return saved;
    }

    private Donation saveRequested(Long userId, String locgovCode, BigDecimal amount, Long dsgnDntnBizId) {
        return saveRequested(userId, locgovCode, amount, dsgnDntnBizId, PATH_ONLINE, null, null);
    }

    private Donation saveRequested(Long userId, String locgovCode, BigDecimal amount, Long dsgnDntnBizId,
                                    String pathCode, String rceptBankCode, String rceptBankNm) {
        LocalDateTime now = LocalDateTime.now();
        Donation donation = new Donation();
        donation.setCntrSn(generateCntrSn());
        donation.setCntrDe(DE_FORMAT.format(now));
        donation.setUserId(userId);
        donation.setCntrLocgovCode(locgovCode);
        donation.setCntrAmt(amount);
        donation.setCntrSttusCode(STATUS_REQUESTED);
        donation.setCntrPathCode(pathCode);
        donation.setFrstRegistPnttm(PNTTM_FORMAT.format(now));
        donation.setDsgnDntnBizId(dsgnDntnBizId);
        donation.setRceptBankCode(rceptBankCode);
        donation.setRceptBankNm(rceptBankNm);
        return donationRepository.save(donation);
    }

    /**
     * 기탁서(오프라인) 기부 등록 (SFR-003) - 우편/방문 등으로 접수된 기부를 운영자가
     * 대신 등록한다. 온라인 기부와 동일하게 연간한도 검증을 거치고 REQUESTED로
     * 생성되며, completeDonation() 호출 시 세외수입/국세청 연계도 동일하게 탄다 -
     * 접수 경로만 CNTR_PATH_CODE=OFFLINE으로 구분된다.
     */
    @Transactional
    public Donation registerOfflineDonation(Long userId, String locgovCode, BigDecimal amount,
                                             String rceptBankCode, String rceptBankNm) {
        requireUser(userId);
        requireAmount(amount);
        Locgov locgov = locgovRepository.findById(locgovCode)
                .filter(l -> "Y".equals(l.getUseAt()))
                .orElseThrow(() -> new DonationException("선택한 지자체를 찾을 수 없습니다."));

        validateAnnualLimit(userId, locgov.getLocgovCode(), amount);
        return saveRequested(userId, locgov.getLocgovCode(), amount, null, PATH_OFFLINE, rceptBankCode, rceptBankNm);
    }

    /**
     * 기부혜택(세액공제) 계산 (SFR-003). 해당 연도 COMPLETED 기부를 접수일(CNTR_DE) 순으로
     * 누적하면서, 각 건이 연간 누계 구간(0~10만/10~20만/20만 초과) 중 걸치는 부분마다 해당
     * 구간의 세율을 곱해 합산한다. 20만원 초과분의 세율은 그 기부 건이 특별재난지역
     * 공고 기간(OP_SPEL_DSTR_ZN) 안이었는지에 따라 33%/16.5%로 갈린다.
     */
    public BigDecimal taxCreditOf(Long userId, int year) {
        List<Donation> donations = donationRepository
                .findByUserIdAndCntrDeStartingWithAndCntrSttusCode(userId, String.valueOf(year), STATUS_COMPLETED)
                .stream()
                .sorted(Comparator.comparing(Donation::getCntrDe))
                .toList();

        Map<String, List<SpecialDisasterZone>> spelByLocgov = specialDisasterZoneRepository.findAll().stream()
                .filter(z -> z.getLocgovCode() != null)
                .collect(Collectors.groupingBy(SpecialDisasterZone::getLocgovCode));

        BigDecimal cumulative = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;
        for (Donation d : donations) {
            BigDecimal before = cumulative;
            BigDecimal after = cumulative.add(d.getCntrAmt());
            boolean disaster = isSpecialDisaster(spelByLocgov.get(d.getCntrLocgovCode()), d.getCntrDe());
            totalCredit = totalCredit.add(taxCreditForRange(before, after, disaster));
            cumulative = after;
        }
        return totalCredit.setScale(0, RoundingMode.FLOOR);
    }

    private BigDecimal taxCreditForRange(BigDecimal before, BigDecimal after, boolean disaster) {
        BigDecimal tier3Rate = disaster ? TAX_CREDIT_TIER3_DISASTER_RATE : TAX_CREDIT_TIER3_RATE;
        BigDecimal credit = overlapAmount(before, after, BigDecimal.ZERO, TAX_CREDIT_TIER1_LIMIT).multiply(TAX_CREDIT_TIER1_RATE);
        credit = credit.add(overlapAmount(before, after, TAX_CREDIT_TIER1_LIMIT, TAX_CREDIT_TIER2_LIMIT).multiply(TAX_CREDIT_TIER2_RATE));
        credit = credit.add(overlapAmount(before, after, TAX_CREDIT_TIER2_LIMIT, null).multiply(tier3Rate));
        return credit;
    }

    /** [before, after) 구간이 [low, high) 구간과 겹치는 금액. high == null 이면 상한 없음. */
    private BigDecimal overlapAmount(BigDecimal before, BigDecimal after, BigDecimal low, BigDecimal high) {
        BigDecimal from = before.max(low);
        BigDecimal to = high != null ? after.min(high) : after;
        return to.subtract(from).max(BigDecimal.ZERO);
    }

    /** ReceiptService#spelDstrYn과 동일한 AS-IS 판정 로직(문자열 날짜 구간 포함 비교). */
    private boolean isSpecialDisaster(List<SpecialDisasterZone> zones, String cntrDe) {
        if (zones == null || cntrDe == null) {
            return false;
        }
        for (SpecialDisasterZone z : zones) {
            if (z.getNotiDate() == null || z.getEndDate() == null) {
                continue;
            }
            if (cntrDe.compareTo(z.getNotiDate()) >= 0 && cntrDe.compareTo(z.getEndDate()) <= 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * 메인화면 "총 기부금(전일 기준)" 위젯 (SFR-002/AS-IS total_give_state.vue 재현) -
     * 사이트 전체(전 회원) 합계다. nowYearTotalAmt/prevYearTotalAmt는 AS-IS와 동일하게
     * "올해 1/1~오늘"과 "작년 1/1~작년의 같은 날짜"를 비교하는 동기간 누계이고(전년 동기
     * 대비 % 는 이 두 값을 나눠서 화면에서 계산), dDay는 연말(12/31)까지 남은 일수,
     * nowDayPercent는 올해가 며칠째 지났는지의 비율(진행 바 채움 정도)이다. AS-IS 프론트
     * 소스에는 이 값들을 실제로 어떻게 계산하는지(백엔드) 코드가 없어 이 두 필드의 의미는
     * 합리적으로 추정한 것이다.
     */
    public GiveState giveState() {
        LocalDate today = LocalDate.now();
        LocalDate yearStart = today.withDayOfYear(1);
        LocalDate lastYearSameDay = today.minusYears(1);
        LocalDate lastYearStart = yearStart.minusYears(1);
        LocalDate yearEnd = LocalDate.of(today.getYear(), 12, 31);

        BigDecimal nowYearTotal = sum(donationRepository.findByCntrDeBetweenAndCntrSttusCode(
                DE_FORMAT.format(yearStart), DE_FORMAT.format(today), STATUS_COMPLETED));
        BigDecimal prevYearTotal = sum(donationRepository.findByCntrDeBetweenAndCntrSttusCode(
                DE_FORMAT.format(lastYearStart), DE_FORMAT.format(lastYearSameDay), STATUS_COMPLETED));

        long dDay = ChronoUnit.DAYS.between(today, yearEnd);
        int nowDayPercent = (int) Math.floor(today.getDayOfYear() * 100.0 / today.lengthOfYear());

        return new GiveState(nowYearTotal, prevYearTotal, dDay, nowDayPercent);
    }

    public String honorBenefitOf(String locgovCode) {
        return honorBenefitRepository.findById(locgovCode).map(HonorBenefit::getBenefitDesc).orElse(null);
    }

    /** 지자체별 기부혜택 안내문구 관리 (SFR-003 "지자체별 기부혜택 관리"). */
    @Transactional
    public void updateHonorBenefit(String locgovCode, String benefitDesc) {
        if (!locgovRepository.existsById(locgovCode)) {
            throw new DonationException("존재하지 않는 지자체입니다.");
        }
        HonorBenefit benefit = honorBenefitRepository.findById(locgovCode).orElseGet(HonorBenefit::new);
        benefit.setLocgovCode(locgovCode);
        benefit.setBenefitDesc(benefitDesc);
        benefit.setUpdatedDate(LocalDateTime.now());
        honorBenefitRepository.save(benefit);
    }

    /** 연간 기부한도 검증: 전체 지자체 합산 한도(SYSTEM_CONFIG) + 지자체별 한도(G_CTBNY_SETUP). */
    private void validateAnnualLimit(Long userId, String locgovCode, BigDecimal amount) {
        String year = String.valueOf(LocalDate.now().getYear());

        totalAnnualLimit().ifPresent(totalLimit -> {
            BigDecimal existing = sum(donationRepository
                    .findByUserIdAndCntrDeStartingWithAndCntrSttusCode(userId, year, STATUS_COMPLETED));
            if (existing.add(amount).compareTo(totalLimit) > 0) {
                throw new DonationException("연간 총 기부한도(" + formatWon(totalLimit)
                        + ")를 초과합니다. 올해 누적 기부액: " + formatWon(existing));
            }
        });

        ctbnySetupRepository.findByStdrYearAndLocgovCode(year, locgovCode)
                .map(CtbnySetup::getLmtAmt)
                .ifPresent(lmtAmt -> {
                    BigDecimal locgovLimit = BigDecimal.valueOf(lmtAmt);
                    BigDecimal existing = sum(donationRepository
                            .findByUserIdAndCntrLocgovCodeAndCntrDeStartingWithAndCntrSttusCode(
                                    userId, locgovCode, year, STATUS_COMPLETED));
                    if (existing.add(amount).compareTo(locgovLimit) > 0) {
                        throw new DonationException("해당 지자체의 연간 기부한도(" + formatWon(locgovLimit)
                                + ")를 초과합니다. 올해 누적 기부액: " + formatWon(existing));
                    }
                });
    }

    private Optional<BigDecimal> totalAnnualLimit() {
        return commonCodeRepository.findById(new CommonCodeId("SYSTEM_CONFIG", "ko", "ANNUAL_TOTAL_LIMIT"))
                .map(com.ghlove.donation.domain.CommonCode::getCodeValue)
                .filter(v -> v != null && !v.isBlank())
                .map(BigDecimal::new);
    }

    /** 기부하기 화면의 "개인 최대 기부 한도 금액 : 연 N원" 표기용. */
    public Optional<BigDecimal> annualLimit() {
        return totalAnnualLimit();
    }

    /** 기부하기 화면의 "기부가능 한도" 입력칸 - 연간 한도에서 올해 이미 완료된 기부액을 뺀 잔여. */
    public BigDecimal remainingAnnualLimit(Long userId) {
        Optional<BigDecimal> limit = totalAnnualLimit();
        if (limit.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal used = sum(donationRepository.findByUserIdAndCntrDeStartingWithAndCntrSttusCode(
                userId, String.valueOf(LocalDate.now().getYear()), STATUS_COMPLETED));
        return limit.get().subtract(used).max(BigDecimal.ZERO);
    }

    /**
     * 기부하기 "거주지 확인" (AS-IS donation-main.html rsgstadresinfo). AS-IS는 주민등록번호
     * 뒷자리를 행정정보공동이용센터에 보내 실주소를 받아오지만 그 연계는 이 MSA에 없어(다른
     * 외부연계와 동일한 의도적 축소 - 실명·주민번호를 검증 없이 신뢰하지 않기 위해서라도
     * 임의 입력을 주소로 받아들이면 안 된다), 회원이 마이페이지에 등록해 둔 주소를 거주지로
     * 본다. 반환값은 그 주소가 속한 지자체(G_LOCGOV) - 매칭 실패 시 empty.
     */
    public Optional<Locgov> residenceLocgovOf(String address) {
        if (address == null || address.isBlank()) {
            return Optional.empty();
        }
        String normalized = address.replaceAll("\\s+", "");
        // "서울특별시 강남구 ..." 처럼 시도명+시군구명이 주소 앞부분에 그대로 들어있는지로 찾는다.
        // 더 긴(구체적인) 이름이 먼저 걸리도록 시군구명 길이 내림차순으로 본다
        // (예: "성남시 분당구"가 "성남시"보다 먼저 매칭돼야 함).
        return activeLocgovs().stream()
                .filter(l -> l.getLocgovNm() != null && !l.getLocgovNm().isBlank())
                .filter(l -> {
                    String upper = l.getUpperLocgovNm() != null ? l.getUpperLocgovNm().replaceAll("\\s+", "") : "";
                    String name = l.getLocgovNm().replaceAll("\\s+", "");
                    return normalized.startsWith(upper + name) || normalized.contains(name);
                })
                .max(Comparator.comparingInt(l -> l.getLocgovNm().replaceAll("\\s+", "").length()));
    }

    /**
     * 고향사랑기부금법상 본인의 주민등록 주소지 지자체에는 기부할 수 없다 (AS-IS는
     * "자신의 주민등록주소지의 지자체에는 기부를 하실 수 없습니다"로 안내). 광역시/도에
     * 직접 기부하는 경우(시군구 코드가 아닌 "XX000" 형태)도 자신의 시도와 같으면 막는다 -
     * AS-IS도 동일하게 앞 2자리 + "000"으로 비교한다.
     */
    public boolean isSelfResidence(String donationLocgovCode, String residenceLocgovCode) {
        if (donationLocgovCode == null || residenceLocgovCode == null) {
            return false;
        }
        if (donationLocgovCode.equals(residenceLocgovCode)) {
            return true;
        }
        return residenceLocgovCode.length() >= 2
                && donationLocgovCode.equals(residenceLocgovCode.substring(0, 2) + "000");
    }

    /** 컨트롤러의 소유자 확인용 (SFR-010 - 그 cntrSn이 실제 로그인한 회원 것인지 검증). */
    public java.util.Optional<Donation> find(String cntrSn) {
        return donationRepository.findById(cntrSn);
    }

    private Donation getDonationOrThrow(String cntrSn) {
        return donationRepository.findById(cntrSn)
                .orElseThrow(() -> new DonationException("기부 내역을 찾을 수 없습니다."));
    }

    private void requireUser(Long userId) {
        if (userId == null || userId <= 0) {
            throw new DonationException("회원 ID를 입력해 주세요.");
        }
    }

    private void requireAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DonationException("기부금액은 0보다 커야 합니다.");
        }
    }

    private BigDecimal sum(List<Donation> donations) {
        return donations.stream().map(Donation::getCntrAmt).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String formatWon(BigDecimal amount) {
        return String.format("%,d원", amount.longValueExact());
    }

    private String generateCntrSn() {
        int suffix = RANDOM.nextInt(9000) + 1000;
        return "D" + PNTTM_FORMAT.format(LocalDateTime.now()) + suffix;
    }
}
