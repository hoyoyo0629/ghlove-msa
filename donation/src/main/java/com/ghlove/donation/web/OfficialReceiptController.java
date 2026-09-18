package com.ghlove.donation.web;

import com.ghlove.donation.service.DonationException;
import com.ghlove.donation.service.JwtVerifier;
import com.ghlove.donation.service.OfficialReceiptService;
import com.ghlove.donation.service.ReceiptPdfService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;

/**
 * 기부금영수증(단건, 공식) PDF 출력 - AS-IS mypage/cntrList.html "영수증 출력" →
 * receiptPrint.html(OZReport) 대체. 조회 화면(HTML)은 storefront 프린트 뷰
 * (OfficialReceiptPrintView.vue) + {@code GET /api/my/receipts/official/{cntrSn}}로 이관됐다
 * (2026-09-17 Thymeleaf 폐기). 남은 것은 직인을 서버에서 합성하는 PDF 반출과 인쇄 이력뿐이다.
 * "확인증"(다건집계, DonationMyApiController#certificate)과는 별개의 화면이다.
 */
@Controller
@RequiredArgsConstructor
public class OfficialReceiptController {

    private final OfficialReceiptService officialReceiptService;
    private final ReceiptPdfService receiptPdfService;
    private final JwtVerifier jwtVerifier;

    /**
     * 인쇄용 PDF(서버사이드 합성) - OZ Report + Fasoo DRM 대체. 직인을 서버에서 래스터에
     * 병합한 단일 이미지 PDF를 반환하고, 실제 발급이 일어났으므로 인쇄 이력도 남긴다
     * (AS-IS OZPrintCommand_OZViewer 콜백 역할을 서버가 흡수).
     */
    @GetMapping("/receipts/official/{cntrSn}/pdf")
    public ResponseEntity<byte[]> pdf(@PathVariable String cntrSn, HttpServletRequest request) {
        var authUserId = jwtVerifier.currentUserId(request);
        if (authUserId.isEmpty()) {
            return ResponseEntity.status(401).build();
        }
        try {
            byte[] pdf = receiptPdfService.render(authUserId.get(), cntrSn);
            officialReceiptService.logPrint(authUserId.get(), cntrSn);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.inline()
                    .filename("기부금영수증_" + cntrSn + ".pdf", StandardCharsets.UTF_8).build());
            headers.setCacheControl("no-store");
            return new ResponseEntity<>(pdf, headers, 200);
        } catch (DonationException e) {
            return ResponseEntity.badRequest().build();
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
