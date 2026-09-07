package com.ghlove.admin.web;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.service.MenuService;
import com.ghlove.admin.service.OrderAdminClient;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * PG 결제현황 조회 (AS-IS opmanager/order/payinfo - PayInfoManagerController). 이 플랫폼은
 * 실제 PG(신용카드 등) 연동이 없어(답례품 구매는 기부포인트 단일 결제수단) OD_ORDER에 결제
 * 필드를 새로 추가하지 않고 이미 있는 필드(POINT_AMOUNT/ORDER_STATUS/CANCEL_REASON)를 결제
 * 관점으로 재라벨링해 보여준다(OrderAdminService.payInfoSearch 참고). "취소실패건"은 SAGA
 * 보상실패로 시스템이 자동취소한 주문(CANCEL_REASON이 "재고 부족"/"포인트 부족"으로 시작 -
 * 고객이 직접 취소한 "고객 요청 취소"와 구분)으로 매핑했다.
 */
@Controller
@RequiredArgsConstructor
public class PayInfoAdminController {

    private final OrderAdminClient orderAdminClient;

    @GetMapping("/admin/pay-info")
    public String list(@RequestParam(required = false) String locgovCode,
                        @RequestParam(defaultValue = "false") boolean failedOnly,
                        @RequestParam(required = false) String startDate,
                        @RequestParam(required = false) String endDate,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size,
                        HttpSession session, Model model) {
        Manager manager = manager(session);
        String effectiveLocgov = MenuService.effectiveLocgovCode(manager, locgovCode);
        OrderAdminClient.PayInfoResult result = orderAdminClient.payInfo(manager, effectiveLocgov, failedOnly,
                startDate, endDate, page, size);

        model.addAttribute("payments", result.content());
        model.addAttribute("totalCount", result.totalElements());
        model.addAttribute("totalPages", result.totalPages());
        model.addAttribute("currentPage", page);
        model.addAttribute("size", size);
        model.addAttribute("locgovCode", effectiveLocgov);
        model.addAttribute("failedOnly", failedOnly);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("locgovScoped", MenuService.isLocgovScoped(manager));
        return "order-admin/pay-info";
    }

    private static Manager manager(HttpSession session) {
        return (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
    }
}
