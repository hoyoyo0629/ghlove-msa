package com.ghlove.admin.web;

import com.ghlove.admin.domain.OpConfigGoogleAnalytics;
import com.ghlove.admin.domain.OpConfigPg;
import com.ghlove.admin.domain.ShopConfig;
import com.ghlove.admin.repository.OpConfigGoogleAnalyticsRepository;
import com.ghlove.admin.repository.OpConfigPgRepository;
import com.ghlove.admin.repository.ShopConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/** 쇼핑몰 설정 허브의 잔여 서브화면 (AS-IS opmanager/config - ConfigManagerController,
 *  #33 gap). `site-config`(기본정보/SEO/가입불가아이디/금지어)와 `policy/*`를 제외한 나머지:
 *  가입거부 블랙리스트(IP/이메일도메인), 결제수단 활성화, 배송 기본정책+희망배송일, PG사
 *  연동, 전환추적스크립트, GA 추적ID, 주문임시저장 설정. 인기상품 랭킹 설정은 이미
 *  `/admin/ranking`이 커버하므로 여기서는 다루지 않는다(AS-IS 중복 엔드포인트).
 *  OP_CONFIG/OP_CONFIG_PG/OP_CONFIG_GOOGLE_ANALYTICS 모두 단일 행(ID=1)만 사용한다. */
@Controller
@RequiredArgsConstructor
public class SiteConfigExtController {

    private final ShopConfigRepository shopConfigRepository;
    private final OpConfigPgRepository opConfigPgRepository;
    private final OpConfigGoogleAnalyticsRepository opConfigGoogleAnalyticsRepository;

    private ShopConfig config() {
        return shopConfigRepository.findById(ShopConfig.SHOP_CONFIG_ID).orElseThrow();
    }

    private String redirectWithMessage(String path) {
        return "redirect:" + path + "?message=" +
                URLEncoder.encode("저장되었습니다.", StandardCharsets.UTF_8);
    }

    // ---------------------------------------------------------------- 가입거부 블랙리스트(IP/이메일도메인)

    @GetMapping("/site-config/deny")
    public String denyForm(Model model) {
        model.addAttribute("config", config());
        return "site-config/deny";
    }

    @PostMapping("/site-config/deny")
    public String denySave(ShopConfig form) {
        ShopConfig c = config();
        c.setDeniedIp(form.getDeniedIp());
        c.setDeniedEmailDomain(form.getDeniedEmailDomain());
        shopConfigRepository.save(c);
        return redirectWithMessage("/site-config/deny");
    }

    // ---------------------------------------------------------------- 결제수단 활성화 설정

    @GetMapping("/site-config/payment")
    public String paymentForm(Model model) {
        model.addAttribute("config", config());
        return "site-config/payment";
    }

    @PostMapping("/site-config/payment")
    public String paymentSave(ShopConfig form) {
        ShopConfig c = config();
        // OP_CONFIG의 PAYMENT_* 계열은 saleson 원본부터 Y/N이 아닌 "1"(사용)/"0"(미사용) 코드값이다.
        c.setPaymentCard(flag01(form.getPaymentCard()));
        c.setPaymentBank(flag01(form.getPaymentBank()));
        c.setPaymentVbank(flag01(form.getPaymentVbank()));
        c.setPaymentEscrow(flag01(form.getPaymentEscrow()));
        c.setPaymentConv(flag01(form.getPaymentConv()));
        c.setPaymentDlv(flag01(form.getPaymentDlv()));
        c.setPaymentRealtime(flag01(form.getPaymentRealtime()));
        c.setPaymentHp(flag01(form.getPaymentHp()));
        c.setMinimumPaymentAmount(form.getMinimumPaymentAmount());
        c.setPaymentConvLimit(form.getPaymentConvLimit());
        shopConfigRepository.save(c);
        return redirectWithMessage("/site-config/payment");
    }

    private static String flag01(String checkboxValue) {
        return "1".equals(checkboxValue) ? "1" : "0";
    }

    // ---------------------------------------------------------------- 배송 기본정책 + 희망배송일

    @GetMapping("/site-config/delivery")
    public String deliveryForm(Model model) {
        model.addAttribute("config", config());
        return "site-config/delivery";
    }

    @PostMapping("/site-config/delivery")
    public String deliverySave(ShopConfig form) {
        ShopConfig c = config();
        c.setDeliveryInfo(form.getDeliveryInfo());
        c.setDeliveryHopeFlag(form.getDeliveryHopeFlag() != null ? "Y" : "N");
        c.setDeliveryHopeStartDate(form.getDeliveryHopeStartDate());
        c.setDeliveryHopeEndDate(form.getDeliveryHopeEndDate());
        c.setBankDepositDueDay(form.getBankDepositDueDay());
        c.setShippingCompleteDate(form.getShippingCompleteDate());
        c.setSmartDeliveryCriteriaDate(form.getSmartDeliveryCriteriaDate());
        shopConfigRepository.save(c);
        return redirectWithMessage("/site-config/delivery");
    }

    // ---------------------------------------------------------------- PG사(결제대행사) 연동 설정

    @GetMapping("/site-config/pg")
    public String pgForm(Model model) {
        model.addAttribute("config", opConfigPgRepository.findById(OpConfigPg.ID).orElseGet(OpConfigPg::defaults));
        return "site-config/pg";
    }

    @PostMapping("/site-config/pg")
    public String pgSave(OpConfigPg form) {
        form.setId(OpConfigPg.ID);
        if (form.getPgType() == null) form.setPgType("");
        if (form.getMid() == null) form.setMid("");
        if (form.getRealtimePartcancelFlag() == null) form.setRealtimePartcancelFlag("N");
        if (form.getUseAutoCashReceipt() == null) form.setUseAutoCashReceipt("N");
        if (form.getUseEscroow() == null) form.setUseEscroow("N");
        if (form.getUseNpayOrder() == null) form.setUseNpayOrder("N");
        if (form.getUseNpayPayment() == null) form.setUseNpayPayment("N");
        if (form.getUseVbackRefundService() == null) form.setUseVbackRefundService("N");
        if (form.getCashbillServiceType() == null) form.setCashbillServiceType("N");
        opConfigPgRepository.save(form);
        return redirectWithMessage("/site-config/pg");
    }

    // ---------------------------------------------------------------- 전환추적스크립트(광고 픽셀)

    @GetMapping("/site-config/conversion-tag")
    public String conversionTagForm(Model model) {
        model.addAttribute("config", config());
        return "site-config/conversion-tag";
    }

    @PostMapping("/site-config/conversion-tag")
    public String conversionTagSave(ShopConfig form) {
        ShopConfig c = config();
        c.setTagOverture(form.getTagOverture());
        c.setTagAdwords(form.getTagAdwords());
        shopConfigRepository.save(c);
        return redirectWithMessage("/site-config/conversion-tag");
    }

    // ---------------------------------------------------------------- GA 추적ID 설정

    @GetMapping("/site-config/google-analytics")
    public String gaForm(Model model) {
        model.addAttribute("config", opConfigGoogleAnalyticsRepository.findById(OpConfigGoogleAnalytics.ID)
                .orElseGet(OpConfigGoogleAnalytics::defaults));
        return "site-config/google-analytics";
    }

    @PostMapping("/site-config/google-analytics")
    public String gaSave(OpConfigGoogleAnalytics form) {
        form.setId(OpConfigGoogleAnalytics.ID);
        if (form.getCommonTrackingFlag() == null) form.setCommonTrackingFlag("N");
        if (form.getEcommerceTrackingFlag() == null) form.setEcommerceTrackingFlag("N");
        if (form.getStatisticsFlag() == null) form.setStatisticsFlag("N");
        opConfigGoogleAnalyticsRepository.save(form);
        return redirectWithMessage("/site-config/google-analytics");
    }

    // ---------------------------------------------------------------- 주문임시저장 관련 설정

    @GetMapping("/site-config/order-temp")
    public String orderTempForm(Model model) {
        model.addAttribute("config", config());
        return "site-config/order-temp";
    }

    @PostMapping("/site-config/order-temp")
    public String orderTempSave(ShopConfig form) {
        ShopConfig c = config();
        c.setRetentionPeriod(form.getRetentionPeriod());
        c.setConfirmPurchaseRequestDate(form.getConfirmPurchaseRequestDate());
        c.setConfirmPurchaseDate(form.getConfirmPurchaseDate());
        c.setAlternateSystem(form.getAlternateSystem());
        shopConfigRepository.save(c);
        return redirectWithMessage("/site-config/order-temp");
    }
}
