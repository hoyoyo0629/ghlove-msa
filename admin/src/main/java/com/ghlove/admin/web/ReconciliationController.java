package com.ghlove.admin.web;

import com.ghlove.admin.service.ReconciliationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** 주문금액-포인트사용 대사 (AS-IS view_rc_order_amt_vs_point_use_check 재구현). */
@Controller
@RequiredArgsConstructor
public class ReconciliationController {

    private final ReconciliationService reconciliationService;

    @GetMapping("/reconciliation/order-point")
    public String orderPoint(Model model) {
        var rows = reconciliationService.orderPointReconciliation();
        model.addAttribute("rows", rows);
        model.addAttribute("mismatchCount", rows.stream().filter(r -> !r.matched()).count());
        return "reconciliation/order-point";
    }
}
