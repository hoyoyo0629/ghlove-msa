package com.ghlove.gift.web;

import com.ghlove.gift.domain.Gift;
import com.ghlove.gift.domain.Review;
import com.ghlove.gift.service.GiftException;
import com.ghlove.gift.service.GiftService;
import com.ghlove.gift.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 답례품 리뷰 관리자 대응 (AS-IS opmanager/item review 승인/추천/CSV 다운로드) - 답례품
 * 상품관리 2단계의 마지막 항목. cross-service API - admin 콘솔(/admin/gift-reviews)이 사용한다.
 */
@RestController
@RequestMapping("/api/admin/gift-reviews")
@RequiredArgsConstructor
public class ReviewAdminApiController {

    private final ReviewService reviewService;
    private final GiftService giftService;

    @GetMapping
    public List<Review> search(@RequestParam(required = false) Long itemId,
                                @RequestParam(required = false) String keyword,
                                @RequestParam(required = false) String recommendFlag,
                                @RequestParam(required = false) String displayFlag) {
        return reviewService.adminSearch(itemId, keyword, recommendFlag, displayFlag);
    }

    /** 검색 결과에 등장하는 답례품ID -> 답례품명 매핑 (목록 화면에서 리뷰별 답례품명 표시용). */
    @GetMapping("/item-names")
    public Map<Long, String> itemNames(@RequestParam List<Long> itemIds) {
        return giftService.giftsOf(itemIds).stream()
                .collect(Collectors.toMap(Gift::getItemId, Gift::getItemName, (a, b) -> a));
    }

    /** 신고 건수 (SFR-005 재검토 라운드) - 목록 화면에서 리뷰별 신고 건수 표시용. */
    @GetMapping("/report-counts")
    public Map<Long, Long> reportCounts(@RequestParam List<Long> reviewIds) {
        return reviewService.reportCountsOf(reviewIds);
    }

    @PostMapping("/{id}/recommend")
    public Review recommend(@PathVariable Long id, @RequestParam boolean recommend) {
        try {
            return reviewService.setRecommend(id, recommend);
        } catch (GiftException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PostMapping("/{id}/display")
    public Review display(@PathVariable Long id, @RequestParam boolean display) {
        try {
            return reviewService.setDisplay(id, display);
        } catch (GiftException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> export(@RequestParam(required = false) Long itemId,
                                          @RequestParam(required = false) String keyword,
                                          @RequestParam(required = false) String recommendFlag,
                                          @RequestParam(required = false) String displayFlag) {
        List<Review> reviews = reviewService.adminSearch(itemId, keyword, recommendFlag, displayFlag);
        List<Long> itemIds = reviews.stream().map(Review::getItemId).distinct().toList();
        Map<Long, String> itemNames = giftService.giftsOf(itemIds).stream()
                .collect(Collectors.toMap(Gift::getItemId, Gift::getItemName, (a, b) -> a));
        String csv = reviewService.exportCsv(reviews, itemNames);
        byte[] bytes = csv.getBytes(StandardCharsets.UTF_8);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
        headers.setContentDispositionFormData("attachment", "gift-reviews.csv");
        return ResponseEntity.ok().headers(headers).body(bytes);
    }
}
