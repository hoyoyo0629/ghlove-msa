package com.ghlove.order.web;

import com.ghlove.order.service.CouponService;
import com.ghlove.order.service.JwtVerifier;
import com.ghlove.order.service.OrderException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/** 회원용 쿠폰함 (AS-IS mypage 쿠폰 탭 + coupon 다운로드). 마이페이지 허브(member 서비스)에서
 *  "쿠폰함" 카드로 이 서비스의 /my/coupons로 링크된다. */
@Controller
@RequiredArgsConstructor
public class CouponMyController {

    private final CouponService couponService;
    private final JwtVerifier jwtVerifier;

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String loginRedirect(String returnPath) {
        return "redirect:http://localhost:8081/login?target=" + encode("http://localhost:8085" + returnPath);
    }

    /** 내 쿠폰함 - 사용가능/사용(만료포함) 쿠폰 목록. */
    @GetMapping("/my/coupons")
    public String myCoupons(HttpServletRequest request, Model model) {
        var userId = jwtVerifier.currentUserId(request);
        if (userId.isEmpty()) {
            return loginRedirect("/my/coupons");
        }
        model.addAttribute("myCoupons", couponService.myIssues(userId.get()));
        return "coupon/my";
    }

    /** 다운로드(발급) 가능한 쿠폰 목록. */
    @GetMapping("/coupons")
    public String claimable(@RequestParam(required = false) String errorMessage, HttpServletRequest request, Model model) {
        var userId = jwtVerifier.currentUserId(request);
        if (userId.isEmpty()) {
            return loginRedirect("/coupons");
        }
        model.addAttribute("errorMessage", errorMessage);
        model.addAttribute("coupons", couponService.claimableCoupons(userId.get()));
        return "coupon/claimable";
    }

    @PostMapping("/coupons/{couponId}/claim")
    public String claim(@PathVariable Integer couponId, HttpServletRequest request, Model model) {
        var userId = jwtVerifier.currentUserId(request);
        if (userId.isEmpty()) {
            return loginRedirect("/coupons");
        }
        try {
            couponService.claim(userId.get(), couponId);
            return "redirect:/my/coupons";
        } catch (OrderException e) {
            return "redirect:/coupons?errorMessage=" + encode(e.getMessage());
        }
    }

    /** 오프라인(코드입력형) 쿠폰 다운로드. */
    @GetMapping("/coupons/offline")
    public String offlineForm(HttpServletRequest request, Model model) {
        var userId = jwtVerifier.currentUserId(request);
        if (userId.isEmpty()) {
            return loginRedirect("/coupons/offline");
        }
        return "coupon/offline-claim";
    }

    @PostMapping("/coupons/offline/claim")
    public String offlineClaim(@RequestParam String code, HttpServletRequest request, Model model) {
        var userId = jwtVerifier.currentUserId(request);
        if (userId.isEmpty()) {
            return loginRedirect("/coupons/offline");
        }
        try {
            couponService.claimByOfflineCode(userId.get(), code);
            return "redirect:/my/coupons";
        } catch (OrderException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "coupon/offline-claim";
        }
    }
}
