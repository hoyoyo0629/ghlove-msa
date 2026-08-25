package com.ghlove.admin.service;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.domain.Qna;
import com.ghlove.admin.domain.QnaAnswer;
import com.ghlove.admin.domain.QnaFile;
import com.ghlove.admin.repository.ManagerRepository;
import com.ghlove.admin.repository.QnaAnswerRepository;
import com.ghlove.admin.repository.QnaFileRepository;
import com.ghlove.admin.repository.QnaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/** OP_QNA 하나를 두 화면이 공유한다: 마이페이지 "1:1 문의"(내 글만) / 고객센터 "Q&amp;A"(공개 게시판, 전체 글). */
@Service
@RequiredArgsConstructor
public class QnaService {

    private static final DateTimeFormatter CREATED_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final QnaRepository qnaRepository;
    private final QnaAnswerRepository qnaAnswerRepository;
    private final QnaFileRepository qnaFileRepository;
    private final QnaFileStorageService fileStorageService;
    private final ManagerRepository managerRepository;
    private final MemberClient memberClient;

    @Transactional
    public Qna ask(Long userId, String userName, String email, String qnaGroup, String subject,
                    String question, boolean secret, List<MultipartFile> files) {
        if (subject == null || subject.isBlank()) {
            throw new QnaException("제목을 입력해주세요.");
        }
        if (question == null || question.isBlank()) {
            throw new QnaException("내용을 입력해주세요.");
        }

        Qna qna = new Qna();
        qna.setUserId(userId);
        qna.setUserName(userName);
        qna.setEmail(email);
        qna.setQnaGroup(qnaGroup);
        qna.setSubject(subject);
        qna.setQuestion(question);
        qna.setCreatedDate(LocalDateTime.now().format(CREATED_DATE_FORMAT));
        qna.setAnswerCount(0);
        qna.setSecretFlag(secret ? "Y" : "N");
        qna.setHits(0);
        qna.setDisplayFlag("Y");
        qna = qnaRepository.save(qna);

        if (files != null) {
            for (MultipartFile file : files) {
                if (file == null || file.isEmpty()) {
                    continue;
                }
                String storedName = fileStorageService.store(file);
                QnaFile qnaFile = new QnaFile();
                qnaFile.setQnaId(qna.getQnaId());
                qnaFile.setFileName(storedName);
                qnaFile.setOrgFileName(file.getOriginalFilename());
                qnaFileRepository.save(qnaFile);
            }
        }
        return qna;
    }

    /** 날짜 범위는 CREATED_DATE(yyyyMMddHHmmss)의 앞 8자리(yyyyMMdd)로 비교한다. */
    public List<Qna> myInquiries(Long userId, String searchStartDate, String searchEndDate) {
        List<Qna> all = qnaRepository.findByUserIdOrderByCreatedDateDesc(userId);
        if (searchStartDate == null && searchEndDate == null) {
            return all;
        }
        String start = searchStartDate != null ? searchStartDate.replace("-", "") : null;
        String end = searchEndDate != null ? searchEndDate.replace("-", "") : null;
        return all.stream()
                .filter(q -> {
                    String day = q.getCreatedDate() != null && q.getCreatedDate().length() >= 8
                            ? q.getCreatedDate().substring(0, 8) : null;
                    if (day == null) {
                        return true;
                    }
                    if (start != null && day.compareTo(start) < 0) {
                        return false;
                    }
                    return end == null || day.compareTo(end) <= 0;
                })
                .toList();
    }

    public Optional<Qna> find(Integer qnaId) {
        return qnaRepository.findById(qnaId);
    }

    public Optional<QnaAnswer> answerOf(Integer qnaId) {
        return qnaAnswerRepository.findByQnaId(qnaId);
    }

    /** 운영자 답변 작성 (AS-IS opmanager qna 답변 화면 - 이번 라운드는 사용자 제출+조회까지만
     *  다룬다고 스코프 밖으로 뒀었지만, view_search_qna 이관 과정에서 실제로 필요한 기능임이
     *  드러나 추가한다). ANSWER_COUNT는 이 답변으로 질문 목록 뱃지가 갱신되도록 함께 올린다. */
    @Transactional
    public QnaAnswer answer(Integer qnaId, Long managerId, String title, String content) {
        Qna qna = qnaRepository.findById(qnaId)
                .orElseThrow(() -> new QnaException("문의를 찾을 수 없습니다."));
        if (title == null || title.isBlank()) {
            throw new QnaException("답변 제목을 입력해주세요.");
        }
        if (content == null || content.isBlank()) {
            throw new QnaException("답변 내용을 입력해주세요.");
        }
        QnaAnswer answer = qnaAnswerRepository.findByQnaId(qnaId).orElseGet(QnaAnswer::new);
        answer.setQnaId(qnaId);
        answer.setTitle(title);
        answer.setAnswer(content);
        answer.setUserId(managerId);
        answer.setAnswerDate(LocalDateTime.now().format(CREATED_DATE_FORMAT));
        answer = qnaAnswerRepository.save(answer);

        qna.setAnswerCount((qna.getAnswerCount() != null ? qna.getAnswerCount() : 0) + 1);
        qnaRepository.save(qna);
        return answer;
    }

    /** 답변자명 조회 (AS-IS view_search_qna: OP_MANAGER 우선, 없으면 OP_USER로 폴백해
     *  ifnull(decrypt(m.user_name), decrypt(u.user_name))). 답변은 실질적으로 항상 관리자가
     *  작성하므로(1:1문의에 회원용 답변 작성 화면이 없음) OP_USER 폴백은 지금 이 프로젝트
     *  구조상 사실상 호출되지 않지만, AS-IS 원본 동작을 그대로 재현해 둔다. */
    public String resolveAnswererName(Long userId) {
        if (userId == null) {
            return "관리자";
        }
        Optional<Manager> manager = managerRepository.findById(userId);
        if (manager.isPresent()) {
            return manager.get().getUserName();
        }
        MemberClient.MemberInfo member = memberClient.fetchOrNull(userId);
        return member != null && member.userName() != null ? member.userName() : "관리자";
    }

    /** 운영자 목록 화면 - 답변 여부와 무관하게 전체(신규 문의가 위로 오도록 최신순). */
    public List<QnaListRow> adminList() {
        return qnaRepository.findAllByOrderByCreatedDateDesc().stream()
                .map(q -> new QnaListRow(q, answerOf(q.getQnaId()).orElse(null), filesOf(q.getQnaId())))
                .toList();
    }

    public List<QnaFile> filesOf(Integer qnaId) {
        return qnaFileRepository.findByQnaId(qnaId);
    }

    /**
     * AS-IS mypage/inquiry.html은 목록을 아코디언(FAQ)으로 펼쳐서 질문 전문과 답변을
     * 바로 그 자리에 보여준다(별도 상세페이지 이동이 없다) - 그래서 목록 조회 시점에
     * 답변/첨부파일까지 같이 묶어서 내려준다. 문의 건수가 회원당 소량이라 N+1로도 충분하다.
     */
    public List<QnaListRow> myInquiriesDetailed(Long userId, String searchStartDate, String searchEndDate) {
        return myInquiries(userId, searchStartDate, searchEndDate).stream()
                .map(q -> new QnaListRow(q, answerOf(q.getQnaId()).orElse(null), filesOf(q.getQnaId())))
                .toList();
    }

    public record QnaListRow(Qna qna, QnaAnswer answer, List<QnaFile> files) {
    }

    /**
     * 고객센터 &gt; Q&amp;A (AS-IS qna/qna-open.html) - 전체 회원 글이 다 보이는 공개 게시판.
     * AS-IS는 질문행과 답변행을 하나의 flat 리스트로 묶어 내려준다(답변이 달린 질문은 그
     * 목록에 두 줄을 차지한다) - 순번(No.)도 이 합쳐진 리스트 기준으로 매겨진다.
     */
    @Transactional(readOnly = true)
    public BoardPage publicBoard(String where, String query, String orderBy, String sort,
                                  int page, int size, Long viewerUserId) {
        List<Qna> all = qnaRepository.findAll().stream()
                .filter(q -> !"N".equals(q.getDisplayFlag()))
                .collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new));

        if (query != null && !query.isBlank()) {
            String needle = query.trim().toLowerCase();
            boolean byUserName = "USER_NAME".equals(where);
            all = all.stream()
                    .filter(q -> byUserName
                            ? q.getUserName() != null && q.getUserName().toLowerCase().contains(needle)
                            : q.getSubject() != null && q.getSubject().toLowerCase().contains(needle))
                    .collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new));
        }

        Comparator<Qna> comparator = "HITS".equals(orderBy)
                ? Comparator.comparing((Qna q) -> q.getHits() != null ? q.getHits() : 0, Comparator.reverseOrder())
                : Comparator.comparing(Qna::getCreatedDate, Comparator.nullsLast(Comparator.naturalOrder()));
        if (!"HITS".equals(orderBy) && !"ASC".equals(sort)) {
            comparator = comparator.reversed();
        }
        all.sort(comparator);

        List<QnaAnswer> answers = all.isEmpty() ? List.of()
                : qnaAnswerRepository.findByQnaIdIn(all.stream().map(Qna::getQnaId).toList());
        java.util.Map<Integer, QnaAnswer> answerByQnaId = new java.util.HashMap<>();
        for (QnaAnswer a : answers) {
            answerByQnaId.put(a.getQnaId(), a);
        }

        List<BoardEntry> flat = new java.util.ArrayList<>();
        for (Qna q : all) {
            boolean locked = "Y".equals(q.getSecretFlag()) && (viewerUserId == null || !viewerUserId.equals(q.getUserId()));
            flat.add(new BoardEntry("Q", q, null, locked));
            QnaAnswer answer = answerByQnaId.get(q.getQnaId());
            if (answer != null) {
                flat.add(new BoardEntry("A", q, answer, locked));
            }
        }

        int totalCount = flat.size();
        int totalPages = (int) Math.ceil(totalCount / (double) size);
        int currentPage = Math.max(1, Math.min(page, Math.max(totalPages, 1)));
        int fromIndex = Math.min((currentPage - 1) * size, totalCount);
        int toIndex = Math.min(fromIndex + size, totalCount);

        List<BoardRow> rows = new java.util.ArrayList<>();
        for (int i = fromIndex; i < toIndex; i++) {
            BoardEntry entry = flat.get(i);
            int no = totalCount - i;
            boolean isAnswer = "A".equals(entry.type());
            String displayName = isAnswer ? resolveAnswererName(entry.answer().getUserId()) : entry.qna().getUserName();
            String subject = isAnswer ? ("게시판" + (no + 1) + "번 관리자 답변 입니다.") : entry.qna().getSubject();
            String createdDate = isAnswer ? entry.answer().getAnswerDate() : entry.qna().getCreatedDate();
            String hits = isAnswer ? "-" : String.valueOf(entry.qna().getHits() != null ? entry.qna().getHits() : 0);
            rows.add(new BoardRow(no, entry.type(), displayName, subject, createdDate, hits,
                    entry.locked(), entry.qna().getQnaId(), entry.answer() != null ? entry.answer().getQnaAnswerId() : null));
        }
        return new BoardPage(rows, currentPage, Math.max(totalPages, 1), totalCount);
    }

    /** 상세 조회 - 비밀글이고 작성자 본인이 아니면 잠금 상태로만 반환한다(내용은 내려주지 않는다). */
    @Transactional
    public Optional<BoardDetail> publicBoardDetail(Integer qnaId, Long viewerUserId) {
        Optional<Qna> found = qnaRepository.findById(qnaId);
        if (found.isEmpty()) {
            return Optional.empty();
        }
        Qna qna = found.get();
        boolean mine = viewerUserId != null && viewerUserId.equals(qna.getUserId());
        boolean locked = "Y".equals(qna.getSecretFlag()) && !mine;
        if (locked) {
            return Optional.of(new BoardDetail(qna, null, List.of(), true, false));
        }
        qna.setHits((qna.getHits() != null ? qna.getHits() : 0) + 1);
        QnaAnswer answer = qnaAnswerRepository.findByQnaId(qnaId).orElse(null);
        return Optional.of(new BoardDetail(qna, answer, filesOf(qnaId), false, mine));
    }

    private record BoardEntry(String type, Qna qna, QnaAnswer answer, boolean locked) {
    }

    public record BoardRow(int no, String type, String userName, String subject, String createdDate,
                            String hits, boolean locked, Integer qnaId, Integer qnaAnswerId) {
    }

    public record BoardPage(List<BoardRow> rows, int currentPage, int totalPages, int totalCount) {
    }

    public record BoardDetail(Qna qna, QnaAnswer answer, List<QnaFile> files, boolean locked, boolean mine) {
    }
}
