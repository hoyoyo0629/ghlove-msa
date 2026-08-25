package com.ghlove.order.web;

import com.ghlove.order.domain.Coupon;
import com.ghlove.order.domain.CouponIssue;
import com.ghlove.order.domain.CouponOffline;
import com.ghlove.order.domain.CouponRegular;
import com.ghlove.order.domain.CouponTargetItem;
import com.ghlove.order.domain.CouponTargetUser;
import com.ghlove.order.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 쿠폰관리 (AS-IS opmanager/coupon+coupon-regular+coupon-use) cross-service API - admin
 *  콘솔이 이 엔드포인트로 CRUD한다. */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class CouponApiController {

    private final CouponService couponService;

    @GetMapping("/coupons")
    public List<Coupon> list() {
        return couponService.list();
    }

    @GetMapping("/coupons/{id}")
    public Coupon get(@PathVariable Integer id) {
        return couponService.get(id);
    }

    @PostMapping("/coupons")
    public Coupon create(@RequestBody Coupon form) {
        return couponService.create(form);
    }

    @PutMapping("/coupons/{id}")
    public Coupon update(@PathVariable Integer id, @RequestBody Coupon form) {
        return couponService.update(id, form);
    }

    @PostMapping("/coupons/{id}/delete")
    public void delete(@PathVariable Integer id) {
        couponService.delete(id);
    }

    @PostMapping("/coupons/{id}/publish")
    public Coupon togglePublish(@PathVariable Integer id) {
        return couponService.togglePublish(id);
    }

    @GetMapping("/coupons/{id}/target-items")
    public List<CouponTargetItem> targetItems(@PathVariable Integer id) {
        return couponService.targetItemsOf(id);
    }

    @PostMapping("/coupons/{id}/target-items")
    public void addTargetItem(@PathVariable Integer id, @RequestParam Long itemId) {
        couponService.addTargetItem(id, itemId);
    }

    @PostMapping("/coupons/{id}/target-items/{itemId}/delete")
    public void removeTargetItem(@PathVariable Integer id, @PathVariable Long itemId) {
        couponService.removeTargetItem(id, itemId);
    }

    @GetMapping("/coupons/{id}/target-users")
    public List<CouponTargetUser> targetUsers(@PathVariable Integer id) {
        return couponService.targetUsersOf(id);
    }

    @PostMapping("/coupons/{id}/target-users")
    public void addTargetUser(@PathVariable Integer id, @RequestParam Long userId) {
        couponService.addTargetUser(id, userId);
    }

    @PostMapping("/coupons/{id}/target-users/{userId}/delete")
    public void removeTargetUser(@PathVariable Integer id, @PathVariable Long userId) {
        couponService.removeTargetUser(id, userId);
    }

    @GetMapping("/coupons/{id}/offline-codes")
    public List<CouponOffline> offlineCodes(@PathVariable Integer id) {
        return couponService.offlineCodesOf(id);
    }

    @PostMapping("/coupons/{id}/offline-codes")
    public List<CouponOffline> generateOfflineCodes(@PathVariable Integer id, @RequestParam(defaultValue = "10") int count) {
        return couponService.generateOfflineCodes(id, count);
    }

    @GetMapping("/coupons/{id}/usage")
    public List<CouponIssue> usage(@PathVariable Integer id) {
        return couponService.usageOf(id);
    }

    @GetMapping("/coupons/{id}/counts")
    public CouponService.CouponCounts counts(@PathVariable Integer id) {
        return couponService.countsOf(id);
    }

    // ---- 정기발행쿠폰 ----

    @GetMapping("/coupon-regular")
    public List<CouponRegular> listRegular() {
        return couponService.listRegular();
    }

    @GetMapping("/coupon-regular/{id}")
    public CouponRegular getRegular(@PathVariable Integer id) {
        return couponService.getRegular(id);
    }

    @PostMapping("/coupon-regular")
    public CouponRegular createRegular(@RequestBody CouponRegular form) {
        return couponService.createRegular(form);
    }

    @PutMapping("/coupon-regular/{id}")
    public CouponRegular updateRegular(@PathVariable Integer id, @RequestBody CouponRegular form) {
        return couponService.updateRegular(id, form);
    }

    @PostMapping("/coupon-regular/{id}/delete")
    public void deleteRegular(@PathVariable Integer id) {
        couponService.deleteRegular(id);
    }
}
