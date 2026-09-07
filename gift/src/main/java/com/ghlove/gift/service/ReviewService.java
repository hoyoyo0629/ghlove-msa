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
}
