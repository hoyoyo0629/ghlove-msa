package com.ghlove.donation.web;

import com.ghlove.donation.service.DonationException;
import com.ghlove.donation.service.DonationService;
import com.ghlove.donation.service.JwtVerifier;
import com.ghlove.donation.service.MemberClient;
import com.ghlove.donation.service.ReceiptService;
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

/**
 * 기부확인증 (AS-IS 마이페이지 > 기부확인증) - 목록/필터 조회 및 확인증 보기/출력.
 * SFR-010: userId는 로그인 JWT 쿠키에서만 가져온다.
 */
@Controller
@RequiredArgsConstructor
public class ReceiptController {

    private final ReceiptService receiptService;
    private final DonationService donationService;
    private final MemberClient memberClient;
    private final JwtVerifier jwtVerifier;

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String loginRedirect(String returnPath) {
        return "redirect:http://localhost:8081/login?target=" + encode("http://localhost:8082" + returnPath);
    }

    private static final int PAGE_SIZE = 10;

    @GetMapping("/receipts")
    public String list(@RequestParam(required = false) String locgovCode,
                        @RequestParam(required = false) String upperLocgovCode,
                        @RequestParam(required = false) String searchStartDate,
                        @RequestParam(required = false) String searchEndDate,
                        @RequestParam(required = false) String errorMessage,
                        @RequestParam(defaultValue = "1") int page,
                        HttpServletRequest request, Model model) {
        var authUserId = jwtVerifier.currentUserId(request);
        if (authUserId.isEmpty()) {
            return loginRedirect("/receipts");
        }
        Long userId = authUserId.get();
        model.addAttribute("locgovCode", locgovCode);
        model.addAttribute("upperLocgovCode", upperLocgovCode);
        model.addAttribute("searchStartDate", searchStartDate);
        model.addAttribute("searchEndDate", searchEndDate);
        model.addAttribute("errorMessage", errorMessage);
        var locgovs = donationService.activeLocgovs();
        model.addAttribute("locgovs", locgovs);

        Map<String, String> provinces = new LinkedHashMap<>();
        locgovs.forEach(l -> provinces.putIfAbsent(l.getUpperLocgovCode(), l.getUpperLocgovNm()));
        model.addAttribute("provinces", provinces);

        var memberInfo = memberClient.fetchOrNull(userId);
        model.addAttribute("userName", memberInfo != null ? memberInfo.userName() : null);

        var result = receiptService.receiptList(userId, locgovCode, upperLocgovCode, searchStartDate, searchEndDate);
        model.addAttribute("totalCntrAmt", result.getTotalCntrAmt());
        model.addAttribute("totalCnt", result.getTotalCnt());

        var allRows = result.getRows();
        int totalPages = Math.max(1, (int) Math.ceil(allRows.size() / (double) PAGE_SIZE));
        int currentPage = Math.min(Math.max(page, 1), totalPages);
        int from = (currentPage - 1) * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, allRows.size());
        model.addAttribute("rows", from < to ? allRows.subList(from, to) : List.of());
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        return "receipts";
    }

    @PostMapping("/receipts/certificate")
    public String certificate(@RequestParam(required = false) List<String> cntrSn,
                               @RequestParam(defaultValue = "view") String mode,
                               HttpServletRequest request, Model model) {
        var authUserId = jwtVerifier.currentUserId(request);
        if (authUserId.isEmpty()) {
            return loginRedirect("/receipts");
        }
        Long userId = authUserId.get();
        try {
            model.addAttribute("certificate", receiptService.buildCertificate(userId, cntrSn));
            return "print".equals(mode) ? "certificate-print" : "certificate";
        } catch (DonationException e) {
            return "redirect:/receipts?errorMessage=" + encode(e.getMessage());
        }
    }
}
