package com.ghlove.gift.web;

import com.ghlove.gift.domain.Seller;
import com.ghlove.gift.repository.InquiryRepository;
import com.ghlove.gift.repository.GiftRepository;
import com.ghlove.gift.repository.ReviewRepository;
import com.ghlove.gift.repository.SellerRepository;
import com.ghlove.gift.repository.WishlistRepository;
import com.ghlove.gift.service.CategoryTreeGroup;
import com.ghlove.gift.service.GiftService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Read-only JSON API for other services (member 마이페이지 "관심답례품"/"답례품 후기"/"답례품Q&A" 건수,
 *  order의 장바구니 GNB "전체 카테고리" 메가메뉴, admin 정산화면의 입금계좌 표시) - not part of any write path. */
@RestController
@RequiredArgsConstructor
public class GiftApiController {

    private final ReviewRepository reviewRepository;
    private final InquiryRepository inquiryRepository;
    private final WishlistRepository wishlistRepository;
    private final GiftService giftService;
    private final SellerRepository sellerRepository;
    private final GiftRepository giftRepository;

    @GetMapping("/api/my-summary")
    public MySummaryDto mySummary(@RequestParam Long userId) {
        return new MySummaryDto(reviewRepository.countByUserId(userId), inquiryRepository.countByUserId(userId),
                wishlistRepository.countByUserId(userId));
    }

    @GetMapping("/api/categories")
    public List<CategoryTreeGroup> categories() {
        return giftService.categoryTree();
    }

    /** order 서비스의 장바구니/체크아웃이 가격·재고·품절여부를 확인할 때 쓰는 읽기전용 조회
     *  (order.saga 쓰기경로가 아니라 단순 참조용 동기 호출, GiftClient.fetch() 참고). */
    @GetMapping("/api/gifts/{itemId}")
    public ResponseEntity<GiftItemInfoDto> gift(@PathVariable Long itemId) {
        return giftRepository.findById(itemId)
                .map(g -> {
                    String imageName = giftService.thumbnailsOf(List.of(itemId)).get(itemId);
                    String thumbnailPath = imageName != null ? "/uploads/" + imageName : null;
                    return ResponseEntity.ok(new GiftItemInfoDto(g.getItemId(), g.getItemName(), g.getSellerId(),
                            g.getSalePrice(), g.getStockQuantity(), g.getSoldOut(), g.getDataStatusCode(),
                            g.getLocgovCode(), thumbnailPath));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    public record GiftItemInfoDto(Long itemId, String itemName, Long sellerId, Integer salePrice,
                                   Integer stockQuantity, String soldOut, String dataStatusCode,
                                   String locgovCode, String thumbnailPath) {
    }

    /** admin "정산" 상세화면의 입금계좌 표시용 - AS-IS remittance가 확정 시점에 제공자
     *  계좌를 스냅샷했던 것과 동일한 목적, 여기선 그냥 실시간 조회로 대체한다. */
    @GetMapping("/api/sellers/{sellerId}")
    public ResponseEntity<SellerDto> seller(@PathVariable Long sellerId) {
        return sellerRepository.findById(sellerId)
                .map(s -> ResponseEntity.ok(new SellerDto(s.getSellerId(), s.getSellerName(),
                        s.getBankName(), s.getBankInName(), s.getBankAccountNumber())))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /** admin StatsService ReadModel 재동기화용 (SFR-009 "원장 복구·재동기화 절차") - 답례품
     *  전체 현재 상태 스냅샷. */
    @GetMapping("/api/admin/items/all")
    public List<ItemSnapshotDto> allForResync() {
        return giftRepository.findAll().stream()
                .map(g -> new ItemSnapshotDto(g.getItemId(), g.getSellerId(), g.getCategoryCode(),
                        g.getDataStatusCode(), g.getSalePrice(), g.getLocgovCode(), g.getItemName(), g.getBrandId()))
                .toList();
    }

    public record ItemSnapshotDto(Long itemId, Long sellerId, String categoryCode,
                                   String dataStatusCode, Integer salePrice, String locgovCode,
                                   String itemName, Integer brandId) {
    }

    /** shop-statistics "답례품 선호도" 화면용 - 전체 찜(관심답례품) 원본 목록, admin이 상품별로 집계. */
    @GetMapping("/api/admin/wishlist/all")
    public List<WishlistDto> wishlistAll() {
        return wishlistRepository.findAll().stream()
                .map(w -> new WishlistDto(w.getWishlistId(), w.getItemId(), w.getUserId(), w.getCreatedDate()))
                .toList();
    }

    public record WishlistDto(Integer wishlistId, Long itemId, Long userId, String createdDate) {
    }

    public record MySummaryDto(int reviewCount, int inquiryCount, int wishlistCount) {
    }

    public record SellerDto(Long sellerId, String sellerName, String bankName, String bankInName,
                             String bankAccountNumber) {
    }
}
