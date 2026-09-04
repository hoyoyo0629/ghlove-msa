package com.ghlove.order.web;

import com.ghlove.order.domain.Coupon;
import com.ghlove.order.domain.CouponIssue;
import com.ghlove.order.service.CouponService;
import com.ghlove.order.service.JwtVerifier;
import com.ghlove.order.service.OrderException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** storefront(Vue3 SPA)용 회원 쿠폰함 JSON API - {@link CouponMyController}(Thymeleaf)와 완전히
 *  같은 {@link CouponService} 로직을 감싼다. */
@RestController
@RequiredArgsConstructor
public class CouponMyApiController {

    private final CouponService couponService;
    private final JwtVerifier jwtVerifier;

    private Long requireUser(HttpServletRequest request) {
        return jwtVerifier.currentUserId(request).orElse(null);
    }

    @GetMapping("/api/my/coupons")
    public ResponseEntity<?> myCoupons(HttpServletRequest request) {
        Long userId = requireUser(request);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(couponService.myIssues(userId));
    }

    @GetMapping("/api/coupons")
    public ResponseEntity<List<Coupon>> claimable(HttpServletRequest request) {
        Long userId = requireUser(request);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(couponService.claimableCoupons(userId));
    }

    @PostMapping("/api/coupons/{couponId}/claim")
    public ResponseEntity<?> claim(@PathVariable Integer couponId, HttpServletRequest request) {
        Long userId = requireUser(request);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            CouponIssue issue = couponService.claim(userId, couponId);
            return ResponseEntity.ok(issue);
        } catch (OrderException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    public record OfflineClaimRequest(String code) {
    }

    @PostMapping("/api/coupons/offline/claim")
    public ResponseEntity<?> offlineClaim(@RequestBody OfflineClaimRequest req, HttpServletRequest request) {
        Long userId = requireUser(request);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            CouponIssue issue = couponService.claimByOfflineCode(userId, req.code());
            return ResponseEntity.ok(issue);
        } catch (OrderException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
