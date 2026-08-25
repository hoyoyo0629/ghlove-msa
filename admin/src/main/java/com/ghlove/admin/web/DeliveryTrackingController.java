package com.ghlove.admin.web;

import com.ghlove.admin.service.CommonCodeService;
import com.ghlove.admin.service.integration.SmartDeliveryClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** 스마트택배 배송조회 (택배사 연계는 SFR-006 스펙상 admin 담당 - order는 상태값만 가짐). */
@Controller
@RequiredArgsConstructor
public class DeliveryTrackingController {

    private final SmartDeliveryClient smartDeliveryClient;
    private final CommonCodeService commonCodeService;

    @GetMapping("/delivery-tracking")
    public String track(@RequestParam(required = false) String carrierCode,
                         @RequestParam(required = false) String invoiceNo,
                         @RequestParam(required = false) String errorMessage, Model model) {
        model.addAttribute("carriers", commonCodeService.labelsOf("DELIVERY_CARRIER"));
        model.addAttribute("carrierCode", carrierCode);
        model.addAttribute("invoiceNo", invoiceNo);
        model.addAttribute("errorMessage", errorMessage);
        if (carrierCode != null && !carrierCode.isBlank() && invoiceNo != null && !invoiceNo.isBlank()) {
            try {
                model.addAttribute("result", smartDeliveryClient.track(carrierCode, invoiceNo));
            } catch (RuntimeException e) {
                model.addAttribute("errorMessage", e.getMessage());
            }
        }
        return "delivery-tracking";
    }
}
