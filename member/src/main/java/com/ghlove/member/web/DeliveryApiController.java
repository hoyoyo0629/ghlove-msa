package com.ghlove.member.web;

import com.ghlove.member.domain.User;
import com.ghlove.member.domain.UserDelivery;
import com.ghlove.member.service.DeliveryService;
import com.ghlove.member.service.MemberException;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** storefront(Vue3 SPA)용 "배송지 관리" JSON API - {@link DeliveryController}(Thymeleaf)와
 * 완전히 같은 {@link DeliveryService} 로직을 JSON 요청/응답으로 감싼다. */
@RestController
@RequiredArgsConstructor
public class DeliveryApiController {

    private final DeliveryService deliveryService;

    private User requireLogin(HttpSession session) {
        return (User) session.getAttribute(AuthController.SESSION_USER_KEY);
    }

    public record DeliveryDto(Long userDeliveryId, boolean defaultFlag, String title, String userName,
                               String phone, String mobile, String post, String address, String addressDetail) {
        static DeliveryDto of(UserDelivery d) {
            return new DeliveryDto(d.getUserDeliveryId(), "Y".equals(d.getDefaultFlag()), d.getTitle(),
                    d.getUserName(), d.getPhone(), d.getMobile(), d.getZipcode(), d.getAddress(), d.getAddressDetail());
        }
    }

    @GetMapping("/api/delivery")
    public ResponseEntity<List<DeliveryDto>> list(HttpSession session) {
        User user = requireLogin(session);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(deliveryService.listOf(user.getUserId()).stream().map(DeliveryDto::of).toList());
    }

    public record DeliveryRequest(String title, String userName, String phone, String mobile, String post,
                                   String address, String addressDetail, boolean makeDefault) {
    }

    @PostMapping("/api/delivery")
    public ResponseEntity<?> create(HttpSession session, @RequestBody DeliveryRequest req) {
        User user = requireLogin(session);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            UserDelivery created = deliveryService.create(user.getUserId(), req.title(), req.userName(), req.phone(),
                    req.mobile(), req.post(), req.address(), req.addressDetail(), req.makeDefault());
            return ResponseEntity.ok(DeliveryDto.of(created));
        } catch (MemberException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/api/delivery/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, HttpSession session, @RequestBody DeliveryRequest req) {
        User user = requireLogin(session);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            UserDelivery updated = deliveryService.update(user.getUserId(), id, req.title(), req.userName(),
                    req.phone(), req.mobile(), req.post(), req.address(), req.addressDetail(), req.makeDefault());
            return ResponseEntity.ok(DeliveryDto.of(updated));
        } catch (MemberException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/api/delivery/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, HttpSession session) {
        User user = requireLogin(session);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        deliveryService.delete(user.getUserId(), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/delivery/{id}/default")
    public ResponseEntity<Void> setDefault(@PathVariable Long id, HttpSession session) {
        User user = requireLogin(session);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        deliveryService.setDefault(user.getUserId(), id);
        return ResponseEntity.noContent().build();
    }
}
