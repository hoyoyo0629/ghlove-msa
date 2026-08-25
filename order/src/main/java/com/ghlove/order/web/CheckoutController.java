package com.ghlove.order.web;

import com.ghlove.order.service.CartService;
import com.ghlove.order.service.CouponService;
import com.ghlove.order.service.JwtVerifier;
import com.ghlove.order.service.LocgovClient;
import com.ghlove.order.service.OrderException;
import com.ghlove.order.service.OrderService;
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
import java.util.stream.Collectors;

/**
 * 주문결제/주문완료 (AS-IS order/step1.html, order/step2.html). 장바구니의 "선택 답례품
 * 주문" 버튼은 곧장 주문을 만들지 않고 먼저 이 화면(받는사람 정보 입력 + 최종 확인)으로
 * 온다 - 실제 주문 생성은 "결제하기"를 눌러야 일어난다. SFR-010: userId는 더 이상 요청
 * 파라미터로 받지 않고 로그인 JWT 쿠키에서만 가져온다 (다른 회원 주문을 조회/결제하는
 * IDOR을 막기 위해 /checkout/done도 조회한 주문이 실제 로그인한 회원 소유인지 검증한다).
 */
@Controller
@RequiredArgsConstructor
public class CheckoutController {

    private final CartService cartService;
    private final OrderService orderService;
    private final LocgovClient locgovClient;
    private final JwtVerifier jwtVerifier;
    private final CouponService couponService;

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String loginRedirect(String returnPath) {
        return "redirect:http://localhost:8081/login?target=" + encode("http://localhost:8085" + returnPath);
    }

    @PostMapping("/checkout")
    public String review(@RequestParam(required = false) List<Long> cartItemId,
                          HttpServletRequest request, Model model) {
        var authUserId = jwtVerifier.currentUserId(request);
        if (authUserId.isEmpty()) {
            return loginRedirect("/cart");
        }
        return reviewInternal(authUserId.get(), cartItemId, null, model);
    }

    private String reviewInternal(Long userId, List<Long> cartItemId, String errorMessage, Model model) {
        model.addAttribute("errorMessage", errorMessage);
        model.addAttribute("cartItemId", cartItemId);
        var groups = cartService.viewSelected(userId, cartItemId);
        if (groups.isEmpty()) {
            return "redirect:/cart?errorMessage=" + encode("답례품을 선택해 주세요.");
        }
        model.addAttribute("groups", groups);
        long totalPoint = groups.stream().mapToLong(g -> g.groupTotal()).sum();
        model.addAttribute("totalPoint", totalPoint);

        Map<Long, List<com.ghlove.order.domain.CouponIssue>> couponsByCartItem = new LinkedHashMap<>();
        for (var group : groups) {
            for (var line : group.lines()) {
                couponsByCartItem.put(line.cartItemId(), couponService.usableIssuesForItem(userId, line.itemId()));
            }
        }
        model.addAttribute("couponsByCartItem", couponsByCartItem);
        return "checkout";
    }

    @PostMapping("/checkout/complete")
    public String complete(@RequestParam(required = false) List<Long> cartItemId,
                            @RequestParam(required = false) String receiverName,
                            @RequestParam(required = false) String receiverPhone,
                            @RequestParam(required = false) String deliveryAddress,
                            @RequestParam(required = false) String deliveryAddressDetail,
                            @RequestParam(required = false) String requestNote,
                            @RequestParam(required = false) String agree,
                            HttpServletRequest request, Model model) {
        var authUserId = jwtVerifier.currentUserId(request);
        if (authUserId.isEmpty()) {
            return loginRedirect("/cart");
        }
        Long userId = authUserId.get();
        if (!"on".equals(agree) && !"true".equals(agree)) {
            return reviewInternal(userId, cartItemId, "구매 동의가 필요합니다.", model);
        }
        var deliveryInfo = new OrderService.DeliveryInfo(receiverName, receiverPhone, deliveryAddress, deliveryAddressDetail, requestNote);
        Map<Long, Integer> couponByCartItem = parseCouponSelections(request);
        try {
            List<String> orderIds = cartService.checkout(userId, cartItemId, deliveryInfo, couponByCartItem);
            String idsParam = orderIds.stream().map(CheckoutController::encode).collect(Collectors.joining(","));
            return "redirect:/checkout/done?orderIds=" + idsParam;
        } catch (OrderException e) {
            return reviewInternal(userId, cartItemId, e.getMessage(), model);
        }
    }

    /** 체크아웃 화면의 장바구니행별 쿠폰 선택 - name="couponIssueId_{cartItemId}" 형태로 넘어온다
     *  (행마다 독립적인 select라 표준 Map 바인딩 대신 파라미터명 규칙으로 직접 파싱). */
    private Map<Long, Integer> parseCouponSelections(HttpServletRequest request) {
        Map<Long, Integer> result = new LinkedHashMap<>();
        request.getParameterMap().forEach((name, values) -> {
            if (name.startsWith("couponIssueId_") && values.length > 0 && !values[0].isBlank()) {
                try {
                    Long cartItemId = Long.parseLong(name.substring("couponIssueId_".length()));
                    result.put(cartItemId, Integer.parseInt(values[0]));
                } catch (NumberFormatException ignored) {
                    // 잘못된 파라미터는 무시(쿠폰 미적용으로 진행)
                }
            }
        });
        return result;
    }

    @GetMapping("/checkout/done")
    public String done(@RequestParam String orderIds, HttpServletRequest request, Model model) {
        var authUserId = jwtVerifier.currentUserId(request);
        if (authUserId.isEmpty()) {
            return loginRedirect("/cart");
        }
        List<String> ids = List.of(orderIds.split(","));
        // 남의 주문번호를 URL에 넣어 들어와도 못 보게: 실제 로그인한 회원 소유 주문만 남긴다.
        var orders = orderService.ordersOf(ids).stream()
                .filter(o -> authUserId.get().equals(o.getUserId()))
                .toList();
        if (orders.isEmpty()) {
            return "redirect:/cart";
        }

        Map<String, List<com.ghlove.order.domain.Order>> byLocgov = new LinkedHashMap<>();
        for (var order : orders) {
            String code = order.getLocgovCode() != null ? order.getLocgovCode() : "";
            byLocgov.computeIfAbsent(code, k -> new java.util.ArrayList<>()).add(order);
        }
        List<OrderGroupView> groups = byLocgov.entrySet().stream()
                .map(e -> new OrderGroupView(
                        e.getKey().isEmpty() ? "지자체 미지정" : locgovClient.nameOf(e.getKey()),
                        e.getValue(),
                        e.getValue().stream().mapToLong(com.ghlove.order.domain.Order::getPointAmount).sum()))
                .toList();

        model.addAttribute("groups", groups);
        model.addAttribute("totalPoint", orders.stream().mapToLong(com.ghlove.order.domain.Order::getPointAmount).sum());
        return "order-complete";
    }

    /** 주문완료 화면의 지자체별 그룹. */
    public record OrderGroupView(String locgovNm, List<com.ghlove.order.domain.Order> orders, long groupTotal) {
    }
}
