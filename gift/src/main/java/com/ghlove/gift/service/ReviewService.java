package com.ghlove.gift.service;

import com.ghlove.gift.domain.Review;
import com.ghlove.gift.domain.ReviewImage;
import com.ghlove.gift.repository.ReviewImageRepository;
import com.ghlove.gift.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
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
        review.setOrderCode(orderCode);
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
}
