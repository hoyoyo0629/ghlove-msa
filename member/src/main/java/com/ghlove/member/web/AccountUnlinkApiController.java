package com.ghlove.member.web;

import com.ghlove.member.domain.User;
import com.ghlove.member.service.AuthCookieSupport;
import com.ghlove.member.service.MemberException;
import com.ghlove.member.service.MemberService;
import com.ghlove.member.service.integration.KakaoCertClient;
import com.ghlove.member.service.integration.OnePassClient;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 디지털원패스/카카오 연동해지 (AS-IS AuthController#onepassCancel, KakaoLinkController#kakaoLinkSecede/
 * kakaoLinkClear). storefront '회원정보수정'의 연동해지 버튼과 연동해지 전용 탈퇴화면이 호출한다.
 *
 * <p>외부 연계해지 API(원패스 InterLockRelease / 카카오 톡키트 unlink)는 실연계가 열려야 실제로
 * 호출되고, 지금은 로그인과 마찬가지로 꺼져 있어 각 client가 통과시킨다 - 내부 탈퇴 처리는 항상
 * 수행된다. 응답 형태는 AS-IS 프론트가 읽던 그대로 맞춘다(info.value / result / errMsg).
 * 자세한 매핑은 docs/member-social-unlink-parity-audit.md.
 */
@RestController
@RequiredArgsConstructor
public class AccountUnlinkApiController {

    private final MemberService memberService;
    private final AuthCookieSupport authCookieSupport;
    private final OnePassClient onePassClient;
    private final KakaoCertClient kakaoCertClient;

    private User requireLogin(HttpSession session) {
        return (User) session.getAttribute(AuthController.SESSION_USER_KEY);
    }

    public record SecedeRequest(String leaveCode, String leaveReason) {
    }

    /** AS-IS /api/auth/onepass-cancel - 원패스 연동해지 후 탈퇴. */
    @PostMapping("/api/auth/onepass-cancel")
    public ResponseEntity<Map<String, Object>> onepassCancel(HttpSession session, HttpServletRequest request,
                                                              HttpServletResponse response,
                                                              @RequestBody SecedeRequest req) {
        User user = requireLogin(session);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        if (req.leaveCode() == null || req.leaveCode().isBlank()) {
            return ResponseEntity.ok(Map.<String, Object>of("info",
                    Map.of("value", "01", "message", "탈퇴사유를 선택해 주세요")));
        }
        if (!onePassClient.releaseInterlock()) {
            return ResponseEntity.ok(Map.<String, Object>of("info",
                    Map.of("value", "01", "message", "디지털원패스 연동해지 할 수 없는 상태입니다.")));
        }
        try {
            memberService.secedeExternal(user.getUserId(), req.leaveCode(), req.leaveReason(), request.getRemoteAddr());
        } catch (MemberException e) {
            return ResponseEntity.ok(Map.<String, Object>of("info",
                    Map.of("value", "01", "message", e.getMessage())));
        }
        session.invalidate();
        authCookieSupport.clear(request, response);
        return ResponseEntity.ok(Map.<String, Object>of("info",
                Map.of("value", "00", "message", "디지털원패스 연동해지가 완료되었습니다.")));
    }

    /** AS-IS /api/kakao-link/kakao-link-clear - 카카오 가입(500)이면 CHECK_SECEDE(탈퇴 동반),
     *  아이디/원패스 가입 후 부가연동이면 유저키만 지워 SUCCESS(연동만 해제). */
    @PostMapping("/api/kakao-link/kakao-link-clear")
    public ResponseEntity<Map<String, Object>> kakaoLinkClear(HttpSession session) {
        User user = requireLogin(session);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        MemberService.KakaoLinkClearResult result = memberService.clearKakaoLink(user.getUserId());
        return switch (result) {
            case NOT_VALID_LOGIN -> ResponseEntity.ok(Map.<String, Object>of("errMsg", "올바른 접근 경로가 아닙니다."));
            case CHECK_SECEDE -> ResponseEntity.ok(Map.<String, Object>of("result", "CHECK_SECEDE"));
            case SUCCESS -> {
                kakaoCertClient.unlink(user.getKakaoUserKey());
                yield ResponseEntity.ok(Map.<String, Object>of("result", "SUCCESS"));
            }
        };
    }

    /** AS-IS /api/kakao-link/kakao-link-secede - 카카오 연동해제 후 탈퇴. */
    @PostMapping("/api/kakao-link/kakao-link-secede")
    public ResponseEntity<Map<String, Object>> kakaoLinkSecede(HttpSession session, HttpServletRequest request,
                                                               HttpServletResponse response,
                                                               @RequestBody SecedeRequest req) {
        User user = requireLogin(session);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        if (req.leaveCode() == null || req.leaveCode().isBlank()) {
            return ResponseEntity.ok(Map.<String, Object>of("errMsg", "탈퇴사유를 선택해 주세요"));
        }
        if (!kakaoCertClient.unlink(user.getKakaoUserKey())) {
            return ResponseEntity.ok(Map.<String, Object>of("errMsg", "카카오 연동해제에 실패했습니다."));
        }
        try {
            memberService.secedeExternal(user.getUserId(), req.leaveCode(), req.leaveReason(), request.getRemoteAddr());
        } catch (MemberException e) {
            return ResponseEntity.ok(Map.<String, Object>of("errMsg", e.getMessage()));
        }
        session.invalidate();
        authCookieSupport.clear(request, response);
        return ResponseEntity.ok(Map.<String, Object>of("result", "SUCCESS"));
    }
}
