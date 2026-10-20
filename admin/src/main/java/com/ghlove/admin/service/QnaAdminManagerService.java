package com.ghlove.admin.service;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.domain.QnaAdmin;
import com.ghlove.admin.domain.QnaAdminAnswer;
import com.ghlove.admin.domain.QnaAdminAnswerFile;
import com.ghlove.admin.domain.QnaAdminFile;
import com.ghlove.admin.repository.ManagerRepository;
import com.ghlove.admin.repository.QnaAdminAnswerFileRepository;
import com.ghlove.admin.repository.QnaAdminAnswerRepository;
import com.ghlove.admin.repository.QnaAdminFileRepository;
import com.ghlove.admin.repository.QnaAdminRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 관리자 > 답례품관리 > 관리자 문의 > 관리자 문의(AS-IS opmanager/qna-admin, QnaAdminServiceImpl·qna-admin-mapper.xml, 메뉴 16651).
 *
 * <p>답례품 제공자(판매자)가 판매자 포털 "지자체 문의"에서 자기 지자체에 남긴 문의(G_QNA_ADMIN)를 지자체 담당자가 보고 답한다.
 * 시스템·행안부(ROLE_ADMIN_1~4)는 전체를 보기만 하고, 지자체 담당자는 자기 지자체 문의만 본다(컨트롤러가 지자체를 강제).
 *
 * <p>AS-IS 규칙(그대로):
 * <ul>
 *   <li>목록은 정상('0')·노출('Y') 문의만, 등록일 내림차순. 작성자 아이디는 판매자 계정(OP_SELLER_USER)에 있으면 그 아이디, 없으면 관리자(OP_MANAGER) 아이디.
 *       지자체 칸은 판매자 계정의 지자체코드로 그린다(문의의 지자체코드가 아니다).</li>
 *   <li>답변 등록은 문의 지자체 = 담당자 소속 지자체일 때만, 수정·삭제·첨부 삭제는 답변 쓴 사람 본인만.</li>
 *   <li>문의 삭제는 답변이 있으면 "답변이 존재하여 삭제가 불가능합니다.", 작성자 본인이거나 같은 지자체 담당자만. 목록 일괄 삭제는 한 건씩 처리해
 *       중간에 실패하면 앞의 것은 이미 삭제돼 있다(AS-IS도 트랜잭션으로 묶지 않는다).</li>
 *   <li>첨부는 업로드 루트 아래 문의 {@code qnaadmin/{문의번호}/}, 답변 {@code qnaadminanswer/{문의번호}/}, 저장명은 {@code yyyyMMddHHmmssSSSS.확장자}
 *       (같은 이름이 있으면 뒤에 번호). 확장자·용량 제한은 AS-IS에도 없다.</li>
 * </ul>
 * TO-BE 차이: 개인정보 평문 저장 방침이라 이름·이메일 암복호화는 없고, 목록 이름 마스킹(지자체 담당자가 아니면 가림)만 AS-IS대로 한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QnaAdminManagerService {

    public static final String NORMAL = "0";
    public static final String DELETED = "1";
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter YMD = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter SAVE_NAME = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSSS");

    private final EntityManager em;
    private final QnaAdminRepository qnaAdminRepository;
    private final QnaAdminAnswerRepository answerRepository;
    private final QnaAdminFileRepository fileRepository;
    private final QnaAdminAnswerFileRepository answerFileRepository;
    private final ManagerRepository managerRepository;
    private final SellerManageClient sellerManageClient;
    private final LocgovClient locgovClient;
    private final org.springframework.transaction.support.TransactionTemplate transactionTemplate;

    @Value("${admin.upload.dir}")
    private String uploadDir;

    /** AS-IS OpRuntimeException 자리 - 컨트롤러가 받아 화면 문구로 바꾼다. */
    public static class QnaAdminException extends RuntimeException {
        public QnaAdminException(String message) {
            super(message);
        }
    }

    // ---- 목록 ----

    /** AS-IS QnaAdminParam 중 이 화면이 보내는 것. locgovCode가 있으면 그 지자체만, 없고 upperLocgovCode가 있으면 그 시도 전체. */
    public record Search(String upperLocgovCode, String locgovCode, String where, String query,
                         String searchStartDate, String searchEndDate) {
    }

    /** 목록 한 줄 - userName은 화면에 그대로 찍을 값(마스킹이 필요하면 이미 가려져 있다). */
    public record Row(Long qnaAdminId, String locgovName, String qnaGroup, String subject, Long userId, String userName,
                      String loginId, boolean answered, String createdDateStr) {
    }

    @Transactional(readOnly = true)
    public int count(Search s) {
        Map<String, Object> p = new LinkedHashMap<>();
        String where = where(s, p);
        Query q = em.createNativeQuery("select count(1) from admin.g_qna_admin q where " + where);
        p.forEach(q::setParameter);
        return ((Number) q.getSingleResult()).intValue();
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<Row> list(Search s, int offset, int limit, boolean masking) {
        Map<String, Object> p = new LinkedHashMap<>();
        String where = where(s, p);
        Query q = em.createNativeQuery("select q.qna_admin_id, q.qna_group, q.subject, q.user_id, q.user_name, q.created_date, "
                + "exists (select 1 from admin.g_qna_admin_answer qa where qa.qna_admin_id = q.qna_admin_id and qa.data_status_code = '0') "
                + "from admin.g_qna_admin q where " + where + " order by q.created_date desc limit :limit offset :offset");
        p.forEach(q::setParameter);
        q.setParameter("limit", limit).setParameter("offset", Math.max(offset, 0));
        List<Object[]> raw = q.getResultList();
        if (raw.isEmpty()) {
            return List.of();
        }

        // AS-IS: LEFT JOIN OP_SELLER_USER SU / OP_MANAGER M ON Q.USER_ID - 판매자 계정은 gift, 관리자는 admin에 있다
        List<Long> userIds = raw.stream().map(r -> r[3] == null ? null : ((Number) r[3]).longValue()).filter(Objects::nonNull).distinct().toList();
        Map<Long, SellerManageClient.UserBrief> sellerUsers = sellerManageClient.usersByIds(userIds).stream()
                .collect(Collectors.toMap(SellerManageClient.UserBrief::userId, u -> u, (a, b) -> a));
        Map<Long, String> managerLoginIds = new HashMap<>();
        managerRepository.findAllById(userIds).forEach(m -> managerLoginIds.put(m.getUserId(), m.getLoginId()));
        Map<String, LocgovClient.LocgovInfo> locgovs = new HashMap<>();
        locgovClient.allLocgovs().forEach(l -> locgovs.put(l.locgovCode(), l));

        List<Row> rows = new ArrayList<>();
        for (Object[] r : raw) {
            Long userId = r[3] == null ? null : ((Number) r[3]).longValue();
            SellerManageClient.UserBrief su = userId == null ? null : sellerUsers.get(userId);
            String loginId = su != null && su.loginId() != null ? su.loginId() : (userId == null ? null : managerLoginIds.get(userId));
            String userName = r[4] == null ? null : r[4].toString();
            rows.add(new Row(((Number) r[0]).longValue(), locgovName(su == null ? null : su.locgovCode(), locgovs), str(r[1]), str(r[2]),
                    userId, masking ? com.ghlove.admin.web.support.DataMasking.name(userName) : userName, loginId,
                    Boolean.TRUE.equals(r[6]), dateStr(r[5])));
        }
        return rows;
    }

    /** AS-IS LOCGOV_NAME - 시도면 시도명, 시군구면 "시도명 시군구명". 코드가 없거나 모르면 빈칸. */
    private static String locgovName(String code, Map<String, LocgovClient.LocgovInfo> locgovs) {
        LocgovClient.LocgovInfo l = code == null ? null : locgovs.get(code);
        if (l == null) {
            return null;
        }
        if (Objects.equals(l.locgovCode(), l.upperLocgovCode())) {
            return l.upperLocgovNm();
        }
        return (l.upperLocgovNm() == null ? "" : l.upperLocgovNm()) + " " + l.locgovNm();
    }

    /** AS-IS getQnaAdminListByParam / getQnaAdminListCountByParam의 where(이 화면이 보내는 조건). */
    private String where(Search s, Map<String, Object> p) {
        List<String> w = new ArrayList<>();
        w.add("q.data_status_code = '0'");
        w.add("q.display_flag = 'Y'");
        if (has(s.searchStartDate())) {
            w.add("q.created_date >= :start");
            p.put("start", LocalDate.parse(s.searchStartDate(), DAY).atStartOfDay());
        }
        if (has(s.searchEndDate())) {
            w.add("q.created_date <= :end");
            p.put("end", LocalDate.parse(s.searchEndDate(), DAY).atTime(23, 59, 59));
        }
        if (has(s.query()) && has(s.where())) {
            switch (s.where()) {
                case "USER_NAME" -> w.add("q.user_name like concat('%', cast(:q as varchar), '%')");
                case "EMAIL" -> w.add("q.email like concat('%', cast(:q as varchar), '%')");
                case "SUBJECT" -> w.add("q.subject like concat('%', cast(:q as varchar), '%')");
                case "QUESTION" -> w.add("q.question like concat('%', cast(:q as varchar), '%')");
                case "QNA_GROUP" -> w.add("q.qna_group = cast(:q as varchar)");
                default -> {
                }
            }
            if (List.of("USER_NAME", "EMAIL", "SUBJECT", "QUESTION", "QNA_GROUP").contains(s.where())) {
                p.put("q", s.query());
            }
        }
        if (has(s.locgovCode())) {
            w.add("q.locgov_code = :locgov");
            p.put("locgov", s.locgovCode());
        } else if (has(s.upperLocgovCode())) {
            List<String> codes = locgovClient.allLocgovs().stream()
                    .filter(l -> s.upperLocgovCode().equals(l.upperLocgovCode())).map(LocgovClient.LocgovInfo::locgovCode).toList();
            if (codes.isEmpty()) {
                w.add("1 = 0");
            } else {
                w.add("q.locgov_code in (:locgovs)");
                p.put("locgovs", codes);
            }
        }
        return String.join(" and ", w);
    }

    // ---- 상세 ----

    /** 답변 수정 화면에 필요한 것 전부. answer가 null이면 답변 전(새 답변). */
    public record Detail(QnaAdmin qnaAdmin, String createdDateStr, QnaAdminAnswer answer, String answerUserName, String answerDateStr,
                         List<QnaAdminFile> files, List<QnaAdminAnswerFile> answerFiles) {
    }

    /** AS-IS getQnaAdminByQnaAdminId(삭제 여부를 보지 않는다) + 정상 답변 + 첨부들. 없는 번호면 null. */
    @Transactional(readOnly = true)
    public Detail detail(long qnaAdminId) {
        QnaAdmin q = qnaAdminRepository.findById(qnaAdminId).orElse(null);
        if (q == null) {
            return null;
        }
        QnaAdminAnswer a = answerRepository.findFirstByQnaAdminIdAndDataStatusCodeOrderByAnswerDateDesc(qnaAdminId, NORMAL).orElse(null);
        // AS-IS: 답변 작성자 이름은 OP_USER에서 - TO-BE 관리자 계정은 같은 번호의 회원 사본이라 관리자 이름이 같다
        String writer = a == null ? null : managerRepository.findById(a.getUserId()).map(Manager::getUserName).orElse(null);
        return new Detail(q, dateStr(q.getCreatedDate()), a, writer, a == null ? null : dateStr(a.getAnswerDate()),
                fileRepository.findByQnaAdminIdOrderByQnaAdminFileIdAsc(qnaAdminId),
                a == null ? List.of() : answerFileRepository.findByQnaAdminAnswerIdOrderByQnaAdminAnswerFileIdAsc(a.getQnaAdminAnswerId()));
    }

    // ---- 답변 ----

    /** AS-IS insertQnaAdminAnswer - 문의 지자체가 담당자 지자체와 같아야 한다. */
    @Transactional
    public void insertAnswer(long qnaAdminId, String title, String answerText, List<MultipartFile> files, String locgovCode, Manager manager) {
        QnaAdmin q = qnaAdminRepository.findById(qnaAdminId).orElse(null);
        if (q == null) {
            throw new QnaAdminException("답변 등록할 문의가 없습니다.");
        }
        if (!has(locgovCode) || !locgovCode.equals(q.getLocgovCode())) {
            throw new QnaAdminException("권한이 없습니다.");
        }
        LocalDateTime now = LocalDateTime.now();
        QnaAdminAnswer a = new QnaAdminAnswer();
        a.setQnaAdminId(qnaAdminId);
        a.setAnswer(answerText);
        a.setTitle(title);
        a.setUserId(manager.getUserId());
        a.setAnswerDate(now);
        a.setDataStatusCode(NORMAL);
        a.setHits(0L);
        answerRepository.save(a);
        storeAnswerFiles(qnaAdminId, a.getQnaAdminAnswerId(), files, now);
    }

    /** AS-IS updateQnaAdminAnswer - 답변 쓴 사람만. 문자·메일 발송 여부 칸은 화면이 안 보내 비워진다(AS-IS 그대로). */
    @Transactional
    public void updateAnswer(long qnaAdminId, long qnaAdminAnswerId, String title, String answerText, List<MultipartFile> files, Manager manager) {
        QnaAdminAnswer a = answerWrittenBy(qnaAdminAnswerId, manager);
        storeAnswerFiles(qnaAdminId, qnaAdminAnswerId, files, LocalDateTime.now());
        a.setAnswer(answerText);
        a.setTitle(title);
        a.setSendSmsFlag(null);
        a.setSendMailFlag(null);
        answerRepository.save(a);
    }

    /** AS-IS deleteQnaAdminAnswer - 답변 쓴 사람만, 첨부를 지우고 답변을 '1'로. */
    @Transactional
    public void deleteAnswer(long qnaAdminAnswerId, Manager manager) {
        QnaAdminAnswer a = answerWrittenBy(qnaAdminAnswerId, manager);
        for (QnaAdminAnswerFile f : answerFileRepository.findByQnaAdminAnswerIdOrderByQnaAdminAnswerFileIdAsc(qnaAdminAnswerId)) {
            removeAnswerFile(a.getQnaAdminId(), f);
        }
        a.setDataStatusCode(DELETED);
        answerRepository.save(a);
    }

    /** AS-IS deleteQnaAdminAnswerFile - 답변 쓴 사람만, 지우고 남은 첨부 목록을 돌려준다. */
    @Transactional
    public List<QnaAdminAnswerFile> deleteAnswerFile(long qnaAdminAnswerId, long qnaAdminAnswerFileId, Manager manager) {
        QnaAdminAnswer a = answerWrittenBy(qnaAdminAnswerId, manager);
        answerFileRepository.findById(qnaAdminAnswerFileId).ifPresent(f -> removeAnswerFile(a.getQnaAdminId(), f));
        return answerFileRepository.findByQnaAdminAnswerIdOrderByQnaAdminAnswerFileIdAsc(qnaAdminAnswerId);
    }

    /** AS-IS isQnaAdminAnswerWriter - 정상 답변이고 쓴 사람이 지금 관리자여야 한다. */
    private QnaAdminAnswer answerWrittenBy(long qnaAdminAnswerId, Manager manager) {
        QnaAdminAnswer a = answerRepository.findById(qnaAdminAnswerId).filter(x -> NORMAL.equals(x.getDataStatusCode())).orElse(null);
        if (a == null || manager == null || !Objects.equals(a.getUserId(), manager.getUserId())) {
            throw new QnaAdminException("권한이 없습니다.");
        }
        return a;
    }

    // ---- 문의 삭제 ----

    /**
     * AS-IS deleteQnaAdmin - 작성자 본인이거나 같은 지자체 관리자만, 답변이 있으면 못 지운다. 첨부를 지우고 문의를 '1'로.
     * 트랜잭션 없이 건마다 바로 반영된다(일괄 삭제 중 실패해도 앞의 건은 남지 않는다 - AS-IS 그대로).
     */
    public void deleteQuestion(long qnaAdminId, String locgovCode, Manager manager) {
        transactionTemplate.executeWithoutResult(status -> deleteQuestionTx(qnaAdminId, locgovCode, manager));
    }

    private void deleteQuestionTx(long qnaAdminId, String locgovCode, Manager manager) {
        QnaAdmin q = qnaAdminRepository.findById(qnaAdminId).orElse(null);
        if (q == null || !isWriterOrSameLocgov(q, locgovCode, manager)) {
            throw new QnaAdminException("권한이 없습니다.");
        }
        if (answerRepository.findFirstByQnaAdminIdAndDataStatusCodeOrderByAnswerDateDesc(qnaAdminId, NORMAL).isPresent()) {
            throw new QnaAdminException("답변이 존재하여 삭제가 불가능합니다.");
        }
        for (QnaAdminFile f : fileRepository.findByQnaAdminIdOrderByQnaAdminFileIdAsc(qnaAdminId)) {
            fileRepository.delete(f);
            deleteQuietly(questionFilePath(qnaAdminId, f));
        }
        em.createNativeQuery("update admin.g_qna_admin set data_status_code = '1' where qna_admin_id = :id and data_status_code = '0' and display_flag = 'Y'")
                .setParameter("id", qnaAdminId).executeUpdate();
    }

    /**
     * AS-IS deleteQnaAdminList - 고른 순서대로 한 건씩(건마다 따로 반영). 실패하면 거기서 멈추고, 앞에서 지운 건은 지워진 채로 남는다
     * (AS-IS도 이 메서드에 트랜잭션이 없다).
     */
    public void deleteQuestions(List<Long> ids, String locgovCode, Manager manager) {
        if (ids == null) {
            return;
        }
        for (Long id : ids) {
            deleteQuestion(id, locgovCode, manager);
        }
    }

    /** AS-IS isQnaAdminWriter - 쓴 사람이거나, 관리자이면서 문의 지자체가 넘겨받은 지자체코드와 같을 때. */
    private boolean isWriterOrSameLocgov(QnaAdmin q, String locgovCode, Manager manager) {
        if (manager != null && Objects.equals(q.getUserId(), manager.getUserId())) {
            return true;
        }
        return manager != null && q.getLocgovCode() != null && q.getLocgovCode().equals(locgovCode);
    }

    // ---- 첨부 ----

    private void storeAnswerFiles(long qnaAdminId, Long qnaAdminAnswerId, List<MultipartFile> files, LocalDateTime now) {
        if (files == null) {
            return;
        }
        for (MultipartFile f : files) {
            if (f == null || f.isEmpty() || f.getOriginalFilename() == null || f.getOriginalFilename().isEmpty()) {
                continue;
            }
            String original = Paths.get(f.getOriginalFilename()).getFileName().toString();
            Path dir = Paths.get(uploadDir, "qnaadminanswer", String.valueOf(qnaAdminId));
            String saved = newFileName(dir, LocalDateTime.now().format(SAVE_NAME) + "." + extension(original));
            try {
                Files.createDirectories(dir);
                f.transferTo(dir.resolve(saved).toAbsolutePath());
            } catch (IOException e) {
                log.error("관리자 문의 답변 첨부 저장 실패 qnaAdminId={}", qnaAdminId, e);
                throw new QnaAdminException("첨부파일 저장 오류입니다.");
            }
            QnaAdminAnswerFile row = new QnaAdminAnswerFile();
            row.setQnaAdminAnswerId(qnaAdminAnswerId);
            row.setFileName(saved);
            row.setFileTy(f.getContentType() == null || f.getContentType().isEmpty() ? extension(original) : f.getContentType());
            row.setOrgFileName(original);
            row.setCreatedDate(now);
            answerFileRepository.save(row);
        }
    }

    private void removeAnswerFile(Long qnaAdminId, QnaAdminAnswerFile f) {
        answerFileRepository.delete(f);
        deleteQuietly(answerFilePath(qnaAdminId, f));
    }

    /** 문의 첨부 실제 위치 - PATH_NAME(이관분)이 있으면 루트/PATH_NAME, 없으면 루트/qnaadmin/{문의번호}. */
    public Path questionFilePath(long qnaAdminId, QnaAdminFile f) {
        return isBlank(f.getPathName()) ? Paths.get(uploadDir, "qnaadmin", String.valueOf(qnaAdminId), f.getFileName())
                : Paths.get(uploadDir, f.getPathName(), f.getFileName());
    }

    /** 답변 첨부 실제 위치 - PATH_NAME(이관분)이 있으면 루트/PATH_NAME, 없으면 루트/qnaadminanswer/{문의번호}. */
    public Path answerFilePath(Long qnaAdminId, QnaAdminAnswerFile f) {
        return isBlank(f.getPathName()) ? Paths.get(uploadDir, "qnaadminanswer", String.valueOf(qnaAdminId), f.getFileName())
                : Paths.get(uploadDir, f.getPathName(), f.getFileName());
    }

    /** 내려받기용 - 문의 첨부. 없으면 null. */
    @Transactional(readOnly = true)
    public QnaAdminFile questionFile(long qnaAdminFileId) {
        return fileRepository.findById(qnaAdminFileId).orElse(null);
    }

    /** 내려받기용 - 답변 첨부와 그 답변의 문의번호. 없으면 null. */
    @Transactional(readOnly = true)
    public Map.Entry<Long, QnaAdminAnswerFile> answerFile(long qnaAdminAnswerId, long qnaAdminAnswerFileId) {
        QnaAdminAnswer a = answerRepository.findById(qnaAdminAnswerId).filter(x -> NORMAL.equals(x.getDataStatusCode())).orElse(null);
        QnaAdminAnswerFile f = answerFileRepository.findById(qnaAdminAnswerFileId).orElse(null);
        return a == null || f == null ? null : Map.entry(a.getQnaAdminId(), f);
    }

    /** AS-IS FileUtils.getNewFileName - 같은 이름이 있으면 이름 뒤에 _1, _2 …를 붙인다. */
    private static String newFileName(Path dir, String name) {
        if (!Files.exists(dir.resolve(name))) {
            return name;
        }
        int dot = name.lastIndexOf('.');
        String base = dot < 0 ? name : name.substring(0, dot);
        String ext = dot < 0 ? "" : name.substring(dot);
        for (int i = 1; ; i++) {
            String candidate = base + "_" + i + ext;
            if (!Files.exists(dir.resolve(candidate))) {
                return candidate;
            }
        }
    }

    private static void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.warn("관리자 문의 첨부 삭제 실패 {}", path, e);
        }
    }

    private static String extension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot < 0 ? "" : fileName.substring(dot + 1);
    }

    private static String dateStr(Object o) {
        if (o == null) {
            return "";
        }
        if (o instanceof LocalDateTime t) {
            return t.format(YMD);
        }
        if (o instanceof java.sql.Timestamp t) {
            return t.toLocalDateTime().format(YMD);
        }
        return o.toString();
    }

    private static String str(Object o) {
        return o == null ? null : o.toString();
    }

    private static boolean has(String s) {
        return s != null && !s.isBlank();
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
