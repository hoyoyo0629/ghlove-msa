package com.ghlove.member.web;

import com.ghlove.member.domain.User;
import com.ghlove.member.service.DonationClient;
import com.ghlove.member.service.GiftClient;
import com.ghlove.member.service.OrderClient;
import com.ghlove.member.service.OrderSummaryInfo;
import com.ghlove.member.service.PointClient;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** storefront(Vue3 SPA)용 마이페이지 허브 JSON API - {@link MyPageController}(Thymeleaf)와
 * 완전히 같은 요약 데이터 조합을 JSON으로 감싼다. */
@RestController
@RequiredArgsConstructor
public class MyPageApiController {

    private final DonationClient donationClient;
    private final PointClient pointClient;
    private final OrderClient orderClient;
    private final GiftClient giftClient;
    private final com.ghlove.member.service.DeliveryService deliveryService;

    @GetMapping("/api/mypage/summary")
    public ResponseEntity<MyPageSummaryResponse> summary(HttpSession session) {
        User user = (User) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        Long userId = user.getUserId();
        return ResponseEntity.ok(new MyPageSummaryResponse(
                user.getUserName(),
                donationClient.myTotal(userId),
                pointClient.balanceOf(userId),
                orderClient.mySummary(userId),
                giftClient.mySummary(userId),
                deliveryService.listOf(userId).size(),
                donationClient.interestLocgovsOf(userId).size()));
    }

    public record MyPageSummaryResponse(String userName, java.math.BigDecimal donationTotal, long pointBalance,
                                         OrderSummaryInfo orderSummary,
                                         GiftClient.GiftSummaryInfo giftSummary,
                                         int deliveryCount, int interestLocgovCount) {
    }
}
