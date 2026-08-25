package com.ghlove.donation.web;

import com.ghlove.donation.service.DonationException;
import com.ghlove.donation.service.DonationService;
import com.ghlove.donation.service.JwtVerifier;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 마이페이지 "관심지자체" (AS-IS mypage/intrstLocGov.html). 이 목록 화면 자체의 조회+선택삭제
 * 외에, member의 "회원 정보 수정" 화면(:8081)이 관심지자체 칩을 추가/삭제하는 크로스오리진
 * AJAX 호출도 여기서 받는다(add/remove) - 그래서 이 둘만 CORS를 열고 JSON으로 응답한다.
 * SFR-010: userId는 로그인 JWT 쿠키에서만 가져온다 - 이 쿠키는 어느 서비스 페이지에서
 * 요청을 보냈든(member의 프로필 화면 포함) 브라우저가 동일하게 자동으로 실어 보낸다
 * (쿠키 스코프는 host 기준이지 origin 기준이 아님).
 */
@Controller
@RequiredArgsConstructor
public class InterestLocgovController {

    private final DonationService donationService;
    private final JwtVerifier jwtVerifier;

    private String loginRedirect(String returnPath) {
        return "redirect:http://localhost:8081/login?target=" + encode("http://localhost:8082" + returnPath);
    }

    @GetMapping("/interest-locgovs")
    public String list(HttpServletRequest request, Model model) {
        var authUserId = jwtVerifier.currentUserId(request);
        if (authUserId.isEmpty()) {
            return loginRedirect("/interest-locgovs");
        }
        model.addAttribute("interestLocgovs", donationService.interestLocgovsOf(authUserId.get()));
        return "interest-locgovs";
    }

    /** member 프로필 화면의 "추가" 버튼이 fetch()로 호출 (AJAX 전용). */
    @PostMapping("/interest-locgovs")
    @ResponseBody
    @CrossOrigin(origins = "http://localhost:8081", allowCredentials = "true")
    public ResponseEntity<?> add(@RequestParam String locgovCode, HttpServletRequest request) {
        var authUserId = jwtVerifier.currentUserId(request);
        if (authUserId.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("message", "로그인이 필요합니다."));
        }
        try {
            String locgovName = donationService.addInterestLocgov(authUserId.get(), locgovCode);
            return ResponseEntity.ok(Map.of("locgovCode", locgovCode, "locgovName", locgovName));
        } catch (DonationException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /** member 프로필 화면의 칩 "×" 버튼이 fetch()로 호출 (AJAX 전용). */
    @PostMapping("/interest-locgovs/{locgovCode}/delete")
    @ResponseBody
    @CrossOrigin(origins = "http://localhost:8081", allowCredentials = "true")
    public ResponseEntity<?> remove(@PathVariable String locgovCode, HttpServletRequest request) {
        var authUserId = jwtVerifier.currentUserId(request);
        if (authUserId.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("message", "로그인이 필요합니다."));
        }
        donationService.removeInterestLocgov(authUserId.get(), locgovCode);
        return ResponseEntity.noContent().build();
    }

    /** AS-IS는 개별 삭제 없이 체크박스로 골라 "선택삭제"만 지원한다 (이 목록 화면 자체의 동작 - 위 단건 add/remove는 회원정보수정 화면의 AJAX용). */
    @PostMapping("/interest-locgovs/delete")
    public String removeSelected(@RequestParam(required = false) java.util.List<String> locgovCodes,
                                  HttpServletRequest request) {
        var authUserId = jwtVerifier.currentUserId(request);
        if (authUserId.isEmpty()) {
            return loginRedirect("/interest-locgovs");
        }
        if (locgovCodes != null && !locgovCodes.isEmpty()) {
            donationService.removeInterestLocgovs(authUserId.get(), locgovCodes);
        }
        return "redirect:/interest-locgovs";
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
