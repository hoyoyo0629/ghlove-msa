package com.ghlove.gift.web;

import com.ghlove.gift.domain.Gift;
import com.ghlove.gift.domain.Seller;
import com.ghlove.gift.repository.SellerRepository;
import com.ghlove.gift.service.GiftService;
import com.ghlove.gift.service.JwtVerifier;
import com.ghlove.gift.service.SellerOrderClient;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 판매자 셀프포털 (AS-IS opmanager/seller/self - SellerController, 대시보드만). 이전에는 이
 * MSA에 판매자 로그인 모델이 없어(GiftController "/my" 주석 참고) "판매자 화면"이 sellerId를
 * URL 쿼리파라미터로 그냥 입력받는 무인증 스푸핑 가능 임시툴이었다 - 누구나 아무 sellerId나
 * 넣으면 남의 답례품 목록/주문현황을 볼 수 있었다.
 *
 * 이 컨트롤러는 member 서비스의 ROLE_PROVIDER 로그인을 재사용해 그 구멍을 메운 신규 진입점
 * 이다: GH_AUTH JWT 쿠키(JwtVerifier, gift 서비스가 이미 관심답례품 등에서 쓰던 것과 동일한
 * SFR-010 패턴)로 회원 본인 확인 → OP_SELLER.MEMBER_USER_ID(이번에 추가한 신규 컬럼)로 그
 * 회원과 연결된 판매자 레코드를 찾는다. 연결된 판매자가 없으면 "등록된 판매자 계정이
 * 아닙니다" 안내만 하고 끝난다(admin이 입점업체관리에서 연결해줘야 하는데, 그 연결 UI 자체는
 * 이번 라운드 범위 밖 - DB로만 시딩했다).
 *
 * 범위: "본인 답례품 목록 + 주문현황" 대시보드 1개 화면만. 공지구독/배송관리/정산조회 등은
 * 명시적으로 범위 밖 - 필요하면 이 컨트롤러를 확장하면 된다. 답례품 목록 자체의 등록/수정/
 * 재고조정/판매중지(GiftController "/my")는 이 라운드에서 손대지 않았다 - 그 화면들이 여전히
 * sellerId 쿼리파라미터를 무인증으로 신뢰하는 스푸핑 가능 상태로 남아있다는 점은 그대로다
 * (알려진 잔존 갭, 이번 라운드는 신규 진입점만 인증 기반으로 만드는 것까지가 범위).
 */
@Controller
@RequiredArgsConstructor
public class SellerPortalController {

    private final JwtVerifier jwtVerifier;
    private final SellerRepository sellerRepository;
    private final GiftService giftService;
    private final SellerOrderClient sellerOrderClient;

    @GetMapping("/seller/dashboard")
    public String dashboard(HttpServletRequest request, Model model) {
        var authUserId = jwtVerifier.currentUserId(request);
        if (authUserId.isEmpty()) {
            return "redirect:http://localhost:8081/login?target="
                    + URLEncoder.encode("http://localhost:8084/seller/dashboard", StandardCharsets.UTF_8);
        }

        Seller seller = sellerRepository.findByMemberUserId(authUserId.get()).orElse(null);
        model.addAttribute("seller", seller);
        if (seller == null) {
            return "seller-dashboard";
        }

        List<Gift> gifts = giftService.myGifts(seller.getSellerId());
        model.addAttribute("gifts", gifts);
        model.addAttribute("statusLabels", giftService.codesOf("GIFT_STATUS"));
        model.addAttribute("orders", sellerOrderClient.ordersOf(seller.getSellerId()));
        return "seller-dashboard";
    }
}
