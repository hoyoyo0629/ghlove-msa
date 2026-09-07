package com.ghlove.donation.web;

import com.ghlove.donation.domain.Donation;
import com.ghlove.donation.domain.DonationLevy;
import com.ghlove.donation.domain.DesignatedProject;
import com.ghlove.donation.domain.Locgov;
import com.ghlove.donation.service.Certificate;
import com.ghlove.donation.service.DonationException;
import com.ghlove.donation.service.DonationService;
import com.ghlove.donation.service.JwtVerifier;
import com.ghlove.donation.service.MemberClient;
import com.ghlove.donation.service.MemberInfo;
import com.ghlove.donation.service.ReceiptService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** storefront(Vue3 SPA)용 마이페이지 하위화면 JSON API - 관심지자체/기부혜택증/기부내역조회.
 * {@link InterestLocgovController}, {@link HonorBenefitController}, {@link DonationController}
 * (전부 Thymeleaf)와 완전히 같은 {@link DonationService} 로직을 JSON으로 감싼다. */
@RestController
@RequiredArgsConstructor
public class DonationMyApiController {

    private static final DateTimeFormatter CNTR_DE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final DonationService donationService;
    private final com.ghlove.donation.service.PointClient pointClient;
    private final ReceiptService receiptService;
    private final MemberClient memberClient;
    private final JwtVerifier jwtVerifier;
    private static final int RECEIPT_PAGE_SIZE = 10;

    private Optional<Long> authUserId(HttpServletRequest request) {
        return jwtVerifier.currentUserId(request);
    }

    // ---- 관심지자체 ----

    public record InterestLocgovRowDto(String locgovCode, String locgovName, BigDecimal myTotal) {
    }

    @GetMapping("/api/my/interest-locgovs")
    public ResponseEntity<List<InterestLocgovRowDto>> myInterestLocgovs(HttpServletRequest request) {
        return authUserId(request)
                .map(userId -> ResponseEntity.ok(donationService.interestLocgovsOf(userId).stream()
                        .map(r -> new InterestLocgovRowDto(r.locgovCode(), r.locgovName(), r.myTotal()))
                        .toList()))
                .orElseGet(() -> ResponseEntity.status(401).build());
    }

    // ---- 기부혜택증 ----

    public record HonorCertificateDto(Integer stdrYear, String locgovCode, String locgovName, String levelCode,
                                       String levelLabel) {
    }

    @GetMapping("/api/honor/certificates")
    public ResponseEntity<List<HonorCertificateDto>> myCertificates(HttpServletRequest request) {
        return authUserId(request)
                .map(userId -> {
                    Map<String, String> levelLabels = donationService.codesOf("HONOR_STD");
                    var rows = donationService.myHonorCertificates(userId).stream()
                            .map(c -> new HonorCertificateDto(c.stdrYear(), c.locgovCode(), c.locgovName(),
                                    c.levelCode(), levelLabels.get(c.levelCode())))
                            .toList();
                    return ResponseEntity.ok(rows);
                })
                .orElseGet(() -> ResponseEntity.status(401).build());
    }

    // ---- 기부내역 조회 ----

    public record DonationRowDto(String cntrSn, String locgovCode, String locgovName, BigDecimal cntrAmt, String cntrDe,
                                  Long earnedPoints, String bugaNo, String sunapDate, String projectTitle,
                                  String statusCode, String statusLabel, boolean canComplete, boolean canCancel,
                                  String presentType) {
    }

    public record MyDonationsResponse(List<DonationRowDto> donations, BigDecimal totalCntrAmt, List<Integer> years,
                                       List<LocgovOptionDto> locgovs) {
    }

    public record LocgovOptionDto(String locgovCode, String locgovNm, String upperLocgovCode, String upperLocgovNm) {
    }

    @GetMapping("/api/my/donations")
    public ResponseEntity<MyDonationsResponse> myDonations(@RequestParam(required = false) Integer year,
                                                             @RequestParam(required = false, defaultValue = "ALL") String period,
                                                             @RequestParam(required = false) String upperLocgovCode,
                                                             @RequestParam(required = false) String locgovCode,
                                                             HttpServletRequest request) {
        var authUserId = authUserId(request);
        if (authUserId.isEmpty()) {
            return ResponseEntity.status(401).build();
        }
        Long userId = authUserId.get();

        var locgovs = donationService.activeLocgovs();
        Map<String, Locgov> locgovsByCode = new LinkedHashMap<>();
        locgovs.forEach(l -> locgovsByCode.put(l.getLocgovCode(), l));

        List<Donation> all = donationService.myDonations(userId);
        List<Integer> years = all.stream()
                .filter(d -> d.getCntrDe() != null && d.getCntrDe().length() >= 4)
                .map(d -> Integer.parseInt(d.getCntrDe().substring(0, 4)))
                .distinct().sorted(java.util.Comparator.reverseOrder()).toList();

        List<Donation> filtered = all.stream()
                .filter(d -> year == null || (d.getCntrDe() != null && d.getCntrDe().startsWith(String.valueOf(year))))
                .filter(d -> withinPeriod(d.getCntrDe(), period))
                .filter(d -> upperLocgovCode == null || upperLocgovCode.isBlank()
                        || upperLocgovCode.equals(locgovUpperCodeOf(d.getCntrLocgovCode(), locgovsByCode)))
                .filter(d -> locgovCode == null || locgovCode.isBlank() || locgovCode.equals(d.getCntrLocgovCode()))
                .toList();

        Map<String, String> statusLabels = donationService.codesOf("CNTR_STATUS");
        Map<String, DonationLevy> levies = donationService.leviesOf(filtered);
        Map<String, Long> earnedPoints = pointClient.earnedPointsByCntrSn(filtered.stream().map(Donation::getCntrSn).toList());
        Map<Long, String> projectTitles = new LinkedHashMap<>();
        for (DesignatedProject p : donationService.openProjects()) {
            projectTitles.put(p.getDsgnDntnBizId(), p.getDsgnDntnBizTtl());
        }

        List<DonationRowDto> rows = filtered.stream().map(d -> {
            Locgov l = locgovsByCode.get(d.getCntrLocgovCode());
            String locgovName = l != null ? (l.getUpperLocgovNm() != null ? l.getUpperLocgovNm() + " " : "") + l.getLocgovNm() : d.getCntrLocgovCode();
            DonationLevy levy = levies.get(d.getCntrSn());
            String projectTitle = d.getDsgnDntnBizId() != null ? projectTitles.get(d.getDsgnDntnBizId()) : null;
            boolean completed = "COMPLETED".equals(d.getCntrSttusCode());
            boolean requested = "REQUESTED".equals(d.getCntrSttusCode());
            return new DonationRowDto(d.getCntrSn(), d.getCntrLocgovCode(), locgovName, d.getCntrAmt(), d.getCntrDe(),
                    earnedPoints.get(d.getCntrSn()), levy != null ? levy.getBugaNo() : null,
                    levy != null && levy.getSunapDate() != null ? levy.getSunapDate().toString() : null,
                    projectTitle != null ? projectTitle : "자치단체기부", d.getCntrSttusCode(),
                    statusLabels.get(d.getCntrSttusCode()), requested, requested || completed,
                    d.getRtnpsntReqstCode());
        }).toList();

        BigDecimal totalCntrAmt = filtered.stream().map(Donation::getCntrAmt).filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        List<LocgovOptionDto> locgovOptions = locgovs.stream()
                .map(l -> new LocgovOptionDto(l.getLocgovCode(), l.getLocgovNm(), l.getUpperLocgovCode(), l.getUpperLocgovNm()))
                .toList();

        return ResponseEntity.ok(new MyDonationsResponse(rows, totalCntrAmt, years, locgovOptions));
    }

    /** "100"(답례품을 제공 받음)이면 프론트가 결제완료 직후 답례품 선택 화면으로 안내한다
     *  (SFR-003 "기부금 납부 시 답례품 선택 기능 추가" - 재검토 라운드에서 신규 통합). */
    @PostMapping("/api/my/donations/{cntrSn}/complete")
    public ResponseEntity<Map<String, Object>> complete(@PathVariable String cntrSn, HttpServletRequest request) {
        var authUserId = authUserId(request);
        if (authUserId.isEmpty() || !ownsDonation(cntrSn, authUserId.get())) {
            return ResponseEntity.status(401).build();
        }
        try {
            Donation completed = donationService.completeDonation(cntrSn);
            return ResponseEntity.ok(Map.of("status", "OK", "locgovCode", completed.getCntrLocgovCode(),
                    "presentType", completed.getRtnpsntReqstCode() != null ? completed.getRtnpsntReqstCode() : ""));
        } catch (DonationException e) {
            return ResponseEntity.badRequest().body(Map.of("status", "ERROR", "message", e.getMessage()));
        }
    }

    @PostMapping("/api/my/donations/{cntrSn}/cancel")
    public ResponseEntity<Map<String, Object>> cancel(@PathVariable String cntrSn, HttpServletRequest request) {
        var authUserId = authUserId(request);
        if (authUserId.isEmpty() || !ownsDonation(cntrSn, authUserId.get())) {
            return ResponseEntity.status(401).build();
        }
        try {
            donationService.cancelDonation(cntrSn);
            return ResponseEntity.ok(Map.of("status", "OK"));
        } catch (DonationException e) {
            return ResponseEntity.badRequest().body(Map.of("status", "ERROR", "message", e.getMessage()));
        }
    }

    // ---- 기부확인증 (ReceiptController/ReceiptService의 Thymeleaf 화면과 완전히 같은
    // ReceiptService 로직을 재사용한다) ----

    public record ReceiptRowDto(String cntrSn, String cntrDeDisplay, String upperLocgovNm, String locgovNm,
                                 BigDecimal cntrAmt) {
    }

    public record MyReceiptsResponse(List<ReceiptRowDto> rows, BigDecimal totalCntrAmt, int totalCnt,
                                      int currentPage, int totalPages, List<LocgovOptionDto> locgovs) {
    }

    @GetMapping("/api/my/receipts")
    public ResponseEntity<?> myReceipts(@RequestParam(required = false) String locgovCode,
                                         @RequestParam(required = false) String upperLocgovCode,
                                         @RequestParam(required = false) String searchStartDate,
                                         @RequestParam(required = false) String searchEndDate,
                                         @RequestParam(defaultValue = "1") int page,
                                         HttpServletRequest request) {
        var authUserId = authUserId(request);
        if (authUserId.isEmpty()) {
            return ResponseEntity.status(401).build();
        }
        var result = receiptService.receiptList(authUserId.get(), locgovCode, upperLocgovCode, searchStartDate, searchEndDate);
        List<ReceiptRowDto> allRows = result.getRows().stream()
                .map(r -> new ReceiptRowDto(r.getCntrSn(), r.getCntrDeDisplay(), r.getUpperLocgovNm(), r.getLocgovNm(), r.getCntrAmt()))
                .toList();
        int totalPages = Math.max(1, (int) Math.ceil(allRows.size() / (double) RECEIPT_PAGE_SIZE));
        int currentPage = Math.min(Math.max(page, 1), totalPages);
        int from = (currentPage - 1) * RECEIPT_PAGE_SIZE;
        int to = Math.min(from + RECEIPT_PAGE_SIZE, allRows.size());
        List<ReceiptRowDto> pageRows = from < to ? allRows.subList(from, to) : List.of();

        List<LocgovOptionDto> locgovOptions = donationService.activeLocgovs().stream()
                .map(l -> new LocgovOptionDto(l.getLocgovCode(), l.getLocgovNm(), l.getUpperLocgovCode(), l.getUpperLocgovNm()))
                .toList();
        return ResponseEntity.ok(new MyReceiptsResponse(pageRows, result.getTotalCntrAmt(), result.getTotalCnt(),
                currentPage, totalPages, locgovOptions));
    }

    public record ReceiptDetailRowDto(String cntrSn, String cntrDeDisplay, String upperLocgovNm, String locgovNm,
                                       BigDecimal cntrAmt, String bizRno, String spelDstrYn) {
    }

    public record CertificateDto(String userName, String birthdayDisplay, String topLocGovDisplay,
                                  BigDecimal totalCntrAmt, int totalCnt, String nowDateDisplay,
                                  List<ReceiptDetailRowDto> rows) {
    }

    @GetMapping("/api/my/receipts/certificate")
    public ResponseEntity<?> certificate(@RequestParam List<String> cntrSn, HttpServletRequest request) {
        var authUserId = authUserId(request);
        if (authUserId.isEmpty()) {
            return ResponseEntity.status(401).build();
        }
        try {
            Certificate c = receiptService.buildCertificate(authUserId.get(), cntrSn);
            List<ReceiptDetailRowDto> rows = c.getRows().stream()
                    .map(r -> new ReceiptDetailRowDto(r.getCntrSn(), r.getCntrDeDisplay(), r.getUpperLocgovNm(),
                            r.getLocgovNm(), r.getCntrAmt(), r.getBizRno(), r.getSpelDstrYn()))
                    .toList();
            return ResponseEntity.ok(new CertificateDto(c.getUserName(), c.getBirthdayDisplay(), c.getTopLocGovDisplay(),
                    c.getTotalCntrAmt(), c.getTotalCnt(), c.getNowDateDisplay(), rows));
        } catch (DonationException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ---- 세액공제 예상액 (HonorBenefitController.estimate()와 완전히 같은 로직 -
    // AS-IS에도 이 계산을 보여줄 전용 화면이 없어 GNB에는 연결되지 않지만, 기부혜택증과
    // 같은 "기부혜택" 화면군으로 묶어 마이페이지에서 접근 가능하게 한다). ----

    public record LocgovBenefitDto(String locgovName, String benefitDesc) {
    }

    public record TaxCreditEstimateDto(int year, String userName, int donationCount, BigDecimal totalAmount,
                                        BigDecimal taxCredit, List<LocgovBenefitDto> benefits) {
    }

    @GetMapping("/api/my/tax-credit-estimate")
    public ResponseEntity<?> taxCreditEstimate(@RequestParam(required = false) Integer year, HttpServletRequest request) {
        var authUserId = authUserId(request);
        if (authUserId.isEmpty()) {
            return ResponseEntity.status(401).build();
        }
        Long userId = authUserId.get();
        int targetYear = year != null ? year : LocalDate.now().getYear();

        List<Donation> donationsThisYear = donationService.myDonations(userId).stream()
                .filter(d -> "COMPLETED".equals(d.getCntrSttusCode()))
                .filter(d -> d.getCntrDe() != null && d.getCntrDe().startsWith(String.valueOf(targetYear)))
                .toList();

        List<Locgov> activeLocgovs = donationService.activeLocgovs();
        List<LocgovBenefitDto> benefits = donationsThisYear.stream()
                .map(Donation::getCntrLocgovCode).distinct()
                .map(locgovCode -> {
                    String benefit = donationService.honorBenefitOf(locgovCode);
                    if (benefit == null || benefit.isBlank()) {
                        return null;
                    }
                    Locgov l = activeLocgovs.stream().filter(x -> x.getLocgovCode().equals(locgovCode)).findFirst().orElse(null);
                    String name = l != null ? (l.getUpperLocgovNm() != null ? l.getUpperLocgovNm() + " " : "") + l.getLocgovNm() : locgovCode;
                    return new LocgovBenefitDto(name, benefit);
                })
                .filter(java.util.Objects::nonNull)
                .toList();

        BigDecimal totalAmount = donationsThisYear.stream().map(Donation::getCntrAmt)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        MemberInfo member = memberClient.fetch(userId);

        return ResponseEntity.ok(new TaxCreditEstimateDto(targetYear, member.userName(), donationsThisYear.size(),
                totalAmount, donationService.taxCreditOf(userId, targetYear), benefits));
    }

    // ---- 기탁서(오프라인) 등록 ----
    // 기존 OfflineDonationController(/donations/offline)는 사이트 디자인이 전혀 적용되지 않은
    // 내부용 임시 폼이었고, 회원ID를 로그인 세션과 무관하게 직접 입력받는 보안 결함이 있었다
    // (누구나 남의 이름으로 기부 등록 가능). 정식 마이페이지 화면으로 옮기며 다른 /api/my/...
    // 엔드포인트와 동일하게 JWT로 확인한 본인 명의로만 등록되도록 고쳤다(사용자 확인 완료).
    // DonationService.registerOfflineDonation()의 검증/저장 로직 자체는 그대로 재사용.

    public record RegisterOfflineDonationRequest(String locgovCode, BigDecimal amount, String rceptBankCode,
                                                   String rceptBankNm) {
    }

    @PostMapping("/api/my/donations/offline")
    public ResponseEntity<?> registerOffline(@RequestBody RegisterOfflineDonationRequest req, HttpServletRequest request) {
        var authUserId = authUserId(request);
        if (authUserId.isEmpty()) {
            return ResponseEntity.status(401).build();
        }
        try {
            Donation donation = donationService.registerOfflineDonation(authUserId.get(), req.locgovCode(),
                    req.amount(), req.rceptBankCode(), req.rceptBankNm());
            return ResponseEntity.ok(Map.of("cntrSn", donation.getCntrSn()));
        } catch (DonationException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    private boolean ownsDonation(String cntrSn, Long userId) {
        return donationService.find(cntrSn).map(Donation::getUserId).map(userId::equals).orElse(false);
    }

    private static String locgovUpperCodeOf(String locgovCode, Map<String, Locgov> locgovsByCode) {
        Locgov l = locgovsByCode.get(locgovCode);
        return l != null ? l.getUpperLocgovCode() : null;
    }

    private static boolean withinPeriod(String cntrDe, String period) {
        if (period == null || "ALL".equals(period) || cntrDe == null || cntrDe.length() < 8) {
            return true;
        }
        LocalDate donatedOn = LocalDate.parse(cntrDe, CNTR_DE_FORMAT);
        LocalDate today = LocalDate.now();
        LocalDate cutoff = switch (period) {
            case "TODAY" -> today;
            case "7D" -> today.minusDays(7);
            case "1M" -> today.minusMonths(1);
            case "3M" -> today.minusMonths(3);
            case "6M" -> today.minusMonths(6);
            case "1Y" -> today.minusYears(1);
            default -> LocalDate.MIN;
        };
        return !donatedOn.isBefore(cutoff);
    }
}
