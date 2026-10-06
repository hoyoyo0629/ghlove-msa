package com.ghlove.admin.web;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.service.CommonCodeService;
import com.ghlove.admin.service.CommonMessageService;
import com.ghlove.admin.service.QnaException;
import com.ghlove.admin.service.QnaFileStorageService;
import com.ghlove.admin.service.QnaOpenAdminService;
import com.ghlove.admin.service.QnaService;
import com.ghlove.admin.web.support.Pagination;
import com.ghlove.admin.web.support.QnaOpenSearchParam;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Q&A 관리 (메뉴 5112) - AS-IS {@code QnaOpenManagerController}
 * ({@code /opmanager/qna-open}) 이식. 공개 {@code QnaController}와 같은
 * {@code OP_QNA}/{@code OP_QNA_ANSWER}를 쓰지만 로그인은 회원 JWT가 아니라 매니저 세션이다
 * ({@code AdminAuthInterceptor}가 게이트). URL을 {@code /qna-admin}으로 분리해 공개
 * {@code /qna}와 겹치지 않게 했다.
 *
 * <p><b>★ 이름 충돌 주의</b>: 여기 {@code /qna-admin}은 <b>공개 Q&A(5112)</b> 관리화면이고,
 * AS-IS {@code /opmanager/qna-admin}({@code QnaAdminManagerController})은 <b>내부문의(5120,
 * {@code G_QNA_ADMIN})</b>다 - 서로 다른 화면이다. TO-BE 내부문의는
 * {@link InternalInquiryAdminController}({@code /admin/internal-inquiry})에 있다.
 *
 * <p><b>AS-IS 동작 그대로</b>: 목록 <b>진입(GET)은 조회하지 않고 빈 목록</b>이고 검색(POST)에서만
 * 조회한다. 정렬은 화면에서 못 바꾸고 컨트롤러가 고정한다.
 * 조회 조건·고정값·AS-IS 결함은 {@link com.ghlove.admin.service.QnaOpenAdminService}와
 * {@link com.ghlove.admin.repository.QnaOpenAdminRepository} 주석에 적어 두었다.
 */
@Controller
@RequiredArgsConstructor
public class QnaAdminController {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final QnaService qnaService;
    private final com.ghlove.admin.web.support.FlashRedirect flashRedirect;
    private final QnaOpenAdminService qnaOpenAdminService;
    private final CommonCodeService commonCodeService;
    private final CommonMessageService commonMessageService;
    private final QnaFileStorageService qnaFileStorageService;

    /** AS-IS GET qna-open/list - 진입에서는 조회하지 않는다(빈 목록). */
    @GetMapping("/qna-admin")
    public String list(@ModelAttribute("searchParam") QnaOpenSearchParam searchParam,
                       HttpServletRequest request, Model model) {
        fillList(searchParam, request, model, false);
        return "qna-admin/list";
    }

    /** AS-IS POST qna-open/list - 검색. */
    @PostMapping("/qna-admin")
    public String searchList(@ModelAttribute("searchParam") QnaOpenSearchParam searchParam,
                             HttpServletRequest request, Model model) {
        fillList(searchParam, request, model, true);
        return "qna-admin/list";
    }

    private void fillList(QnaOpenSearchParam searchParam, HttpServletRequest request, Model model,
                          boolean doSearch) {
        searchParam.applyDefaults();

        int count = 0;
        List<QnaOpenAdminService.QnaRow> list = List.of();
        if (doSearch) {
            count = qnaOpenAdminService.count(searchParam.getQnaOpenAnswerCode(),
                    searchParam.getWhere(), searchParam.getQuery(),
                    searchParam.getSearchStartDate(), searchParam.getSearchEndDate());
        }
        Pagination pagination = Pagination.of(count, searchParam.getPage(), searchParam.getItemsPerPage())
                .withLinkFrom(request);
        if (doSearch) {
            list = qnaOpenAdminService.list(searchParam.getQnaOpenAnswerCode(), searchParam.getWhere(),
                    searchParam.getQuery(), searchParam.getSearchStartDate(),
                    searchParam.getSearchEndDate(), pagination.getStartRow(),
                    pagination.getItemsPerPage());
        }

        model.addAttribute("qnaList", list);
        model.addAttribute("qnaCount", count);
        model.addAttribute("pagination", pagination);
        // AS-IS가 화면에 같이 내려주는 날짜 기준값들 - 이 화면만 month3(다른 화면은 month2)다
        String today = LocalDate.now().format(DAY);
        model.addAttribute("today", today);
        model.addAttribute("week", LocalDate.now().minusDays(7).format(DAY));
        model.addAttribute("month1", LocalDate.now().minusMonths(1).format(DAY));
        model.addAttribute("month3", LocalDate.now().minusMonths(3).format(DAY));
        model.addAttribute("qnaGroups", commonCodeService.labelsOf("QNA_GROUPS"));
        // AS-IS qnaTypes는 항상 빈 목록이다(QnaOpenAdminService 주석 참고) - 그대로 둔다
        model.addAttribute("qnaTypes", List.of());
    }

    /**
     * AS-IS POST qna-open/delete - 목록에서 체크한 문의 일괄삭제.
     * <b>답변이 있는 건은 조용히 건너뛴다</b>(AS-IS 규칙) - 자세한 내용은
     * {@link QnaOpenAdminService#deleteList(List)} 주석.
     */
    @PostMapping("/qna-admin/delete")
    @ResponseBody
    public Map<String, Object> deleteList(@RequestParam(value = "id", required = false) List<Integer> id) {
        qnaOpenAdminService.deleteList(id);
        // AS-IS JsonViewUtils.success() - 메시지 없이 성공만 돌려준다
        return Map.of("isSuccess", true);
    }

    /**
     * AS-IS GET qna-open/answer/{qnaId} - <b>답변 화면</b>. 목록에서 제목을 누르면 여기로 온다
     * (검색조건을 그대로 들고 GET 전송). AS-IS는 등록·수정을 한 화면(form)에서 처리하고
     * 버튼 문구를 "답글등록"으로 내려준다.
     */
    @GetMapping("/qna-admin/answer/{qnaId}")
    public String answerForm(@PathVariable Integer qnaId,
                             @ModelAttribute("searchParam") QnaOpenSearchParam searchParam,
                             Model model) {
        return fillAnswerForm(qnaId, model, true, "답글등록");
    }

    /**
     * AS-IS GET qna-open/edit/{qnaId} - 답변화면과 <b>같은 화면</b>(form.jsp)을 보여준다.
     * AS-IS는 버튼문구만 "저장"으로 내려주는데 form.jsp가 그 값을 쓰지 않아 화면은 완전히 같고,
     * <b>첨부파일만 내려주지 않아</b> 첨부 칸이 비어 보인다(AS-IS qnaUpdate에 qnaImage가 없다).
     * 그 차이까지 그대로 둔다. <b>AS-IS 목록·화면 어디에도 이 URL로 가는 링크가 없다</b>.
     */
    @GetMapping("/qna-admin/edit/{qnaId}")
    public String editForm(@PathVariable Integer qnaId,
                           @ModelAttribute("searchParam") QnaOpenSearchParam searchParam,
                           Model model) {
        return fillAnswerForm(qnaId, model, false, "저장");
    }

    private String fillAnswerForm(Integer qnaId, Model model, boolean withFiles, String buttonName) {
        var qna = qnaService.find(qnaId).orElse(null);
        if (qna == null) {
            return "redirect:/qna-admin";
        }
        var qnaAnswer = qnaOpenAdminService.answerOf(qnaId).orElse(null);
        model.addAttribute("qna", qna);
        model.addAttribute("qnaAnswer", qnaAnswer);
        model.addAttribute("qnaImage", withFiles ? qnaService.filesOf(qnaId) : List.of());
        // AS-IS op:nl2br 자리 - 이스케이프 후 줄바꿈만 <br/>로 바꾼다(QnaOpenAdminService 주석)
        model.addAttribute("subjectHtml", QnaOpenAdminService.escapeNl2br(qna.getSubject()));
        model.addAttribute("questionHtml", QnaOpenAdminService.escapeNl2br(qna.getQuestion()));
        // AS-IS 답변자 칸은 "역할명 (관리자 로그인ID)"다 - 사람 이름이 아니다
        model.addAttribute("answerer", qnaOpenAdminService.answerer(
                qnaAnswer == null ? null : qnaAnswer.getUserId()));
        model.addAttribute("answerId", qnaAnswer == null ? 0 : qnaAnswer.getQnaAnswerId());
        // AS-IS는 OP_USER를 조인해 문의자 로그인ID를 같이 내려준다(정상회원만)
        model.addAttribute("askerLoginId", qnaOpenAdminService.askerLoginId(qna.getUserId()));
        // AS-IS: 문의유형 라벨을 코드에서 찾아 내려준다
        model.addAttribute("qnaAnswerTypeLabel",
                commonCodeService.labelsOf("QNA_GROUPS").getOrDefault(qna.getQnaGroup(), ""));
        model.addAttribute("buttonName", buttonName);
        return "qna-admin/answer-form";
    }

    /**
     * AS-IS GET qna-open/delete/{qnaId} - 답변화면의 [문의글 삭제]. <b>ajax로 호출되고 JSON을
     * 돌려준다</b>(AS-IS도 {@code JsonView}다 - 화면이 $.get으로 부른다).
     * 일괄삭제와 달리 <b>답변이 있어도 지운다</b>.
     */
    @GetMapping("/qna-admin/delete/{qnaId}")
    @ResponseBody
    public Map<String, Object> deleteQna(@PathVariable Integer qnaId) {
        try {
            qnaOpenAdminService.deleteQna(qnaId);
        } catch (QnaException e) {
            return Map.of("isSuccess", false, "errorMessage", e.getMessage());
        }
        return Map.of("isSuccess", true);
    }

    /** AS-IS GET qna-open/delete/{qnaId}/answer/{qnaAnswerId} - 답변만 삭제(ajax). */
    @GetMapping("/qna-admin/delete/{qnaId}/answer/{qnaAnswerId}")
    @ResponseBody
    public Map<String, Object> deleteQnaAnswer(@PathVariable Integer qnaId,
                                               @PathVariable Integer qnaAnswerId) {
        try {
            qnaOpenAdminService.deleteQnaAnswer(qnaId, qnaAnswerId);
        } catch (QnaException e) {
            return Map.of("isSuccess", false, "errorMessage", e.getMessage());
        }
        return Map.of("isSuccess", true);
    }

    /**
     * AS-IS POST qna-open/delete-item-image - 첨부파일 삭제(ajax).
     * AS-IS 파라미터 이름이 {@code itemId}인데 실제로는 첨부파일 id다(원제품 잔재) - 이름을 그대로 받는다.
     */
    @PostMapping("/qna-admin/delete-item-image")
    @ResponseBody
    public Map<String, Object> deleteItemImage(@RequestParam("itemId") Integer itemId) {
        qnaOpenAdminService.deleteFile(itemId);
        return Map.of("isSuccess", true);
    }

    /**
     * AS-IS GET qna-open/file-download/{qnaFileId}/{qnaDetailType} - 첨부 다운로드.
     *
     * <p>{@code qnaDetailType}은 질문첨부({@code 'Q'} → {@code OP_QNA_FILE})와
     * 답변첨부(그 외 → {@code OP_QNA_ANSWER_FILE})를 가르는 값이다
     * ({@code getFrontQnaOpenFileDetail}). 이 화면은 항상 {@code 'Q'}를 보낸다 -
     * 답변 첨부를 등록하는 경로가 없어 답변첨부 표는 비어 있다.
     */
    @GetMapping("/qna-admin/file-download/{qnaFileId}/{qnaDetailType}")
    public ResponseEntity<?> fileDownload(@PathVariable Integer qnaFileId,
                                          @PathVariable String qnaDetailType) throws IOException {
        if (!"Q".equals(qnaDetailType)) {
            // AS-IS는 답변첨부 표를 보는데 그 표에 행이 생기는 경로가 없다 - 없는 파일과 같다
            return ResponseEntity.notFound().build();
        }
        var file = qnaOpenAdminService.file(qnaFileId);
        Resource resource = new UrlResource(qnaFileStorageService.resolve(file.getFileName()).toUri());
        if (!resource.exists()) {
            return ResponseEntity.ok()
                    .contentType(new MediaType("text", "html", StandardCharsets.UTF_8))
                    .body("<script>alert('파일이 존재하지 않습니다. 시스템관리자에 문의하여 주십시오.');"
                            + " history.back();</script>");
        }
        String downloadName = file.getOrgFileName() != null ? file.getOrgFileName() : file.getFileName();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(downloadName, StandardCharsets.UTF_8)
                                .build().toString())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    /**
     * AS-IS POST qna-open/answer/{qnaId} - 답변 등록·수정. 끝나면 목록으로 돌아가며
     * {@code M00492}("답변이 등록되었습니다.")를 띄운다(AS-IS도 수정일 때도 같은 문구다).
     * 메일·UMS 분기를 옮기지 않은 이유는 {@link QnaOpenAdminService#answer} 주석에 적어 두었다.
     */
    @PostMapping("/qna-admin/answer/{qnaId}")
    public String answer(@PathVariable Integer qnaId, @RequestParam String title,
                         @RequestParam("answer") String answer, HttpSession session) {
        Manager manager = (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
        try {
            qnaOpenAdminService.answer(qnaId, manager.getUserId(), title, answer);
        } catch (QnaException e) {
            return flashRedirect.to("/qna-admin/answer/" + qnaId, e.getMessage());
        }
        return flashRedirect.to("/qna-admin", msg("M00492", "답변이 등록되었습니다."));
    }

    /**
     * AS-IS POST qna-open/edit/{qnaId} - <b>답변만 저장하고 문의 본문은 저장하지 않는다</b>
     * (AS-IS 결함). 이 URL로 전송하는 화면도 없다 - 자세한 내용은
     * {@link QnaOpenAdminService#updateAnswerOnly} 주석.
     */
    @PostMapping("/qna-admin/edit/{qnaId}")
    public String edit(@PathVariable Integer qnaId,
                       @RequestParam(value = "qnaAnswerId", required = false) Integer qnaAnswerId,
                       @RequestParam(required = false) String title,
                       @RequestParam(value = "answer", required = false) String answer,
                       HttpSession session) {
        Manager manager = (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
        qnaOpenAdminService.updateAnswerOnly(qnaAnswerId, manager.getUserId(), title, answer);
        return flashRedirect.to("/qna-admin", "수정되었습니다.");
    }

    /** 문구는 코드로 조회한다 - 없으면 코드 자체가 돌아오므로 AS-IS 주석의 문구로 대신한다. */
    private String msg(String code, String fallback) {
        String message = commonMessageService.get(code);
        return message == null || message.equals(code) ? fallback : message;
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
