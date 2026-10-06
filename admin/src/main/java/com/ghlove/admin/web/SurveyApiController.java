package com.ghlove.admin.web;

import com.ghlove.admin.repository.QestnarRepository;
import com.ghlove.admin.service.JwtVerifier;
import com.ghlove.admin.service.QestnarAdminService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * storefront(Vue3 SPA)용 공개 설문 참여 API - AS-IS `/api/qustnr/{qustnrSn}`(QustnrController) 재현.
 *
 * <p>2026-10-02: 운영 콘솔 설문관리({@link QustnrAdminController})를 AS-IS 모델
 * ({@code G_QESTNAR}/{@code G_QUSTNR_QESITM}/{@code G_QUSTNR_IEM}/{@code G_QUSTNR_RSPNS_RESULT})로
 * 이식하면서 이 API도 같은 표를 보도록 옮겼다 - 그대로 두면 관리자가 등록한 설문이 이용자 화면에
 * 보이지 않는 split-brain이 된다. 문항이 객관식 선택지(qustnrIem)를 갖게 되었으므로 응답은
 * 선택지 id(qustnrIemSn)와 텍스트(respondAnswerCn, 주관식)를 함께 받는다.
 *
 * <p>AS-IS 판정을 유지한다: 로그인 필수, 노출기간 안이어야 하고, 이미 응답한 사용자는 재참여 불가.
 */
@RestController
@RequiredArgsConstructor
public class SurveyApiController {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final QestnarRepository qestnarRepository;
    private final QestnarAdminService qestnarAdminService;
    private final JwtVerifier jwtVerifier;

    /** 선택지 - 객관식 문항의 보기. 주관식(stype)은 입력칸 자리표시용으로 1건만 온다. */
    public record ChoiceDto(Integer qustnrIemSn, Integer iemSn, String iemCn) {
    }

    public record QuestionDto(Integer qustnrQesitmSn, String qestnCn, String qestnTyCode,
                              Integer parentSn, List<ChoiceDto> choices) {
    }

    public record SurveyDto(Long qustnrSn, String qustnrSj, String qustnrBgnDe, String qustnrEndDe,
                            String srvyTrgt, boolean loggedIn, boolean alreadyResponded,
                            List<QuestionDto> questions) {
    }

    /** 응답 한 건 - 객관식은 qustnrIemSn, 주관식은 respondAnswerCn. */
    public record AnswerDto(Integer qustnrQesitmSn, Integer qustnrIemSn, String respondAnswerCn) {
    }

    /** 현재 참여 가능한(기간 내) 대민 설문 중 가장 최근 것. 없으면 204. */
    @GetMapping("/api/surveys/active")
    public ResponseEntity<SurveyDto> active(HttpServletRequest request) {
        return qestnarRepository.getQustnrList(null).stream()
                .filter(q -> "U".equals(q.srvyTrgt()))
                .filter(q -> "Y".equals(q.isShow()))
                .findFirst()
                .map(q -> ResponseEntity.ok(toDto(q, request)))
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    /** 특정 설문(팝업·배너에서 링크로 진입). 없으면 404. */
    @GetMapping("/api/surveys/{id}")
    public ResponseEntity<SurveyDto> get(@PathVariable Long id, HttpServletRequest request) {
        QestnarRepository.QestnarRow survey = qestnarRepository.getQustnr(id);
        return survey == null ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(toDto(survey, request));
    }

    /** 설문 응답 제출 - 로그인 필수, 1인 1회. */
    @PostMapping("/api/surveys/{id}/responses")
    @Transactional
    public ResponseEntity<Void> submit(@PathVariable Long id, @RequestBody List<AnswerDto> answers,
                                       HttpServletRequest request) {
        Long userId = jwtVerifier.currentUserId(request)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."));

        QestnarRepository.QestnarRow survey = qestnarRepository.getQustnr(id);
        if (survey == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "설문을 찾을 수 없습니다.");
        }
        if (!withinPeriod(survey)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "참여할 수 없는 설문입니다.");
        }
        if (qestnarRepository.existsResponse(id, userId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 참여한 설문입니다.");
        }
        if (answers == null || answers.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "응답을 입력해 주세요.");
        }

        for (AnswerDto a : answers) {
            if (a.qustnrQesitmSn() == null) {
                continue;
            }
            qestnarRepository.insertQustnrRspnsResult(id, a.qustnrQesitmSn(), userId,
                    a.qustnrIemSn() == null ? 0 : a.qustnrIemSn(), a.respondAnswerCn());
        }
        return ResponseEntity.noContent().build();
    }

    /** 기간 판정 - 시작/종료일이 비어 있으면 그 방향 제한은 없다(AS-IS도 기간을 선택 입력으로 둔다). */
    private boolean withinPeriod(QestnarRepository.QestnarRow q) {
        String today = LocalDate.now().format(DATE);
        String bgn = q.qustnrBgnDe();
        String end = q.qustnrEndDe();
        boolean afterStart = bgn == null || bgn.isBlank() || bgn.compareTo(today) <= 0;
        boolean beforeEnd = end == null || end.isBlank() || today.compareTo(end) <= 0;
        return afterStart && beforeEnd;
    }

    private SurveyDto toDto(QestnarRepository.QestnarRow q, HttpServletRequest request) {
        Long userId = jwtVerifier.currentUserId(request).orElse(null);
        boolean already = userId != null && qestnarRepository.existsResponse(q.qustnrSn(), userId);
        List<QuestionDto> questions = qestnarAdminService.qesitmViews(q.qustnrSn()).stream()
                .map(v -> new QuestionDto(v.qustnrQesitmSn(), v.qestnCn(), v.qestnTyCode(), v.parentSn(),
                        v.qustnrIem().stream()
                                .map(i -> new ChoiceDto(i.qustnrIemSn(), i.iemSn(), i.iemCn()))
                                .toList()))
                .toList();
        return new SurveyDto(q.qustnrSn(), q.qustnrSj(), q.qustnrBgnDe(), q.qustnrEndDe(), q.srvyTrgt(),
                userId != null, already, questions);
    }
}
