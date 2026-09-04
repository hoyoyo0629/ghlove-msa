package com.ghlove.member.web;

import com.ghlove.member.domain.UserLevel;
import com.ghlove.member.service.MemberException;
import com.ghlove.member.service.UserLevelAdminService;
import com.ghlove.member.service.UserLevelIconStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * admin 콘솔 회원등급 관리 API (docs/as-is-admin-gap-deep-audit-part2.md 배치D D11) -
 * admin 서비스의 UserLevelAdminClient가 호출하는 관리자 전용 엔드포인트(AdminMemberApiController와
 * 동일한 관행 - 브라우저에 직접 노출되지 않고 admin 콘솔의 로그인+메뉴RBAC이 실제 게이트다).
 */
@RestController
@RequestMapping("/api/admin/user-levels")
@RequiredArgsConstructor
public class UserLevelAdminApiController {

    private final UserLevelAdminService userLevelAdminService;
    private final UserLevelIconStorageService iconStorageService;

    @GetMapping
    public List<UserLevel> list() {
        return userLevelAdminService.list();
    }

    @GetMapping("/{levelId}")
    public ResponseEntity<UserLevel> get(@PathVariable Integer levelId) {
        try {
            return ResponseEntity.ok(userLevelAdminService.get(levelId));
        } catch (MemberException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestParam String levelName, @RequestParam(required = false) Integer depth,
                                     @RequestParam(required = false) Integer priceStart,
                                     @RequestParam(required = false) Integer priceEnd,
                                     @RequestParam(required = false) Double discountRate,
                                     @RequestParam(required = false) Double pointRate,
                                     @RequestParam(required = false) Integer shippingCouponCount,
                                     @RequestParam(required = false) Integer retentionPeriod,
                                     @RequestParam(required = false) Integer referencePeriod,
                                     @RequestParam(required = false) Integer exceptReferencePeriod,
                                     @RequestParam(required = false) MultipartFile icon) {
        try {
            String fileName = icon != null && !icon.isEmpty() ? iconStorageService.store(icon) : null;
            UserLevel level = userLevelAdminService.create(levelName, depth, priceStart, priceEnd, discountRate,
                    pointRate, shippingCouponCount, retentionPeriod, referencePeriod, exceptReferencePeriod, fileName);
            return ResponseEntity.ok(level);
        } catch (MemberException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{levelId}")
    public ResponseEntity<?> update(@PathVariable Integer levelId, @RequestParam String levelName,
                                     @RequestParam(required = false) Integer depth,
                                     @RequestParam(required = false) Integer priceStart,
                                     @RequestParam(required = false) Integer priceEnd,
                                     @RequestParam(required = false) Double discountRate,
                                     @RequestParam(required = false) Double pointRate,
                                     @RequestParam(required = false) Integer shippingCouponCount,
                                     @RequestParam(required = false) Integer retentionPeriod,
                                     @RequestParam(required = false) Integer referencePeriod,
                                     @RequestParam(required = false) Integer exceptReferencePeriod,
                                     @RequestParam(required = false) MultipartFile icon) {
        try {
            String fileName = icon != null && !icon.isEmpty() ? iconStorageService.store(icon) : null;
            userLevelAdminService.update(levelId, levelName, depth, priceStart, priceEnd, discountRate, pointRate,
                    shippingCouponCount, retentionPeriod, referencePeriod, exceptReferencePeriod, fileName);
            return ResponseEntity.noContent().build();
        } catch (MemberException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{levelId}")
    public ResponseEntity<?> delete(@PathVariable Integer levelId) {
        try {
            userLevelAdminService.delete(levelId);
            return ResponseEntity.noContent().build();
        } catch (MemberException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
