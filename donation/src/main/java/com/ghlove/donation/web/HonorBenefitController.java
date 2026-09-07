package com.ghlove.donation.web;

import com.ghlove.donation.domain.Donation;
import com.ghlove.donation.domain.Locgov;
import com.ghlove.donation.service.DonationService;
import com.ghlove.donation.service.JwtVerifier;
import com.ghlove.donation.service.MemberClient;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 기부혜택(소득공제) 조회 (SFR-003 "기부혜택 관리 및 발급 기능"). SFR-010: userId는 로그인
 * JWT 쿠키에서만 가져온다. 지자체별 혜택 문구 "등록/수정"은 관리자 전용 기능이라
 * admin 콘솔의 지자체관리 화면(LocgovAdminApiController)으로 옮겼다 - 원래 여기 무인증으로
 * 노출돼 있던 /honor/benefit 쓰기 엔드포인트는 보안결함이라 제거했다(SFR-003 재검토 라운드).
 */
@Controller
@RequiredArgsConstructor
public class HonorBenefitController {

    private static final String STATUS_COMPLETED = "COMPLETED";

    private final DonationService donationService;
    private final MemberClient memberClient;
    private final JwtVerifier jwtVerifier;

    private String loginRedirect(String returnPath) {
        return "redirect:http://localhost:8081/login?target="
                + java.net.URLEncoder.encode("http://localhost:8082" + returnPath, java.nio.charset.StandardCharsets.UTF_8);
    }

    /** 안내사항 &gt; 연말정산 세액공제 안내 (AS-IS donation/guide3.html) - 로그인 불필요한
     *  순수 정적 안내 페이지. 개인화된 세액공제 계산은 /honor/estimate로 분리했다(AS-IS
     *  자체에도 이 계산을 보여줄 전용 페이지가 없고, mypage/cntrList.html 소스에 "세액공제
     *  예상금액 임시데이터, 개발필요"라는 주석만 있다 - 실제 화면이 생기기 전까지는 이
     *  계산 로직을 잃지 않도록 별도 경로로만 남겨둔다). */
    @GetMapping("/honor")
    public String guide() {
        return "honor";
    }

    /** 개인화된 세액공제 예상액 계산 - AS-IS에 아직 전용 화면이 없는 기능이라 GNB에는
     *  연결하지 않고 경로만 유지한다. */
    @GetMapping("/honor/estimate")
    public String estimate(@RequestParam(required = false) Integer year,
                            HttpServletRequest request, Model model) {
        int targetYear = year != null ? year : LocalDate.now().getYear();
        model.addAttribute("year", targetYear);
        var authUserId = jwtVerifier.currentUserId(request);
        if (authUserId.isEmpty()) {
            return loginRedirect("/honor/estimate");
        }
        Long userId = authUserId.get();

        List<Donation> donationsThisYear = donationService.myDonations(userId).stream()
                .filter(d -> STATUS_COMPLETED.equals(d.getCntrSttusCode()))
                .filter(d -> d.getCntrDe() != null && d.getCntrDe().startsWith(String.valueOf(targetYear)))
                .toList();

        Map<String, String> benefitsByLocgov = new LinkedHashMap<>();
        for (String locgovCode : donationsThisYear.stream().map(Donation::getCntrLocgovCode).distinct().toList()) {
            String benefit = donationService.honorBenefitOf(locgovCode);
            if (benefit != null && !benefit.isBlank()) {
                Locgov locgov = donationService.activeLocgovs().stream()
                        .filter(l -> l.getLocgovCode().equals(locgovCode)).findFirst().orElse(null);
                benefitsByLocgov.put(locgov != null ? locgov.getUpperLocgovNm() + " " + locgov.getLocgovNm() : locgovCode, benefit);
            }
        }

        model.addAttribute("userName", memberClient.fetch(userId).userName());
        model.addAttribute("totalAmount", donationsThisYear.stream()
                .map(Donation::getCntrAmt).reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add));
        model.addAttribute("taxCredit", donationService.taxCreditOf(userId, targetYear));
        model.addAttribute("donationCount", donationsThisYear.size());
        model.addAttribute("benefitsByLocgov", benefitsByLocgov);
        return "honor-estimate";
    }

    /** 마이페이지 "기부혜택증" (AS-IS mypage/honorList.html) - /honor(세액공제 계산)와는
     *  별개 화면으로, 완료된 기부로 자동 산정된 명예기부자 등급 목록을 보여준다. */
    @GetMapping("/honor/certificates")
    public String certificates(HttpServletRequest request, Model model) {
        var authUserId = jwtVerifier.currentUserId(request);
        if (authUserId.isEmpty()) {
            return loginRedirect("/honor/certificates");
        }
        model.addAttribute("certificates", donationService.myHonorCertificates(authUserId.get()));
        model.addAttribute("levelLabels", donationService.codesOf("HONOR_STD"));
        return "honor-certificates";
    }

}
