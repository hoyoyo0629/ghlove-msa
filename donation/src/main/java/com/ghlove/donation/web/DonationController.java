package com.ghlove.donation.web;

import com.ghlove.donation.domain.DesignatedProject;
import com.ghlove.donation.domain.Donation;
import com.ghlove.donation.domain.Locgov;
import com.ghlove.donation.service.DonationException;
import com.ghlove.donation.service.DonationService;
import com.ghlove.donation.service.JwtVerifier;
import com.ghlove.donation.service.MemberClient;
import com.ghlove.donation.service.PointClient;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** SFR-010: userId는 더 이상 요청 파라미터로 받지 않고 로그인 JWT 쿠키에서만 가져온다. */
@Controller
@RequiredArgsConstructor
public class DonationController {

    private final DonationService donationService;
    private final PointClient pointClient;
    private final MemberClient memberClient;
    private final JwtVerifier jwtVerifier;

    private String loginRedirect(String returnPath) {
        return "redirect:http://localhost:8081/login?target=" + encode("http://localhost:8082" + returnPath);
    }

    /**
     * GNB "기부 &gt; 자치단체에 기부하기" (AS-IS donation-main.html). AS-IS는 이 메뉴를
     * $s.donation.goDonationPage()로 열면서 Saleson.init({loginPage:true})를 태우기 때문에,
     * 비로그인 상태면 화면이 아니라 로그인 페이지(target=원래 가려던 기부하기 화면)로
     * 먼저 보낸다 - 로그인 후 다시 여기로 돌아온다.
     */
    @GetMapping("/")
    public String donateForm(@RequestParam(required = false) String locgovCode,
                              @RequestParam(required = false) String prjId,
                              HttpServletRequest request, Model model) {
        var authUserId = jwtVerifier.currentUserId(request);
        if (authUserId.isEmpty()) {
            return loginRedirect(donateFormQuery(locgovCode, prjId));
        }
        populateDonateForm(authUserId.get(), locgovCode, model);
        return "donate";
    }

    /** designated-detail.html의 "기부하기" 버튼이 locgovCode+prjId를 붙여 여기로 보낸다
     *  (AS-IS $s.donation.goDonationPage('?prjId=...&locgovCode=...')와 동일) - 둘 다
     *  로그인 리다이렉트 target에 보존해야 로그인 후 특정사업이 계속 선택된 채로 돌아온다. */
    private String donateFormQuery(String locgovCode, String prjId) {
        StringBuilder query = new StringBuilder();
        if (locgovCode != null && !locgovCode.isBlank()) {
            query.append(query.isEmpty() ? '?' : '&').append("locgovCode=").append(encode(locgovCode));
        }
        if (prjId != null && !prjId.isBlank()) {
            query.append(query.isEmpty() ? '?' : '&').append("prjId=").append(encode(prjId));
        }
        return "/" + query;
    }

    /** 기부하기 화면에 필요한 지자체/특정사업/회원(거주지 확인)/한도 데이터를 채운다. */
    private void populateDonateForm(Long userId, String locgovCode, Model model) {
        var locgovs = donationService.activeLocgovs();
        model.addAttribute("locgovs", locgovs);
        model.addAttribute("selectedLocgovCode", locgovCode);

        Map<String, String> provinces = new LinkedHashMap<>();
        locgovs.forEach(l -> provinces.putIfAbsent(l.getUpperLocgovCode(), l.getUpperLocgovNm()));
        model.addAttribute("provinces", provinces);

        var member = memberClient.fetchOrNull(userId);
        model.addAttribute("userName", member != null ? member.userName() : null);
        model.addAttribute("birthday", member != null ? member.birthday() : null);
        model.addAttribute("hasAddress", member != null && member.address() != null && !member.address().isBlank());

        model.addAttribute("remainingLimit", donationService.remainingAnnualLimit(userId));
        model.addAttribute("annualLimit", donationService.annualLimit().orElse(null));
        addProjectsToModel(model);
    }

    /**
     * "주소확인하기" (AS-IS rsgstadresinfo) - 회원의 등록 주소로 거주지 지자체를 판정하고,
     * 선택한 기부 지자체가 본인 주소지인지 검사한다. AS-IS는 행정정보공동이용센터에
     * 주민등록번호를 보내 실주소를 받지만 이 MSA엔 그 연계가 없어(다른 외부연계와 동일한
     * 의도적 축소) 마이페이지에 등록된 주소를 쓴다. 화면은 AS-IS와 동일하게 주민등록번호
     * 뒷자리 입력칸을 갖고 있고 그 값이 juminNo로 전달되지만, 실제 연계가 붙기 전까지는
     * 사용하지 않는다 - 나중에 연계가 준비되면 이 파라미터로 실제 검증 로직을 갈아끼우면 된다.
     */
    @PostMapping("/donate/verify-residence")
    @ResponseBody
    public ResponseEntity<?> verifyResidence(@RequestParam String locgovCode,
                                              @RequestParam(required = false) String juminNo,
                                              HttpServletRequest request) {
        var authUserId = jwtVerifier.currentUserId(request);
        if (authUserId.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("message", "로그인이 필요합니다."));
        }
        var member = memberClient.fetchOrNull(authUserId.get());
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
        if (donationService.isSelfResidence(locgovCode, residenceLocgov.getLocgovCode())) {
            return ResponseEntity.badRequest().body(Map.of("message",
                    "자신의 주민등록주소지의 지자체에는 기부를 하실 수 없습니다.\n다른 지자체를 선택해 주세요."));
        }
        return ResponseEntity.ok(Map.of(
                "psitnLocgovCode", residenceLocgov.getLocgovCode(),
                "userRegion", (residenceLocgov.getUpperLocgovNm() != null ? residenceLocgov.getUpperLocgovNm() + " " : "")
                        + residenceLocgov.getLocgovNm()));
    }

    /** 기부하기 화면의 "포인트 적립예상" - 지자체를 고를 때마다 적립률을 다시 읽어온다. */
    @GetMapping("/donate/point-rate")
    @ResponseBody
    public Map<String, Object> pointRate(@RequestParam String locgovCode) {
        BigDecimal rate = pointClient.pointRateOf(locgovCode);
        return rate != null ? Map.of("rate", rate) : Map.of();
    }

    @PostMapping("/donate")
    public String donate(@RequestParam String locgovCode, @RequestParam BigDecimal amount,
                          @RequestParam(required = false) String psitnLocgovCode,
                          @RequestParam(required = false, defaultValue = "100") String presentType,
                          HttpServletRequest request, Model model) {
        var authUserId = jwtVerifier.currentUserId(request);
        if (authUserId.isEmpty()) {
            return loginRedirect("/");
        }
        try {
            donationService.createGeneralDonation(authUserId.get(), locgovCode, amount, psitnLocgovCode, presentType);
            return "redirect:/my?donated=success";
        } catch (DonationException e) {
            model.addAttribute("errorMessage", e.getMessage());
            populateDonateForm(authUserId.get(), locgovCode, model);
            return "donate";
        }
    }

    @PostMapping("/donate/designated")
    public String donateDesignated(@RequestParam Long dsgnDntnBizId, @RequestParam BigDecimal amount,
                                    @RequestParam(required = false) String cheerMsg,
                                    HttpServletRequest request) {
        var authUserId = jwtVerifier.currentUserId(request);
        if (authUserId.isEmpty()) {
            return loginRedirect("/designated-donation/" + dsgnDntnBizId);
        }
        try {
            donationService.createDesignatedDonation(authUserId.get(), dsgnDntnBizId, amount, cheerMsg);
            return "redirect:/my?donated=success";
        } catch (DonationException e) {
            return "redirect:/designated-donation/" + dsgnDntnBizId + "?error=" + encode(e.getMessage());
        }
    }

    @PostMapping("/donations/{cntrSn}/complete")
    public String complete(@PathVariable String cntrSn, HttpServletRequest request) {
        var authUserId = jwtVerifier.currentUserId(request);
        if (authUserId.isEmpty()) {
            return loginRedirect("/my");
        }
        if (!ownsDonation(cntrSn, authUserId.get())) {
            return "redirect:/my";
        }
        try {
            donationService.completeDonation(cntrSn);
        } catch (DonationException e) {
            return "redirect:/my?error=" + encode(e.getMessage());
        }
        return "redirect:/my";
    }

    @PostMapping("/donations/{cntrSn}/cancel")
    public String cancel(@PathVariable String cntrSn, HttpServletRequest request) {
        var authUserId = jwtVerifier.currentUserId(request);
        if (authUserId.isEmpty()) {
            return loginRedirect("/my");
        }
        if (!ownsDonation(cntrSn, authUserId.get())) {
            return "redirect:/my";
        }
        try {
            donationService.cancelDonation(cntrSn);
        } catch (DonationException e) {
            return "redirect:/my?error=" + encode(e.getMessage());
        }
        return "redirect:/my";
    }

    private boolean ownsDonation(String cntrSn, Long userId) {
        return donationService.find(cntrSn).map(Donation::getUserId).map(userId::equals).orElse(false);
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static final DateTimeFormatter CNTR_DE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * 마이페이지 "기부내역 조회" (AS-IS mypage/cntrList.html). 년도/기간(전체·오늘·7일·
     * 1개월·3개월·6개월·1년)/시도·시군구 필터는 지금 시드 규모(회원당 최대 십수 건)에서는
     * 파생 리포지토리 메서드를 늘리기보다 인메모리로 거르는 편이 간단하다 - ReceiptController
     * 도 페이지네이션은 같은 방식(전체 조회 후 자바에서 자르기)을 쓴다.
     */
    @GetMapping("/my")
    public String myDonations(@RequestParam(required = false) String error,
                               @RequestParam(required = false) Integer year,
                               @RequestParam(required = false, defaultValue = "ALL") String period,
                               @RequestParam(required = false) String upperLocgovCode,
                               @RequestParam(required = false) String locgovCode,
                               HttpServletRequest request, Model model) {
        var authUserId = jwtVerifier.currentUserId(request);
        if (authUserId.isEmpty()) {
            return loginRedirect("/my");
        }
        model.addAttribute("errorMessage", error);
        model.addAttribute("year", year);
        model.addAttribute("period", period);
        model.addAttribute("upperLocgovCode", upperLocgovCode);
        model.addAttribute("locgovCode", locgovCode);

        var locgovs = donationService.activeLocgovs();
        model.addAttribute("locgovs", locgovs);
        Map<String, String> provinces = new LinkedHashMap<>();
        locgovs.forEach(l -> provinces.putIfAbsent(l.getUpperLocgovCode(), l.getUpperLocgovNm()));
        model.addAttribute("provinces", provinces);
        Map<String, Locgov> locgovsByCode = new LinkedHashMap<>();
        locgovs.forEach(l -> locgovsByCode.put(l.getLocgovCode(), l));
        model.addAttribute("locgovsByCode", locgovsByCode);

        {
            List<Donation> all = donationService.myDonations(authUserId.get());

            List<Integer> years = all.stream()
                    .filter(d -> d.getCntrDe() != null && d.getCntrDe().length() >= 4)
                    .map(d -> Integer.parseInt(d.getCntrDe().substring(0, 4)))
                    .distinct().sorted(Comparator.reverseOrder()).toList();
            model.addAttribute("years", years);

            List<Donation> donations = all.stream()
                    .filter(d -> year == null || (d.getCntrDe() != null && d.getCntrDe().startsWith(String.valueOf(year))))
                    .filter(d -> withinPeriod(d.getCntrDe(), period))
                    .filter(d -> upperLocgovCode == null || upperLocgovCode.isBlank()
                            || upperLocgovCode.equals(locgovUpperCodeOf(d.getCntrLocgovCode(), locgovsByCode)))
                    .filter(d -> locgovCode == null || locgovCode.isBlank() || locgovCode.equals(d.getCntrLocgovCode()))
                    .toList();
            model.addAttribute("donations", donations);
            model.addAttribute("totalCntrAmt", sumAmounts(donations));
            model.addAttribute("statusLabels", donationService.codesOf("CNTR_STATUS"));
            model.addAttribute("levies", donationService.leviesOf(donations));
            model.addAttribute("ntsStatusLabels", donationService.codesOf("NTS_STATUS"));
            model.addAttribute("earnedPoints", pointClient.earnedPointsByCntrSn(donations.stream().map(Donation::getCntrSn).toList()));

            Map<Long, String> projectTitles = new LinkedHashMap<>();
            for (DesignatedProject p : donationService.openProjects()) {
                projectTitles.put(p.getDsgnDntnBizId(), p.getDsgnDntnBizTtl());
            }
            model.addAttribute("projectTitles", projectTitles);
        }
        return "my";
    }

    private static BigDecimal sumAmounts(List<Donation> donations) {
        return donations.stream().map(Donation::getCntrAmt).filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
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

    /** donate.html의 "특정사업 선택" 드롭다운용 - AS-IS donation-main.html에는 모금현황
     *  진행바가 없다(그건 /designated-donation의 별도 화면 몫). */
    private void addProjectsToModel(Model model) {
        model.addAttribute("projects", donationService.openProjects());
    }
}
