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
 * 주문대행 조회 (AS-IS opmanager/order/agency - OrderAgencyController). 콜센터 상담원이 고객의
 * 문의전화를 받고 "타 지자체 답례품 주문까지" 지자체 스코프 없이 전체 조회하는 전용 도구다.
 * /admin/orders(OrderAdminController)도 시스템/행안부담당자(ROLE_ADMIN_1~4)에게는 이미 전체
 * 조회를 허용하지만, 이 화면은 그 사실에 기대지 않고 명시적으로 시스템관리자 전용임을 코드로
 * 못박는다 - 지자체담당자(ROLE_ADMIN_5/6)는 OP_MENU_RIGHT를 이 메뉴에 등록하지 않아
 * AdminAuthInterceptor가 이미 차단하지만, menu_url 매칭이 어긋나는 회귀에 대비해 컨트롤러
 * 에서도 한 번 더 방어한다.
 *
 * 검색구분에 전화번호(RECEIVER_PHONE)를 추가로 지원한다 - 콜센터 상담 흐름상 고객이 자기
 * 주문번호를 모르는 경우가 대부분이라 실제로 가장 많이 쓰이는 조회 축이다.
 */
@Controller
@RequiredArgsConstructor
public class OrderAgencyAdminController {

    private final OrderAdminClient orderAdminClient;

    @GetMapping("/admin/order-agency")
    public String search(@RequestParam(required = false) String locgovCode,
                          @RequestParam(required = false) String orderStatus,
                          @RequestParam(required = false) String searchType,
                          @RequestParam(required = false) String keyword,
                          @RequestParam(required = false) String startDate,
                          @RequestParam(required = false) String endDate,
                          @RequestParam(defaultValue = "0") int page,
                          @RequestParam(defaultValue = "20") int size,
                          HttpSession session, Model model) {
        Manager manager = manager(session);
        if (!MenuService.UNRESTRICTED_ROLES.contains(manager.getAuthority())) {
            return "redirect:/admin/access-denied";
        }

        OrderAdminClient.SearchResult result = orderAdminClient.agencySearch(manager, locgovCode, orderStatus,
                searchType, keyword, startDate, endDate, page, size);

        model.addAttribute("orders", result.content());
        model.addAttribute("totalCount", result.totalElements());
        model.addAttribute("totalPages", result.totalPages());
        model.addAttribute("currentPage", page);
        model.addAttribute("size", size);
        model.addAttribute("locgovCode", locgovCode);
        model.addAttribute("orderStatus", orderStatus);
        model.addAttribute("searchType", searchType);
        model.addAttribute("keyword", keyword);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        return "order-admin/agency";
    }

    private static Manager manager(HttpSession session) {
        return (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
    }
}
