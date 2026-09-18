package com.ghlove.order.web;

import com.ghlove.order.service.CartService;
import com.ghlove.order.service.JwtVerifier;
import com.ghlove.order.service.OrderException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** storefront(Vue3 SPA)용 장바구니 JSON API - {@link CartController}(Thymeleaf)와 완전히 같은
 *  {@link CartService} 로직을 JSON 요청/응답으로 감싼다. */
@RestController
@RequiredArgsConstructor
public class CartApiController {

    private final CartService cartService;
    private final JwtVerifier jwtVerifier;

    private Long requireUser(HttpServletRequest request) {
        return jwtVerifier.currentUserId(request).orElse(null);
    }

    @GetMapping("/api/cart")
    public ResponseEntity<?> view(HttpServletRequest request) {
        Long userId = requireUser(request);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(cartService.view(userId));
    }

    public record AddRequest(Long itemId, Integer quantity) {
    }

    /**
     * AS-IS items/details-main.html의 "장바구니" 버튼은 화면에 머문 채 비동기로 담고 알림만
     * 띄운다. 이 MSA는 답례품 상세가 gift(8084), 장바구니가 order(8085)로 갈라져 있어 브라우저
     * 입장에서 교차 오리진 호출이 된다 - donation의 관심지자체 API가 member 화면(8081)에서
     * 불릴 때 쓰는 방식과 같이 호출 오리진만 열어준다. GH_AUTH 쿠키를 실어야 하므로
     * allowCredentials가 필요하다(쿠키 도메인이 localhost라 포트가 달라도 공유된다).
     */
    @CrossOrigin(origins = {"http://localhost:8084", "http://localhost:5173"}, allowCredentials = "true")
    @PostMapping("/api/cart/items")
    public ResponseEntity<?> add(@RequestBody AddRequest req, HttpServletRequest request) {
        Long userId = requireUser(request);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            cartService.add(userId, req.itemId(), req.quantity());
            return ResponseEntity.noContent().build();
        } catch (OrderException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    public record QuantityRequest(Integer quantity) {
    }

    @PutMapping("/api/cart/items/{cartItemId}/quantity")
    public ResponseEntity<?> updateQuantity(@PathVariable Long cartItemId, @RequestBody QuantityRequest req,
                                             HttpServletRequest request) {
        Long userId = requireUser(request);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            cartService.updateQuantity(userId, cartItemId, req.quantity());
            return ResponseEntity.noContent().build();
        } catch (OrderException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    public record DeleteRequest(List<Long> cartItemId) {
    }

    @PostMapping("/api/cart/items/delete")
    public ResponseEntity<?> remove(@RequestBody DeleteRequest req, HttpServletRequest request) {
        Long userId = requireUser(request);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        cartService.remove(userId, req.cartItemId());
        return ResponseEntity.noContent().build();
    }
}
