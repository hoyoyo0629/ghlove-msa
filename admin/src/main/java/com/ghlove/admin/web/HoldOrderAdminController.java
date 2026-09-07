package com.ghlove.admin.web;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.service.ManagerException;
import com.ghlove.admin.service.MenuService;
import com.ghlove.admin.service.OrderAdminClient;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 보류주문 처리 (AS-IS opmanager/order/temp - TempProcessManagerController). 배송 시작 전
 * 주문을 운영자가 일시적으로 "보류" 표시해두는 큐 - 재고/결제 등 정상 SAGA 흐름과는 별개인
 * 순수 운영 플래그다(주문상태 자체는 건드리지 않는다, OrderAdminService.hold/release 참고).
 * 지자체담당자(ROLE_ADMIN_5/6)는 소속 지자체 주문만 보류/해제할 수 있다 - order 서비스의
 * assertLocgovAllowed가 최종 방어선.
 */
@Controller
@RequestMapping("/admin/orders/hold")
@RequiredArgsConstructor
public class HoldOrderAdminController {

    private final OrderAdminClient orderAdminClient;

    @GetMapping
    public String queue(@RequestParam(required = false) String locgovCode,
                         @RequestParam(defaultValue = "0") int page,
                         @RequestParam(defaultValue = "20") int size,
                         @RequestParam(required = false) String errorMessage,
                         @RequestParam(required = false) String orderId,
                         HttpSession session, Model model) {
        Manager manager = manager(session);
        String effectiveLocgov = MenuService.effectiveLocgovCode(manager, locgovCode);
        OrderAdminClient.SearchResult result = orderAdminClient.heldOrders(manager, effectiveLocgov, page, size);

        model.addAttribute("orders", result.content());
        model.addAttribute("totalCount", result.totalElements());
        model.addAttribute("totalPages", result.totalPages());
        model.addAttribute("currentPage", page);
        model.addAttribute("size", size);
        model.addAttribute("locgovCode", effectiveLocgov);
        model.addAttribute("locgovScoped", MenuService.isLocgovScoped(manager));
        model.addAttribute("errorMessage", errorMessage);

        // 주문번호로 바로 보류처리하기 위한 조회 - 주문번호를 입력하면 상세를 미리 보여준다.
        if (orderId != null && !orderId.isBlank()) {
            try {
                model.addAttribute("lookupOrder", orderAdminClient.detail(manager, orderId.trim()));
            } catch (ManagerException e) {
                model.addAttribute("lookupError", e.getMessage());
            }
            model.addAttribute("orderId", orderId.trim());
        }
        return "order-admin/hold";
    }

    @PostMapping("/{orderId}")
    public String hold(@PathVariable String orderId, @RequestParam String reason,
                        HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            orderAdminClient.hold(manager(session), orderId, reason);
        } catch (ManagerException e) {
            return "redirect:/admin/orders/hold?orderId=" + orderId + "&errorMessage=" + encode(e.getMessage());
        }
        return "redirect:/admin/orders/hold";
    }

    @PostMapping("/{orderId}/release")
    public String release(@PathVariable String orderId, HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            orderAdminClient.release(manager(session), orderId);
        } catch (ManagerException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/orders/hold";
    }

    private static String encode(String value) {
        return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8);
    }

    private static Manager manager(HttpSession session) {
        return (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
    }
}
