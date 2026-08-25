package com.ghlove.donation.web;

import com.ghlove.donation.service.DonationException;
import com.ghlove.donation.service.DonationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

/** 기탁서(오프라인) 기부 등록 (SFR-003) - 우편/방문 등으로 접수된 기부를 운영자가 대신 등록. */
@Controller
@RequiredArgsConstructor
public class OfflineDonationController {

    private final DonationService donationService;

    @GetMapping("/donations/offline")
    public String form(Model model) {
        model.addAttribute("locgovs", donationService.activeLocgovs());
        return "offline";
    }

    @PostMapping("/donations/offline")
    public String register(@RequestParam Long userId, @RequestParam String locgovCode,
                            @RequestParam BigDecimal amount,
                            @RequestParam(required = false) String rceptBankCode,
                            @RequestParam(required = false) String rceptBankNm, Model model) {
        try {
            var donation = donationService.registerOfflineDonation(userId, locgovCode, amount, rceptBankCode, rceptBankNm);
            return "redirect:/my?userId=" + userId + "&offlineRegistered=" + donation.getCntrSn();
        } catch (DonationException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("locgovs", donationService.activeLocgovs());
            return "offline";
        }
    }
}
