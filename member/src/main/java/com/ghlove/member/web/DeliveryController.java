package com.ghlove.member.web;

import com.ghlove.member.domain.User;
import com.ghlove.member.domain.UserDelivery;
import com.ghlove.member.service.DeliveryService;
import com.ghlove.member.service.MemberException;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/** 마이페이지 "배송지 관리" (AS-IS mypage/deliveryInfo.html). 본인 소유 배송지만
 *  조회/수정/삭제할 수 있다 (SessionRehydrateInterceptor가 GH_AUTH JWT로 세션을
 *  채워두므로, 다른 마이페이지 컨트롤러와 동일하게 HttpSession 기반으로 식별한다). */
@Controller
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;

    private User requireLogin(HttpSession session) {
        return (User) session.getAttribute(AuthController.SESSION_USER_KEY);
    }

    @GetMapping("/delivery")
    public String list(HttpSession session, Model model) {
        User loginUser = requireLogin(session);
        if (loginUser == null) {
            return AuthController.loginRedirect("/delivery");
        }
        model.addAttribute("user", loginUser);
        model.addAttribute("deliveries", deliveryService.listOf(loginUser.getUserId()));
        return "delivery/list";
    }

    @GetMapping("/delivery/write")
    public String writeForm(HttpSession session, Model model) {
        User loginUser = requireLogin(session);
        if (loginUser == null) {
            return AuthController.loginRedirect("/delivery/write");
        }
        model.addAttribute("user", loginUser);
        model.addAttribute("delivery", new UserDelivery());
        return "delivery/write";
    }

    @PostMapping("/delivery/write")
    public String write(HttpSession session, @RequestParam String title, @RequestParam String userName,
                         @RequestParam(required = false) String phone, @RequestParam String mobile,
                         @RequestParam(required = false) String post, @RequestParam String address,
                         @RequestParam(required = false) String addressDetail,
                         @RequestParam(required = false, defaultValue = "false") boolean makeDefault,
                         Model model) {
        User loginUser = requireLogin(session);
        if (loginUser == null) {
            return "redirect:/login";
        }
        try {
            deliveryService.create(loginUser.getUserId(), title, userName, phone, mobile, post, address, addressDetail, makeDefault);
            return "redirect:/delivery";
        } catch (MemberException e) {
            model.addAttribute("user", loginUser);
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("delivery", new UserDelivery());
            return "delivery/write";
        }
    }

    @GetMapping("/delivery/edit/{id}")
    public String editForm(@PathVariable Long id, HttpSession session, Model model) {
        User loginUser = requireLogin(session);
        if (loginUser == null) {
            return AuthController.loginRedirect("/delivery/edit/" + id);
        }
        try {
            model.addAttribute("user", loginUser);
            model.addAttribute("delivery", deliveryService.get(loginUser.getUserId(), id));
            return "delivery/write";
        } catch (MemberException e) {
            return "redirect:/delivery";
        }
    }

    @PostMapping("/delivery/edit/{id}")
    public String edit(@PathVariable Long id, HttpSession session, @RequestParam String title,
                        @RequestParam String userName, @RequestParam(required = false) String phone,
                        @RequestParam String mobile, @RequestParam(required = false) String post,
                        @RequestParam String address, @RequestParam(required = false) String addressDetail,
                        @RequestParam(required = false, defaultValue = "false") boolean makeDefault,
                        Model model) {
        User loginUser = requireLogin(session);
        if (loginUser == null) {
            return "redirect:/login";
        }
        try {
            deliveryService.update(loginUser.getUserId(), id, title, userName, phone, mobile, post, address, addressDetail, makeDefault);
            return "redirect:/delivery";
        } catch (MemberException e) {
            model.addAttribute("user", loginUser);
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("delivery", deliveryService.get(loginUser.getUserId(), id));
            return "delivery/write";
        }
    }

    @PostMapping("/delivery/{id}/delete")
    public String delete(@PathVariable Long id, HttpSession session) {
        User loginUser = requireLogin(session);
        if (loginUser == null) {
            return "redirect:/login";
        }
        deliveryService.delete(loginUser.getUserId(), id);
        return "redirect:/delivery";
    }

    @PostMapping("/delivery/{id}/default")
    public String setDefault(@PathVariable Long id, HttpSession session) {
        User loginUser = requireLogin(session);
        if (loginUser == null) {
            return "redirect:/login";
        }
        deliveryService.setDefault(loginUser.getUserId(), id);
        return "redirect:/delivery";
    }
}
