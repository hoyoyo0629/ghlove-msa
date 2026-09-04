package com.ghlove.admin.web;

import com.ghlove.admin.service.ManagerException;
import com.ghlove.admin.service.UserLevelAdminClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * D11 회원등급 관리 (AS-IS UserLevelManagerController, docs/as-is-admin-gap-deep-audit-part2.md
 * D11 참고). 실제 데이터는 member 서비스에 있고(UserLevelAdminClient), 여기는 admin 콘솔의
 * 로그인/RBAC과 화면만 담당한다(MemberAdminController와 동일한 패턴).
 */
@Controller
@RequestMapping("/admin/user-levels")
@RequiredArgsConstructor
public class UserLevelAdminController {

    private final UserLevelAdminClient client;

    @GetMapping
    public String list(@RequestParam(required = false) String errorMessage, Model model) {
        model.addAttribute("levels", client.list());
        model.addAttribute("errorMessage", errorMessage);
        return "user-level-admin/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("level", null);
        return "user-level-admin/form";
    }

    @PostMapping
    public String create(@RequestParam String levelName, @RequestParam(required = false) Integer depth,
                          @RequestParam(required = false) Integer priceStart, @RequestParam(required = false) Integer priceEnd,
                          @RequestParam(required = false) Double discountRate, @RequestParam(required = false) Double pointRate,
                          @RequestParam(required = false) Integer shippingCouponCount,
                          @RequestParam(required = false) Integer retentionPeriod,
                          @RequestParam(required = false) Integer referencePeriod,
                          @RequestParam(required = false) Integer exceptReferencePeriod,
                          @RequestParam(required = false) MultipartFile icon, Model model) {
        try {
            client.create(levelName, depth, priceStart, priceEnd, discountRate, pointRate, shippingCouponCount,
                    retentionPeriod, referencePeriod, exceptReferencePeriod, icon);
            return "redirect:/admin/user-levels";
        } catch (ManagerException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("level", null);
            return "user-level-admin/form";
        }
    }

    @GetMapping("/{levelId}/edit")
    public String editForm(@PathVariable Integer levelId, Model model) {
        model.addAttribute("level", client.get(levelId));
        return "user-level-admin/form";
    }

    @PostMapping("/{levelId}")
    public String update(@PathVariable Integer levelId, @RequestParam String levelName,
                          @RequestParam(required = false) Integer depth,
                          @RequestParam(required = false) Integer priceStart, @RequestParam(required = false) Integer priceEnd,
                          @RequestParam(required = false) Double discountRate, @RequestParam(required = false) Double pointRate,
                          @RequestParam(required = false) Integer shippingCouponCount,
                          @RequestParam(required = false) Integer retentionPeriod,
                          @RequestParam(required = false) Integer referencePeriod,
                          @RequestParam(required = false) Integer exceptReferencePeriod,
                          @RequestParam(required = false) MultipartFile icon, Model model) {
        try {
            client.update(levelId, levelName, depth, priceStart, priceEnd, discountRate, pointRate,
                    shippingCouponCount, retentionPeriod, referencePeriod, exceptReferencePeriod, icon);
            return "redirect:/admin/user-levels";
        } catch (ManagerException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("level", client.get(levelId));
            return "user-level-admin/form";
        }
    }

    @PostMapping("/{levelId}/delete")
    public String delete(@PathVariable Integer levelId, RedirectAttributes redirectAttributes) {
        try {
            client.delete(levelId);
        } catch (ManagerException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/user-levels";
    }
}
