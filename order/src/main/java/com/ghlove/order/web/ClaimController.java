package com.ghlove.order.web;

import com.ghlove.order.service.ClaimService;
import com.ghlove.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * 클레임(취소·반품·교환) 운영자 승인 큐. 판매자/운영자용 경로라 이 라운드의 "본인 확인" 범위
 * 밖이고(아직 이 MSA에 운영자 로그인 모델이 없다), 운영관리 프론트가 PL 그룹 결정 대기라
 * 서버렌더(claims/queue.html)로 남는다.
 *
 * <p>회원용 흐름(주문상세의 클레임 신청, 마이페이지 "취소반품교환")은 storefront Vue3 SPA
 * (OrderDetailView/MyClaimsView) + {@code /api/orders/{id}/claim}·{@code /api/claims/my}
 * (OrderMyApiController·ClaimMyApiController)로 이관되어 이 컨트롤러에서 제거됐다
 * (2026-09-17 Thymeleaf 폐기).
 */
@Controller
@RequiredArgsConstructor
public class ClaimController {

    private final ClaimService claimService;
    private final OrderService orderService;

    @GetMapping("/claims")
    public String queue(Model model) {
        model.addAttribute("pending", claimService.pending());
        model.addAttribute("approved", claimService.approved());
        model.addAttribute("claimTypeLabels", orderService.codesOf("CLAIM_TYPE"));
        return "claims/queue";
    }

    @PostMapping("/claims/{id}/approve")
    public String approve(@PathVariable Long id) {
        claimService.approve(id);
        return "redirect:/claims";
    }

    @PostMapping("/claims/{id}/reject")
    public String reject(@PathVariable Long id) {
        claimService.reject(id);
        return "redirect:/claims";
    }

    @PostMapping("/claims/{id}/complete")
    public String complete(@PathVariable Long id) {
        claimService.complete(id);
        return "redirect:/claims";
    }
}
