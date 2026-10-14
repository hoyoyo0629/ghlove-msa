package com.ghlove.admin.service;

import com.ghlove.admin.domain.Notice;
import com.ghlove.admin.repository.LocgovNoticeAdminRepository;
import com.ghlove.admin.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 지자체공지사항 (메뉴 5110) - AS-IS {@code LocgovNoticeManagerController}/{@code NoticeServiceImpl}
 * 중 {@code insertLocgovNotice}/{@code locgovNoticeDelete} 계열 이식. 5111 공지사항과
 * {@link NoticeRepository}(OP_NOTICE)를 같이 쓰되 조회·등록 로직은 분리되어 있다(AS-IS도 그렇다).
 */
@Service
@RequiredArgsConstructor
public class LocgovNoticeAdminService {

    private static final DateTimeFormatter CREATED_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final String NOTICE_FLAG_DEFAULT = "N";

    private final LocgovNoticeAdminRepository locgovNoticeAdminRepository;
    private final NoticeRepository noticeRepository;

    public int count(String where, String query, String startCreateDate, String endCreateDate, String locgovCode) {
        return locgovNoticeAdminRepository.count(where, query, startCreateDate, endCreateDate, locgovCode);
    }

    public List<Notice> list(String where, String query, String startCreateDate, String endCreateDate,
                              String locgovCode, int offset, int limit) {
        return locgovNoticeAdminRepository.list(where, query, startCreateDate, endCreateDate, locgovCode, offset, limit);
    }

    public Notice notice(Integer noticeId) {
        return noticeRepository.findById(noticeId)
                .orElseThrow(() -> new ContentException("공지사항을 찾을 수 없습니다."));
    }

    /** AS-IS {@code insertLocgovNotice} - LOC 담당자일 때만 컨트롤러가 자기 지자체 코드를 채워 넘긴다. */
    @Transactional
    public Notice create(String subject, String content, String noticeFlag, String useYn, String locgovCode) {
        if (subject == null || subject.isBlank()) {
            throw new ContentException("제목을 입력해 주세요.");
        }
        Notice notice = new Notice();
        notice.setSubject(subject);
        notice.setContent(content);
        notice.setNoticeFlag(noticeFlag == null || noticeFlag.isBlank() ? NOTICE_FLAG_DEFAULT : noticeFlag);
        notice.setUseYn(useYn);
        notice.setLocgovCode(locgovCode);
        notice.setCreatedDate(CREATED_DATE_FORMAT.format(LocalDateTime.now()));
        notice.setHits(0);
        return noticeRepository.save(notice);
    }

    /** AS-IS {@code updateNotice} 매퍼 - LOCGOV_CODE는 수정 대상 컬럼이 아니다(등록 시점 값 그대로 유지). */
    @Transactional
    public Notice update(Integer noticeId, String subject, String content, String noticeFlag, String useYn) {
        if (subject == null || subject.isBlank()) {
            throw new ContentException("제목을 입력해 주세요.");
        }
        Notice notice = notice(noticeId);
        notice.setSubject(subject);
        notice.setContent(content);
        notice.setNoticeFlag(noticeFlag == null || noticeFlag.isBlank() ? NOTICE_FLAG_DEFAULT : noticeFlag);
        notice.setUseYn(useYn);
        return noticeRepository.save(notice);
    }

    /** AS-IS {@code deleteNotice} - 하드 삭제. 존재하지 않으면 예외(컨트롤러가 실패 응답으로 감싼다). */
    @Transactional
    public void delete(Integer noticeId) {
        noticeRepository.delete(notice(noticeId));
    }

    /**
     * AS-IS {@code locgovNoticeDelete} - 선택건을 하나씩 지우고 <b>요청 건수와 삭제 건수가 같을 때만</b>
     * "SUCC"를 돌려준다. AS-IS는 불일치 시 예외를 던지는데 그 예외를 컨트롤러가 삼켜 버려 결과코드가
     * 빈 문자열로 내려간다(화면은 "SUCC"가 아니면 전부 오류 alert다) - 그 효과까지 그대로 재현한다.
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
