package com.ghlove.admin.web;

import com.ghlove.admin.domain.ShopConfig;
import com.ghlove.admin.repository.ShopConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

/** 사이트 설정 (AS-IS opmanager/config/site-config, shop-config, deny/edit 통합). */
@Controller
@RequiredArgsConstructor
public class ShopConfigController {

    private final ShopConfigRepository shopConfigRepository;

    @GetMapping("/site-config")
    public String form(Model model) {
        model.addAttribute("config", shopConfigRepository.findById(ShopConfig.SHOP_CONFIG_ID).orElseThrow());
        return "site-config/form";
    }

    @PostMapping("/site-config")
    public String save(ShopConfig form) {
        ShopConfig config = shopConfigRepository.findById(ShopConfig.SHOP_CONFIG_ID).orElseThrow();
        config.setShopName(form.getShopName());
        config.setCompanyName(form.getCompanyName());
        config.setBossName(form.getBossName());
        config.setCompanyNumber(form.getCompanyNumber());
        config.setTelNumber(form.getTelNumber());
        config.setAddress(form.getAddress());
        config.setAddressDetail(form.getAddressDetail());
        config.setAdminName(form.getAdminName());
        config.setAdminEmail(form.getAdminEmail());
        config.setAdminTelNumber(form.getAdminTelNumber());
        config.setSeoTitle(form.getSeoTitle());
        config.setSeoKeywords(form.getSeoKeywords());
        config.setSeoDescription(form.getSeoDescription());
        config.setDeniedId(form.getDeniedId());
        config.setBanWord(form.getBanWord());
        shopConfigRepository.save(config);
        return "redirect:/site-config?message=" +
                java.net.URLEncoder.encode("저장되었습니다.", java.nio.charset.StandardCharsets.UTF_8);
    }
}
