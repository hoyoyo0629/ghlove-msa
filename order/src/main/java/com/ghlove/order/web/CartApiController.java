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
