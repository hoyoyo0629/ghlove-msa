package com.ghlove.member.web;

import com.ghlove.member.domain.User;
import com.ghlove.member.service.DeliveryService;
import com.ghlove.member.service.DonationClient;
import com.ghlove.member.service.GiftClient;
import com.ghlove.member.service.OrderClient;
import com.ghlove.member.service.PointClient;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 마이페이지 허브 화면 (AS-IS users/mypage.html 재현) - 기부/포인트/주문 등 각 서비스의
 * 요약치를 한 화면에 모아 보여주고, 상세는 각 서비스로 링크만 연결한다. 다른 서비스가
 * 죽어도 이 화면 자체는 안 깨지도록 각 카드는 조회 실패 시 0으로 조용히 대체된다
 * (DonationClient/PointClient/OrderClient 공통 패턴).
 */
@Controller
@RequiredArgsConstructor
public class MyPageController {

    private final DonationClient donationClient;
    private final PointClient pointClient;
    private final OrderClient orderClient;
    private final GiftClient giftClient;
    private final DeliveryService deliveryService;

    @GetMapping("/mypage")
    public String mypage(HttpSession session, Model model) {
        User loginUser = (User) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (loginUser == null) {
            return AuthController.loginRedirect("/mypage");
        }
        model.addAttribute("user", loginUser);
        model.addAttribute("donationTotal", donationClient.myTotal(loginUser.getUserId()));
        model.addAttribute("pointBalance", pointClient.balanceOf(loginUser.getUserId()));
        model.addAttribute("orderSummary", orderClient.mySummary(loginUser.getUserId()));
        model.addAttribute("giftSummary", giftClient.mySummary(loginUser.getUserId()));
        model.addAttribute("deliveryCount", deliveryService.listOf(loginUser.getUserId()).size());
        model.addAttribute("interestLocgovs", donationClient.interestLocgovsOf(loginUser.getUserId()));
        return "mypage";
    }
}
