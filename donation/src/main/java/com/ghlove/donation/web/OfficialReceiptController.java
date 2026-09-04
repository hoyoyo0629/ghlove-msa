package com.ghlove.donation.web;

import com.ghlove.donation.service.DonationException;
import com.ghlove.donation.service.JwtVerifier;
import com.ghlove.donation.service.OfficialReceiptService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 기부금영수증(단건, 공식) 출력 - AS-IS mypage/cntrList.html "영수증 출력" →
 * receiptPrint.html(OZReport) 대체. "확인증"(ReceiptController, 다건집계)과는
 * 별개의 화면이다.
 */
@Controller
@RequiredArgsConstructor
public class OfficialReceiptController {

    private final OfficialReceiptService officialReceiptService;
    private final JwtVerifier jwtVerifier;

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    @GetMapping("/receipts/official/{cntrSn}")
    public String view(@PathVariable String cntrSn, HttpServletRequest request, Model model) {
        var authUserId = jwtVerifier.currentUserId(request);
        if (authUserId.isEmpty()) {
            return "redirect:http://localhost:8081/login?target="
                    + encode("http://localhost:8082/receipts/official/" + cntrSn);
        }
        try {
            model.addAttribute("receipt", officialReceiptService.build(authUserId.get(), cntrSn));
            model.addAttribute("cntrSn", cntrSn);
            return "receipt-official-print";
        } catch (DonationException e) {
            return "redirect:/mypage/donations?errorMessage=" + encode(e.getMessage());
        }
    }

    /** AS-IS OZPrintCommand_OZViewer 콜백(receipt-print/insert)에 해당 - 실제 인쇄 직전 JS가 호출한다. */
    @PostMapping("/receipts/official/{cntrSn}/print-log")
    @ResponseBody
    public ResponseEntity<Void> printLog(@PathVariable String cntrSn, HttpServletRequest request) {
        var authUserId = jwtVerifier.currentUserId(request);
        if (authUserId.isEmpty()) {
            return ResponseEntity.status(401).build();
        }
        try {
            officialReceiptService.logPrint(authUserId.get(), cntrSn);
            return ResponseEntity.noContent().build();
        } catch (DonationException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
