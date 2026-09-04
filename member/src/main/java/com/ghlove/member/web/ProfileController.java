package com.ghlove.member.web;

import com.ghlove.member.domain.User;
import com.ghlove.member.service.DonationClient;
import com.ghlove.member.service.MemberException;
import com.ghlove.member.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.LinkedHashMap;
import java.util.Map;

/** 마이페이지 "회원 정보 수정" (AS-IS users/modify.html). */
@Controller
@RequiredArgsConstructor
public class ProfileController {

    private final MemberService memberService;
    private final DonationClient donationClient;

    @GetMapping("/profile")
    public String view(HttpSession session, @RequestParam(required = false) String errorMessage, Model model) {
        User loginUser = (User) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (loginUser == null) {
            return AuthController.loginRedirect("/profile");
        }
        model.addAttribute("user", loginUser);
        model.addAttribute("userTypeLabel", memberService.codesOf("USER_TYPE").get(loginUser.getSbscrbSeCode()));
        var profile = memberService.profileOf(loginUser.getUserId());
        model.addAttribute("profile", profile);
        model.addAttribute("addressRegion", addressRegionOf(profile.getAddress()));
        model.addAttribute("interestLocgovs", donationClient.interestLocgovsOf(loginUser.getUserId()));

        var locgovs = donationClient.allLocgovs();
        model.addAttribute("allLocgovs", locgovs);
        Map<String, String> provinces = new LinkedHashMap<>();
        locgovs.forEach(l -> provinces.putIfAbsent(l.upperLocgovCode(), l.upperLocgovNm()));
        model.addAttribute("provinces", provinces);

        model.addAttribute("errorMessage", errorMessage);
        return "profile";
    }

    /** SFR-002 "다중 인증체계(MFA) 선택 적용" - 회원이 마이페이지에서 스스로 켜고 끈다. */
    @PostMapping("/profile/mfa")
    public String setMfa(HttpSession session, @RequestParam boolean enabled) {
        User loginUser = (User) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (loginUser == null) {
            return "redirect:/login";
        }
        memberService.setMfaEnabled(loginUser.getUserId(), enabled);
        loginUser.setMfaEnabled(enabled ? "Y" : "N");
        session.setAttribute(AuthController.SESSION_USER_KEY, loginUser);
        return "redirect:/profile";
    }

    /**
     * AS-IS users/modify.html의 "개인정보 수정"(이름/생년월일 변경) - Siren24 PCC 본인인증
     * 팝업을 통과해야만 열리는 화면이다. 이 MSA는 그 유료 실명인증 외부연동이 없고, 검증
     * 안 된 요청으로 실명·생년월일이 바뀌면 안 되므로(software 개발보안 원칙) AS-IS의
     * 안내 화면/문구만 재현하고 실제 변경 폼은 열지 않는다.
     */
    @GetMapping("/profile/personal-info")
    public String personalInfo(HttpSession session, Model model) {
        User loginUser = (User) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (loginUser == null) {
            return AuthController.loginRedirect("/profile/personal-info");
        }
        return "personal-info";
    }

    @PostMapping("/profile")
    public String update(HttpSession session, HttpServletRequest request,
                          @RequestParam String phoneNumber, @RequestParam String email,
                          @RequestParam(required = false) String post,
                          @RequestParam(required = false) String address,
                          @RequestParam(required = false) String addressDetail,
                          @RequestParam(required = false, defaultValue = "false") boolean receiveEmail,
                          @RequestParam(required = false, defaultValue = "false") boolean receiveSms,
                          @RequestParam(required = false, defaultValue = "false") boolean receiveKakao) {
        User loginUser = (User) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (loginUser == null) {
            return "redirect:/login";
        }
        try {
            memberService.updateProfile(loginUser.getUserId(), phoneNumber, email, post, address, addressDetail,
                    receiveEmail, receiveSms, receiveKakao, request.getRemoteAddr());
        } catch (MemberException e) {
            return "redirect:/profile?errorMessage="
                    + java.net.URLEncoder.encode(e.getMessage(), java.nio.charset.StandardCharsets.UTF_8);
        }
        return "redirect:/profile";
    }

    /**
     * AS-IS는 우편번호 검색(Daum API) 결과에 딸려오는 시도/시군구 코드로 이 배너를 채우지만,
     * 이 MSA는 그 외부연동이 없어 저장된 주소 문자열의 앞 두 어절("경기도 남양주시" 등)로
     * 대신한다.
     */
    private String addressRegionOf(String address) {
        if (address == null || address.isBlank()) {
            return null;
        }
        String[] tokens = address.trim().split("\\s+");
        if (tokens.length == 1) {
            return tokens[0];
        }
        return tokens[0] + " " + tokens[1];
    }
}
