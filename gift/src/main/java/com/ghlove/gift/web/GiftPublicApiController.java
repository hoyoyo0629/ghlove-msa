package com.ghlove.gift.web;

import com.ghlove.gift.domain.Gift;
import com.ghlove.gift.domain.ItemImage;
import com.ghlove.gift.domain.Review;
import com.ghlove.gift.domain.ReviewImage;
import com.ghlove.gift.domain.Seller;
import com.ghlove.gift.service.GiftException;
import com.ghlove.gift.service.GiftOptionService;
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
    private final GiftOptionService giftOptionService;
    private final WishlistService wishlistService;
    private final LocgovClient locgovClient;
    private final JwtVerifier jwtVerifier;
    private final com.ghlove.gift.service.RestockNoticeService restockNoticeService;

    public record GiftCardDto(Long itemId, String itemName, Integer salePrice, boolean soldOut,
                               String locgovCode, String locgovName, String thumbnailUrl,
                               boolean isNew, boolean wishlisted) {
    }

    public record ListResponse(String pageTitle, String selectedCategory, String q, String locgovCode,
                                Map<String, String> categories, Map<String, String> categoryIcons,
                                List<GiftCardDto> gifts) {
    }

    /** 제철식품관 첫 화면의 월별 키워드 카드(1~12월). 각 월을 클릭하면 /api/gifts?mode=seasonal&month=N 으로
     *  그 달의 제철 답례품을 조회한다(AS-IS seasonList의 dataList + monthClick 흐름). */
    @GetMapping("/api/season-food")
    public List<GiftService.SeasonFoodKeyword> seasonFood() {
        return giftService.seasonFoodKeywords();
    }

    /** mode: null(전체 목록/카테고리/검색/지자체몰), "seasonal"(제철식품관), "community-business"(마을기업관). */
    @GetMapping("/api/gifts")
    public ListResponse list(@RequestParam(required = false) String categoryCode,
                              @RequestParam(required = false) String q,
                              @RequestParam(required = false) String locgovCode,
                              @RequestParam(required = false) String mode,
                              @RequestParam(required = false) Integer month,
                              HttpServletRequest request) {
        String pageTitle = null;
        List<Gift> gifts;
        if ("seasonal".equals(mode)) {
            pageTitle = "제철식품관";
            gifts = giftService.seasonalGifts(month, locgovCode);
        } else if ("community-business".equals(mode)) {
            pageTitle = "마을기업관";
            gifts = giftService.communityBusinessGifts(locgovCode);
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

        // AS-IS op_item_option_soldout 요약이 하던 "아이템 단위 옵션 품절" 판정을 조회시점 계산으로 재현:
        // 아이템 재고는 남아도 표시 옵션이 전부 품절이면 목록에서 품절로 뱃지한다.
        Set<Long> optionSoldOutItemIds = giftOptionService.optionSoldOutItemIds(itemIds);

        List<GiftCardDto> cards = gifts.stream()
                .map(g -> {
                    String imageName = thumbnails.get(g.getItemId());
                    boolean soldOut = "1".equals(g.getSoldOut()) || optionSoldOutItemIds.contains(g.getItemId());
                    return new GiftCardDto(g.getItemId(), g.getItemName(), g.getSalePrice(),
                            soldOut, g.getLocgovCode(), locgovNames.get(g.getLocgovCode()),
                            imageName != null ? "/uploads/" + imageName : null,
                            newItemIds.contains(g.getItemId()), wishlistedItemIds.contains(g.getItemId()));
                })
                .toList();

        return new ListResponse(pageTitle, categoryCode, q, locgovCode,
                giftService.codesOf("GIFT_CATEGORY"), CATEGORY_ICONS, cards);
    }

    public record ReviewDto(Long itemReviewId, Long userId, String userName, String createdDate,
                             Integer score, String subject, String content, List<String> imageUrls,
                             boolean reportedByMe, Integer likeCount, boolean likedByMe) {
    }

    public record InquiryDto(Long inquiryId, String status, String statusLabel, String secretYn,
                              String question, String answer, boolean reportedByMe) {
    }

    public record SellerDto(String companyName, String telephoneNumber) {
    }

    /** SFR-005 "카탈로그 관리: 옵션" - AS-IS와 동일하게 S/S2/S3/T 지원. optionName(=name1)은
     *  기존 단일옵션 storefront 호환용, optionName2/3은 조합형(S2·S3)용. 옵션형태(itemOptionType)와
     *  각인(itemTextOptionFlag/Title1~3)은 응답의 gift 필드에 실려 나간다. */
    public record OptionDto(Long itemOptionId, String optionType, String optionName, String optionName2,
                             String optionName3, Integer optionPrice, boolean soldOut,
                             boolean stockTracked, Integer stockQuantity) {
    }

    /** 추가구성 답례품(op_item_addition) - 본품과 별도로 함께 담는 부가품. */
    public record AdditionDto(Long itemId, String itemName, Integer salePrice, boolean soldOut) {
    }

    public record DetailResponse(Gift gift, List<String> imageUrls, String locgovName, String categoryLabel,
                                  SellerDto seller, boolean wishlisted, List<ReviewDto> reviews,
                                  double averageScore, List<InquiryDto> inquiries, List<OptionDto> options,
                                  List<AdditionDto> additions, boolean restockRequested) {
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
                                .map(ri -> "/uploads/" + ri.getReviewImage()).toList(),
                        authUserId.isPresent() && reviewService.reportedBy(r.getItemReviewId(), authUserId.get()),
                        r.getLikeCount() == null ? 0 : r.getLikeCount(),
                        reviewService.likedBy(r.getItemReviewId().intValue(), authUserId.orElse(null))))
                .toList();

        Map<String, String> inquiryStatusLabels = giftService.codesOf("GIFT_INQUIRY_STATUS");
        List<InquiryDto> inquiryDtos = inquiryService.inquiriesOf(itemId).stream()
                .map(q -> new InquiryDto(q.getInquiryId(), q.getStatus(),
                        inquiryStatusLabels.getOrDefault(q.getStatus(), q.getStatus()), q.getSecretYn(),
                        q.getQuestion(), q.getAnswer(),
                        authUserId.isPresent() && inquiryService.reportedBy(q.getInquiryId(), authUserId.get())))
                .toList();

        List<OptionDto> optionDtos = giftOptionService.optionsOf(itemId).stream()
                .map(o -> new OptionDto(o.getItemOptionId(), o.getOptionType(), o.getOptionName1(),
                        o.getOptionName2(), o.getOptionName3(), o.getOptionPrice(),
                        "Y".equals(o.getOptionSoldOutFlag()),
                        "Y".equals(o.getOptionStockFlag()), o.getOptionStockQuantity()))
                .toList();

        List<AdditionDto> additionDtos = giftOptionService.additionsOf(itemId).stream()
                .map(a -> new AdditionDto(a.getItemId(), a.getItemName(), a.getSalePrice(),
                        "Y".equals(a.getSoldOut())))
                .toList();

        DetailResponse response = new DetailResponse(gift,
                images.stream().map(img -> "/uploads/" + img.getImageName()).toList(),
                locgovClient.namesByCode().get(gift.getLocgovCode()),
                giftService.codesOf("GIFT_CATEGORY").get(gift.getCategoryCode()),
                seller != null ? new SellerDto(seller.getCompanyName(), seller.getTelephoneNumber()) : null,
                wishlisted, reviewDtos, averageScore, inquiryDtos, optionDtos, additionDtos,
                authUserId.isPresent() && restockNoticeService.isRequested(itemId.intValue(), authUserId.get()));
        return ResponseEntity.ok(response);
    }

    /** order 서비스가 장바구니 담기 시 옵션명·추가금액을 스냅샷하려고 호출하는 단건 옵션 조회. */
    public record OptionInfoDto(Long itemOptionId, Long itemId, String optionName, Integer optionPrice, boolean soldOut) {
    }

    @GetMapping("/api/gifts/options/{itemOptionId}")
    public ResponseEntity<?> option(@PathVariable Long itemOptionId) {
        try {
            var o = giftOptionService.optionById(itemOptionId);
            return ResponseEntity.ok(new OptionInfoDto(o.getItemOptionId(), o.getItemId(), combinedOptionName(o),
                    o.getOptionPrice(), "Y".equals(o.getOptionSoldOutFlag())));
        } catch (GiftException e) {
            return ResponseEntity.status(404).body(java.util.Map.of("message", e.getMessage()));
        }
    }

    /** 조합형(S2·S3) 옵션명은 name1/2/3을 " / "로 이어 붙여 스냅샷한다(AS-IS 옵션명 표기). */
    private String combinedOptionName(com.ghlove.gift.domain.GiftOption o) {
        StringBuilder sb = new StringBuilder(o.getOptionName1() == null ? "" : o.getOptionName1());
        if (o.getOptionName2() != null && !o.getOptionName2().isBlank()) {
            sb.append(" / ").append(o.getOptionName2());
        }
        if (o.getOptionName3() != null && !o.getOptionName3().isBlank()) {
            sb.append(" / ").append(o.getOptionName3());
        }
        return sb.toString();
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

    public record ReportRequest(String reason) {
    }

    /** 리뷰 신고 (SFR-005). */
    @PostMapping("/api/gifts/{itemId}/reviews/{reviewId}/report")
    public ResponseEntity<?> reportReview(@PathVariable Long itemId, @PathVariable Long reviewId,
                                           @RequestBody(required = false) ReportRequest req, HttpServletRequest request) {
        var authUserId = jwtVerifier.currentUserId(request);
        if (authUserId.isEmpty()) {
            return ResponseEntity.status(401).build();
        }
        try {
            reviewService.report(reviewId, authUserId.get(), req != null ? req.reason() : null);
            return ResponseEntity.noContent().build();
        } catch (GiftException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /** 문의 신고 (SFR-005). */
    @PostMapping("/api/gifts/{itemId}/inquiries/{inquiryId}/report")
    public ResponseEntity<?> reportInquiry(@PathVariable Long itemId, @PathVariable Long inquiryId,
                                            @RequestBody(required = false) ReportRequest req, HttpServletRequest request) {
        var authUserId = jwtVerifier.currentUserId(request);
        if (authUserId.isEmpty()) {
            return ResponseEntity.status(401).build();
        }
        try {
            inquiryService.report(inquiryId, authUserId.get(), req != null ? req.reason() : null);
            return ResponseEntity.noContent().build();
        } catch (GiftException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
