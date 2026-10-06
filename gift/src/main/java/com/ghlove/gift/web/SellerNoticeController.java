package com.ghlove.gift.web;

import com.ghlove.gift.service.JwtVerifier;
import com.ghlove.gift.service.SellerNoticeClient;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 판매자 공지사항 조회 (AS-IS SellerISysNoticeController, /seller/sys-notice). 운영자가 등록한
 * 판매자 공지(admin op_sys_notice_seller)를 판매자가 조회한다 - 데이터는 admin 소유라
 * {@link SellerNoticeClient}로 admin 내부 API를 호출한다. 인증은 셀프포털 대시보드
 * ({@link SellerPortalController})와 동일하게 GH_AUTH JWT(ROLE_PROVIDER 로그인)로 본인 확인만 한다
 * (공지는 전체 판매자 대상 broadcast라 판매자별 필터는 없다).
 */
@Controller
@RequiredArgsConstructor
public class SellerNoticeController {

    private final JwtVerifier jwtVerifier;
    private final SellerNoticeClient sellerNoticeClient;

    private boolean authed(HttpServletRequest request) {
        return jwtVerifier.currentUserId(request).isPresent();
    }

    private String loginRedirect(String target) {
        // 로그인 화면은 storefront(SPA, 5173)의 /login 이다 - member 서비스(8081)는 JSON 인증
        // API만 있어 /login HTML이 없다(8081/login은 404). storefront 로그인 성공 후
        // navigateAfterLogin이 target 절대URL(localhost:808x)로 되돌려보낸다(LoginView.vue).
        return "redirect:http://localhost:5173/login?target="
                + URLEncoder.encode("http://localhost:8084" + target, StandardCharsets.UTF_8);
    }

    @GetMapping("/seller/sys-notice/list")
    public String list(HttpServletRequest request, Model model,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(required = false) String startDt,
                       @RequestParam(required = false) String endDt) {
        if (!authed(request)) {
            return loginRedirect("/seller/sys-notice/list");
        }
        List<SellerNoticeClient.NoticeRow> list = sellerNoticeClient.list(keyword, startDt, endDt);
        model.addAttribute("list", list);
        model.addAttribute("count", list.size());
        model.addAttribute("keyword", keyword);
        model.addAttribute("startDt", startDt);
        model.addAttribute("endDt", endDt);
        return "seller-notice-list";
    }

    @GetMapping("/seller/sys-notice/detail/{noticeId}")
    public String detail(HttpServletRequest request, Model model, @PathVariable Long noticeId) {
        if (!authed(request)) {
            return loginRedirect("/seller/sys-notice/detail/" + noticeId);
        }
        SellerNoticeClient.NoticeDetail detail = sellerNoticeClient.detail(noticeId);
        if (detail == null) {
            return "redirect:/seller/sys-notice/list";
        }
        model.addAttribute("detail", detail);
        return "seller-notice-detail";
    }

    @GetMapping("/seller/sys-notice/file-download/{fileId}")
    @ResponseBody
    public ResponseEntity<byte[]> fileDownload(HttpServletRequest request, @PathVariable Long fileId) {
        if (!authed(request)) {
            return ResponseEntity.status(401).build();
        }
        return sellerNoticeClient.downloadFile(fileId);
    }
}
