package com.ghlove.gift.web;

import com.ghlove.gift.domain.Gift;
import com.ghlove.gift.domain.Inquiry;
import com.ghlove.gift.domain.Review;
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
import java.util.Map;
import java.util.stream.Collectors;

/**
 * sellerId 기반 판매자 화면(내 답례품 관리/문의 답변/재고 조정 등)은 이 라운드의 "본인 확인"
 * 범위 밖 - 아직 이 MSA에 판매자 로그인 모델이 없다. 구매자 자신을 식별하는 화면(리뷰
 * 작성/문의 작성/관심답례품)만 SFR-010에 따라 userId를 로그인 JWT 쿠키에서 가져온다.
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

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private java.time.LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return java.time.LocalDate.parse(value);
        } catch (java.time.format.DateTimeParseException e) {
            return null;
        }
    }

    private String loginRedirect(String returnPath) {
        return "redirect:http://localhost:8081/login?target=" + encode("http://localhost:8084" + returnPath);
    }

    @GetMapping("/")
    public String list(@RequestParam(required = false) String categoryCode,
                        @RequestParam(required = false) String q,
                        @RequestParam(required = false) String locgovCode,
                        HttpServletRequest request, Model model) {
        List<Gift> gifts = giftService.publicGifts(categoryCode, q, locgovCode);
        model.addAttribute("selectedCategory", categoryCode);
        model.addAttribute("q", q);
        model.addAttribute("locgovCode", locgovCode);
        populateListModel(gifts, request, model);
        return "list";
    }

    /** 답례품몰 GNB "제철식품관" (AS-IS /event/seasonList.html). */
    @GetMapping("/seasonal")
    public String seasonal(HttpServletRequest request, Model model) {
        model.addAttribute("pageTitle", "제철식품관");
        populateListModel(giftService.seasonalGifts(), request, model);
        return "list";
    }

    /** 답례품몰 GNB "마을기업관" (AS-IS /community-business/communityList-main.html). */
    @GetMapping("/community-business")
    public String communityBusiness(HttpServletRequest request, Model model) {
        model.addAttribute("pageTitle", "마을기업관");
        populateListModel(giftService.communityBusinessGifts(), request, model);
        return "list";
    }

    /** AS-IS goods/index-main.html의 goods-list-group 카드 그리드에 공통으로 필요한 데이터
     *  (썸네일/카테고리 아이콘/지자체명/관심답례품 여부/신규 배지)를 모아 넣는다. */
    private void populateListModel(List<Gift> gifts, HttpServletRequest request, Model model) {
        List<Long> itemIds = gifts.stream().map(Gift::getItemId).toList();
        model.addAttribute("gifts", gifts);
        model.addAttribute("categories", giftService.codesOf("GIFT_CATEGORY"));
        model.addAttribute("categoryIcons", CATEGORY_ICONS);
        model.addAttribute("thumbnails", giftService.thumbnailsOf(itemIds));
        model.addAttribute("locgovNames", locgovClient.namesByCode());
        model.addAttribute("newItemIds", giftService.newItemIds(itemIds));

        var authUserId = jwtVerifier.currentUserId(request);
        model.addAttribute("wishlistedItemIds", authUserId.isPresent()
                ? wishlistService.wishlistedItemIds(authUserId.get(), itemIds)
                : java.util.Set.of());
    }

    /** GIFT_CATEGORY 6개 대분류(TOUR/AGRI/SEAFOOD/PROCESSED/LIVING/VOUCHER, ordering순)에
     *  대응하는 AS-IS 아이콘(goods/cli-icon_category01~06.png)을 순서대로 매핑한다. */
    private static final Map<String, String> CATEGORY_ICONS = Map.of(
            "TOUR", "cli-icon_category01.png",
            "AGRI", "cli-icon_category02.png",
            "SEAFOOD", "cli-icon_category03.png",
            "PROCESSED", "cli-icon_category04.png",
            "LIVING", "cli-icon_category05.png",
            "VOUCHER", "cli-icon_category06.png");

    @GetMapping("/gifts/{itemId}")
    public String detail(@PathVariable Long itemId,
                          @RequestParam(required = false) String errorMessage, Model model) {
        model.addAttribute("errorMessage", errorMessage);
        Gift gift = giftService.detail(itemId);
        model.addAttribute("gift", gift);
        model.addAttribute("categories", giftService.codesOf("GIFT_CATEGORY"));
        model.addAttribute("statusLabels", giftService.codesOf("GIFT_STATUS"));
        model.addAttribute("images", giftService.imagesOf(itemId));
        model.addAttribute("locgovName", locgovClient.namesByCode().get(gift.getLocgovCode()));
        model.addAttribute("seller", giftService.sellerOf(gift.getSellerId()));

        List<Review> reviews = reviewService.reviewsOf(itemId);
        model.addAttribute("reviews", reviews);
        model.addAttribute("reviewImages", reviewService.imagesOf(reviews));
        model.addAttribute("averageScore", reviewService.averageScore(reviews));

        model.addAttribute("inquiries", inquiryService.inquiriesOf(itemId));
        model.addAttribute("inquiryStatusLabels", giftService.codesOf("GIFT_INQUIRY_STATUS"));
        return "detail";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("categories", giftService.codesOf("GIFT_CATEGORY"));
        model.addAttribute("displayTypes", giftService.codesOf("GIFT_DISPLAY_TYPE"));
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam Long sellerId, @RequestParam String itemName,
                            @RequestParam(required = false) String itemSummary,
                            @RequestParam(required = false) String detailContent,
                            @RequestParam String categoryCode, @RequestParam String locgovCode,
                            @RequestParam Integer salePrice, @RequestParam Integer stockQuantity,
                            @RequestParam(required = false) String displayType,
                            @RequestParam(required = false) String displayStartDate,
                            @RequestParam(required = false) String displayEndDate,
                            @RequestParam(required = false) Integer minDonationAmount,
                            @RequestParam(required = false) List<MultipartFile> images, Model model) {
        try {
            var gift = giftService.register(sellerId, itemName, itemSummary, detailContent,
                    categoryCode, locgovCode, salePrice, stockQuantity,
                    displayType, displayStartDate, displayEndDate, minDonationAmount, images);
            // 트랜잭션 커밋 이후(register()가 리턴한 뒤)에 비동기 썸네일 생성을 트리거한다 -
            // 같은 트랜잭션 안에서 호출하면 별도 스레드가 아직 안 보이는 ITEM_IMAGE 행을 조회하게 됨.
            giftService.imagesOf(gift.getItemId())
                    .forEach(img -> thumbnailService.generateThumbnails(img.getItemImageId(), img.getImageName()));
            return "redirect:/my?sellerId=" + sellerId + "&registered=" + gift.getItemId();
        } catch (GiftException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("categories", giftService.codesOf("GIFT_CATEGORY"));
            model.addAttribute("displayTypes", giftService.codesOf("GIFT_DISPLAY_TYPE"));
            return "register";
        }
    }

    @GetMapping("/gifts/{itemId}/edit")
    public String editForm(@PathVariable Long itemId, @RequestParam Long sellerId, Model model) {
        Gift gift = giftService.detail(itemId);
        if (!gift.getSellerId().equals(sellerId)) {
            return "redirect:/my?sellerId=" + sellerId;
        }
        model.addAttribute("gift", gift);
        model.addAttribute("categories", giftService.codesOf("GIFT_CATEGORY"));
        model.addAttribute("displayTypes", giftService.codesOf("GIFT_DISPLAY_TYPE"));
        return "edit";
    }

    @PostMapping("/gifts/{itemId}/edit")
    public String edit(@PathVariable Long itemId, @RequestParam Long sellerId, @RequestParam String itemName,
                        @RequestParam(required = false) String itemSummary,
                        @RequestParam(required = false) String detailContent,
                        @RequestParam String categoryCode, @RequestParam Integer salePrice,
                        @RequestParam(required = false) String displayType,
                        @RequestParam(required = false) String displayStartDate,
                        @RequestParam(required = false) String displayEndDate,
                        @RequestParam(required = false) Integer minDonationAmount, Model model) {
        try {
            giftService.edit(itemId, sellerId, itemName, itemSummary, detailContent, categoryCode, salePrice,
                    displayType, displayStartDate, displayEndDate, minDonationAmount);
            return "redirect:/my?sellerId=" + sellerId + "&edited=" + itemId;
        } catch (GiftException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("gift", giftService.detail(itemId));
            model.addAttribute("categories", giftService.codesOf("GIFT_CATEGORY"));
            model.addAttribute("displayTypes", giftService.codesOf("GIFT_DISPLAY_TYPE"));
            return "edit";
        }
    }

    @PostMapping("/gifts/{itemId}/discontinue")
    public String discontinue(@PathVariable Long itemId, @RequestParam Long sellerId, Model model) {
        try {
            giftService.discontinue(itemId, sellerId);
        } catch (GiftException e) {
            return "redirect:/my?sellerId=" + sellerId + "&errorMessage=" + encode(e.getMessage());
        }
        return "redirect:/my?sellerId=" + sellerId;
    }

    @PostMapping("/gifts/{itemId}/reviews")
    public String writeReview(@PathVariable Long itemId,
                               @RequestParam(required = false) String userName,
                               @RequestParam(required = false) String orderCode,
                               @RequestParam String subject, @RequestParam String content,
                               @RequestParam Integer score,
                               @RequestParam(required = false, defaultValue = "false") boolean recommend,
                               @RequestParam(required = false) List<MultipartFile> images,
                               HttpServletRequest request) {
        var authUserId = jwtVerifier.currentUserId(request);
        if (authUserId.isEmpty()) {
            return loginRedirect("/gifts/" + itemId);
        }
        try {
            var gift = giftService.detail(itemId);
            reviewService.write(itemId, gift.getSellerId(), authUserId.get(), userName, orderCode,
                    subject, content, score, recommend, images);
            return "redirect:/gifts/" + itemId;
        } catch (GiftException e) {
            return "redirect:/gifts/" + itemId + "?errorMessage=" + encode(e.getMessage());
        }
    }

    @PostMapping("/gifts/{itemId}/inquiries")
    public String askInquiry(@PathVariable Long itemId, @RequestParam String question,
                              @RequestParam(required = false, defaultValue = "false") boolean secret,
                              HttpServletRequest request) {
        var authUserId = jwtVerifier.currentUserId(request);
        if (authUserId.isEmpty()) {
            return loginRedirect("/gifts/" + itemId);
        }
        try {
            inquiryService.ask(itemId, authUserId.get(), question, secret);
            return "redirect:/gifts/" + itemId;
        } catch (GiftException e) {
            return "redirect:/gifts/" + itemId + "?errorMessage=" + encode(e.getMessage());
        }
    }

    /** 마이페이지 "답례품 후기" - 본인이 작성한 리뷰 목록. AS-IS mypage/review.html과
     *  동일하게 기간/답례품명으로 필터링한다(시드 규모가 작아 인메모리 처리). */
    @GetMapping("/my/reviews")
    public String myReviews(@RequestParam(required = false) String searchStartDate,
                             @RequestParam(required = false) String searchEndDate,
                             @RequestParam(required = false) String itemName,
                             HttpServletRequest request, Model model) {
        var authUserId = jwtVerifier.currentUserId(request);
        if (authUserId.isEmpty()) {
            return loginRedirect("/my/reviews");
        }
        List<Review> reviews = reviewService.myReviews(authUserId.get());
        Map<Long, Gift> giftsById = giftService.giftsOf(reviews.stream().map(Review::getItemId).distinct().toList())
                .stream().collect(Collectors.toMap(Gift::getItemId, java.util.function.Function.identity()));

        var start = parseDate(searchStartDate);
        var end = parseDate(searchEndDate);
        var filtered = reviews.stream()
                .filter(r -> start == null || !r.getCreatedDate().toLocalDate().isBefore(start))
                .filter(r -> end == null || !r.getCreatedDate().toLocalDate().isAfter(end))
                .filter(r -> {
                    if (itemName == null || itemName.isBlank()) {
                        return true;
                    }
                    Gift g = giftsById.get(r.getItemId());
                    return g != null && g.getItemName() != null && g.getItemName().contains(itemName);
                })
                .toList();

        model.addAttribute("reviews", filtered);
        model.addAttribute("giftsById", giftsById);
        model.addAttribute("thumbnails", giftService.thumbnailsOf(giftsById.keySet().stream().toList()));
        model.addAttribute("searchStartDate", searchStartDate);
        model.addAttribute("searchEndDate", searchEndDate);
        model.addAttribute("itemName", itemName);
        return "my-reviews";
    }

    /** 마이페이지 "답례품Q&A" - 본인이 작성한 상품문의 목록 (판매자용 /my/inquiries와 다름).
     *  AS-IS mypage/orderList.html류와 동일한 기간/답례품명 필터를 쓴다. */
    @GetMapping("/my/qna")
    public String myQna(@RequestParam(required = false) String searchStartDate,
                         @RequestParam(required = false) String searchEndDate,
                         @RequestParam(required = false) String itemName,
                         HttpServletRequest request, Model model) {
        var authUserId = jwtVerifier.currentUserId(request);
        if (authUserId.isEmpty()) {
            return loginRedirect("/my/qna");
        }
        List<Inquiry> inquiries = inquiryService.myInquiries(authUserId.get());
        Map<Long, Gift> giftsById = giftService.giftsOf(inquiries.stream().map(Inquiry::getItemId).distinct().toList())
                .stream().collect(Collectors.toMap(Gift::getItemId, java.util.function.Function.identity()));

        var start = parseDate(searchStartDate);
        var end = parseDate(searchEndDate);
        var filtered = inquiries.stream()
                .filter(q -> start == null || !q.getCreatedDate().toLocalDate().isBefore(start))
                .filter(q -> end == null || !q.getCreatedDate().toLocalDate().isAfter(end))
                .filter(q -> {
                    if (itemName == null || itemName.isBlank()) {
                        return true;
                    }
                    Gift g = giftsById.get(q.getItemId());
                    return g != null && g.getItemName() != null && g.getItemName().contains(itemName);
                })
                .toList();

        model.addAttribute("inquiries", filtered);
        model.addAttribute("giftsById", giftsById);
        model.addAttribute("searchStartDate", searchStartDate);
        model.addAttribute("searchEndDate", searchEndDate);
        model.addAttribute("itemName", itemName);
        model.addAttribute("statusLabels", giftService.codesOf("GIFT_INQUIRY_STATUS"));
        return "my-qna";
    }

    @GetMapping("/my/inquiries")
    public String myInquiries(@RequestParam(required = false) Long sellerId, Model model) {
        model.addAttribute("sellerId", sellerId);
        if (sellerId != null) {
            model.addAttribute("inquiries", inquiryService.inquiriesForSeller(sellerId));
            model.addAttribute("statusLabels", giftService.codesOf("GIFT_INQUIRY_STATUS"));
        }
        return "inquiries";
    }

    @PostMapping("/inquiries/{inquiryId}/answer")
    public String answerInquiry(@PathVariable Long inquiryId, @RequestParam Long sellerId,
                                 @RequestParam String answer) {
        try {
            inquiryService.answer(inquiryId, answer);
        } catch (GiftException e) {
            return "redirect:/my/inquiries?sellerId=" + sellerId + "&errorMessage=" + encode(e.getMessage());
        }
        return "redirect:/my/inquiries?sellerId=" + sellerId;
    }

    @GetMapping("/my")
    public String myGifts(@RequestParam(required = false) Long sellerId, Model model) {
        model.addAttribute("sellerId", sellerId);
        if (sellerId != null) {
            model.addAttribute("gifts", giftService.myGifts(sellerId));
            model.addAttribute("statusLabels", giftService.codesOf("GIFT_STATUS"));
            model.addAttribute("categories", giftService.codesOf("GIFT_CATEGORY"));
        }
        return "my";
    }

    @PostMapping("/gifts/{itemId}/stop")
    public String stop(@PathVariable Long itemId, @RequestParam Long sellerId) {
        giftService.stop(itemId);
        return "redirect:/my?sellerId=" + sellerId;
    }

    @PostMapping("/gifts/{itemId}/stock")
    public String adjustStock(@PathVariable Long itemId, @RequestParam Long sellerId, @RequestParam int quantity) {
        giftService.adjustStock(itemId, quantity);
        return "redirect:/my?sellerId=" + sellerId;
    }

    @PostMapping("/wishlist")
    public String addWishlist(@RequestParam Long itemId, HttpServletRequest request) {
        var authUserId = jwtVerifier.currentUserId(request);
        if (authUserId.isEmpty()) {
            return loginRedirect("/gifts/" + itemId);
        }
        wishlistService.add(authUserId.get(), itemId);
        return "redirect:/gifts/" + itemId;
    }

    /** 답례품 목록 카드의 하트 아이콘 클릭 (AS-IS goods/index-main.html addToWishList, AJAX). */
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

    @GetMapping("/wishlist")
    public String wishlist(HttpServletRequest request, Model model) {
        var authUserId = jwtVerifier.currentUserId(request);
        if (authUserId.isEmpty()) {
            return loginRedirect("/wishlist");
        }
        List<WishlistService.WishlistItemView> wishlist = wishlistService.wishlistOf(authUserId.get());
        model.addAttribute("wishlist", wishlist);
        model.addAttribute("thumbnails", giftService.thumbnailsOf(
                wishlist.stream().map(w -> w.gift().getItemId()).toList()));
        model.addAttribute("locgovNames", locgovClient.namesByCode());
        return "wishlist";
    }

    @PostMapping("/wishlist/delete")
    public String removeWishlist(@RequestParam(required = false) List<Integer> wishlistIds,
                                  HttpServletRequest request) {
        var authUserId = jwtVerifier.currentUserId(request);
        if (authUserId.isEmpty()) {
            return loginRedirect("/wishlist");
        }
        if (wishlistIds != null && !wishlistIds.isEmpty()) {
            wishlistService.removeAll(wishlistIds, authUserId.get());
        }
        return "redirect:/wishlist";
    }

    @GetMapping("/admin")
    public String adminQueue(Model model) {
        model.addAttribute("gifts", giftService.pendingGifts());
        model.addAttribute("categories", giftService.codesOf("GIFT_CATEGORY"));
        return "admin";
    }

    @PostMapping("/gifts/{itemId}/approve")
    public String approve(@PathVariable Long itemId) {
        giftService.approve(itemId);
        return "redirect:/admin";
    }

    @PostMapping("/gifts/{itemId}/reject")
    public String reject(@PathVariable Long itemId) {
        giftService.reject(itemId);
        return "redirect:/admin";
    }

    /** 대표상품관리 (AS-IS opmanager/item/representative-item). */
    @GetMapping("/admin/representative-items")
    public String representativeItems(Model model) {
        model.addAttribute("gifts", giftService.representativeGifts());
        model.addAttribute("candidates", giftService.publicGifts(null, null));
        return "representative-items";
    }

    @PostMapping("/gifts/{itemId}/representative/register")
    public String registerRepresentative(@PathVariable Long itemId) {
        giftService.registerRepresentative(itemId);
        return "redirect:/admin/representative-items";
    }

    @PostMapping("/gifts/{itemId}/representative/delete")
    public String deleteRepresentative(@PathVariable Long itemId) {
        giftService.unregisterRepresentative(itemId);
        return "redirect:/admin/representative-items";
    }
}
