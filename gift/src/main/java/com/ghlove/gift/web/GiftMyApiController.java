package com.ghlove.gift.web;

import com.ghlove.gift.domain.Gift;
import com.ghlove.gift.domain.Inquiry;
import com.ghlove.gift.domain.Review;
import com.ghlove.gift.service.GiftService;
import com.ghlove.gift.service.InquiryService;
import com.ghlove.gift.service.JwtVerifier;
import com.ghlove.gift.service.LocgovClient;
import com.ghlove.gift.service.ReviewService;
import com.ghlove.gift.service.WishlistService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

/** storefront(Vue3 SPA)용 "관심답례품"/"답례품 후기"/"답례품Q&A" 마이페이지 JSON API -
 * {@link GiftController}(Thymeleaf)의 `/wishlist`, `/my/reviews`, `/my/qna` GET과 완전히
 * 같은 조합·필터링 로직을 재사용한다. 담기/찜취소는 이미 JSON인
 * `POST /wishlist/{itemId}/toggle`을 그대로 재사용한다(이 컨트롤러엔 그 액션을 다시 만들지 않음). */
@RestController
@RequiredArgsConstructor
public class GiftMyApiController {

    private final WishlistService wishlistService;
    private final GiftService giftService;
    private final ReviewService reviewService;
    private final InquiryService inquiryService;
    private final LocgovClient locgovClient;
    private final JwtVerifier jwtVerifier;

    public record WishlistRowDto(Integer wishlistId, Long itemId, String itemName, Integer salePrice,
                                  boolean soldOut, boolean onSale, String locgovCode, String locgovName,
                                  String thumbnailUrl) {
    }

    @GetMapping("/api/wishlist")
    public ResponseEntity<List<WishlistRowDto>> myWishlist(HttpServletRequest request) {
        var authUserId = jwtVerifier.currentUserId(request);
        if (authUserId.isEmpty()) {
            return ResponseEntity.status(401).build();
        }
        List<WishlistService.WishlistItemView> wishlist = wishlistService.wishlistOf(authUserId.get());
        var thumbnails = giftService.thumbnailsOf(wishlist.stream().map(w -> w.gift().getItemId()).toList());
        var locgovNames = locgovClient.namesByCode();

        var rows = wishlist.stream().map(w -> {
            Gift g = w.gift();
            return new WishlistRowDto(w.wishlistId(), g.getItemId(), g.getItemName(), g.getSalePrice(),
                    "1".equals(g.getSoldOut()), "APPROVED".equals(g.getDataStatusCode()), g.getLocgovCode(),
                    locgovNames.get(g.getLocgovCode()), thumbnails.get(g.getItemId()));
        }).toList();
        return ResponseEntity.ok(rows);
    }

    private static LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public record ReviewRowDto(Long itemReviewId, Long itemId, String itemName, String thumbnailUrl,
                                Integer score, String subject, String content, String createdDateDisplay) {
    }

    @GetMapping("/api/my/reviews")
    public ResponseEntity<List<ReviewRowDto>> myReviews(@RequestParam(required = false) String searchStartDate,
                                                          @RequestParam(required = false) String searchEndDate,
                                                          @RequestParam(required = false) String itemName,
                                                          HttpServletRequest request) {
        var authUserId = jwtVerifier.currentUserId(request);
        if (authUserId.isEmpty()) {
            return ResponseEntity.status(401).build();
        }
        List<Review> reviews = reviewService.myReviews(authUserId.get());
        Map<Long, Gift> giftsById = giftService.giftsOf(reviews.stream().map(Review::getItemId).distinct().toList())
                .stream().collect(java.util.stream.Collectors.toMap(Gift::getItemId, g -> g, (a, b) -> a));
        var thumbnails = giftService.thumbnailsOf(giftsById.keySet().stream().toList());

        LocalDate start = parseDate(searchStartDate);
        LocalDate end = parseDate(searchEndDate);
        var rows = reviews.stream()
                .filter(r -> start == null || !r.getCreatedDate().toLocalDate().isBefore(start))
                .filter(r -> end == null || !r.getCreatedDate().toLocalDate().isAfter(end))
                .filter(r -> {
                    if (itemName == null || itemName.isBlank()) {
                        return true;
                    }
                    Gift g = giftsById.get(r.getItemId());
                    return g != null && g.getItemName() != null && g.getItemName().contains(itemName);
                })
                .map(r -> {
                    Gift g = giftsById.get(r.getItemId());
                    return new ReviewRowDto(r.getItemReviewId(), r.getItemId(), g != null ? g.getItemName() : "-",
                            thumbnails.get(r.getItemId()), r.getScore(), r.getSubject(), r.getContent(),
                            r.getCreatedDate().toLocalDate().toString());
                })
                .toList();
        return ResponseEntity.ok(rows);
    }

    public record InquiryRowDto(Long inquiryId, Long itemId, String itemName, String question, boolean secret,
                                 String answer, String status, String statusLabel, String createdDateDisplay) {
    }

    @GetMapping("/api/my/qna")
    public ResponseEntity<List<InquiryRowDto>> myQna(@RequestParam(required = false) String searchStartDate,
                                                       @RequestParam(required = false) String searchEndDate,
                                                       @RequestParam(required = false) String itemName,
                                                       HttpServletRequest request) {
        var authUserId = jwtVerifier.currentUserId(request);
        if (authUserId.isEmpty()) {
            return ResponseEntity.status(401).build();
        }
        List<Inquiry> inquiries = inquiryService.myInquiries(authUserId.get());
        Map<Long, Gift> giftsById = giftService.giftsOf(inquiries.stream().map(Inquiry::getItemId).distinct().toList())
                .stream().collect(java.util.stream.Collectors.toMap(Gift::getItemId, g -> g, (a, b) -> a));
        Map<String, String> statusLabels = giftService.codesOf("GIFT_INQUIRY_STATUS");

        LocalDate start = parseDate(searchStartDate);
        LocalDate end = parseDate(searchEndDate);
        var rows = inquiries.stream()
                .filter(q -> start == null || !q.getCreatedDate().toLocalDate().isBefore(start))
                .filter(q -> end == null || !q.getCreatedDate().toLocalDate().isAfter(end))
                .filter(q -> {
                    if (itemName == null || itemName.isBlank()) {
                        return true;
                    }
                    Gift g = giftsById.get(q.getItemId());
                    return g != null && g.getItemName() != null && g.getItemName().contains(itemName);
                })
                .map(q -> {
                    Gift g = giftsById.get(q.getItemId());
                    boolean secret = "Y".equals(q.getSecretYn());
                    return new InquiryRowDto(q.getInquiryId(), q.getItemId(), g != null ? g.getItemName() : "-",
                            secret ? null : q.getQuestion(), secret, q.getAnswer(), q.getStatus(),
                            statusLabels.getOrDefault(q.getStatus(), q.getStatus()),
                            q.getCreatedDate().toLocalDate().toString());
                })
                .toList();
        return ResponseEntity.ok(rows);
    }
}
