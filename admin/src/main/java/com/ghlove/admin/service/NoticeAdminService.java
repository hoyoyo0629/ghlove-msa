package com.ghlove.admin.service;

import com.ghlove.admin.domain.Notice;
import com.ghlove.admin.repository.NoticeAdminRepository;
import com.ghlove.admin.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 공지사항(전체공지) 관리 (메뉴 5111) - AS-IS {@code NoticeManagerController} +
 * {@code NoticeServiceImpl}의 {@code insertNotice}/{@code updateNotice}/{@code deleteNotice}/
 * {@code deleteListNotice} 이식. 지자체공지사항(5110, {@link LocgovNoticeAdminService})과
 * 같은 {@code OP_NOTICE}를 쓰지만 AS-IS도 서비스 메서드가 갈라져 있다.
 */
@Service
@RequiredArgsConstructor
public class NoticeAdminService {

    private static final DateTimeFormatter CREATED_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final String NOTICE_FLAG_DEFAULT = "N";
    /** AS-IS {@code insertNotice}가 못박는 전체공지 코드. */
    private static final String WHOLE_NOTICE_LOCGOV_CODE = "00000";

    private final NoticeAdminRepository noticeAdminRepository;
    private final NoticeRepository noticeRepository;

    public int count(String where, String query, String startCreateDate, String endCreateDate) {
        return noticeAdminRepository.count(where, query, startCreateDate, endCreateDate);
    }

    public List<Notice> list(String where, String query, String startCreateDate, String endCreateDate,
                              int offset, int limit) {
        return noticeAdminRepository.list(where, query, startCreateDate, endCreateDate, offset, limit);
    }

    public Notice notice(Integer noticeId) {
        return noticeRepository.findById(noticeId)
                .orElseThrow(() -> new ContentException("공지사항을 찾을 수 없습니다."));
    }

    /** AS-IS {@code insertNotice} - 지자체코드를 '00000'(전체공지)으로 못박고 작성자명을 넣는다. */
    @Transactional
    public Notice create(String subject, String content, String noticeFlag, String useYn, String userName) {
        if (subject == null || subject.isBlank()) {
            throw new ContentException("제목을 입력해 주세요.");
        }
        Notice notice = new Notice();
        notice.setSubject(subject);
        notice.setContent(content);
        notice.setNoticeFlag(noticeFlag == null || noticeFlag.isBlank() ? NOTICE_FLAG_DEFAULT : noticeFlag);
        notice.setUseYn(useYn);
        notice.setUserName(userName);
        notice.setLocgovCode(WHOLE_NOTICE_LOCGOV_CODE);
        notice.setCreatedDate(CREATED_DATE_FORMAT.format(LocalDateTime.now()));
        notice.setHits(0);
        return noticeRepository.save(notice);
    }

    /**
     * AS-IS {@code updateNotice} - 작성자명도 다시 쓴다(수정하면 마지막에 고친 운영자로 바뀐다).
     *
     * <p>AS-IS 매퍼는 {@code URL}·{@code TARGET_OPTION}·{@code REL_OPTION}·{@code VISIBLE_TYPE}·
     * {@code SELLER_SELECT_FLAG}도 같이 UPDATE 하는데, 화면에 그 입력칸이 없어(전부 주석처리)
     * <b>수정할 때마다 NULL로 덮어쓰는 결함</b>이다. 특히 {@code VISIBLE_TYPE}이 NULL이 되면
     * AS-IS 공개 조회조건({@code VISIBLE_TYPE = 2 OR = 1})에서 빠진다. 옮기지 않는다.
     */
    @Transactional
    public Notice update(Integer noticeId, String subject, String content, String noticeFlag,
                         String useYn, String userName) {
        if (subject == null || subject.isBlank()) {
            throw new ContentException("제목을 입력해 주세요.");
        }
        Notice notice = notice(noticeId);
        notice.setSubject(subject);
        notice.setContent(content);
        notice.setNoticeFlag(noticeFlag == null || noticeFlag.isBlank() ? NOTICE_FLAG_DEFAULT : noticeFlag);
        notice.setUseYn(useYn);
        notice.setUserName(userName);
        return noticeRepository.save(notice);
    }

    /** AS-IS {@code deleteNotice} - 하드 삭제. */
    @Transactional
    public void delete(Integer noticeId) {
        noticeRepository.delete(notice(noticeId));
    }

    /**
     * AS-IS {@code deleteListNotice} - 요청 건수와 삭제 건수가 같을 때만 "SUCC"다. 불일치 시
     * AS-IS는 예외를 던지는데 컨트롤러가 그걸 삼켜 결과코드가 빈 문자열로 내려간다
     * (화면은 "SUCC"가 아니면 전부 오류 alert) - 그 효과까지 그대로 재현한다.
     */
    @Transactional
    public String deleteList(List<Integer> noticeIds) {
        if (noticeIds == null || noticeIds.isEmpty()) {
            return "FAIL";
        }
        int deleted = 0;
        for (Integer id : noticeIds) {
            if (noticeRepository.existsById(id)) {
                noticeRepository.deleteById(id);
                deleted++;
            }
        }
        return deleted == noticeIds.size() ? "SUCC" : "";
    }
}
