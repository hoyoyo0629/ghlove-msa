package com.ghlove.admin.web;

import com.ghlove.admin.service.CaptchaService;
import com.ghlove.admin.service.CommonCodeService;
import com.ghlove.admin.service.JwtVerifier;
import com.ghlove.admin.service.QnaException;
import com.ghlove.admin.service.QnaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/** storefront(Vue3 SPA)용 마이페이지 "1:1 문의" JSON API - {@link QnaController}(Thymeleaf)의
 * `/qna` GET/POST와 완전히 같은 {@link QnaService} 로직을 감싼다. 캡차 이미지(`GET /qna/captcha`)
 * 와 음성듣기(`GET /qna/captcha/audio-text`)는 이미 순수 GET이라 프론트가 그대로 재사용한다
 * (여기서 다시 만들지 않음). 첨부파일이 있어 등록은 JSON 바디가 아니라 multipart로 받는다. */
@RestController
@RequiredArgsConstructor
public class QnaApiController {

    private final QnaService qnaService;
    private final CommonCodeService commonCodeService;
    private final JwtVerifier jwtVerifier;

    public record QnaFileDto(Integer qnaFileId, String orgFileName) {
    }

    public record QnaRowDto(Integer qnaId, String qnaGroup, String subject, String question, String createdDate,
                             boolean secretFlag, String answerTitle, String answer, String answerDate,
                             List<QnaFileDto> files) {
    }

    public record QnaListResponse(Map<String, String> qnaGroups, List<QnaRowDto> inquiries) {
    }

    @GetMapping("/api/qna")
    public ResponseEntity<QnaListResponse> list(@RequestParam(required = false) String searchStartDate,
                                                 @RequestParam(required = false) String searchEndDate,
                                                 HttpServletRequest request) {
        var authUserId = jwtVerifier.currentUserId(request);
        if (authUserId.isEmpty()) {
            return ResponseEntity.status(401).build();
        }
        var rows = qnaService.myInquiriesDetailed(authUserId.get(), searchStartDate, searchEndDate).stream()
                .map(r -> new QnaRowDto(r.qna().getQnaId(), r.qna().getQnaGroup(), r.qna().getSubject(),
                        r.qna().getQuestion(), r.qna().getCreatedDate(), "Y".equals(r.qna().getSecretFlag()),
                        r.answer() != null ? r.answer().getTitle() : null,
                        r.answer() != null ? r.answer().getAnswer() : null,
                        r.answer() != null ? r.answer().getAnswerDate() : null,
                        r.files().stream().map(f -> new QnaFileDto(f.getQnaFileId(), f.getOrgFileName())).toList()))
                .toList();
        return ResponseEntity.ok(new QnaListResponse(commonCodeService.labelsOf("QNA_GROUPS"), rows));
    }

    @PostMapping(value = "/api/qna", consumes = "multipart/form-data")
    public ResponseEntity<Map<String, Object>> submit(@RequestParam(required = false) String userName,
                                                        @RequestParam(required = false) String email,
                                                        @RequestParam String qnaGroup, @RequestParam String subject,
                                                        @RequestParam String question,
                                                        @RequestParam(required = false) boolean secretFlag,
                                                        @RequestParam(required = false) List<MultipartFile> files,
                                                        @RequestParam(required = false) String captcha,
                                                        HttpServletRequest request, HttpSession session) {
        var authUserId = jwtVerifier.currentUserId(request);
        if (authUserId.isEmpty()) {
            return ResponseEntity.status(401).build();
        }
        String answer = (String) session.getAttribute(CaptchaService.SESSION_KEY);
        session.removeAttribute(CaptchaService.SESSION_KEY);
        if (answer == null || !answer.equals(captcha)) {
            return ResponseEntity.badRequest().body(Map.of("status", "ERROR", "message", "자동입력 방지문자가 일치하지 않습니다."));
        }
        try {
            qnaService.ask(authUserId.get(), userName, email, qnaGroup, subject, question, secretFlag, files);
            return ResponseEntity.ok(Map.of("status", "OK"));
        } catch (QnaException e) {
            return ResponseEntity.badRequest().body(Map.of("status", "ERROR", "message", e.getMessage()));
        }
    }

    // ---- 고객센터 > Q&A (공개 게시판, AS-IS qna/qna-open.html) ----

    public record BoardRowDto(int no, String type, String userName, String subject, String createdDate,
                               String hits, boolean locked, Integer qnaId) {
    }

    public record BoardPageResponse(List<BoardRowDto> rows, int currentPage, int totalPages, int totalCount) {
    }

    @GetMapping("/api/qna/board")
    public BoardPageResponse board(@RequestParam(required = false, defaultValue = "SUBJECT") String where,
                                    @RequestParam(required = false) String q,
                                    @RequestParam(required = false, defaultValue = "CREATED_DATE") String orderBy,
                                    @RequestParam(required = false, defaultValue = "DESC") String sort,
                                    @RequestParam(required = false, defaultValue = "1") int page,
                                    @RequestParam(required = false, defaultValue = "10") int size,
                                    HttpServletRequest request) {
        Long viewerUserId = jwtVerifier.currentUserId(request).orElse(null);
        var result = qnaService.publicBoard(where, q, orderBy, sort, page, size, viewerUserId);
        var rows = result.rows().stream()
                .map(r -> new BoardRowDto(r.no(), r.type(), r.userName(), r.subject(), r.createdDate(), r.hits(),
                        r.locked(), r.qnaId()))
                .toList();
        return new BoardPageResponse(rows, result.currentPage(), result.totalPages(), result.totalCount());
    }

    public record BoardFileDto(Integer qnaFileId, String orgFileName) {
    }

    public record BoardAnswerDto(String title, String answer) {
    }

    public record BoardDetailDto(Integer qnaId, String subject, String question, String userName, String createdDate,
                                  int hits, List<BoardFileDto> files, BoardAnswerDto answer, boolean locked) {
    }

    @GetMapping("/api/qna/board/{qnaId}")
    public ResponseEntity<BoardDetailDto> boardDetail(@PathVariable Integer qnaId, HttpServletRequest request) {
        Long viewerUserId = jwtVerifier.currentUserId(request).orElse(null);
        var found = qnaService.publicBoardDetail(qnaId, viewerUserId);
        if (found.isEmpty()) {
            return ResponseEntity.status(404).build();
        }
        var detail = found.get();
        if (detail.locked()) {
            return ResponseEntity.ok(new BoardDetailDto(qnaId, null, null, null, null, 0, List.of(), null, true));
        }
        var files = detail.files().stream().map(f -> new BoardFileDto(f.getQnaFileId(), f.getOrgFileName())).toList();
        var answer = detail.answer() != null ? new BoardAnswerDto(detail.answer().getTitle(), detail.answer().getAnswer()) : null;
        return ResponseEntity.ok(new BoardDetailDto(qnaId, detail.qna().getSubject(), detail.qna().getQuestion(),
                detail.qna().getUserName(), detail.qna().getCreatedDate(),
                detail.qna().getHits() == null ? 0 : detail.qna().getHits(), files, answer, false));
    }
}
