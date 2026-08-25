package com.ghlove.admin.web;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.service.GiftClient;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/** 입점업체관리 (AS-IS opmanager/seller) - gift 서비스의 OP_SELLER를 cross-service로
 *  CRUD한다. 판매자 자기서비스 로그인(mall/item/shipment 등)은 openmarket과 같은 이유로
 *  범위 밖 - 여기서는 admin이 등록/승인/정보수정만 관리한다. */
@Controller
@RequestMapping("/seller")
@RequiredArgsConstructor
public class SellerAdminController {

    private final GiftClient giftClient;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("sellers", giftClient.sellers());
        return "seller/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("seller", emptySeller());
        return "seller/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("seller", giftClient.sellerDetail(id));
        return "seller/form";
    }

    @PostMapping
    public String create(GiftClient.SellerDetail form, HttpSession session) {
        Manager manager = (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
        giftClient.createSeller(withUserId(form, manager.getUserId()));
        return "redirect:/seller";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, GiftClient.SellerDetail form, HttpSession session) {
        Manager manager = (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
        giftClient.updateSeller(id, withUserId(form, manager.getUserId()));
        return "redirect:/seller";
    }

    private GiftClient.SellerDetail withUserId(GiftClient.SellerDetail form, Long userId) {
        return new GiftClient.SellerDetail(form.sellerId(), form.communityBusinessYn(), form.sellerName(),
                form.bankName(), form.bankInName(), form.bankAccountNumber(), form.loginId(), form.password(),
                form.userName(), form.telephoneNumber(), form.phoneNumber(), form.email(), form.address(),
                form.addressDetail(), form.companyName(), form.representativeName(), form.businessNumber(),
                form.businessLocation(), form.commissionRate(), form.remittanceType(), form.statusCode(),
                form.createdDate(), userId, form.updatedDate(), userId);
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id, @RequestParam String statusCode) {
        giftClient.updateSellerStatus(id, statusCode);
        return "redirect:/seller";
    }

    private GiftClient.SellerDetail emptySeller() {
        return new GiftClient.SellerDetail(null, "N", null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null,
                "1", "0", null, null, null, null);
    }
}
