package com.ghlove.admin.web;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.service.ManagerException;
import com.ghlove.admin.service.MenuService;
import com.ghlove.admin.service.OrderAdminClient;
import com.ghlove.admin.service.OrderReadModelService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 주문관리 콘솔 (AS-IS opmanager/order - OrderManagerController, 3700줄/70+ 엔드포인트 중
 * 검색/상세/상태변경/클레임처리큐/엑셀다운로드 핵심을 재구현). 실제 데이터/도메인효과는 order
 * 서비스에 있고(OrderAdminClient), 여기는 admin 콘솔의 로그인/RBAC과 화면만 담당한다 -
 * give-reqmng/coupon과 동일한 "쓰기 대행" cross-service 패턴.
 *
 * 지자체담당자(ROLE_ADMIN_5/6)는 MenuService.effectiveLocgovCode()로 조회범위가 소속
 * 지자체로 강제된다. 처리(상태변경/클레임승인)는 시스템/행안부담당자도 가능하게 뒀다 -
 * AS-IS 원칙(지자체담당자만 처리)대로 하면 시스템관리자 계정으로는 아무 것도 처리해볼 수
 * 없어 실사용/테스트가 막히므로 완화했다(과제 지시사항의 명시적 완화 허용 범위). 대신
 * order 서비스가 지자체담당자에 대해서는 소속 지자체 외 주문 처리를 헤더검증으로 거부한다
 * (OrderAdminService.assertLocgovAllowed) - "아무나 무인증으로 처리"라는 원래 결함만큼은
 * 절대 재발하지 않도록 그 방어선은 시스템/행안부 담당자에게도 항상 적용된다(공유시크릿
 * 자체가 없으면 order가 아예 거부).
 */
@Controller
@RequiredArgsConstructor
public class OrderAdminController {

    private final OrderAdminClient orderAdminClient;
    private final OrderReadModelService orderReadModelService;

    @GetMapping("/admin/orders")
    public String list(@RequestParam(required = false) String locgovCode,
                        @RequestParam(required = false) String orderStatus,
                        @RequestParam(required = false) String searchType,
                        @RequestParam(required = false) String keyword,
                        @RequestParam(required = false) String startDate,
                        @RequestParam(required = false) String endDate,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size,
                        HttpSession session, Model model) {
        Manager manager = manager(session);
        String effectiveLocgov = MenuService.effectiveLocgovCode(manager, locgovCode);
        OrderReadModelService.SearchResult result = orderReadModelService.search(effectiveLocgov, orderStatus,
                searchType, keyword, startDate, endDate, page, size);

        model.addAttribute("orders", result.content());
        model.addAttribute("totalCount", result.totalElements());
        model.addAttribute("totalPages", result.totalPages());
        model.addAttribute("currentPage", page);
        model.addAttribute("size", size);
        model.addAttribute("locgovCode", effectiveLocgov);
        model.addAttribute("orderStatus", orderStatus);
        model.addAttribute("searchType", searchType);
        model.addAttribute("keyword", keyword);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("locgovScoped", MenuService.isLocgovScoped(manager));
        return "order-admin/list";
    }

    @GetMapping("/admin/orders/{orderId}")
    public String detail(@PathVariable String orderId, @RequestParam(required = false) String errorMessage,
                          HttpSession session, Model model) {
        Manager manager = manager(session);
        try {
            OrderAdminClient.OrderDetail order = orderAdminClient.detail(manager, orderId);
            model.addAttribute("order", order);
            model.addAttribute("claims", orderAdminClient.claimsOf(manager, orderId));
            // errorMessage가 쿼리파라미터로 명시되지 않았으면 절대 덮어쓰지 않는다 - 처리
            // 실패 후 리다이렉트로 넘어온 RedirectAttributes flash "errorMessage"가 이미
            // Model에 채워져 있는데, 여기서 null을 다시 넣으면 그 메시지가 지워져 버린다
            // (실제로 재현해서 잡은 버그 - RBAC 거부 메시지가 화면에 전혀 안 보였다).
            if (errorMessage != null) {
                model.addAttribute("errorMessage", errorMessage);
            }
            return "order-admin/detail";
        } catch (ManagerException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "order-admin/list";
        }
    }

    @PostMapping("/admin/orders/{orderId}/memo")
    public String updateMemo(@PathVariable String orderId, @RequestParam String memo,
                              HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            orderAdminClient.updateMemo(manager(session), orderId, memo);
        } catch (ManagerException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/orders/" + orderId;
    }

    @PostMapping("/admin/orders/{orderId}/invoice")
    public String registerInvoice(@PathVariable String orderId, @RequestParam String carrierCode,
                                   @RequestParam String invoiceNo, HttpSession session,
                                   RedirectAttributes redirectAttributes) {
        try {
            orderAdminClient.registerInvoice(manager(session), orderId, carrierCode, invoiceNo);
        } catch (ManagerException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/orders/" + orderId;
    }

    @PostMapping("/admin/orders/{orderId}/delivery-status")
    public String updateDeliveryStatus(@PathVariable String orderId, @RequestParam String deliveryStatus,
                                        HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            orderAdminClient.updateDeliveryStatus(manager(session), orderId, deliveryStatus);
        } catch (ManagerException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/orders/" + orderId;
    }

    /** 목록 화면 체크박스 일괄 배송상태변경. */
    @PostMapping("/admin/orders/bulk-delivery-status")
    public String bulkDeliveryStatus(@RequestParam(required = false) List<String> orderIds,
                                      @RequestParam String deliveryStatus,
                                      @RequestParam(required = false) String returnQuery,
                                      HttpSession session, RedirectAttributes redirectAttributes) {
        if (orderIds != null && !orderIds.isEmpty()) {
            try {
                Map<String, String> result = orderAdminClient.bulkUpdateDeliveryStatus(manager(session), orderIds, deliveryStatus);
                long failCount = result.values().stream().filter(v -> !"OK".equals(v)).count();
                if (failCount > 0) {
                    redirectAttributes.addFlashAttribute("errorMessage", failCount + "건 처리 실패 (권한/상태 확인 필요)");
                }
            } catch (ManagerException e) {
                redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            }
        }
        return "redirect:/admin/orders" + (returnQuery != null && !returnQuery.isBlank() ? "?" + returnQuery : "");
    }

    @GetMapping("/admin/orders/export")
    public void export(@RequestParam(required = false) String locgovCode,
                        @RequestParam(required = false) String orderStatus,
                        @RequestParam(required = false) String searchType,
                        @RequestParam(required = false) String keyword,
                        @RequestParam(required = false) String startDate,
                        @RequestParam(required = false) String endDate,
                        @RequestParam String reason,
                        HttpSession session, HttpServletResponse response) throws IOException {
        Manager manager = manager(session);
        String effectiveLocgov = MenuService.effectiveLocgovCode(manager, locgovCode);
        try {
            byte[] csv = orderAdminClient.export(manager, effectiveLocgov, orderStatus, searchType, keyword,
                    startDate, endDate, reason);
            response.setContentType("text/csv; charset=UTF-8");
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"orders.csv\"; filename*=UTF-8''" + URLEncoder.encode("주문목록.csv", StandardCharsets.UTF_8));
            response.getOutputStream().write(csv);
        } catch (ManagerException e) {
            response.setContentType("text/plain; charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(e.getMessage());
        }
    }

    // ==================== 클레임 처리 큐 ====================

    @GetMapping("/admin/claims")
    public String claimQueue(@RequestParam(required = false) String status, HttpSession session, Model model) {
        Manager manager = manager(session);
        String effectiveLocgov = MenuService.effectiveLocgovCode(manager, null);
        List<OrderAdminClient.ClaimQueueRow> rows = orderReadModelService.claimQueue(effectiveLocgov, status);
        Map<Long, List<OrderAdminClient.ClaimMemoInfo>> memos = rows.stream()
                .collect(Collectors.toMap(OrderAdminClient.ClaimQueueRow::claimId,
                        r -> orderAdminClient.claimMemos(manager, r.claimId()), (a, b) -> a));
        model.addAttribute("claims", rows);
        model.addAttribute("memosByClaimId", memos);
        model.addAttribute("status", status);
        return "order-admin/claim-queue";
    }

    @PostMapping("/admin/claims/{claimId}/approve")
    public String approveClaim(@PathVariable Long claimId, @RequestParam(required = false) String memo,
                                HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            orderAdminClient.approveClaim(manager(session), claimId, memo);
        } catch (ManagerException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/claims";
    }

    @PostMapping("/admin/claims/{claimId}/reject")
    public String rejectClaim(@PathVariable Long claimId, @RequestParam(required = false) String memo,
                               HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            orderAdminClient.rejectClaim(manager(session), claimId, memo);
        } catch (ManagerException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/claims";
    }

    @PostMapping("/admin/claims/{claimId}/complete")
    public String completeClaim(@PathVariable Long claimId, @RequestParam(required = false) String memo,
                                 HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            orderAdminClient.completeClaim(manager(session), claimId, memo);
        } catch (ManagerException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/claims";
    }

    @PostMapping("/admin/claims/{claimId}/memo")
    public String addClaimMemo(@PathVariable Long claimId, @RequestParam String memo,
                                HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            orderAdminClient.addClaimMemo(manager(session), claimId, memo);
        } catch (ManagerException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/claims";
    }

    private static Manager manager(HttpSession session) {
        return (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
    }
}
