package com.ghlove.gift.service;

import com.ghlove.gift.domain.Gift;
import com.ghlove.gift.domain.Inquiry;
import com.ghlove.gift.domain.InquiryReport;
import com.ghlove.gift.repository.GiftRepository;
import com.ghlove.gift.repository.InquiryReportRepository;
import com.ghlove.gift.repository.InquiryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InquiryService {

    private static final String STATUS_WAITING = "WAITING";
    private static final String STATUS_ANSWERED = "ANSWERED";
    private static final String SECRET_YES = "Y";
    private static final String SECRET_NO = "N";
    private static final String DISPLAY_ON = "Y";
    private static final String DISPLAY_OFF = "N";

    private final InquiryRepository inquiryRepository;
    private final InquiryReportRepository inquiryReportRepository;
    private final GiftRepository giftRepository;

    /** 답례품 상세화면용 - 블라인드된 문의는 제외. */
    public List<Inquiry> inquiriesOf(Long itemId) {
        return inquiryRepository.findByItemIdOrderByCreatedDateDesc(itemId).stream()
                .filter(q -> !DISPLAY_OFF.equals(q.getDisplayFlag()))
                .toList();
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
        inquiry.setDisplayFlag(DISPLAY_ON);
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

    /** 문의 신고 (SFR-005) - 회원 1인당 문의 1건에 중복 신고할 수 없다. */
    @Transactional
    public void report(Long inquiryId, Long userId, String reason) {
        if (!inquiryRepository.existsById(inquiryId)) {
            throw new GiftException("문의를 찾을 수 없습니다.");
        }
        if (inquiryReportRepository.existsByInquiryIdAndUserId(inquiryId, userId)) {
            throw new GiftException("이미 신고한 문의입니다.");
        }
        InquiryReport report = new InquiryReport();
        report.setInquiryId(inquiryId);
        report.setUserId(userId);
        report.setReason(reason);
        report.setCreatedDate(LocalDateTime.now());
        inquiryReportRepository.save(report);
    }

    public Map<Long, Long> reportCountsOf(List<Long> inquiryIds) {
        if (inquiryIds.isEmpty()) {
            return Map.of();
        }
        return inquiryReportRepository.findByInquiryIdIn(inquiryIds).stream()
                .collect(Collectors.groupingBy(InquiryReport::getInquiryId, Collectors.counting()));
    }

    public boolean reportedBy(Long inquiryId, Long userId) {
        return inquiryReportRepository.existsByInquiryIdAndUserId(inquiryId, userId);
    }

    // ---- admin "답례품 문의 관리" (SFR-005 재검토 라운드 - 원래 admin 콘솔에 노출되지 않던 gap) ----

    public List<Inquiry> adminSearch(Long itemId, String keyword, String status, String displayFlag) {
        return inquiryRepository.findAll().stream()
                .filter(q -> itemId == null || itemId.equals(q.getItemId()))
                .filter(q -> keyword == null || keyword.isBlank() || (q.getQuestion() != null && q.getQuestion().contains(keyword)))
                .filter(q -> status == null || status.isBlank() || status.equals(q.getStatus()))
                .filter(q -> displayFlag == null || displayFlag.isBlank() || displayFlag.equals(q.getDisplayFlag()))
                .sorted(Comparator.comparing(Inquiry::getInquiryId, Comparator.reverseOrder()))
                .toList();
    }

    /** 부적절한 문의 블라인드 처리(비노출)/복원. */
    @Transactional
    public Inquiry setDisplay(Long inquiryId, boolean display) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new GiftException("문의를 찾을 수 없습니다."));
        inquiry.setDisplayFlag(display ? DISPLAY_ON : DISPLAY_OFF);
        return inquiryRepository.save(inquiry);
    }
}
