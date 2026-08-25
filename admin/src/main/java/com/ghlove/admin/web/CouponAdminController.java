package com.ghlove.admin.web;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.service.OrderClient;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/** 쿠폰관리 (AS-IS opmanager/coupon+coupon-regular+coupon-use) - order 서비스의
 *  OP_COUPON*를 cross-service로 CRUD한다. */
@Controller
@RequiredArgsConstructor
public class CouponAdminController {

    private final OrderClient orderClient;

    @GetMapping("/coupon")
    public String list(Model model) {
        model.addAttribute("coupons", orderClient.coupons());
        return "coupon/list";
    }

    @GetMapping("/coupon/new")
    public String createForm(Model model) {
        model.addAttribute("coupon", emptyCoupon());
        return "coupon/form";
    }

    @GetMapping("/coupon/{id}/edit")
    public String editForm(@PathVariable Integer id, Model model) {
        model.addAttribute("coupon", orderClient.coupon(id));
        return "coupon/form";
    }

    @GetMapping("/coupon/{id}/copy")
    public String copyForm(@PathVariable Integer id, Model model) {
        OrderClient.CouponDetail c = orderClient.coupon(id);
        model.addAttribute("coupon", new OrderClient.CouponDetail(null, c.couponType(), c.couponName() + " (복사본)",
                c.couponComment(), c.issueType(), c.issueStartDate(), c.issueEndDate(), c.applyType(), c.applyDay(),
                c.applyStartDate(), c.applyEndDate(), c.targetTimeType(), c.targetUserType(), c.targetUserLevel(),
                c.payRestriction(), c.concurrently(), c.payType(), c.pay(), c.discountLimitPrice(), c.targetItemType(),
                "N", c.offlineFlag(), c.birthday(), "0", c.downloadLimit(), c.downloadUserLimit(),
                c.multipleDownloadFlag(), null, null, null, "N", null));
        return "coupon/form";
    }

    @PostMapping("/coupon")
    public String create(OrderClient.CouponDetail form, HttpSession session) {
        OrderClient.CouponDetail created = orderClient.createCoupon(withUpdateUserName(form, session));
        return "redirect:/coupon/" + created.couponId() + "/edit";
    }

    @PostMapping("/coupon/{id}")
    public String update(@PathVariable Integer id, OrderClient.CouponDetail form, HttpSession session) {
        orderClient.updateCoupon(id, withUpdateUserName(form, session));
        return "redirect:/coupon";
    }

    @PostMapping("/coupon/{id}/delete")
    public String delete(@PathVariable Integer id) {
        orderClient.deleteCoupon(id);
        return "redirect:/coupon";
    }

    @PostMapping("/coupon/{id}/publish")
    public String publish(@PathVariable Integer id) {
        orderClient.togglePublishCoupon(id);
        return "redirect:/coupon";
    }

    // ---- 발급대상: 특정상품 ----

    @GetMapping("/coupon/{id}/target-items")
    public String targetItems(@PathVariable Integer id, Model model) {
        model.addAttribute("coupon", orderClient.coupon(id));
        model.addAttribute("items", orderClient.couponTargetItems(id));
        return "coupon/target-items";
    }

    @PostMapping("/coupon/{id}/target-items")
    public String addTargetItem(@PathVariable Integer id, @RequestParam Long itemId) {
        orderClient.addCouponTargetItem(id, itemId);
        return "redirect:/coupon/" + id + "/target-items";
    }

    @PostMapping("/coupon/{id}/target-items/{itemId}/delete")
    public String removeTargetItem(@PathVariable Integer id, @PathVariable Long itemId) {
        orderClient.removeCouponTargetItem(id, itemId);
        return "redirect:/coupon/" + id + "/target-items";
    }

    // ---- 발급대상: 선택회원 ----

    @GetMapping("/coupon/{id}/target-users")
    public String targetUsers(@PathVariable Integer id, Model model) {
        model.addAttribute("coupon", orderClient.coupon(id));
        model.addAttribute("users", orderClient.couponTargetUsers(id));
        return "coupon/target-users";
    }

    @PostMapping("/coupon/{id}/target-users")
    public String addTargetUser(@PathVariable Integer id, @RequestParam Long userId) {
        orderClient.addCouponTargetUser(id, userId);
        return "redirect:/coupon/" + id + "/target-users";
    }

    @PostMapping("/coupon/{id}/target-users/{userId}/delete")
    public String removeTargetUser(@PathVariable Integer id, @PathVariable Long userId) {
        orderClient.removeCouponTargetUser(id, userId);
        return "redirect:/coupon/" + id + "/target-users";
    }

    // ---- 오프라인 코드 ----

    @GetMapping("/coupon/{id}/offline-codes")
    public String offlineCodes(@PathVariable Integer id, Model model) {
        model.addAttribute("coupon", orderClient.coupon(id));
        model.addAttribute("codes", orderClient.couponOfflineCodes(id));
        return "coupon/offline-codes";
    }

    @PostMapping("/coupon/{id}/offline-codes")
    public String generateOfflineCodes(@PathVariable Integer id, @RequestParam(defaultValue = "10") int count) {
        orderClient.generateCouponOfflineCodes(id, count);
        return "redirect:/coupon/" + id + "/offline-codes";
    }

    // ---- 사용내역 ----

    @GetMapping("/coupon/{id}/usage")
    public String usage(@PathVariable Integer id, Model model) {
        model.addAttribute("coupon", orderClient.coupon(id));
        model.addAttribute("counts", orderClient.couponCounts(id));
        model.addAttribute("usageList", orderClient.couponUsage(id));
        return "coupon/usage";
    }

    // ---- 정기발행쿠폰 ----

    @GetMapping("/coupon-regular")
    public String listRegular(Model model) {
        model.addAttribute("coupons", orderClient.couponRegulars());
        return "coupon/regular-list";
    }

    @GetMapping("/coupon-regular/new")
    public String createRegularForm(Model model) {
        model.addAttribute("coupon", emptyRegular());
        return "coupon/regular-form";
    }

    @GetMapping("/coupon-regular/{id}/edit")
    public String editRegularForm(@PathVariable Integer id, Model model) {
        model.addAttribute("coupon", orderClient.couponRegular(id));
        return "coupon/regular-form";
    }

    @PostMapping("/coupon-regular")
    public String createRegular(OrderClient.CouponRegularDetail form) {
        orderClient.createCouponRegular(form);
        return "redirect:/coupon-regular";
    }

    @PostMapping("/coupon-regular/{id}")
    public String updateRegular(@PathVariable Integer id, OrderClient.CouponRegularDetail form) {
        orderClient.updateCouponRegular(id, form);
        return "redirect:/coupon-regular";
    }

    @PostMapping("/coupon-regular/{id}/delete")
    public String deleteRegular(@PathVariable Integer id) {
        orderClient.deleteCouponRegular(id);
        return "redirect:/coupon-regular";
    }

    private OrderClient.CouponDetail withUpdateUserName(OrderClient.CouponDetail form, HttpSession session) {
        Manager manager = (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
        return new OrderClient.CouponDetail(form.couponId(), form.couponType(), form.couponName(), form.couponComment(),
                form.issueType(), form.issueStartDate(), form.issueEndDate(), form.applyType(), form.applyDay(),
                form.applyStartDate(), form.applyEndDate(), form.targetTimeType(), form.targetUserType(),
                form.targetUserLevel(), form.payRestriction(), form.concurrently(), form.payType(), form.pay(),
                form.discountLimitPrice(), form.targetItemType(), form.couponFlag(), form.offlineFlag(),
                form.birthday(), form.dataStatusCode(), form.downloadLimit(), form.downloadUserLimit(),
                form.multipleDownloadFlag(), form.createdDate(), manager.getUserName(), form.updatedDate(),
                form.directInputFlag(), form.directInputValue());
    }

    private OrderClient.CouponDetail emptyCoupon() {
        return new OrderClient.CouponDetail(null, "WEB", null, null, "0", null, null, "0", null, null, null,
                "1", "1", null, -1, "1", "1", null, -1, "1", "Y", "N", null, "1", -1, -1, "N", null, null, null, "N", null);
    }

    private OrderClient.CouponRegularDetail emptyRegular() {
        return new OrderClient.CouponRegularDetail(null, "WEB", null, null, "0", null, null, "1", "1", null,
                -1, "1", "1", null, -1, "1", "Y", null, "1", -1, -1, "N", null, null, null);
    }
}
