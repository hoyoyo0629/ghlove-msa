package com.ghlove.gift.web;

import com.ghlove.gift.domain.Gift;
import com.ghlove.gift.service.GiftException;
import com.ghlove.gift.service.GiftService;
import com.ghlove.gift.service.InquiryService;
import com.ghlove.gift.service.JwtVerifier;
import com.ghlove.gift.service.LocgovClient;
import com.ghlove.gift.service.ReviewService;
import com.ghlove.gift.service.ThumbnailService;
import com.ghlove.gift.service.WishlistService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 답례품몰 제공자(판매자) 화면 + 구매자 라이브 AJAX.
 *
 * <p>구매자 서버렌더 화면(답례품 목록/상세, 관심답례품, 내 후기/Q&A: list·detail·wishlist·
 * my-reviews·my-qna.html)은 storefront Vue3 SPA(GiftListView/GiftDetailView/WishlistView/
 * GiftReviewsView/GiftQnaView) + {@code /api/*}로 이관되어 제거됐다(2026-09-17 Thymeleaf 폐기).
 * 남은 구매자 흐름은 SPA가 직접 호출하는 관심답례품 토글 AJAX({@link #toggleWishlist})와,
 * AS-IS 상품평 좋아요·재입고 알림 엔드포인트뿐이다.
 *
 * <p>제공자(판매자) 화면(내 답례품 관리/등록·수정·판매중지·재고조정/문의 답변)은 아직 SPA로
 * 이관되지 않아 서버렌더로 남는다(판매자 포털은 PL 그룹 결정 대기). <b>요청이 주장하는 신원을
 * 믿지 않는다</b> - 같은 JWT의 userId로 {@code OP_SELLER.MEMBER_USER_ID}를 찾아 sellerId를
 * <b>서버가 결정한다</b>({@link #currentSellerId}). 서비스 계층(GiftService.stop/adjustStock,
 * InquiryService.answer)에도 소유권 검사를 함께 넣어, 컨트롤러를 우회해도 막히도록 했다.
 */
@Controller
@RequiredArgsConstructor
public class GiftController {

    private final GiftService giftService;
    private final ReviewService reviewService;
    private final InquiryService inquiryService;
    private final ThumbnailService thumbnailService;
    private final WishlistService wishlistService;
    private final LocgovClient locgovClient;
    private final JwtVerifier jwtVerifier;
    private final com.ghlove.gift.repository.SellerRepository sellerRepository;
    private final com.ghlove.gift.service.RestockNoticeService restockNoticeService;
    private final com.ghlove.gift.service.GiftOptionService giftOptionService;

    /**
     * 로그인한 회원과 연결된 판매자 ID. <b>판매자 화면의 모든 쓰기·조회는 이 값만 쓴다.</b>
     * 요청이 무엇을 주장하든(쿼리파라미터/hidden input) 믿지 않고, GH_AUTH JWT → userId →
     * {@code OP_SELLER.MEMBER_USER_ID}로 서버가 스스로 판매자를 결정한다.
     *
     * @return 연결된 판매자가 없으면 비어 있음 (로그인은 했으나 판매자 계정이 아닌 경우 포함)
     */
    private java.util.Optional<Long> currentSellerId(HttpServletRequest request) {
        return jwtVerifier.currentUserId(request)
                .flatMap(sellerRepository::findByMemberUserId)
                .map(com.ghlove.gift.domain.Seller::getSellerId);
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String loginRedirect(String returnPath) {
        // 로그인 화면은 storefront(5173)의 /login - member(8081)엔 /login HTML이 없어 404였다.
        return "redirect:http://localhost:5173/login?target=" + encode("http://localhost:8084" + returnPath);
    }

    /**
     * 판매자 화면에 들어올 자격이 없을 때의 처리. 로그인 자체가 없으면 로그인으로 보내고,
     * 로그인은 했으나 연결된 판매자 계정이 없으면 안내 화면을 보여준다(어느 쪽인지 알려주지
     * 않으면 로그인 무한루프가 된다).
     */
    private String sellerGate(HttpServletRequest request, String returnPath, Model model) {
        if (jwtVerifier.currentUserId(request).isEmpty()) {
            return loginRedirect(returnPath);
        }
        model.addAttribute("notSeller", true);
        return "seller-dashboard";
    }

    @GetMapping("/register")
    public String registerForm(HttpServletRequest request, Model model) {
        var sellerId = currentSellerId(request);
        if (sellerId.isEmpty()) {
            return sellerGate(request, "/register", model);
        }
        model.addAttribute("categories", giftService.codesOf("GIFT_CATEGORY"));
        model.addAttribute("displayTypes", giftService.codesOf("GIFT_DISPLAY_TYPE"));
        return "register";
    }

    @PostMapping("/register")
    public String register(HttpServletRequest request, @RequestParam String itemName,
                            @RequestParam(required = false) String itemSummary,
                            @RequestParam(required = false) String detailContent,
                            @RequestParam String categoryCode, @RequestParam String locgovCode,
                            @RequestParam Integer salePrice, @RequestParam Integer stockQuantity,
                            @RequestParam(required = false) String displayType,
                            @RequestParam(required = false) String displayStartDate,
                            @RequestParam(required = false) String displayEndDate,
                            @RequestParam(required = false) Integer minDonationAmount,
                            @RequestParam(required = false) List<MultipartFile> images,
                            Model model) {
        var seller = currentSellerId(request);
        if (seller.isEmpty()) {
            return sellerGate(request, "/register", model);
        }
        Long sellerId = seller.get();
        try {
            var gift = giftService.register(sellerId, itemName, itemSummary, detailContent,
                    categoryCode, locgovCode, salePrice, stockQuantity,
                    displayType, displayStartDate, displayEndDate, minDonationAmount, images);
            // 트랜잭션 커밋 이후(register()가 리턴한 뒤)에 비동기 썸네일 생성을 트리거한다 -
            // 같은 트랜잭션 안에서 호출하면 별도 스레드가 아직 안 보이는 ITEM_IMAGE 행을 조회하게 됨.
            giftService.imagesOf(gift.getItemId())
                    .forEach(img -> thumbnailService.generateThumbnails(img.getItemImageId(), img.getImageName()));
            return "redirect:/my?registered=" + gift.getItemId();
        } catch (GiftException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("categories", giftService.codesOf("GIFT_CATEGORY"));
            model.addAttribute("displayTypes", giftService.codesOf("GIFT_DISPLAY_TYPE"));
            return "register";
        }
    }

    @GetMapping("/gifts/{itemId}/edit")
    public String editForm(@PathVariable Long itemId, HttpServletRequest request, Model model) {
        var seller = currentSellerId(request);
        if (seller.isEmpty()) {
            return sellerGate(request, "/gifts/" + itemId + "/edit", model);
        }
        Gift gift = giftService.detail(itemId);
        if (!gift.getSellerId().equals(seller.get())) {
            return "redirect:/my";
        }
        model.addAttribute("gift", gift);
        model.addAttribute("categories", giftService.codesOf("GIFT_CATEGORY"));
        model.addAttribute("displayTypes", giftService.codesOf("GIFT_DISPLAY_TYPE"));
        return "edit";
    }

    @PostMapping("/gifts/{itemId}/edit")
    public String edit(@PathVariable Long itemId, HttpServletRequest request, @RequestParam String itemName,
                        @RequestParam(required = false) String itemSummary,
                        @RequestParam(required = false) String detailContent,
                        @RequestParam String categoryCode, @RequestParam Integer salePrice,
                        @RequestParam(required = false) String displayType,
                        @RequestParam(required = false) String displayStartDate,
                        @RequestParam(required = false) String displayEndDate,
                        @RequestParam(required = false) Integer minDonationAmount, Model model) {
        var seller = currentSellerId(request);
        if (seller.isEmpty()) {
            return sellerGate(request, "/gifts/" + itemId + "/edit", model);
        }
        Long sellerId = seller.get();
        try {
            giftService.edit(itemId, sellerId, itemName, itemSummary, detailContent, categoryCode, salePrice,
                    displayType, displayStartDate, displayEndDate, minDonationAmount);
            return "redirect:/my?edited=" + itemId;
        } catch (GiftException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("gift", giftService.detail(itemId));
            model.addAttribute("categories", giftService.codesOf("GIFT_CATEGORY"));
            model.addAttribute("displayTypes", giftService.codesOf("GIFT_DISPLAY_TYPE"));
            return "edit";
        }
    }

    // ── 판매자 옵션 관리 (AJAX, 판매자 수정화면 edit.html이 호출) ────────────────
    //   /api/admin/** 옵션 API는 InternalApiAuthInterceptor(X-Internal-Secret)로 내부전용이라
    //   브라우저에서 못 부른다. 판매자 자기서비스는 여기서 currentSellerId 소유권 검사 후 처리.
    //   AS-IS와 동일하게 S/S2/S3/T + 각인 + 추가구성을 전부 지원(S2·T 숨김은 edit.html 화면단).

    public record SellerOptionsRequest(String optionType,
                                       List<com.ghlove.gift.service.GiftOptionService.OptionRow> rows) {
    }

    public record SellerTextOptionsRequest(boolean use, String title1, String title2, String title3) {
    }

    public record SellerAdditionsRequest(List<com.ghlove.gift.service.GiftOptionService.AdditionRow> rows) {
    }

    /** 소유권 확인 - 로그인 판매자의 답례품이 아니면 비어 있음. */
    private java.util.Optional<Long> ownedItem(Long itemId, HttpServletRequest request) {
        var sellerId = currentSellerId(request);
        if (sellerId.isEmpty()) {
            return java.util.Optional.empty();
        }
        Gift gift = giftService.detail(itemId);
        return gift.getSellerId().equals(sellerId.get()) ? sellerId : java.util.Optional.empty();
    }

    @GetMapping("/gifts/{itemId}/options")
    @ResponseBody
    public org.springframework.http.ResponseEntity<?> listOptions(@PathVariable Long itemId,
                                                                  HttpServletRequest request) {
        if (ownedItem(itemId, request).isEmpty()) {
            return org.springframework.http.ResponseEntity.status(403).build();
        }
        return org.springframework.http.ResponseEntity.ok(giftOptionService.adminOptionsOf(itemId));
    }

    @PostMapping("/gifts/{itemId}/options")
    @ResponseBody
    public org.springframework.http.ResponseEntity<?> saveOptions(@PathVariable Long itemId,
                                                                  @RequestBody SellerOptionsRequest req,
                                                                  HttpServletRequest request) {
        if (ownedItem(itemId, request).isEmpty()) {
            return org.springframework.http.ResponseEntity.status(403).build();
        }
        try {
            giftOptionService.saveOptions(itemId, req.optionType(), req.rows());
            return org.springframework.http.ResponseEntity.ok().build();
        } catch (GiftException e) {
            return org.springframework.http.ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/gifts/{itemId}/text-options")
    @ResponseBody
    public org.springframework.http.ResponseEntity<?> saveTextOptions(@PathVariable Long itemId,
                                                                      @RequestBody SellerTextOptionsRequest req,
                                                                      HttpServletRequest request) {
        if (ownedItem(itemId, request).isEmpty()) {
            return org.springframework.http.ResponseEntity.status(403).build();
        }
        try {
            giftOptionService.saveTextOptions(itemId, req.use(), req.title1(), req.title2(), req.title3());
            return org.springframework.http.ResponseEntity.ok().build();
        } catch (GiftException e) {
            return org.springframework.http.ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /** 추가구성 자식 답례품 한 줄(판매자 편집화면 로드용). */
    public record AdditionView(String additionItemName, Integer additionSalePrice, Integer stockQuantity) {
    }

    @GetMapping("/gifts/{itemId}/additions")
    @ResponseBody
    public org.springframework.http.ResponseEntity<?> listAdditions(@PathVariable Long itemId,
                                                                    HttpServletRequest request) {
        if (ownedItem(itemId, request).isEmpty()) {
            return org.springframework.http.ResponseEntity.status(403).build();
        }
        List<AdditionView> views = giftOptionService.additionsOf(itemId).stream()
                .map(a -> new AdditionView(a.getItemName(), a.getSalePrice(), a.getStockQuantity()))
                .toList();
        return org.springframework.http.ResponseEntity.ok(views);
    }

    @PostMapping("/gifts/{itemId}/additions")
    @ResponseBody
    public org.springframework.http.ResponseEntity<?> saveAdditions(@PathVariable Long itemId,
                                                                    @RequestBody SellerAdditionsRequest req,
                                                                    HttpServletRequest request) {
        if (ownedItem(itemId, request).isEmpty()) {
            return org.springframework.http.ResponseEntity.status(403).build();
        }
        try {
            giftOptionService.saveAdditions(itemId, req.rows());
            return org.springframework.http.ResponseEntity.ok().build();
        } catch (GiftException e) {
            return org.springframework.http.ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/gifts/{itemId}/discontinue")
    public String discontinue(@PathVariable Long itemId, HttpServletRequest request, Model model) {
        var seller = currentSellerId(request);
        if (seller.isEmpty()) {
            return sellerGate(request, "/my", model);
        }
        try {
            giftService.discontinue(itemId, seller.get());
        } catch (GiftException e) {
            return "redirect:/my?errorMessage=" + encode(e.getMessage());
        }
        return "redirect:/my";
    }

    @GetMapping("/my/inquiries")
    public String myInquiries(HttpServletRequest request, Model model) {
        var seller = currentSellerId(request);
        if (seller.isEmpty()) {
            return sellerGate(request, "/my/inquiries", model);
        }
        Long sellerId = seller.get();
        model.addAttribute("sellerId", sellerId);
        {
            model.addAttribute("inquiries", inquiryService.inquiriesForSeller(sellerId));
            model.addAttribute("statusLabels", giftService.codesOf("GIFT_INQUIRY_STATUS"));
        }
        return "inquiries";
    }

    @PostMapping("/inquiries/{inquiryId}/answer")
    public String answerInquiry(@PathVariable Long inquiryId, HttpServletRequest request,
                                 @RequestParam String answer, Model model) {
        var seller = currentSellerId(request);
        if (seller.isEmpty()) {
            return sellerGate(request, "/my/inquiries", model);
        }
        try {
            inquiryService.answer(inquiryId, seller.get(), answer);
        } catch (GiftException e) {
            return "redirect:/my/inquiries?errorMessage=" + encode(e.getMessage());
        }
        // 판매자 문의답변 완료 - AS-IS 업무Web 화면이라 대응 문구가 없어 일반 문구를 쓴다.
        return Done.redirect("/my/inquiries", "답변이 등록되었습니다.");
    }

    @GetMapping("/my")
    public String myGifts(HttpServletRequest request, Model model) {
        var seller = currentSellerId(request);
        if (seller.isEmpty()) {
            return sellerGate(request, "/my", model);
        }
        Long sellerId = seller.get();
        model.addAttribute("sellerId", sellerId);
        {
            model.addAttribute("gifts", giftService.myGifts(sellerId));
            model.addAttribute("statusLabels", giftService.codesOf("GIFT_STATUS"));
            model.addAttribute("categories", giftService.codesOf("GIFT_CATEGORY"));
        }
        return "my";
    }

    @PostMapping("/gifts/{itemId}/stop")
    public String stop(@PathVariable Long itemId, HttpServletRequest request, Model model) {
        var seller = currentSellerId(request);
        if (seller.isEmpty()) {
            return sellerGate(request, "/my", model);
        }
        try {
            giftService.stop(itemId, seller.get());
        } catch (GiftException e) {
            return "redirect:/my?errorMessage=" + encode(e.getMessage());
        }
        return "redirect:/my";
    }

    @PostMapping("/gifts/{itemId}/stock")
    public String adjustStock(@PathVariable Long itemId, HttpServletRequest request,
                              @RequestParam int quantity, Model model) {
        var seller = currentSellerId(request);
        if (seller.isEmpty()) {
            return sellerGate(request, "/my", model);
        }
        try {
            giftService.adjustStock(itemId, seller.get(), quantity);
        } catch (GiftException e) {
            return "redirect:/my?errorMessage=" + encode(e.getMessage());
        }
        return "redirect:/my";
    }

    /** 답례품 목록 카드/상세의 하트 아이콘 클릭 (AS-IS goods/index-main.html addToWishList) -
     *  storefront SPA(GiftListView/GiftDetailView/WishlistView)가 직접 호출하는 라이브 AJAX. */
    @PostMapping("/wishlist/{itemId}/toggle")
    @ResponseBody
    public org.springframework.http.ResponseEntity<?> toggleWishlist(@PathVariable Long itemId, HttpServletRequest request) {
        var authUserId = jwtVerifier.currentUserId(request);
        if (authUserId.isEmpty()) {
            return org.springframework.http.ResponseEntity.status(401).build();
        }
        boolean wishlisted = wishlistService.toggle(authUserId.get(), itemId);
        return org.springframework.http.ResponseEntity.ok(java.util.Map.of("wishlisted", wishlisted));
    }

    /**
     * 상품평 좋아요 (AS-IS `POST /api/item/review/add-like/{id}`, `ItemController:899`).
     *
     * <p>AS-IS는 <b>비로그인도 IP 기준으로</b> 누를 수 있고 취소는 없다. 그 동작을 그대로 옮겼다 -
     * 응답의 `liked`가 false면 "이미 누른 상태"라는 뜻이지 실패가 아니다.
     */
    @PostMapping("/reviews/{itemReviewId}/like")
    @ResponseBody
    public org.springframework.http.ResponseEntity<?> likeReview(@PathVariable Integer itemReviewId,
                                                                  HttpServletRequest request) {
        Long userId = jwtVerifier.currentUserId(request).orElse(null);
        try {
            boolean liked = reviewService.like(itemReviewId, userId, clientIp(request));
            return org.springframework.http.ResponseEntity.ok(java.util.Map.of("liked", liked));
        } catch (GiftException e) {
            return org.springframework.http.ResponseEntity.badRequest()
                    .body(java.util.Map.of("message", e.getMessage()));
        }
    }

    /** AS-IS `CommonUtils.getClientIp()` - 프록시 경유 시 X-Forwarded-For의 첫 주소를 쓴다. */
    private static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * 재입고 알림 신청 (AS-IS `POST /api/item/restock`, `ItemController:879`).
     * 품절 답례품 상세화면(storefront GiftDetailView)의 "재입고 알림" 버튼이 직접 호출하는 AJAX.
     */
    @PostMapping("/gifts/{itemId}/restock-notice")
    @ResponseBody
    public org.springframework.http.ResponseEntity<?> requestRestockNotice(@PathVariable Long itemId, HttpServletRequest request) {
        var authUserId = jwtVerifier.currentUserId(request);
        if (authUserId.isEmpty()) {
            return org.springframework.http.ResponseEntity.status(401).body(java.util.Map.of("message", "로그인이 필요합니다."));
        }
        try {
            restockNoticeService.request(itemId.intValue(), authUserId.get());
            // AS-IS items/details-main.html
            return org.springframework.http.ResponseEntity.ok(java.util.Map.of("message", "재입고 시 알려드리겠습니다."));
        } catch (GiftException e) {
            return org.springframework.http.ResponseEntity.badRequest().body(java.util.Map.of("message", e.getMessage()));
        }
    }
}
