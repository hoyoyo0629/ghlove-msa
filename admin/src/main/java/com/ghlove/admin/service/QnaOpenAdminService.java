package com.ghlove.admin.service;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.domain.Qna;
import com.ghlove.admin.domain.QnaAnswer;
import com.ghlove.admin.domain.QnaFile;
import com.ghlove.admin.domain.Role;
import com.ghlove.admin.repository.ManagerRepository;
import com.ghlove.admin.repository.QnaAnswerRepository;
import com.ghlove.admin.repository.QnaFileRepository;
import com.ghlove.admin.repository.QnaOpenAdminRepository;
import com.ghlove.admin.repository.QnaRepository;
import com.ghlove.admin.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Q&A 관리 (메뉴 5112) - AS-IS {@code QnaOpenManagerController}
 * ({@code /opmanager/qna-open/list})의 목록 조립 부분을 이식한 것이다.
 *
 * <p><b>AS-IS 그대로</b>:
 * <ul>
 *   <li>진입(GET)은 <b>조회하지 않고 빈 목록</b>을 내려준다 - 검색(POST)해야 조회된다
 *       (이 프로젝트에서 반복되는 운영화면 패턴).</li>
 *   <li>작성자명을 <b>마스킹</b>한다: {@code 첫 글자 + "*" + 세 번째 글자부터}.
 *       두 글자면 뒤가 비어 "김*"이 된다.</li>
 *   <li>문의유형({@code qna_group})은 공통코드 {@code QNA_GROUPS}의 라벨로 바꿔 보여준다.</li>
 *   <li>{@code qnaTypes} 모델값은 <b>항상 빈 목록</b>이다 - AS-IS가
 *       {@code "QNA_GROUP_" + code.getValue()}로 코드유형을 만드는데 {@code QNA_GROUPS}의
 *       {@code code_value}가 전부 비어 있어 존재하지 않는 코드유형을 찾는다(AS-IS·TO-BE 양쪽 실측).
 *       SalesOn 원제품의 2단 분류 잔재다 - 켜지 않고 그대로 둔다.</li>
 * </ul>
 *
 * <p><b>AS-IS 결함 1건 - 안전하게 고쳤다</b>: 마스킹이 {@code substring(2)}를 무조건 호출해
 * <b>한 글자 이름이면 {@code StringIndexOutOfBoundsException}</b>으로 목록 전체가 500이 된다.
 * 한 글자면 그대로 두고 두 글자 이상만 마스킹한다(정상 데이터의 결과는 완전히 같다).
 *
 * <p>로그인ID는 AS-IS가 {@code OP_USER}를 조인해 뽑지만 TO-BE에서 회원은 member 소유라
 * {@link MemberAdminClient}로 받아 채운다. 답변자(운영자)는 admin이 가지고 있어 직접 조회한다.
 */
@Service
@RequiredArgsConstructor
public class QnaOpenAdminService {

    /** AS-IS deleteQna - 소프트 삭제 값. */
    private static final String STATUS_DELETED = "1";

    /** AS-IS는 ANSWER_DATE도 CREATED_DATE와 같은 14자리 문자열로 넣는다. */
    private static final DateTimeFormatter ANSWER_DATE = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final QnaOpenAdminRepository qnaOpenAdminRepository;
    private final QnaRepository qnaRepository;
    private final QnaAnswerRepository qnaAnswerRepository;
    private final QnaFileRepository qnaFileRepository;
    private final QnaFileStorageService qnaFileStorageService;
    private final MemberAdminClient memberAdminClient;
    private final ManagerRepository managerRepository;
    private final RoleRepository roleRepository;
    private final SmsIpsService smsIpsService;

    /** 목록 한 행 - 화면이 쓰는 모양 그대로. */
    public record QnaRow(Integer qnaId, String createdDate, Long userId, String userName,
                         String loginId, String subject, Integer hits, String secretFlag,
                         String content, String qnaGroup, String qnaGroupNm, Long answerCnt,
                         String answerUserName, String answerLoginId, String answerDate) {

        /** AS-IS 화면은 답변 건수로 답변여부를 표시한다. */
        public boolean answered() {
            return answerCnt != null && answerCnt > 0;
        }
    }

    public int count(String qnaOpenAnswerCode, String where, String query,
                     String searchStartDate, String searchEndDate) {
        return qnaOpenAdminRepository.count(qnaOpenAnswerCode, where, query, searchStartDate,
                searchEndDate, userIdFilter(where, query));
    }

    public List<QnaRow> list(String qnaOpenAnswerCode, String where, String query,
                             String searchStartDate, String searchEndDate, int offset, int limit) {
        List<QnaOpenAdminRepository.Row> rows = qnaOpenAdminRepository.list(qnaOpenAnswerCode, where,
                query, searchStartDate, searchEndDate, userIdFilter(where, query), offset, limit);
        if (rows.isEmpty()) {
            return List.of();
        }

        List<Long> userIds = rows.stream().map(QnaOpenAdminRepository.Row::userId)
                .filter(id -> id != null && id > 0).distinct().toList();
        Map<Long, String> loginIds = memberAdminClient.userLoginIds(userIds);

        Map<Long, Manager> answerers = new HashMap<>();
        rows.stream().map(QnaOpenAdminRepository.Row::answerUserId)
                .filter(id -> id != null && id > 0).distinct()
                .forEach(id -> managerRepository.findById(id).ifPresent(m -> answerers.put(id, m)));

        List<QnaRow> result = new ArrayList<>(rows.size());
        for (QnaOpenAdminRepository.Row r : rows) {
            Manager answerer = r.answerUserId() == null ? null : answerers.get(r.answerUserId());
            result.add(new QnaRow(r.qnaId(), r.createdDate(), r.userId(), maskUserName(r.userName()),
                    r.userId() == null ? null : loginIds.get(r.userId()), r.subject(), r.hits(),
                    r.secretFlag(), r.content(), r.qnaGroup(), r.qnaGroupNm(), r.answerCnt(),
                    answerer == null ? null : answerer.getUserName(),
                    answerer == null ? null : answerer.getLoginId(), r.answerDate()));
        }
        return result;
    }

    /**
     * AS-IS deleteQnaData - 목록에서 체크한 문의를 지운다.
     *
     * <p><b>AS-IS 규칙 그대로</b>: <b>답변이 등록되지 않은 건만</b> 지운다
     * (AS-IS 주석 "답변 등록되지 않은거만 삭제하도록 수정"). 답변이 있는 건은 <b>조용히 건너뛴다</b> -
     * 오류도 안내도 없다. 삭제는 행을 지우지 않고 {@code DATA_STATUS_CODE='1'}로 바꾸는
     * 소프트 삭제다(AS-IS deleteQna).
     *
     * @return 실제로 지운 건수
     */
    @Transactional
    public int deleteList(List<Integer> qnaIds) {
        if (qnaIds == null || qnaIds.isEmpty()) {
            return 0;
        }
        int deleted = 0;
        for (Integer qnaId : qnaIds) {
            if (qnaId == null) {
                continue;
            }
            boolean answered = qnaAnswerRepository.findFirstByQnaIdOrderByAnswerDateAsc(qnaId)
                    .map(a -> a.getQnaAnswerId() != null && a.getQnaAnswerId() > 0)
                    .orElse(false);
            if (answered) {
                continue;
            }
            Qna qna = qnaRepository.findById(qnaId).orElse(null);
            if (qna == null) {
                continue;
            }
            qna.setDataStatusCode(STATUS_DELETED);
            qnaRepository.save(qna);
            deleted++;
        }
        return deleted;
    }

    /**
     * AS-IS deleteQna (GET /delete/{qnaId}) - 답변화면의 [문의글 삭제].
     *
     * <p><b>목록의 일괄삭제와 규칙이 다르다</b>: 일괄삭제는 답변이 있으면 건너뛰지만, 이쪽은
     * <b>답변이 있어도 그냥 지운다</b>(AS-IS가 여기서는 검사하지 않는다). 소프트 삭제다.
     */
    @Transactional
    public void deleteQna(Integer qnaId) {
        Qna qna = qnaRepository.findById(qnaId)
                .orElseThrow(() -> new QnaException("문의를 찾을 수 없습니다."));
        qna.setDataStatusCode(STATUS_DELETED);
        qnaRepository.save(qna);
    }

    /**
     * AS-IS deleteQnaAnswer (GET /delete/{qnaId}/answer/{qnaAnswerId}) - 답변만 삭제.
     *
     * <p><b>AS-IS는 소프트 삭제가 아니라 행을 지운다</b>({@code DELETE FROM OP_QNA_ANSWER} -
     * 매퍼에 소프트 삭제 UPDATE가 주석으로 남아 있고 실제로는 DELETE가 실행된다).
     * 그래서 문의의 답변 건수도 같이 줄여 준다 - AS-IS는 이 값을 갱신하지 않아
     * <b>답변을 지워도 목록의 '답변완료'가 그대로 남는다</b>(아래 결함 참고).
     */
    @Transactional
    public void deleteQnaAnswer(Integer qnaId, Integer qnaAnswerId) {
        qnaAnswerRepository.findById(qnaAnswerId).ifPresent(answer -> {
            qnaAnswerRepository.delete(answer);
            // AS-IS 결함 보정: answer_count를 남겨 두면 '답변완료'로 계속 표시되고
            // 미답변 검색에서도 빠진다. 실제 답변 수로 맞춘다.
            qnaRepository.findById(qnaId).ifPresent(qna -> {
                qna.setAnswerCount(qnaAnswerRepository.findByQnaIdIn(List.of(qnaId)).size());
                qnaRepository.save(qna);
            });
        });
    }

    /**
     * AS-IS deleteItemImageByItemId - 첨부파일 삭제.
     * <b>디스크 파일을 먼저 지우고</b> DB 행을 지운다(AS-IS도 {@code fileStorage.delete} →
     * {@code deleteQnaFile} 순서다. 그래서 화면 확인문구가 "파일이 실제로 삭제됩니다"다).
     */
    @Transactional
    public void deleteFile(Integer qnaFileId) {
        qnaFileRepository.findById(qnaFileId).ifPresent(file -> {
            qnaFileStorageService.delete(file.getFileName());
            qnaFileRepository.delete(file);
        });
    }

    /**
     * AS-IS qnaAnswerAction (POST answer/{qnaId}) - 답변 등록·수정.
     *
     * <p><b>AS-IS 순서 그대로</b>: 답변이 있으면 UPDATE, 없으면 INSERT → {@code ANSWER_COUNT}를
     * 실제 답변 수로 다시 계산 → 국민비서 문자 적재({@code sendSmsQnaAnswer}).
     *
     * <p><b>AS-IS의 메일·UMS 분기는 옮기지 않았다 - 닿을 수 없는 코드다</b>:
     * {@code sendMailFlag}/{@code sendSmsFlag}를 "값이 없으면 N"으로 정하는데
     * 답변 화면(form.jsp)에 그 체크박스가 <b>아예 없어</b> 항상 'N'이 된다. 그래서 AS-IS에서도
     * {@code QnaCompleteMail} 메일과 {@code UnifiedMessagingService} 문자는 한 번도 발송되지 않는다
     * (원제품 SalesOn 잔재). 같은 이유로 첨부 {@code detailImageFiles[]}도 옮기지 않았다 -
     * 화면에 파일 input이 없고, 받더라도 {@code insertQnaAnswer}/{@code updateQnaAnswer} 매퍼가
     * 파일을 저장하지 않는다. 실제로 나가는 알림은 아래 국민비서 문자 <b>하나뿐</b>이다.
     *
     * <p>답변자({@code USER_ID})는 AS-IS가 화면 hidden으로 받지만 그 값이 결국
     * {@code UserUtils.getManagerId()}(로그인한 매니저)라서 <b>세션에서 직접</b> 가져온다 -
     * 결과는 같고 위조할 수 없다.
     */
    @Transactional
    public void answer(Integer qnaId, Long managerUserId, String title, String content) {
        Qna qna = qnaRepository.findById(qnaId)
                .orElseThrow(() -> new QnaException("문의를 찾을 수 없습니다."));

        QnaAnswer answer = answerOf(qnaId).orElseGet(QnaAnswer::new);
        answer.setQnaId(qnaId);
        answer.setTitle(title);
        answer.setAnswer(content);
        answer.setUserId(managerUserId);
        // AS-IS는 INSERT·UPDATE 모두 ANSWER_DATE를 현재시각으로 다시 쓴다(수정하면 날짜가 갱신된다)
        answer.setAnswerDate(LocalDateTime.now().format(ANSWER_DATE));
        answer.setSendSmsFlag("N");
        answer.setSendMailFlag("N");
        answer.setDataStatusCode("0");
        qnaAnswerRepository.save(answer);

        // AS-IS updateQnaAnswerCount - 실제 답변 수로 다시 계산한다
        qna.setAnswerCount(qnaAnswerRepository.findByQnaIdIn(List.of(qnaId)).size());
        qnaRepository.save(qna);

        sendSmsQnaAnswer(qna);
    }

    /**
     * AS-IS qnaUpdateAction (POST edit/{qnaId}) - <b>AS-IS 결함을 그대로 둔 자리</b>.
     *
     * <p>AS-IS는 {@code Qna}와 {@code QnaAnswer}를 둘 다 받고 {@code qna.setQnaId(qnaId)}까지
     * 해 두고도 {@code updateQnaAnswer(qnaAnswer)} <b>하나만</b> 호출한다 - 즉 "저장"을 눌러도
     * <b>문의 본문은 저장되지 않고</b> 답변만 저장된다. 게다가 이 POST로 전송하는 화면이 없다
     * (수정 화면 form.jsp의 action이 {@code answer/{qnaId}}로 고정되어 있다). 닿을 수 없는
     * 엔드포인트라 AS-IS 동작(답변만 저장) 그대로 옮기고, 답변 건수·문자는 AS-IS처럼 건드리지 않는다.
     */
    @Transactional
    public void updateAnswerOnly(Integer qnaAnswerId, Long managerUserId, String title, String content) {
        if (qnaAnswerId == null) {
            return;
        }
        qnaAnswerRepository.findById(qnaAnswerId).ifPresent(answer -> {
            answer.setTitle(title);
            answer.setAnswer(content);
            answer.setUserId(managerUserId);
            answer.setAnswerDate(LocalDateTime.now().format(ANSWER_DATE));
            answer.setSendSmsFlag("N");
            answer.setSendMailFlag("N");
            qnaAnswerRepository.save(answer);
        });
    }

    /**
     * AS-IS sendSmsQnaAnswer - 문의자에게 국민비서 문자(SVC_ID {@code 812-A022}, "1:1 QnA 응답시")를
     * 적재한다. AS-IS는 {@code getQnaUserInfo}로 {@code OP_QNA·OP_USER·OP_USER_DETAIL}을 INNER JOIN해
     * 이름·전화번호·수신동의·CI를 뽑는데, TO-BE에서 회원은 member 소유라 {@link MemberAdminClient}로 받는다.
     *
     * <p>AS-IS는 <b>답변 등록·수정마다 무조건</b> 호출한다 - 답변을 고칠 때도 문자가 다시 나간다.
     * 그대로 둔다(수신동의·CI가 없으면 어차피 적재되지 않는다).
     */
    private void sendSmsQnaAnswer(Qna qna) {
        if (qna.getUserId() == null || qna.getUserId() <= 0) {
            // AS-IS는 비회원 문의에서 NPE로 500이 난다(SmsIpsService 주석 참고) - 조용히 건너뛴다
            return;
        }
        smsIpsService.send(SmsType.QNA, memberAdminClient.smsReceiver(qna.getUserId()));
    }

    /**
     * 답변화면의 답변자 표시값 - AS-IS form.jsp는 {@code 답변자 역할명 (관리자 로그인ID)}을 보여준다
     * ({@code getQnaByQnaId}가 {@code OP_ROLE.ROLE_NAME}과 {@code OP_MANAGER.LOGIN_ID}를 뽑아
     * {@code roleNm}/{@code answerLoginId}로 내려준다). 사람 이름이 아니라 <b>역할명</b>이다.
     *
     * <p>AS-IS는 역할을 {@code OP_USER_ROLE}에서 {@code AUTHORITY LIKE 'ROLE_ADMIN%'}로 찾지만
     * TO-BE는 담당자 권한이 {@code op_manager.authority}에 있어 거기서 찾는다(이 프로젝트의
     * 고정 경계 번역). 운영자 권한이 아니면 AS-IS도 역할명이 비어 보인다.
     */
    public Answerer answerer(Long userId) {
        if (userId == null || userId <= 0) {
            return new Answerer("", "");
        }
        Manager manager = managerRepository.findById(userId).orElse(null);
        if (manager == null) {
            return new Answerer("", "");
        }
        String roleName = manager.getAuthority() != null && manager.getAuthority().startsWith("ROLE_ADMIN")
                ? roleRepository.findById(manager.getAuthority()).map(Role::getRoleName).orElse("")
                : "";
        return new Answerer(roleName, manager.getLoginId() == null ? "" : manager.getLoginId());
    }

    /** 답변화면 답변자 칸 - 역할명과 관리자 로그인ID. */
    public record Answerer(String roleName, String loginId) {
    }

    /** 문의자 로그인ID - AS-IS getQnaByQnaId의 {@code LEFT JOIN OP_USER ... STATUS_CODE='9'} 자리. */
    public String askerLoginId(Long userId) {
        if (userId == null || userId <= 0) {
            return null;
        }
        return memberAdminClient.userLoginIds(List.of(userId)).get(userId);
    }

    /** 답변 단건 - AS-IS getQnaAnswerByQnaId와 같이 가장 먼저 등록된 한 건만 본다. */
    public java.util.Optional<QnaAnswer> answerOf(Integer qnaId) {
        return qnaAnswerRepository.findFirstByQnaIdOrderByAnswerDateAsc(qnaId);
    }

    /** 첨부 단건 - 다운로드용. */
    public QnaFile file(Integer qnaFileId) {
        return qnaFileRepository.findById(qnaFileId)
                .orElseThrow(() -> new QnaException("첨부파일을 찾을 수 없습니다."));
    }

    /**
     * 검색구분이 '아이디'일 때만 member에서 회원ID를 받아 온다 - AS-IS의
     * {@code U.LOGIN_ID LIKE} 조건을 서비스 경계 밖에서 대신 수행하는 것이다.
     */
    private List<Long> userIdFilter(String where, String query) {
        if (!"LOGIN_ID".equals(where) || query == null || query.isBlank()) {
            return List.of();
        }
        return memberAdminClient.userIdsByLoginIdLike(query.trim());
    }

    /**
     * AS-IS {@code op:nl2br}로 뿌리는 칸(제목·내용)용 - <b>HTML을 이스케이프한 뒤</b> 줄바꿈만
     * {@code <br/>}로 바꾼다.
     *
     * <p>AS-IS는 회원이 적어 넣은 문의 본문을 이스케이프 없이 그대로 렌더링해
     * <b>저장형 XSS</b>가 성립한다(목록 제목도 같다). 이 프로젝트의 고정 방침대로
     * 이스케이프를 넣었다 - 정상 글의 보이는 결과는 같다.
     * 커뮤니티 게시판 본문은 에디터 HTML이라 그대로 렌더링해야 해서 {@link CmntyText#nl2br}을
     * 쓰지만, 여기는 <b>평문</b>이라 이스케이프가 맞다.
     */
    public static String escapeNl2br(String value) {
        if (value == null) {
            return "";
        }
        return CmntyText.nl2br(org.springframework.web.util.HtmlUtils.htmlEscape(value));
    }

    /**
     * AS-IS 마스킹: {@code 첫 글자 + "*" + 세 번째 글자부터}.
     * 한 글자 이름이면 AS-IS는 예외로 터지므로 그대로 둔다(클래스 주석의 결함 참고).
     */
    static String maskUserName(String userName) {
        if (userName == null || userName.isBlank()) {
            return userName;
        }
        if (userName.length() < 2) {
            return userName;
        }
        return userName.charAt(0) + "*" + userName.substring(2);
    }
}
