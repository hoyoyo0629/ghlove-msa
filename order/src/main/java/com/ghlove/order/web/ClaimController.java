package com.ghlove.order.web;

import com.ghlove.order.domain.Order;
import com.ghlove.order.service.ClaimException;
import com.ghlove.order.service.ClaimService;
import com.ghlove.order.service.JwtVerifier;
import com.ghlove.order.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Function;

/** /claims (운영자 승인 큐)는 판매자/운영자용 경로라 이 라운드의 "본인 확인" 범위 밖 -
 *  아직 이 MSA에 운영자 로그인 모델이 없다. /orders/{orderId}/claim(신청)과 /claims/my
 *  (마이페이지 "취소반품교환")만 본인 소유 주문/클레임인지 검증한다. */
@Controller
@RequiredArgsConstructor
public class ClaimController {

    private final ClaimService claimService;
    private final OrderService orderService;
    private final JwtVerifier jwtVerifier;

    @PostMapping("/orders/{orderId}/claim")
    public String request(@PathVariable String orderId, @RequestParam String claimType,
                           @RequestParam(required = false) String reason, HttpServletRequest httpRequest) {
        var authUserId = jwtVerifier.currentUserId(httpRequest);
        if (authUserId.isEmpty()) {
            return "redirect:http://localhost:8081/login?target="
                    + encode("http://localhost:8085/orders/" + orderId);
        }
        if (!authUserId.get().equals(orderService.detail(orderId).getUserId())) {
            return "redirect:/my";
        }
        try {
            claimService.request(orderId, claimType, reason);
        } catch (ClaimException e) {
            return "redirect:/orders/" + orderId + "?errorMessage=" + encode(e.getMessage());
        }
        return "redirect:/orders/" + orderId;
    }

    /** 마이페이지 "취소반품교환" - 본인이 신청한 클레임 목록. AS-IS mypage/orderList.html과
     *  동일하게 기간/답례품명으로 필터링한다(시드 규모가 작아 인메모리 처리). */
    @GetMapping("/claims/my")
    public String myClaims(@RequestParam(required = false) String searchStartDate,
                            @RequestParam(required = false) String searchEndDate,
                            @RequestParam(required = false) String itemName,
                            HttpServletRequest request, Model model) {
        var authUserId = jwtVerifier.currentUserId(request);
        if (authUserId.isEmpty()) {
            return "redirect:http://localhost:8081/login?target="
                    + encode("http://localhost:8085/claims/my");
        }
        var claims = claimService.myClaims(authUserId.get());
        Map<String, Order> ordersById = orderService.ordersOf(claims.stream().map(c -> c.getOrderId()).distinct().toList())
                .stream().collect(java.util.stream.Collectors.toMap(Order::getOrderId, Function.identity()));

        var start = parseDate(searchStartDate);
        var end = parseDate(searchEndDate);
        var filtered = claims.stream()
                .filter(c -> start == null || !c.getCreatedDate().toLocalDate().isBefore(start))
                .filter(c -> end == null || !c.getCreatedDate().toLocalDate().isAfter(end))
                .filter(c -> {
                    if (itemName == null || itemName.isBlank()) {
                        return true;
                    }
                    Order o = ordersById.get(c.getOrderId());
                    return o != null && o.getItemName() != null && o.getItemName().contains(itemName);
                })
                .toList();

        model.addAttribute("claims", filtered);
        model.addAttribute("ordersById", ordersById);
        model.addAttribute("claimTypeLabels", orderService.codesOf("CLAIM_TYPE"));
        model.addAttribute("claimStatusLabels", orderService.codesOf("CLAIM_STATUS"));
        model.addAttribute("searchStartDate", searchStartDate);
        model.addAttribute("searchEndDate", searchEndDate);
        model.addAttribute("itemName", itemName);
        return "claims/my";
    }

    private java.time.LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return java.time.LocalDate.parse(value);
        } catch (java.time.format.DateTimeParseException e) {
            return null;
        }
    }

    @GetMapping("/claims")
    public String queue(Model model) {
        model.addAttribute("pending", claimService.pending());
        model.addAttribute("approved", claimService.approved());
        model.addAttribute("claimTypeLabels", orderService.codesOf("CLAIM_TYPE"));
        return "claims/queue";
    }

    @PostMapping("/claims/{id}/approve")
    public String approve(@PathVariable Long id) {
        claimService.approve(id);
        return "redirect:/claims";
    }

    @PostMapping("/claims/{id}/reject")
    public String reject(@PathVariable Long id) {
        claimService.reject(id);
        return "redirect:/claims";
    }

    @PostMapping("/claims/{id}/complete")
    public String complete(@PathVariable Long id) {
        claimService.complete(id);
        return "redirect:/claims";
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
