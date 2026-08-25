package com.ghlove.gift.service;

import com.ghlove.gift.domain.Gift;
import com.ghlove.gift.domain.Inquiry;
import com.ghlove.gift.repository.GiftRepository;
import com.ghlove.gift.repository.InquiryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InquiryService {

    private static final String STATUS_WAITING = "WAITING";
    private static final String STATUS_ANSWERED = "ANSWERED";
    private static final String SECRET_YES = "Y";
    private static final String SECRET_NO = "N";

    private final InquiryRepository inquiryRepository;
    private final GiftRepository giftRepository;

    public List<Inquiry> inquiriesOf(Long itemId) {
        return inquiryRepository.findByItemIdOrderByCreatedDateDesc(itemId);
    }

    /** 마이페이지 "답례품Q&A" - 본인이 등록한 문의(구매자 시점, 판매자용 inquiriesForSeller와 다름). */
    public List<Inquiry> myInquiries(Long userId) {
        return inquiryRepository.findByUserIdOrderByCreatedDateDesc(userId);
    }

    /** 제공자용: 본인이 등록한 모든 답례품에 걸린 문의를 한 번에 조회. */
    public List<Inquiry> inquiriesForSeller(Long sellerId) {
        List<Long> itemIds = giftRepository.findBySellerIdOrderByItemIdDesc(sellerId).stream()
                .map(Gift::getItemId).collect(Collectors.toList());
        if (itemIds.isEmpty()) {
            return List.of();
        }
        return inquiryRepository.findByItemIdInOrderByCreatedDateDesc(itemIds);
    }

    @Transactional
    public Inquiry ask(Long itemId, Long userId, String question, boolean secret) {
        if (userId == null || userId <= 0) {
            throw new GiftException("문의자 ID를 입력해 주세요.");
        }
        if (question == null || question.isBlank()) {
            throw new GiftException("문의 내용을 입력해 주세요.");
        }

        Inquiry inquiry = new Inquiry();
        inquiry.setItemId(itemId);
        inquiry.setUserId(userId);
        inquiry.setQuestion(question);
        inquiry.setSecretYn(secret ? SECRET_YES : SECRET_NO);
        inquiry.setStatus(STATUS_WAITING);
        inquiry.setCreatedDate(LocalDateTime.now());
        return inquiryRepository.save(inquiry);
    }

    @Transactional
    public Inquiry answer(Long inquiryId, String answer) {
        if (answer == null || answer.isBlank()) {
            throw new GiftException("답변 내용을 입력해 주세요.");
        }
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new GiftException("문의를 찾을 수 없습니다."));
        if (STATUS_ANSWERED.equals(inquiry.getStatus())) {
            throw new GiftException("이미 답변이 등록된 문의입니다.");
        }
        inquiry.setAnswer(answer);
        inquiry.setAnsweredDate(LocalDateTime.now());
        inquiry.setStatus(STATUS_ANSWERED);
        return inquiryRepository.save(inquiry);
    }
}
