package com.ghlove.order.web;

import com.ghlove.order.service.JwtVerifier;
import com.ghlove.order.service.OrderException;
import com.ghlove.order.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * SFR-010: userId는 더 이상 요청 파라미터로 받지 않고 로그인 JWT 쿠키에서만 가져온다.
 * 주문 상세/영수확인/배송지변경은 그 주문이 실제 로그인한 회원 소유인지도 같이 검증한다
 * (다른 회원의 주문번호를 URL에 넣어 들어와도 볼 수 없게). invoice/delivery-status는
 * 판매자·운영자가 쓰는 경로라 이 라운드의 "본인 확인" 범위 밖(별도 판매자 인증 필요 -
 * 아직 이 MSA에 없음)이라 손대지 않았다.
 */
@Controller
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final JwtVerifier jwtVerifier;

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String loginRedirect(String returnPath) {
        return "redirect:http://localhost:8081/login?target=" + encode("http://localhost:8085" + returnPath);
    }

    @GetMapping("/")
    public String createForm(@RequestParam(required = false) Long itemId, Model model) {
        model.addAttribute("itemId", itemId);
        return "create";
    }

    @PostMapping("/orders")
    public String create(@RequestParam Long itemId, @RequestParam Integer quantity,
                          HttpServletRequest request, Model model) {
        var authUserId = jwtVerifier.currentUserId(request);
        if (authUserId.isEmpty()) {
            return loginRedirect("/");
        }
        try {
            var order = orderService.createOrder(authUserId.get(), itemId, quantity);
            return "redirect:/orders/" + order.getOrderId();
        } catch (OrderException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("itemId", itemId);
            return "create";
        }
    }

    @GetMapping("/orders/{orderId}")
    public String detail(@PathVariable String orderId, @RequestParam(required = false) String errorMessage,
                          HttpServletRequest request, Model model) {
        var authUserId = jwtVerifier.currentUserId(request);
        if (authUserId.isEmpty()) {
            return loginRedirect("/orders/" + orderId);
        }
        var order = orderService.detail(orderId);
        if (!authUserId.get().equals(order.getUserId())) {
            return "redirect:/my";
        }
        model.addAttribute("order", order);
        model.addAttribute("statusLabels", orderService.codesOf("ORDER_STATUS"));
        model.addAttribute("claimTypes", orderService.codesOf("CLAIM_TYPE"));
        model.addAttribute("deliveryStatusLabels", orderService.codesOf("DELIVERY_STATUS"));
        model.addAttribute("carriers", orderService.codesOf("DELIVERY_CARRIER"));
        model.addAttribute("errorMessage", errorMessage);
        return "detail";
    }

    @PostMapping("/orders/{orderId}/cancel")
    public String cancel(@PathVariable String orderId) {
        orderService.cancel(orderId);
        return "redirect:/orders/" + orderId;
    }

    @PostMapping("/orders/{orderId}/invoice")
    public String registerInvoice(@PathVariable String orderId, @RequestParam String carrierCode,
                                   @RequestParam String invoiceNo) {
        try {
            orderService.registerInvoice(orderId, carrierCode, invoiceNo);
        } catch (OrderException e) {
            return "redirect:/orders/" + orderId + "?errorMessage=" + encode(e.getMessage());
        }
        return "redirect:/orders/" + orderId;
    }

    @PostMapping("/orders/{orderId}/delivery-status")
    public String updateDeliveryStatus(@PathVariable String orderId, @RequestParam String deliveryStatus) {
        try {
            orderService.updateDeliveryStatus(orderId, deliveryStatus);
        } catch (OrderException e) {
            return "redirect:/orders/" + orderId + "?errorMessage=" + encode(e.getMessage());
        }
        return "redirect:/orders/" + orderId;
    }

    @PostMapping("/orders/{orderId}/confirm-receipt")
    public String confirmReceipt(@PathVariable String orderId, HttpServletRequest request) {
        var authUserId = jwtVerifier.currentUserId(request);
        if (authUserId.isEmpty()) {
            return loginRedirect("/orders/" + orderId);
        }
        try {
            orderService.confirmReceipt(orderId, authUserId.get());
        } catch (OrderException e) {
            return "redirect:/orders/" + orderId + "?errorMessage=" + encode(e.getMessage());
        }
        return "redirect:/orders/" + orderId;
    }

    @PostMapping("/orders/{orderId}/delivery-address")
    public String changeDeliveryAddress(@PathVariable String orderId,
                                         @RequestParam String address,
                                         @RequestParam(required = false) String addressDetail,
                                         HttpServletRequest request) {
        var authUserId = jwtVerifier.currentUserId(request);
        if (authUserId.isEmpty()) {
            return loginRedirect("/orders/" + orderId);
        }
        try {
            orderService.changeDeliveryAddress(orderId, authUserId.get(), address, addressDetail);
        } catch (OrderException e) {
            return "redirect:/orders/" + orderId + "?errorMessage=" + encode(e.getMessage());
        }
        return "redirect:/orders/" + orderId;
    }

    /** AS-IS mypage/orderList.html의 "기간/답례품명" 검색 - 현재 시드 규모가 작아
     *  인메모리 필터링으로 충분하다(donation의 receipts.html과 동일한 패턴). */
    @GetMapping("/my")
    public String myOrders(@RequestParam(required = false) String searchStartDate,
                            @RequestParam(required = false) String searchEndDate,
                            @RequestParam(required = false) String itemName,
                            HttpServletRequest request, Model model) {
        var authUserId = jwtVerifier.currentUserId(request);
        if (authUserId.isEmpty()) {
            return loginRedirect("/my");
        }
        var orders = orderService.myOrders(authUserId.get());
        java.time.LocalDate start = parseDate(searchStartDate);
        java.time.LocalDate end = parseDate(searchEndDate);
        var filtered = orders.stream()
                .filter(o -> start == null || !o.getCreatedDate().toLocalDate().isBefore(start))
                .filter(o -> end == null || !o.getCreatedDate().toLocalDate().isAfter(end))
                .filter(o -> itemName == null || itemName.isBlank()
                        || (o.getItemName() != null && o.getItemName().contains(itemName)))
                .toList();
        model.addAttribute("orders", filtered);
        model.addAttribute("statusLabels", orderService.codesOf("ORDER_STATUS"));
        model.addAttribute("searchStartDate", searchStartDate);
        model.addAttribute("searchEndDate", searchEndDate);
        model.addAttribute("itemName", itemName);
        return "my";
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
}
