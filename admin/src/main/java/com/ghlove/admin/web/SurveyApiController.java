package com.ghlove.admin.web;

import com.ghlove.admin.domain.Qustnr;
import com.ghlove.admin.domain.QustnrQesitm;
import com.ghlove.admin.domain.QustnrRspns;
import com.ghlove.admin.repository.QustnrQesitmRepository;
import com.ghlove.admin.repository.QustnrRepository;
import com.ghlove.admin.repository.QustnrRspnsRepository;
import com.ghlove.admin.service.JwtVerifier;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * storefront(Vue3 SPA)용 공개 설문 참여 API - AS-IS `/api/qustnr/{qustnrSn}`(QustnrController)
 * 재현. 운영 콘솔의 설문관리({@link QustnrAdminController} /admin/surveys)는 문항 등록·결과집계까지
 * 있었으나 이용자가 참여할 화면·경로가 없었다.
 *
 * <p>AS-IS getQustnrByApi의 판정을 옮겼다: 로그인 필수, 노출중(isShow=Y)+노출기간 안이어야 하고,
 * 이미 응답한 사용자는 재참여 불가(regCnt&gt;0 → ALREADY_DONE). AS-IS의 객관식 보기(QustnrIem)는
 * MSA가 자유서술형 문항으로 단순화했으므로({@link QustnrAdminController} 참고) 응답도 문항별 텍스트다.
 */
@RestController
@RequiredArgsConstructor
public class SurveyApiController {

    private static final String SHOW_Y = "Y";
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final QustnrRepository qustnrRepository;
    private final QustnrQesitmRepository qesitmRepository;
    private final QustnrRspnsRepository rspnsRepository;
    private final JwtVerifier jwtVerifier;

    public record QuestionDto(Long qustnrQesitmSn, String qestnCn, Integer qestnSeq) {
    }

    public record SurveyDto(Long qustnrSn, String qustnrSj, String qustnrBgnDe, String qustnrEndDe,
                            boolean loggedIn, boolean alreadyResponded, List<QuestionDto> questions) {
    }

    public record AnswerDto(Long qustnrQesitmSn, String rspnsCn) {
    }

    /** 현재 참여 가능한(노출중 + 기간 내) 설문 중 가장 최근 것. 없으면 204. */
    @GetMapping("/api/surveys/active")
    public ResponseEntity<SurveyDto> active(HttpServletRequest request) {
        String today = LocalDate.now().format(DATE);
        return qustnrRepository.findByIsShowOrderByQustnrSnDesc(SHOW_Y).stream()
                .filter(q -> withinPeriod(q, today))
                .findFirst()
                .map(q -> ResponseEntity.ok(toDto(q, request)))
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    /** 특정 설문(팝업·배너에서 링크로 진입). 없으면 404. */
    @GetMapping("/api/surveys/{id}")
    public ResponseEntity<SurveyDto> get(@PathVariable Long id, HttpServletRequest request) {
        return qustnrRepository.findById(id)
                .map(q -> ResponseEntity.ok(toDto(q, request)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /** 설문 응답 제출 - 로그인 필수, 문항별 텍스트 1건씩 저장. 1인 1회. */
    @PostMapping("/api/surveys/{id}/responses")
    @Transactional
    public ResponseEntity<Void> submit(@PathVariable Long id, @RequestBody List<AnswerDto> answers,
                                       HttpServletRequest request) {
        Long userId = jwtVerifier.currentUserId(request)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."));

        Qustnr survey = qustnrRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "설문을 찾을 수 없습니다."));
        String today = LocalDate.now().format(DATE);
        if (!SHOW_Y.equals(survey.getIsShow()) || !withinPeriod(survey, today)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "참여할 수 없는 설문입니다.");
        }
        if (rspnsRepository.existsByQustnrSnAndUserId(id, userId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 참여한 설문입니다.");
        }
        if (answers == null || answers.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "응답을 입력해 주세요.");
        }

        LocalDateTime now = LocalDateTime.now();
        for (AnswerDto a : answers) {
            if (a.qustnrQesitmSn() == null) {
                continue;
            }
            QustnrRspns r = new QustnrRspns();
            r.setQustnrSn(id);
            r.setQustnrQesitmSn(a.qustnrQesitmSn());
            r.setUserId(userId);
            r.setRspnsCn(a.rspnsCn());
            r.setRspnsDt(now);
            rspnsRepository.save(r);
        }
        return ResponseEntity.noContent().build();
    }

    private boolean withinPeriod(Qustnr q, String today) {
        String bgn = q.getQustnrBgnDe();
        String end = q.getQustnrEndDe();
        // 시작일/종료일이 비어 있으면 그 방향 제한 없음 (운영 콘솔이 기간을 선택 입력이라)
        boolean afterStart = bgn == null || bgn.isBlank() || bgn.compareTo(today) <= 0;
        boolean beforeEnd = end == null || end.isBlank() || today.compareTo(end) <= 0;
        return afterStart && beforeEnd;
    }

    private SurveyDto toDto(Qustnr q, HttpServletRequest request) {
        Long userId = jwtVerifier.currentUserId(request).orElse(null);
        boolean already = userId != null && rspnsRepository.existsByQustnrSnAndUserId(q.getQustnrSn(), userId);
        List<QuestionDto> questions = qesitmRepository.findByQustnrSnOrderByQestnSeq(q.getQustnrSn()).stream()
                .map(this::toQuestion)
                .toList();
        return new SurveyDto(q.getQustnrSn(), q.getQustnrSj(), q.getQustnrBgnDe(), q.getQustnrEndDe(),
                userId != null, already, questions);
    }

    private QuestionDto toQuestion(QustnrQesitm e) {
        return new QuestionDto(e.getQustnrQesitmSn(), e.getQestnCn(), e.getQestnSeq());
    }
}
