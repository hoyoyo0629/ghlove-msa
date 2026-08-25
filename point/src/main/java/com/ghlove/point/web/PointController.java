package com.ghlove.point.web;

import com.ghlove.point.service.JwtVerifier;
import com.ghlove.point.service.LocgovClient;
import com.ghlove.point.service.MemberClient;
import com.ghlove.point.service.PointException;
import com.ghlove.point.service.PointService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** SFR-010: userId는 더 이상 요청 파라미터로 받지 않고 로그인 JWT 쿠키에서만 가져온다. */
@Controller
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;
    private final LocgovClient locgovClient;
    private final MemberClient memberClient;
    private final JwtVerifier jwtVerifier;

    private String loginRedirect(String returnPath) {
        return "redirect:http://localhost:8081/login?target=" + encode("http://localhost:8083" + returnPath);
    }

    @GetMapping("/")
    public String myPoints(HttpServletRequest request, Model model) {
        var authUserId = jwtVerifier.currentUserId(request);
        if (authUserId.isEmpty()) {
            return loginRedirect("/");
        }
        Long userId = authUserId.get();
        var member = memberClient.fetchOrNull(userId);
        model.addAttribute("userName", member != null ? member.userName() : null);
        model.addAttribute("balance", pointService.balanceOf(userId));
        model.addAttribute("reservedAmount", pointService.reservedAmountOf(userId));
        model.addAttribute("availableBalance", pointService.availableBalanceOf(userId));
        model.addAttribute("ledger", pointService.ledgerOf(userId));
        model.addAttribute("txnTypeLabels", pointService.codesOf("PT_TXN_TYPE"));
        model.addAttribute("upcomingExpirations", pointService.upcomingExpirations(userId));

        model.addAttribute("totalEarned", pointService.totalEarnedOf(userId));
        model.addAttribute("totalUsed", pointService.totalUsedOf(userId));

        Map<String, LocgovClient.LocgovInfo> locgovsByCode = new LinkedHashMap<>();
        locgovClient.allLocgovs().forEach(l -> locgovsByCode.put(l.locgovCode(), l));
        List<PointLocgovRow> locgovSummary = pointService.ledgerSummaryByLocgov(userId).stream()
                .map(s -> {
                    LocgovClient.LocgovInfo l = locgovsByCode.get(s.locgovCode());
                    String upperNm = l != null ? l.upperLocgovNm() : null;
                    String nm = l != null ? l.locgovNm() : s.locgovCode();
                    return new PointLocgovRow(s.locgovCode(), upperNm, nm, s.earned(), s.used(), s.remaining());
                })
                .toList();
        model.addAttribute("locgovSummary", locgovSummary);
        return "my";
    }

    /** 마이페이지 "기부포인트 조회" 지자체별 집계 테이블 행. */
    public record PointLocgovRow(String locgovCode, String upperLocgovNm, String locgovNm,
                                  long earned, long used, long remaining) {
    }

    @PostMapping("/use")
    public String usePoints(@RequestParam long amount, @RequestParam(required = false) String orderCode,
                             HttpServletRequest request, Model model) {
        var authUserId = jwtVerifier.currentUserId(request);
        if (authUserId.isEmpty()) {
            return loginRedirect("/");
        }
        try {
            pointService.usePoints(authUserId.get(), amount, orderCode);
        } catch (PointException e) {
            return "redirect:/?error=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
        }
        return "redirect:/?used=success";
    }

    /** 포인트 예약 큐 (SFR-004 예약/예약해제) - 실제 order SAGA는 아직 이 API를 타지 않고
     *  직접 차감/복원 방식을 그대로 쓴다 (틀만 먼저 마련해 두는 단계). */
    @GetMapping("/reservations")
    public String reservations(@RequestParam(required = false) String errorMessage,
                                HttpServletRequest request, Model model) {
        var authUserId = jwtVerifier.currentUserId(request);
        if (authUserId.isEmpty()) {
            return loginRedirect("/reservations");
        }
        Long userId = authUserId.get();
        model.addAttribute("errorMessage", errorMessage);
        model.addAttribute("reservations", pointService.reservationsOf(userId));
        model.addAttribute("availableBalance", pointService.availableBalanceOf(userId));
        model.addAttribute("statusLabels", pointService.codesOf("PT_RESERVATION_STATUS"));
        return "reservations";
    }

    @PostMapping("/reservations")
    public String reserve(@RequestParam long amount, @RequestParam(required = false) String refKey,
                           @RequestParam(required = false) String reason, HttpServletRequest request) {
        var authUserId = jwtVerifier.currentUserId(request);
        if (authUserId.isEmpty()) {
            return loginRedirect("/reservations");
        }
        try {
            pointService.reserve(authUserId.get(), amount, refKey, reason);
        } catch (PointException e) {
            return "redirect:/reservations?errorMessage=" + encode(e.getMessage());
        }
        return "redirect:/reservations";
    }

    @PostMapping("/reservations/{id}/confirm")
    public String confirmReservation(@PathVariable Long id, HttpServletRequest request) {
        var authUserId = jwtVerifier.currentUserId(request);
        if (authUserId.isEmpty()) {
            return loginRedirect("/reservations");
        }
        if (!pointService.isReservationOwner(id, authUserId.get())) {
            return "redirect:/reservations";
        }
        try {
            pointService.confirmReservation(id);
        } catch (PointException e) {
            return "redirect:/reservations?errorMessage=" + encode(e.getMessage());
        }
        return "redirect:/reservations";
    }

    @PostMapping("/reservations/{id}/release")
    public String releaseReservation(@PathVariable Long id, HttpServletRequest request) {
        var authUserId = jwtVerifier.currentUserId(request);
        if (authUserId.isEmpty()) {
            return loginRedirect("/reservations");
        }
        if (!pointService.isReservationOwner(id, authUserId.get())) {
            return "redirect:/reservations";
        }
        try {
            pointService.releaseReservation(id);
        } catch (PointException e) {
            return "redirect:/reservations?errorMessage=" + encode(e.getMessage());
        }
        return "redirect:/reservations";
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    /** 실시간 스케줄러가 없어 운영자가 수동으로 트리거하는 소멸 배치 실행 화면. */
    @GetMapping("/batch/expire")
    public String expireBatchForm(Model model) {
        return "expire-batch";
    }

    @PostMapping("/batch/expire")
    public String runExpireBatch(Model model) {
        int count = pointService.runExpirationBatch();
        model.addAttribute("resultCount", count);
        return "expire-batch";
    }
}
