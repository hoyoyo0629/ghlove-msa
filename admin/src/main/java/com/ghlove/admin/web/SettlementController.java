package com.ghlove.admin.web;

import com.ghlove.admin.domain.Settlement;
import com.ghlove.admin.service.CommonCodeService;
import com.ghlove.admin.service.GiftClient;
import com.ghlove.admin.service.SettlementException;
import com.ghlove.admin.service.SettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/settlements")
@RequiredArgsConstructor
public class SettlementController {

    private final SettlementService settlementService;
    private final CommonCodeService commonCodeService;
    private final GiftClient giftClient;

    @GetMapping
    public String list(Model model, @RequestParam(required = false) String errorMessage) {
        List<Settlement> settlements = settlementService.list();
        model.addAttribute("settlements", settlements);
        model.addAttribute("statusLabels", commonCodeService.labelsOf("SETTLEMENT_STATUS"));
        model.addAttribute("sellersById", sellersOf(settlements));
        model.addAttribute("errorMessage", errorMessage);
        return "settlements/list";
    }

    /** AS-IS remittance는 확정 시점에 제공자 계좌를 스냅샷해 "누구에게 입금해야 하는지"를
     *  화면에 보여줬다 - 이 프로젝트는 스냅샷 대신 gift 서비스를 실시간 조회한다(읽기전용
     *  cross-service, 실패해도 화면 자체는 깨지지 않게 GiftClient가 null로 대체). */
    private Map<Long, GiftClient.SellerInfo> sellersOf(List<Settlement> settlements) {
        Map<Long, GiftClient.SellerInfo> map = new LinkedHashMap<>();
        for (Settlement s : settlements) {
            if (s.getSellerId() != null) {
                map.computeIfAbsent(s.getSellerId(), giftClient::sellerOf);
            }
        }
        return map;
    }

    @PostMapping("/generate")
    public String generate() {
        try {
            settlementService.generate();
        } catch (SettlementException e) {
            return "redirect:/settlements?errorMessage=" + encode(e.getMessage());
        }
        return "redirect:/settlements";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, @RequestParam(required = false) String errorMessage, Model model) {
        Settlement settlement = settlementService.get(id);
        model.addAttribute("settlement", settlement);
        model.addAttribute("orders", settlementService.ordersOf(id));
        model.addAttribute("statusLabels", commonCodeService.labelsOf("SETTLEMENT_STATUS"));
        model.addAttribute("seller", settlement.getSellerId() != null ? giftClient.sellerOf(settlement.getSellerId()) : null);
        model.addAttribute("errorMessage", errorMessage);
        return "settlements/detail";
    }

    @PostMapping("/{id}/invoice")
    public String invoice(@PathVariable Long id, @RequestParam String invoiceNo) {
        try {
            settlementService.issueInvoice(id, invoiceNo);
        } catch (SettlementException e) {
            return "redirect:/settlements/" + id + "?errorMessage=" + encode(e.getMessage());
        }
        return "redirect:/settlements/" + id;
    }

    @PostMapping("/{id}/deposit")
    public String deposit(@PathVariable Long id) {
        try {
            settlementService.confirmDeposit(id);
        } catch (SettlementException e) {
            return "redirect:/settlements/" + id + "?errorMessage=" + encode(e.getMessage());
        }
        return "redirect:/settlements/" + id;
    }

    @PostMapping("/{id}/close")
    public String close(@PathVariable Long id) {
        try {
            settlementService.close(id);
        } catch (SettlementException e) {
            return "redirect:/settlements/" + id + "?errorMessage=" + encode(e.getMessage());
        }
        return "redirect:/settlements/" + id;
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
