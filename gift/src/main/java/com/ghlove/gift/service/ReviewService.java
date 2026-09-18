package com.ghlove.gift.service;

import com.ghlove.gift.domain.Review;
import com.ghlove.gift.domain.ReviewImage;
import com.ghlove.gift.domain.ReviewReport;
import com.ghlove.gift.repository.ReviewImageRepository;
import com.ghlove.gift.repository.ReviewReportRepository;
import com.ghlove.gift.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private static final String DISPLAY_ON = "Y";
    private static final String RECOMMEND_YES = "Y";
    private static final String RECOMMEND_NO = "N";

    private final ReviewRepository reviewRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final ReviewReportRepository reviewReportRepository;
    private final com.ghlove.gift.repository.ReviewLikeRepository reviewLikeRepository;
    private final FileStorageService fileStorageService;

    public List<Review> reviewsOf(Long itemId) {
        return reviewRepository.findByItemIdAndDisplayFlagOrderByCreatedDateDesc(itemId, DISPLAY_ON);
    }

    /** 마이페이지 "답례품 후기" - 본인이 작성한 리뷰. */
    public List<Review> myReviews(Long userId) {
        return reviewRepository.findByUserIdOrderByCreatedDateDesc(userId);
    }

    /** 리뷰 ID -> 첨부 이미지 목록. */
    public Map<Long, List<ReviewImage>> imagesOf(List<Review> reviews) {
        List<Long> reviewIds = reviews.stream().map(Review::getItemReviewId).collect(Collectors.toList());
        if (reviewIds.isEmpty()) {
            return Map.of();
        }
        return reviewImageRepository.findByItemReviewIdInOrderByOrderingAsc(reviewIds).stream()
                .collect(Collectors.groupingBy(ReviewImage::getItemReviewId));
    }

    public double averageScore(List<Review> reviews) {
        return reviews.stream().mapToInt(Review::getScore).average().orElse(0);
    }

    @Transactional
    public Review write(Long itemId, Long sellerId, Long userId, String userName, String orderCode,
                         String subject, String content, Integer score, boolean recommend,
                         List<MultipartFile> images) {
        if (userId == null || userId <= 0) {
            throw new GiftException("작성자 ID를 입력해 주세요.");
        }
        if (subject == null || subject.isBlank()) {
            throw new GiftException("제목을 입력해 주세요.");
        }
        if (content == null || content.isBlank()) {
            throw new GiftException("내용을 입력해 주세요.");
        }
        if (score == null || score < 1 || score > 5) {
            throw new GiftException("평점은 1~5 사이여야 합니다.");
        }

        Review review = new Review();
        review.setItemId(itemId);
        review.setSellerId(sellerId);
        review.setUserId(userId);
        review.setUserName(userName);
        // ORDER_CODE는 DB에 NOT NULL DEFAULT '0'이지만, Hibernate가 null을 명시적으로 바인딩하면
        // DEFAULT가 적용되지 않고 그대로 제약조건 위반이 난다 - 리뷰를 실제로 등록해보다가 발견함.
        review.setOrderCode(orderCode == null || orderCode.isBlank() ? "0" : orderCode);
        review.setSubject(subject);
        review.setContent(content);
        review.setScore(score);
        review.setRecommendFlag(recommend ? RECOMMEND_YES : RECOMMEND_NO);
        review.setDisplayFlag(DISPLAY_ON);
        review.setCreatedDate(LocalDateTime.now());
        Review saved = reviewRepository.save(review);

        if (images != null) {
            int ordering = 0;
            for (MultipartFile file : images) {
                if (file == null || file.isEmpty()) {
                    continue;
                }
                String storedName = fileStorageService.store(file);
                ReviewImage reviewImage = new ReviewImage();
                reviewImage.setItemReviewId(saved.getItemReviewId());
                reviewImage.setReviewImage(storedName);
                reviewImage.setOrdering(ordering++);
                reviewImage.setCreatedDate(LocalDateTime.now());
                reviewImageRepository.save(reviewImage);
            }
        }
        return saved;
    }

    /** 리뷰 신고 (SFR-005) - 회원 1인당 리뷰 1건에 중복 신고할 수 없다. */
    @Transactional
    public void report(Long itemReviewId, Long userId, String reason) {
        if (!reviewRepository.existsById(itemReviewId)) {
            throw new GiftException("리뷰를 찾을 수 없습니다.");
        }
        if (reviewReportRepository.existsByItemReviewIdAndUserId(itemReviewId, userId)) {
            throw new GiftException("이미 신고한 리뷰입니다.");
        }
        ReviewReport report = new ReviewReport();
        report.setItemReviewId(itemReviewId);
        report.setUserId(userId);
        report.setReason(reason);
        report.setCreatedDate(LocalDateTime.now());
        reviewReportRepository.save(report);
    }

    /** 리뷰ID -> 신고 건수 (admin 목록화면용). */
    public Map<Long, Long> reportCountsOf(List<Long> reviewIds) {
        if (reviewIds.isEmpty()) {
            return Map.of();
        }
        return reviewReportRepository.findByItemReviewIdIn(reviewIds).stream()
                .collect(Collectors.groupingBy(ReviewReport::getItemReviewId, Collectors.counting()));
    }

    public boolean reportedBy(Long itemReviewId, Long userId) {
        return reviewReportRepository.existsByItemReviewIdAndUserId(itemReviewId, userId);
    }

    // ---- 답례품 상품관리 2단계: 리뷰 관리자 대응 (AS-IS opmanager/item review 승인/추천/CSV
    // 다운로드) - 데이터 규모가 작아 다른 admin 목록화면들과 동일하게 인메모리 필터링으로 처리한다. ----

    public List<Review> adminSearch(Long itemId, String keyword, String recommendFlag, String displayFlag) {
        return reviewRepository.findAll().stream()
                .filter(r -> itemId == null || itemId.equals(r.getItemId()))
                .filter(r -> keyword == null || keyword.isBlank()
                        || (r.getSubject() != null && r.getSubject().contains(keyword))
                        || (r.getContent() != null && r.getContent().contains(keyword)))
                .filter(r -> recommendFlag == null || recommendFlag.isBlank() || recommendFlag.equals(r.getRecommendFlag()))
                .filter(r -> displayFlag == null || displayFlag.isBlank() || displayFlag.equals(r.getDisplayFlag()))
                .sorted(Comparator.comparing(Review::getItemReviewId, Comparator.reverseOrder()))
                .toList();
    }

    /** 리뷰 추천(채택) 처리 - AS-IS RECOMMEND_FLAG. */
    @Transactional
    public Review setRecommend(Long reviewId, boolean recommend) {
        Review r = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new GiftException("리뷰를 찾을 수 없습니다."));
        r.setRecommendFlag(recommend ? RECOMMEND_YES : RECOMMEND_NO);
        return reviewRepository.save(r);
    }

    /** 부적절한 리뷰 블라인드 처리(비노출)/복원. */
    @Transactional
    public Review setDisplay(Long reviewId, boolean display) {
        Review r = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new GiftException("리뷰를 찾을 수 없습니다."));
        r.setDisplayFlag(display ? DISPLAY_ON : "N");
        return reviewRepository.save(r);
    }

    /** 엑셀 다운로드(CSV) - POI 등 엑셀 라이브러리가 프로젝트에 없어 CSV로 실행한다. */
    public String exportCsv(List<Review> reviews, Map<Long, String> itemNames) {
        StringBuilder sb = new StringBuilder("﻿");
        sb.append("리뷰ID,답례품ID,답례품명,작성자,평점,추천,노출,제목,내용,작성일\n");
        for (Review r : reviews) {
            sb.append(csvCell(String.valueOf(r.getItemReviewId()))).append(',')
                    .append(csvCell(String.valueOf(r.getItemId()))).append(',')
                    .append(csvCell(itemNames.getOrDefault(r.getItemId(), ""))).append(',')
                    .append(csvCell(r.getUserName())).append(',')
                    .append(csvCell(String.valueOf(r.getScore()))).append(',')
                    .append(csvCell(r.getRecommendFlag())).append(',')
                    .append(csvCell(r.getDisplayFlag())).append(',')
                    .append(csvCell(r.getSubject())).append(',')
                    .append(csvCell(r.getContent())).append(',')
                    .append(csvCell(r.getCreatedDate() == null ? "" : r.getCreatedDate().toString()))
                    .append('\n');
        }
        return sb.toString();
    }

    private String csvCell(String value) {
        if (value == null) {
            return "";
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    /** 마이페이지 "답례품 후기"의 행별 삭제 (AS-IS mypage/review.html deleteReview) - 본인이 쓴
     * 후기만 지울 수 있다. 첨부 이미지와 신고 이력은 후기에 딸린 행이라 같이 정리한다
     * (업로드된 파일 자체는 다른 곳에서 참조될 수 있어 건드리지 않는다). */
    @Transactional
    public void deleteMine(Long itemReviewId, Long userId) {
        Review review = reviewRepository.findById(itemReviewId)
                .orElseThrow(() -> new GiftException("후기를 찾을 수 없습니다."));
        if (!userId.equals(review.getUserId())) {
            throw new GiftException("본인이 작성한 후기만 삭제할 수 있습니다.");
        }
        reviewImageRepository.deleteAll(reviewImageRepository.findByItemReviewIdOrderByOrderingAsc(itemReviewId));
        reviewReportRepository.deleteAll(reviewReportRepository.findByItemReviewIdIn(List.of(itemReviewId)));
        reviewRepository.delete(review);
    }

    /**
     * 상품평 좋아요 (AS-IS `ItemServiceImpl.saveItemReviewLike()` `:5523~5553`).
     *
     * <p><b>취소가 없는 1회성</b>이다 - 이미 누른 상태면 아무 일도 하지 않고 false를 돌려준다.
     * 토글이 아니라는 점이 관심답례품(wishlist)과 다르다. 중복 판정은 로그인 회원이면
     * USER_ID, 비로그인이면 IP로 한다(AS-IS 그대로).
     *
     * @return 이번 호출로 실제 좋아요가 기록됐으면 true, 이미 누른 상태였으면 false
     */
    @Transactional
    public boolean like(Integer itemReviewId, Long userId, String ip) {
        Review review = reviewRepository.findById(itemReviewId.longValue())
                .orElseThrow(() -> new GiftException("상품평을 찾을 수 없습니다."));

        boolean already = userId != null
                ? reviewLikeRepository.existsByItemReviewIdAndUserId(itemReviewId, userId)
                : reviewLikeRepository.existsByItemReviewIdAndUserIdIsNullAndIp(itemReviewId, ip);
        if (already) {
            return false;
        }

        com.ghlove.gift.domain.ReviewLike like = new com.ghlove.gift.domain.ReviewLike();
        like.setItemReviewId(itemReviewId);
        like.setUserId(userId);
        like.setIp(ip);
        like.setCreated(java.time.LocalDateTime.now());
        reviewLikeRepository.save(like);

        // AS-IS는 updateItemReviewLikeCount 쿼리로 집계했다 - 여기서는 카운터를 올린다.
        review.setLikeCount((review.getLikeCount() == null ? 0 : review.getLikeCount()) + 1);
        reviewRepository.save(review);
        return true;
    }

    /** 로그인 회원이 이 상품평에 이미 좋아요를 눌렀는지 (상세화면 버튼 상태 표시용). 비로그인은
     *  IP 기준이라 화면 상태로는 표시하지 않는다(AS-IS도 count만 보여주고 누른 상태는 안 남긴다). */
    public boolean likedBy(Integer itemReviewId, Long userId) {
        return userId != null && reviewLikeRepository.existsByItemReviewIdAndUserId(itemReviewId, userId);
    }
}
