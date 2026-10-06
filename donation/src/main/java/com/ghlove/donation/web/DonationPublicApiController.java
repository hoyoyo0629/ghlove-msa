package com.ghlove.donation.web;

import com.ghlove.donation.domain.DesignatedProject;
import com.ghlove.donation.domain.Donation;
import com.ghlove.donation.domain.Locgov;
import com.ghlove.donation.service.DonationException;
import com.ghlove.donation.service.DonationService;
import com.ghlove.donation.service.JwtVerifier;
import com.ghlove.donation.service.MemberClient;
import com.ghlove.donation.service.MemberInfo;
import com.ghlove.donation.service.PointClient;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** storefront(Vue3 SPA)용 "자치단체에 기부하기"(일반기부) JSON API - {@link DonationController}
 *  (Thymeleaf donate.html)와 완전히 같은 {@link DonationService} 로직을 재사용한다.
 *  기존 `/donate/point-rate`는 이미 JSON이지만 @RequestParam(쿼리스트링) 기반이라 그대로
 *  재사용하고, 여기서는 JSON 바디로 받아야 하는 verify-residence/생성만 새로 감싼다. */
@RestController
@RequiredArgsConstructor
public class DonationPublicApiController {

    private final DonationService donationService;
    private final PointClient pointClient;
    private final MemberClient memberClient;
    private final JwtVerifier jwtVerifier;
    private final com.ghlove.donation.service.LocgovAdminService locgovAdminService;

    /** AS-IS getLocGovLmtt 응답의 표시용 3개 필드. 날짜는 AS-IS SQL이 DATE_FORMAT(...,'%Y.%m.%d')로
     *  내려주던 형식(yyyy.MM.dd) 그대로 맞춘다 - 화면 문구에 그대로 박히는 값이라서다. */
    public record LocgovLmttDto(String lmttBgnDe, String lmttEndDe, String violtResnCn) {
    }

    /**
     * 지자체 기부제한 조회 - AS-IS `POST /api/ngdonation/getLocGovLmtt` 재현.
     * AS-IS는 기부 진입점(답례품 상세의 기부하기, 기부하기 화면의 납부 제출, 지자체 선택 화면)
     * 마다 이걸 먼저 불러 제한 기간이면 안내를 띄우고 진행을 막는다.
     *
     * <p>제한이 없으면 AS-IS와 같이 data를 비워 보낸다(그쪽도 data가 없으면 통과시킨다).
     * 답례품 상세가 gift(8084)에 있어 교차 오리진 호출이 되므로 그 오리진을 열어 둔다.
     */
    @CrossOrigin(origins = {"http://localhost:8084", "http://localhost:5173"}, allowCredentials = "true")
    @GetMapping("/api/locgov-lmtt")
    public Map<String, Object> locgovLmtt(@RequestParam String locgovCode) {
        return locgovAdminService.activeLmttOf(locgovCode)
                .<Map<String, Object>>map(l -> Map.of("data", new LocgovLmttDto(
                        formatLmttDate(l.getLmttBgnDe()), formatLmttDate(l.getLmttEndDe()), l.getVioltResnCn())))
                .orElseGet(Map::of);
    }

    /** yyyyMMdd -> yyyy.MM.dd (AS-IS DATE_FORMAT '%Y.%m.%d'). 형식이 다르면 원문을 그대로 둔다. */
    private static String formatLmttDate(String yyyyMMdd) {
        if (yyyyMMdd == null || yyyyMMdd.length() != 8) {
            return yyyyMMdd;
        }
        return yyyyMMdd.substring(0, 4) + "." + yyyyMMdd.substring(4, 6) + "." + yyyyMMdd.substring(6, 8);
    }

    public record LocgovOptionDto(String locgovCode, String locgovNm, String upperLocgovCode, String upperLocgovNm) {
    }

    public record ProjectOptionDto(Long dsgnDntnBizId, String dsgnDntnBizTtl, String lclgvCd) {
    }

    public record DonateFormDto(List<LocgovOptionDto> locgovs, Map<String, String> provinces,
                                 String userName, String birthday, boolean hasAddress,
                                 java.math.BigDecimal remainingLimit, java.math.BigDecimal annualLimit,
                                 List<ProjectOptionDto> projects) {
    }

    @GetMapping("/api/donate/form")
    public ResponseEntity<?> form(HttpServletRequest request) {
        var authUserId = jwtVerifier.currentUserId(request);
        if (authUserId.isEmpty()) {
            return ResponseEntity.status(401).build();
        }
        Long userId = authUserId.get();

        List<Locgov> locgovs = donationService.activeLocgovs();
        List<LocgovOptionDto> locgovDtos = locgovs.stream()
                .map(l -> new LocgovOptionDto(l.getLocgovCode(), l.getLocgovNm(), l.getUpperLocgovCode(), l.getUpperLocgovNm()))
                .toList();
        Map<String, String> provinces = new LinkedHashMap<>();
        locgovs.forEach(l -> provinces.putIfAbsent(l.getUpperLocgovCode(), l.getUpperLocgovNm()));

        MemberInfo member = memberClient.fetchOrNull(userId);
        List<ProjectOptionDto> projects = donationService.openProjects().stream()
                .map(p -> new ProjectOptionDto(p.getDsgnDntnBizId(), p.getDsgnDntnBizTtl(), p.getLclgvCd()))
                .toList();

        return ResponseEntity.ok(new DonateFormDto(locgovDtos, provinces,
                member != null ? member.userName() : null, member != null ? member.birthday() : null,
                member != null && member.address() != null && !member.address().isBlank(),
                donationService.remainingAnnualLimit(userId), donationService.annualLimit().orElse(null),
                projects));
    }

    public record VerifyResidenceRequest(String locgovCode) {
    }

    public record VerifyResidenceResponse(String psitnLocgovCode, String userRegion) {
    }

    @PostMapping("/api/donate/verify-residence")
    public ResponseEntity<?> verifyResidence(@RequestBody VerifyResidenceRequest req, HttpServletRequest request) {
        var authUserId = jwtVerifier.currentUserId(request);
        if (authUserId.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("message", "로그인이 필요합니다."));
        }
        MemberInfo member = memberClient.fetchOrNull(authUserId.get());
        if (member == null || member.address() == null || member.address().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message",
                    "등록된 주소가 없습니다. 마이페이지 > 회원정보수정에서 주소를 먼저 등록해 주세요."));
        }
        var residence = donationService.residenceLocgovOf(member.address());
        if (residence.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message",
                    "등록된 주소에서 거주 지자체를 확인할 수 없습니다. 마이페이지 > 회원정보수정에서 주소를 다시 확인해 주세요."));
        }
        Locgov residenceLocgov = residence.get();
        if (donationService.isSelfResidence(req.locgovCode(), residenceLocgov.getLocgovCode())) {
            return ResponseEntity.badRequest().body(Map.of("message",
                    "자신의 주민등록주소지의 지자체에는 기부를 하실 수 없습니다.\n다른 지자체를 선택해 주세요."));
        }
        return ResponseEntity.ok(new VerifyResidenceResponse(residenceLocgov.getLocgovCode(),
                (residenceLocgov.getUpperLocgovNm() != null ? residenceLocgov.getUpperLocgovNm() + " " : "")
                        + residenceLocgov.getLocgovNm()));
    }

    @GetMapping("/api/donate/point-rate")
    public Map<String, Object> pointRate(@RequestParam String locgovCode) {
        BigDecimal rate = pointClient.pointRateOf(locgovCode);
        return rate != null ? Map.of("rate", rate) : Map.of();
    }

    /** 지자체 선택 시 검사 (AS-IS /api/ngdonation/locGovInfo) - 하루 중복기부(dupl)와
     *  기부불가기간(lmttBgnDe~lmttEndDe) 차단 문구를 함께 돌려준다. */
    @GetMapping("/api/donate/today-duplicate")
    public ResponseEntity<?> todayDuplicate(@RequestParam String locgovCode, HttpServletRequest request) {
        var authUserId = jwtVerifier.currentUserId(request);
        if (authUserId.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("message", "로그인이 필요합니다."));
        }
        String restriction = donationService.donationRestrictionMessage(locgovCode);
        Map<String, Object> body = new java.util.HashMap<>();
        body.put("duplicate", donationService.hasTodayDonation(authUserId.get(), locgovCode));
        body.put("restricted", restriction != null);
        body.put("restrictionMessage", restriction);
        return ResponseEntity.ok(body);
    }

    public record CreateDonationRequest(String locgovCode, BigDecimal amount, String psitnLocgovCode, String presentType) {
    }

    public record CreateDonationResponse(String cntrSn) {
    }

    @PostMapping("/api/donate")
    public ResponseEntity<?> donate(@RequestBody CreateDonationRequest req, HttpServletRequest request) {
        var authUserId = jwtVerifier.currentUserId(request);
        if (authUserId.isEmpty()) {
            return ResponseEntity.status(401).build();
        }
        try {
            Donation donation = donationService.createGeneralDonation(authUserId.get(), req.locgovCode(), req.amount(),
                    req.psitnLocgovCode(), req.presentType());
            return ResponseEntity.ok(new CreateDonationResponse(donation.getCntrSn()));
        } catch (DonationException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ===== 지정기부(특정사업에 기부하기) - DesignatedProjectController(designated-list/detail)와
    // DonationController(POST /donate/designated)의 Thymeleaf 화면과 완전히 같은 DonationService
    // 로직을 재사용한다. gaugeClassOf/formatYmd는 DesignatedProjectController의 private 헬퍼와
    // 동일한 이유(SpEL BigDecimal.valueOf 모호성 회피)로 여기도 Java에서 미리 계산해 둔다. */

    private static final int DSGN_PAGE_SIZE = 12;
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    // "/api/designated-projects"(단순 openProjects() 목록)는 이미 DonationApiController가
    // member 메인화면 위젯용으로 선점하고 있어, 이 목록/상세는 "/api/designated-donation/..."
    // (화면 자체의 URL 네임스페이스, /api/donate/...와 같은 컨벤션)로 경로를 분리했다.

    public record DesignatedProjectCardDto(Long dsgnDntnBizId, String dsgnDntnBizTtl, String lclgvCd, String locgovName,
                                            String imageUrl, String periodText, Long goalAmt, BigDecimal raisedAmount,
                                            BigDecimal rate, String gaugeClass, boolean overGoal, String status) {
    }

    public record DesignatedListResponse(List<DesignatedProjectCardDto> projects, int currentPage, int totalPages,
                                          Map<String, String> bsnsTypes) {
    }

    @GetMapping("/api/designated-donation/projects")
    public DesignatedListResponse designatedList(@RequestParam(required = false, defaultValue = "OPEN") String status,
                                                  @RequestParam(required = false) String bsnsType,
                                                  @RequestParam(required = false, defaultValue = "RATE") String sort,
                                                  @RequestParam(required = false, defaultValue = "1") int page,
                                                  @RequestParam(required = false) String q,
                                                  @RequestParam(required = false) String locgovCode) {
        List<DesignatedProject> allProjects = donationService.designatedProjects(status, bsnsType, sort, q, locgovCode);
        int totalPages = (int) Math.ceil(allProjects.size() / (double) DSGN_PAGE_SIZE);
        int currentPage = Math.max(1, Math.min(page, Math.max(totalPages, 1)));
        int fromIndex = Math.min((currentPage - 1) * DSGN_PAGE_SIZE, allProjects.size());
        int toIndex = Math.min(fromIndex + DSGN_PAGE_SIZE, allProjects.size());
        List<DesignatedProject> pageProjects = allProjects.subList(fromIndex, toIndex);

        Map<String, Locgov> locgovsByCode = locgovsByCode();
        List<DesignatedProjectCardDto> cards = pageProjects.stream().map(p -> {
            BigDecimal rate = donationService.fundingRatePercent(p);
            return new DesignatedProjectCardDto(p.getDsgnDntnBizId(), p.getDsgnDntnBizTtl(), p.getLclgvCd(),
                    locgovNameOf(locgovsByCode, p.getLclgvCd()), p.getImageUrl(),
                    formatYmd(p.getDsgnDntnBizBgngYmd()) + " ~ " + formatYmd(p.getDsgnDntnBizEndYmd()), p.getGoalAmt(),
                    donationService.raisedAmount(p.getDsgnDntnBizId()), rate, gaugeClassOf(rate),
                    rate.compareTo(HUNDRED) > 0, p.getDsgnDntnBizSttsCd());
        }).toList();
        return new DesignatedListResponse(cards, currentPage, totalPages, donationService.codesOf("DSGN_BSNS_TYPE"));
    }

    public record CheerMessageDto(String cntrSn, int giveOrder, String maskedUserName, String maskedLoginId,
                                   String cntrDeFormatted, BigDecimal cntrAmt, String cheerMsg) {
    }

    public record NoticeDto(Long prjNoticeId, String prjNoticeSubject, String prjNoticeCn, String dateText) {
    }

    public record DesignatedDetailDto(Long dsgnDntnBizId, String dsgnDntnBizTtl, String dsgnDntnBizCn, String imageUrl,
                                       String lclgvCd, String locgovName, String periodText, Long goalAmt,
                                       BigDecimal raisedAmount, int participantCount, BigDecimal rate,
                                       String gaugeClass, boolean overGoal, String status,
                                       List<CheerMessageDto> cheerMessages, List<NoticeDto> notices) {
    }

    @GetMapping("/api/designated-donation/projects/{id}")
    public ResponseEntity<?> designatedDetail(@PathVariable Long id, HttpServletRequest request) {
        DesignatedProject project = donationService.findProject(id).orElse(null);
        if (project == null) {
            return ResponseEntity.notFound().build();
        }
        // 공개 화면이지만 로그인 상태면 본인 기부건을 giveOrder=1로 표시해 인라인 편집을 노출한다.
        Long currentUserId = jwtVerifier.currentUserId(request).orElse(null);
        BigDecimal rate = donationService.fundingRatePercent(project);
        List<CheerMessageDto> cheerMessages = donationService.cheerMessagesOf(id, currentUserId).stream()
                .map(c -> new CheerMessageDto(c.cntrSn(), c.giveOrder(), c.maskedUserName(), c.maskedLoginId(),
                        c.cntrDeFormatted(), c.cntrAmt(), c.cheerMsg()))
                .toList();
        List<NoticeDto> notices = donationService.noticesOf(id).stream()
                .map(n -> {
                    String pnttm = n.getFrstRegistPnttm();
                    String dateText = pnttm != null && pnttm.length() >= 8 ? formatYmd(pnttm.substring(0, 8)) : "";
                    return new NoticeDto(n.getPrjNoticeId(), n.getPrjNoticeSubject(), n.getPrjNoticeCn(), dateText);
                })
                .toList();
        return ResponseEntity.ok(new DesignatedDetailDto(project.getDsgnDntnBizId(), project.getDsgnDntnBizTtl(),
                project.getDsgnDntnBizCn(), project.getImageUrl(), project.getLclgvCd(),
                locgovNameOf(locgovsByCode(), project.getLclgvCd()),
                formatYmd(project.getDsgnDntnBizBgngYmd()) + " ~ " + formatYmd(project.getDsgnDntnBizEndYmd()),
                project.getGoalAmt(), donationService.raisedAmount(id), donationService.participantCountOf(id),
                rate, gaugeClassOf(rate), rate.compareTo(HUNDRED) > 0, project.getDsgnDntnBizSttsCd(),
                cheerMessages, notices));
    }

    public record CreateDesignatedDonationRequest(Long dsgnDntnBizId, BigDecimal amount, String cheerMsg) {
    }

    public record SaveCheerMsgRequest(String cntrSn, String cheerMsg) {
    }

    /** 특정사업 상세 "응원메시지(기부내역)" 탭에서 본인 기부건의 응원메시지 저장/수정
     *  (AS-IS designated-donation/details.html saveCheerMsg). 로그인 본인 완료 기부건만. */
    @PostMapping("/api/designated-donation/saveCheerMsg")
    public ResponseEntity<?> saveCheerMsg(@RequestBody SaveCheerMsgRequest req, HttpServletRequest request) {
        var authUserId = jwtVerifier.currentUserId(request);
        if (authUserId.isEmpty()) {
            return ResponseEntity.status(401).build();
        }
        try {
            donationService.saveCheerMsg(authUserId.get(), req.cntrSn(), req.cheerMsg());
            return ResponseEntity.ok(Map.of("result", true));
        } catch (DonationException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/api/donate/designated")
    public ResponseEntity<?> donateDesignated(@RequestBody CreateDesignatedDonationRequest req, HttpServletRequest request) {
        var authUserId = jwtVerifier.currentUserId(request);
        if (authUserId.isEmpty()) {
            return ResponseEntity.status(401).build();
        }
        try {
            Donation donation = donationService.createDesignatedDonation(authUserId.get(), req.dsgnDntnBizId(),
                    req.amount(), req.cheerMsg());
            return ResponseEntity.ok(new CreateDonationResponse(donation.getCntrSn()));
        } catch (DonationException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    private Map<String, Locgov> locgovsByCode() {
        return donationService.activeLocgovs().stream()
                .collect(Collectors.toMap(Locgov::getLocgovCode, l -> l, (a, b) -> a, LinkedHashMap::new));
    }

    private static String locgovNameOf(Map<String, Locgov> locgovsByCode, String locgovCode) {
        Locgov l = locgovsByCode.get(locgovCode);
        return l != null ? (l.getUpperLocgovNm() != null ? l.getUpperLocgovNm() + " " : "") + l.getLocgovNm() : "";
    }

    private static String gaugeClassOf(BigDecimal rate) {
        int cmp = rate.compareTo(HUNDRED);
        return cmp < 0 ? "lt_goal" : (cmp == 0 ? "goal" : "gt_goal");
    }

    private static String formatYmd(String ymd) {
        if (ymd == null || ymd.length() < 8) {
            return ymd;
        }
        return ymd.substring(0, 4) + "-" + ymd.substring(4, 6) + "-" + ymd.substring(6, 8);
    }
}
