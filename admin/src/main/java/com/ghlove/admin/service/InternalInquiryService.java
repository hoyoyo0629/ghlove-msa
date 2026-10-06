package com.ghlove.admin.service;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.domain.QnaAdmin;
import com.ghlove.admin.domain.QnaAdminAnswer;
import com.ghlove.admin.repository.QnaAdminAnswerRepository;
import com.ghlove.admin.repository.QnaAdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * 내부문의(지자체담당자↔본사) 서비스. AS-IS QnaAdminService/QnaAdminManagerController를 이식한다.
 * 소규모(지자체 운영문의) 관례상 목록 필터는 인메모리로 처리한다(FaqService와 동일).
 * 첨부파일·답변완료 SMS/메일 발송은 이번 슬라이스 범위 밖(추후 라운드).
 */
@Service
@RequiredArgsConstructor
public class InternalInquiryService {

    private static final DateTimeFormatter YMD = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final QnaAdminRepository qnaAdminRepository;
    private final QnaAdminAnswerRepository qnaAdminAnswerRepository;

    /** 목록 조회. 지자체담당자(locgovScoped)면 자기 지자체 문의만, 본사면 전체. 유형·기간 필터. */
    public List<QnaAdmin> list(boolean locgovScoped, String viewerLocgovCode, String qnaGroup,
                               String startYmd, String endYmd) {
        LocalDate from = parseYmd(startYmd);
        LocalDate to = parseYmd(endYmd);
        return qnaAdminRepository.findByDataStatusCodeOrderByCreatedDateDesc("Y").stream()
                .filter(q -> !locgovScoped || (viewerLocgovCode != null && viewerLocgovCode.equals(q.getLocgovCode())))
                .filter(q -> qnaGroup == null || qnaGroup.isBlank() || qnaGroup.equals(q.getQnaGroup()))
                .filter(q -> from == null || (q.getCreatedDate() != null && !q.getCreatedDate().toLocalDate().isBefore(from)))
                .filter(q -> to == null || (q.getCreatedDate() != null && !q.getCreatedDate().toLocalDate().isAfter(to)))
                .toList();
    }

    public Optional<QnaAdmin> get(Long qnaAdminId) {
        return qnaAdminRepository.findById(qnaAdminId);
    }

    public Optional<QnaAdminAnswer> answerOf(Long qnaAdminId) {
        return qnaAdminAnswerRepository.findFirstByQnaAdminIdAndDataStatusCodeOrderByAnswerDateDesc(qnaAdminId, "0");
    }

    /** 문의 등록(지자체 담당자). 등록자·지자체는 로그인 매니저 기준. */
    @Transactional
    public QnaAdmin createInquiry(Manager writer, String locgovCode, String qnaGroup, String subject, String question) {
        QnaAdmin q = new QnaAdmin();
        q.setQnaGroup(qnaGroup);
        q.setSubject(subject);
        q.setQuestion(question);
        q.setUserId(writer.getUserId());
        q.setUserName(writer.getUserName() != null ? writer.getUserName() : writer.getLoginId());
        q.setEmail(writer.getEmail() != null ? writer.getEmail() : "");
        q.setLocgovCode(locgovCode);
        q.setAnswerCount(0);
        q.setSecretFlag("N");
        q.setDisplayFlag("N");
        q.setDataStatusCode("Y");
        q.setUseYn("Y");
        q.setHits(0L);
        q.setCreatedDate(LocalDateTime.now());
        return qnaAdminRepository.save(q);
    }

    /** 답변 등록/수정(본사 운영자). 문의당 답변 1건 - 있으면 갱신, 없으면 신규. */
    @Transactional
    public void saveAnswer(Long qnaAdminId, Manager answerer, String title, String answerText) {
        QnaAdmin q = qnaAdminRepository.findById(qnaAdminId).orElseThrow();
        QnaAdminAnswer a = answerOf(qnaAdminId).orElseGet(QnaAdminAnswer::new);
        boolean isNew = a.getQnaAdminAnswerId() == null;
        a.setQnaAdminId(qnaAdminId);
        a.setUserId(answerer.getUserId());
        a.setTitle(title != null && !title.isBlank() ? title : q.getSubject());
        a.setAnswer(answerText);
        a.setAnswerDate(LocalDateTime.now());
        if (isNew) {
            a.setDataStatusCode("0");
            a.setHits(0L);
            a.setSecretFlag("N");
            a.setSendSmsFlag("N");
            a.setSendMailFlag("N");
        }
        qnaAdminAnswerRepository.save(a);
        if (isNew) {
            q.setAnswerCount((q.getAnswerCount() == null ? 0 : q.getAnswerCount()) + 1);
            qnaAdminRepository.save(q);
        }
    }

    /** 문의 삭제(소프트) - 본인 지자체 문의만. AS-IS deleteQnaAdmin. */
    @Transactional
    public void deleteInquiry(Long qnaAdminId) {
        qnaAdminRepository.findById(qnaAdminId).ifPresent(q -> {
            q.setDataStatusCode("N");
            q.setUseYn("N");
            qnaAdminRepository.save(q);
        });
    }

    private static LocalDate parseYmd(String ymd) {
        if (ymd == null || ymd.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(ymd.replaceAll("[^0-9]", ""), YMD);
        } catch (Exception e) {
            return null;
        }
    }
}
