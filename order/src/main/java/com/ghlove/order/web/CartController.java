package com.ghlove.order.web;

import com.ghlove.order.service.CartService;
import com.ghlove.order.service.CategoryClient;
import com.ghlove.order.service.JwtVerifier;
import com.ghlove.order.service.LocgovClient;
import com.ghlove.order.service.LocgovInfo;
import com.ghlove.order.service.LocgovMapProvince;
import com.ghlove.order.service.OrderException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 장바구니 (AS-IS cart/index.html). SFR-010: 이전에는 ?userId= 쿼리파라미터를 그대로
 * 신뢰했지만(IDOR), 이제 member 로그인 시 발급된 공유 JWT 쿠키(JwtVerifier)로 실제 로그인한
 * 회원만 자신의 장바구니를 볼 수 있다 - 로그인 안 돼 있으면 member의 로그인 화면으로 보낸다.
 */
@Controller
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final CategoryClient categoryClient;
    private final LocgovClient locgovClient;
    private final JwtVerifier jwtVerifier;

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String loginRedirect(String returnPath) {
        return "redirect:http://localhost:8081/login?target=" + encode("http://localhost:8085" + returnPath);
    }

    @GetMapping("/cart")
    public String view(@RequestParam(required = false) String errorMessage,
                        HttpServletRequest request, Model model) {
        var authUserId = jwtVerifier.currentUserId(request);
        if (authUserId.isEmpty()) {
            return loginRedirect("/cart");
        }
        Long userId = authUserId.get();
        model.addAttribute("userId", userId);
        model.addAttribute("errorMessage", errorMessage);
        model.addAttribute("categoryGroups", categoryClient.categoryGroups());
        model.addAttribute("mapProvinces", LocgovMapProvince.ALL);
        // donation의 G_LOCGOV에는 초기 테스트 시드(예: 11230)와 이후 실제 행정구역 전량
        // 시드(예: 11680)가 같은 시/군/구를 서로 다른 LOCGOV_CODE로 중복 보유하는 레거시
        // 데이터가 섞여있다 - 지도 그리드에 같은 구/군 이름이 두 번 뜨지 않도록 시/도
        // 그룹 안에서 이름 기준으로 중복 제거한다(먼저 나온 코드를 채택).
        model.addAttribute("locgovsByProvince", locgovClient.allLocgovs().stream()
                .collect(Collectors.groupingBy(LocgovInfo::upperLocgovCode,
                        Collectors.collectingAndThen(
                                Collectors.toMap(LocgovInfo::locgovNm, Function.identity(), (a, b) -> a,
                                        java.util.LinkedHashMap::new),
                                m -> List.copyOf(m.values())))));
        model.addAttribute("groups", cartService.view(userId));
        return "cart";
    }

    /** 답례품 상세화면(gift 서비스)의 "장바구니에 담기" 버튼이 호출한다. */
    @PostMapping("/cart/items")
    public String add(@RequestParam Long itemId, @RequestParam(defaultValue = "1") Integer quantity,
                       HttpServletRequest request) {
        var authUserId = jwtVerifier.currentUserId(request);
        if (authUserId.isEmpty()) {
            return loginRedirect("/cart");
        }
        Long userId = authUserId.get();
        try {
            cartService.add(userId, itemId, quantity);
        } catch (OrderException e) {
            return "redirect:/cart?errorMessage=" + encode(e.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/cart/items/{cartItemId}/quantity")
    public String updateQuantity(@PathVariable Long cartItemId, @RequestParam Integer quantity,
                                  HttpServletRequest request) {
        var authUserId = jwtVerifier.currentUserId(request);
        if (authUserId.isEmpty()) {
            return loginRedirect("/cart");
        }
        try {
            cartService.updateQuantity(authUserId.get(), cartItemId, quantity);
        } catch (OrderException e) {
            return "redirect:/cart?errorMessage=" + encode(e.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/cart/items/delete")
    public String remove(@RequestParam(required = false) List<Long> cartItemId, HttpServletRequest request) {
        var authUserId = jwtVerifier.currentUserId(request);
        if (authUserId.isEmpty()) {
            return loginRedirect("/cart");
        }
        cartService.remove(authUserId.get(), cartItemId);
        return "redirect:/cart";
    }

}
