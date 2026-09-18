package com.ghlove.donation.web;

import com.ghlove.donation.service.DonationException;
import com.ghlove.donation.service.DonationService;
import com.ghlove.donation.service.JwtVerifier;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 마이페이지 "관심지자체" 추가/삭제 AJAX 엔드포인트. 목록 화면(관심지자체 관리)은 storefront
 * Vue3 SPA(InterestLocgovsView.vue) + {@code /api/my/interest-locgovs}로 이관되어 서버렌더
 * (Thymeleaf interest-locgovs.html) 흐름은 제거됐다(2026-09-17 Thymeleaf 폐기).
 *
 * <p>남은 것은 storefront(SPA :5173 - ProfileView.vue/ListSelectView.vue/InterestLocgovsView.vue)가
 * 관심지자체 칩을 추가/삭제할 때 직접 호출하는 이 크로스오리진 AJAX(add/remove)뿐이라, 두 오리진
 * 모두에 CORS를 열고 JSON으로 응답한다. SFR-010: userId는 로그인 JWT 쿠키에서만 가져온다 -
 * 이 쿠키는 어느 오리진에서 요청을 보냈든 브라우저가 동일하게 실어 보낸다(host 스코프).
 */
@Controller
@RequiredArgsConstructor
public class InterestLocgovController {

    private final DonationService donationService;
    private final JwtVerifier jwtVerifier;

    /** member 프로필 화면의 "추가" 버튼이 fetch()로 호출 (AJAX 전용). */
    @PostMapping("/interest-locgovs")
    @ResponseBody
    @CrossOrigin(origins = {"http://localhost:8081", "http://localhost:5173"}, allowCredentials = "true")
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
    @CrossOrigin(origins = {"http://localhost:8081", "http://localhost:5173"}, allowCredentials = "true")
    public ResponseEntity<?> remove(@PathVariable String locgovCode, HttpServletRequest request) {
        var authUserId = jwtVerifier.currentUserId(request);
        if (authUserId.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("message", "로그인이 필요합니다."));
        }
        donationService.removeInterestLocgov(authUserId.get(), locgovCode);
        return ResponseEntity.noContent().build();
    }
}
