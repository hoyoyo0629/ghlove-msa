package com.ghlove.gift.web;

import com.ghlove.gift.domain.Gift;
import com.ghlove.gift.domain.ItemImage;
import com.ghlove.gift.domain.Review;
import com.ghlove.gift.domain.ReviewImage;
import com.ghlove.gift.domain.Seller;
import com.ghlove.gift.service.GiftException;
import com.ghlove.gift.service.GiftService;
import com.ghlove.gift.service.InquiryService;
import com.ghlove.gift.service.JwtVerifier;
import com.ghlove.gift.service.LocgovClient;
import com.ghlove.gift.service.ReviewService;
import com.ghlove.gift.service.WishlistService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** storefront(Vue3 SPA)용 답례품몰 JSON API - {@link GiftController}(Thymeleaf list/detail 화면)와
 *  완전히 같은 {@link GiftService}/{@link ReviewService}/{@link InquiryService} 로직을 재사용한다.
 *  찜 토글(POST /wishlist/{itemId}/toggle)은 이미 JSON이라 그대로 재사용하고 여기서 다시 만들지 않는다. */
@RestController
@RequiredArgsConstructor
public class GiftPublicApiController {

    private static final DateTimeFormatter REVIEW_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final Map<String, String> CATEGORY_ICONS = Map.of(
            "TOUR", "cli-icon_category01.png",
            "AGRI", "cli-icon_category02.png",
            "SEAFOOD", "cli-icon_category03.png",
            "PROCESSED", "cli-icon_category04.png",
            "LIVING", "cli-icon_category05.png",
            "VOUCHER", "cli-icon_category06.png");

    private final GiftService giftService;
    private final ReviewService reviewService;
    private final InquiryService inquiryService;
    private final WishlistService wishlistService;
    private final LocgovClient locgovClient;
    private final JwtVerifier jwtVerifier;

    public record GiftCardDto(Long itemId, String itemName, Integer salePrice, boolean soldOut,
                               String locgovCode, String locgovName, String thumbnailUrl,
                               boolean isNew, boolean wishlisted) {
    }

    public record ListResponse(String pageTitle, String selectedCategory, String q, String locgovCode,
                                Map<String, String> categories, Map<String, String> categoryIcons,
                                List<GiftCardDto> gifts) {
    }

    /** mode: null(전체 목록/카테고리/검색/지자체몰), "seasonal"(제철식품관), "community-business"(마을기업관). */
    @GetMapping("/api/gifts")
    public ListResponse list(@RequestParam(required = false) String categoryCode,
                              @RequestParam(required = false) String q,
                              @RequestParam(required = false) String locgovCode,
                              @RequestParam(required = false) String mode,
                              HttpServletRequest request) {
        String pageTitle = null;
        List<Gift> gifts;
        if ("seasonal".equals(mode)) {
            pageTitle = "제철식품관";
            gifts = giftService.seasonalGifts();
        } else if ("community-business".equals(mode)) {
            pageTitle = "마을기업관";
            gifts = giftService.communityBusinessGifts();
        } else {
            gifts = giftService.publicGifts(categoryCode, q, locgovCode);
        }

        List<Long> itemIds = gifts.stream().map(Gift::getItemId).toList();
        Map<Long, String> thumbnails = giftService.thumbnailsOf(itemIds);
        Map<String, String> locgovNames = locgovClient.namesByCode();
        Set<Long> newItemIds = giftService.newItemIds(itemIds);
        var authUserId = jwtVerifier.currentUserId(request);
        Set<Long> wishlistedItemIds = authUserId.isPresent()
                ? wishlistService.wishlistedItemIds(authUserId.get(), itemIds)
                : Set.of();

        List<GiftCardDto> cards = gifts.stream()
                .map(g -> {
                    String imageName = thumbnails.get(g.getItemId());
                    return new GiftCardDto(g.getItemId(), g.getItemName(), g.getSalePrice(),
                            "1".equals(g.getSoldOut()), g.getLocgovCode(), locgovNames.get(g.getLocgovCode()),
                            imageName != null ? "/uploads/" + imageName : null,
                            newItemIds.contains(g.getItemId()), wishlistedItemIds.contains(g.getItemId()));
                })
                .toList();

        return new ListResponse(pageTitle, categoryCode, q, locgovCode,
                giftService.codesOf("GIFT_CATEGORY"), CATEGORY_ICONS, cards);
    }

    public record ReviewDto(Long itemReviewId, Long userId, String userName, String createdDate,
                             Integer score, String subject, String content, List<String> imageUrls) {
    }

    public record InquiryDto(Long inquiryId, String status, String statusLabel, String secretYn,
                              String question, String answer) {
    }

    public record SellerDto(String companyName, String telephoneNumber) {
    }

    public record DetailResponse(Gift gift, List<String> imageUrls, String locgovName, String categoryLabel,
                                  SellerDto seller, boolean wishlisted, List<ReviewDto> reviews,
                                  double averageScore, List<InquiryDto> inquiries) {
    }

    @GetMapping("/api/gifts/{itemId}/detail")
    public ResponseEntity<?> detail(@PathVariable Long itemId, HttpServletRequest request) {
        Gift gift;
        try {
            gift = giftService.detail(itemId);
        } catch (GiftException e) {
            return ResponseEntity.notFound().build();
        }

        List<ItemImage> images = giftService.imagesOf(itemId);
        Seller seller = giftService.sellerOf(gift.getSellerId());
        var authUserId = jwtVerifier.currentUserId(request);
        boolean wishlisted = authUserId.isPresent()
                && wishlistService.wishlistedItemIds(authUserId.get(), List.of(itemId)).contains(itemId);

        List<Review> reviews = reviewService.reviewsOf(itemId);
        Map<Long, List<ReviewImage>> reviewImages = reviewService.imagesOf(reviews);
        double averageScore = reviewService.averageScore(reviews);
        List<ReviewDto> reviewDtos = reviews.stream()
                .map(r -> new ReviewDto(r.getItemReviewId(), r.getUserId(),
                        r.getUserName() != null ? r.getUserName() : ("회원#" + r.getUserId()),
                        REVIEW_DATE_FORMAT.format(r.getCreatedDate()), r.getScore(), r.getSubject(), r.getContent(),
                        reviewImages.getOrDefault(r.getItemReviewId(), List.of()).stream()
                                .map(ri -> "/uploads/" + ri.getReviewImage()).toList()))
                .toList();

        Map<String, String> inquiryStatusLabels = giftService.codesOf("GIFT_INQUIRY_STATUS");
        List<InquiryDto> inquiryDtos = inquiryService.inquiriesOf(itemId).stream()
                .map(q -> new InquiryDto(q.getInquiryId(), q.getStatus(),
                        inquiryStatusLabels.getOrDefault(q.getStatus(), q.getStatus()), q.getSecretYn(),
                        q.getQuestion(), q.getAnswer()))
                .toList();

        DetailResponse response = new DetailResponse(gift,
                images.stream().map(img -> "/uploads/" + img.getImageName()).toList(),
                locgovClient.namesByCode().get(gift.getLocgovCode()),
                giftService.codesOf("GIFT_CATEGORY").get(gift.getCategoryCode()),
                seller != null ? new SellerDto(seller.getCompanyName(), seller.getTelephoneNumber()) : null,
                wishlisted, reviewDtos, averageScore, inquiryDtos);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/api/gifts/{itemId}/reviews", consumes = "multipart/form-data")
    public ResponseEntity<?> writeReview(@PathVariable Long itemId,
                                          @RequestParam(required = false) String userName,
                                          @RequestParam(required = false) String orderCode,
                                          @RequestParam String subject, @RequestParam String content,
                                          @RequestParam Integer score,
                                          @RequestParam(required = false, defaultValue = "false") boolean recommend,
                                          @RequestParam(required = false) List<MultipartFile> images,
                                          HttpServletRequest request) {
        var authUserId = jwtVerifier.currentUserId(request);
        if (authUserId.isEmpty()) {
            return ResponseEntity.status(401).build();
        }
        try {
            Gift gift = giftService.detail(itemId);
            reviewService.write(itemId, gift.getSellerId(), authUserId.get(), userName, orderCode,
                    subject, content, score, recommend, images);
            return ResponseEntity.noContent().build();
        } catch (GiftException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    public record InquiryRequest(String question, boolean secret) {
    }

    @PostMapping("/api/gifts/{itemId}/inquiries")
    public ResponseEntity<?> askInquiry(@PathVariable Long itemId, @RequestBody InquiryRequest req,
                                         HttpServletRequest request) {
        var authUserId = jwtVerifier.currentUserId(request);
        if (authUserId.isEmpty()) {
            return ResponseEntity.status(401).build();
        }
        try {
            inquiryService.ask(itemId, authUserId.get(), req.question(), req.secret());
            return ResponseEntity.noContent().build();
        } catch (GiftException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
