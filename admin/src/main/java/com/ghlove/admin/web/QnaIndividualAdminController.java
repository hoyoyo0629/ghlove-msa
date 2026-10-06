package com.ghlove.admin.web;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.service.CommonCodeService;
import com.ghlove.admin.service.CommonMessageService;
import com.ghlove.admin.service.QnaException;
import com.ghlove.admin.service.QnaFileStorageService;
import com.ghlove.admin.service.QnaIndividualAdminService;
import com.ghlove.admin.service.QnaOpenAdminService;
import com.ghlove.admin.service.QnaService;
import com.ghlove.admin.web.support.Pagination;
import com.ghlove.admin.web.support.QnaIndividualSearchParam;
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
 * 1:1 문의 관리 (메뉴 5102) - AS-IS {@code QnaManagerController}({@code /opmanager/qna}) 이식.
 *
 * <p><b>★ 이 메뉴에 다른 화면이 올라가 있었다</b>: TO-BE 5102는 {@code /admin/shop-inquiries}에서
 * AS-IS <b>입점문의</b>({@code InquiryManagerController}, {@code /opmanager/inquiry})를 조회 전용으로
 * 옮겨 놓은 것이었고, 표도 AS-IS의 {@code OP_STORE_INQUIRY}가 아닌 <b>{@code OP_SHOP_INQUIRY}</b>를
 * 새로 만들어 쓰고 있었다(0행). 1:1문의의 AS-IS 정본은 {@code OP_QNA}이고 Q&A(5112)와 같은 표다 -
 * 구분은 {@code QNA_TYPE}({@code '0'}=1:1문의, {@code '1'}=상품문의(5103 중지), {@code '2'}=Q&A).
 * 그래서 화면을 새로 만들고 URL을 {@code /admin/inquiries}로 옮겼다.
 * (AS-IS 입점문의는 <b>op_menu에 행이 없어 메뉴로 접근할 수 없는 화면</b>이고 SalesOn 쇼핑몰
 * 입점 상담 기능이라 별도 판단 대상으로 남긴다 - {@code op_store_inquiry}도 0행이다.)
 *
 * <p><b>★ 이름 충돌 주의</b>: TO-BE에는 Q&A 계열 화면이 셋이다 -
 * {@code /admin/inquiries}(1:1문의 5102, 여기) · {@code /qna-admin}(공개 Q&A 5112) ·
 * {@code /admin/internal-inquiry}(내부문의 5120, {@code g_qna_admin}).
 *
 * <p>AS-IS 동작·결함은 {@link QnaIndividualAdminService} 주석에 적어 두었다. 삭제·첨부
 * 엔드포인트는 AS-IS가 두 화면에서 같은 서비스 메서드를 부르므로 {@link QnaOpenAdminService}를 쓴다.
 */
@Controller
@RequiredArgsConstructor
public class QnaIndividualAdminController {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final QnaService qnaService;
    private final com.ghlove.admin.web.support.FlashRedirect flashRedirect;
    private final QnaIndividualAdminService qnaIndividualAdminService;
    private final QnaOpenAdminService qnaOpenAdminService;
    private final CommonCodeService commonCodeService;
    private final CommonMessageService commonMessageService;
    private final QnaFileStorageService qnaFileStorageService;

    /** AS-IS GET qna/list - 진입에서는 조회하지 않는다(빈 목록). */
    @GetMapping("/admin/inquiries")
    public String list(@ModelAttribute("searchParam") QnaIndividualSearchParam searchParam,
                       HttpServletRequest request, Model model) {
        fillList(searchParam, request, model, false);
        return "inquiry-admin/list";
    }

    /** AS-IS POST qna/list - 검색. */
    @PostMapping("/admin/inquiries")
    public String searchList(@ModelAttribute("searchParam") QnaIndividualSearchParam searchParam,
                             HttpServletRequest request, Model model) {
        fillList(searchParam, request, model, true);
        return "inquiry-admin/list";
    }

    private void fillList(QnaIndividualSearchParam searchParam, HttpServletRequest request,
                          Model model, boolean doSearch) {
        searchParam.applyDefaults();

        int count = 0;
        List<QnaIndividualAdminService.QnaRow> list = List.of();
        if (doSearch) {
            count = qnaIndividualAdminService.count(searchParam.getQnaAnswerCode(),
                    searchParam.getWhere(), searchParam.getQuery(),
                    searchParam.getSearchStartDate(), searchParam.getSearchEndDate());
        }
        Pagination pagination = Pagination.of(count, searchParam.getPage(), searchParam.getItemsPerPage())
                .withLinkFrom(request);
        if (doSearch) {
            list = qnaIndividualAdminService.list(searchParam.getQnaAnswerCode(), searchParam.getWhere(),
                    searchParam.getQuery(), searchParam.getSearchStartDate(),
                    searchParam.getSearchEndDate(), pagination.getStartRow(),
                    pagination.getItemsPerPage());
        }

        model.addAttribute("qnaList", list);
        model.addAttribute("qnaCount", count);
        model.addAttribute("pagination", pagination);
        // AS-IS가 같이 내려주는 날짜 기준값들 - 이 화면은 today/week/month1/month3다
        String today = LocalDate.now().format(DAY);
        model.addAttribute("today", today);
        model.addAttribute("week", LocalDate.now().minusDays(7).format(DAY));
        model.addAttribute("month1", LocalDate.now().minusMonths(1).format(DAY));
        model.addAttribute("month3", LocalDate.now().minusMonths(3).format(DAY));
        model.addAttribute("qnaGroups", commonCodeService.labelsOf("QNA_GROUPS"));
        // AS-IS qnaTypes는 항상 빈 목록이다(QnaIndividualAdminService 주석 참고) - 그대로 둔다
        model.addAttribute("qnaTypes", List.of());
    }

    /** AS-IS POST qna/delete - 목록에서 체크한 문의 일괄삭제(답변 있는 건은 조용히 건너뛴다). */
    @PostMapping("/admin/inquiries/delete")
    @ResponseBody
    public Map<String, Object> deleteList(@RequestParam(value = "id", required = false) List<Integer> id) {
        qnaOpenAdminService.deleteList(id);
        return Map.of("isSuccess", true);
    }

    /**
     * AS-IS GET qna/answer/{qnaId} - <b>답변 화면</b>. 목록에서 제목을 누르면 여기로 온다.
     * 5112 답변화면과 거의 같지만 <b>답변제목 입력칸이 화면에 있고</b>(기본값
     * "문의에 대한 답변입니다.") 첨부 행은 <b>첨부가 있을 때만</b> 나오며 답변자 칸 라벨이 "답변자"다.
     */
    @GetMapping("/admin/inquiries/answer/{qnaId}")
    public String answerForm(@PathVariable Integer qnaId,
                             @ModelAttribute("searchParam") QnaIndividualSearchParam searchParam,
                             Model model) {
        return fillAnswerForm(qnaId, model, true, "답글등록");
    }

    /**
     * AS-IS GET qna/edit/{qnaId} - 답변화면과 <b>같은 화면</b>을 보여준다(버튼문구만 "저장"으로
     * 내려주는데 화면이 그 값을 쓰지 않아 완전히 같다). 첨부만 내려주지 않는다.
     * <b>AS-IS 목록·화면 어디에도 이 URL로 가는 링크가 없다.</b>
     */
    @GetMapping("/admin/inquiries/edit/{qnaId}")
    public String editForm(@PathVariable Integer qnaId,
                           @ModelAttribute("searchParam") QnaIndividualSearchParam searchParam,
                           Model model) {
        return fillAnswerForm(qnaId, model, false, "저장");
    }

    private String fillAnswerForm(Integer qnaId, Model model, boolean withFiles, String buttonName) {
        var qna = qnaService.find(qnaId).orElse(null);
        if (qna == null) {
            return "redirect:/admin/inquiries";
        }
        var qnaAnswer = qnaOpenAdminService.answerOf(qnaId).orElse(null);
        model.addAttribute("qna", qna);
        model.addAttribute("qnaAnswer", qnaAnswer);
        model.addAttribute("qnaImage", withFiles ? qnaService.filesOf(qnaId) : List.of());
        // AS-IS 답변자 칸은 "역할명 (관리자 로그인ID)"다 - 사람 이름이 아니다
        model.addAttribute("answerer", qnaOpenAdminService.answerer(
                qnaAnswer == null ? null : qnaAnswer.getUserId()));
        model.addAttribute("answerId", qnaAnswer == null ? 0 : qnaAnswer.getQnaAnswerId());
        model.addAttribute("askerLoginId", qnaOpenAdminService.askerLoginId(qna.getUserId()));
        // AS-IS op:nl2br 자리 - 이스케이프 후 줄바꿈만 <br/>로 바꾼다
        model.addAttribute("subjectHtml", QnaOpenAdminService.escapeNl2br(qna.getSubject()));
        model.addAttribute("questionHtml", QnaOpenAdminService.escapeNl2br(qna.getQuestion()));
        model.addAttribute("qnaAnswerTypeLabel",
                commonCodeService.labelsOf("QNA_GROUPS").getOrDefault(qna.getQnaGroup(), ""));
        model.addAttribute("buttonName", buttonName);
        return "inquiry-admin/answer-form";
    }

    /**
     * AS-IS POST qna/answer/{qnaId} - 답변 등록·수정. 끝나면 목록으로 돌아가며
     * {@code M00492}("답변이 등록되었습니다.")를 띄운다.
     * <b>5112와 달리 국민비서 문자를 보내지 않는다</b>({@link QnaIndividualAdminService} 주석).
     */
    @PostMapping("/admin/inquiries/answer/{qnaId}")
    public String answer(@PathVariable Integer qnaId, @RequestParam String title,
                         @RequestParam("answer") String answer, HttpSession session) {
        Manager manager = (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
        try {
            qnaIndividualAdminService.answer(qnaId, manager.getUserId(), title, answer);
        } catch (QnaException e) {
            return flashRedirect.to("/admin/inquiries/answer/" + qnaId, e.getMessage());
        }
        return flashRedirect.to("/admin/inquiries", msg("M00492", "답변이 등록되었습니다."));
    }

    /**
     * AS-IS POST qna/edit/{qnaId} - <b>답변만 저장하고 문의 본문은 저장하지 않는다</b>(AS-IS 결함).
     * 이 URL로 전송하는 화면도 없다 - {@link QnaIndividualAdminService#updateAnswerOnly} 주석 참고.
     */
    @PostMapping("/admin/inquiries/edit/{qnaId}")
    public String edit(@PathVariable Integer qnaId,
                       @RequestParam(value = "qnaAnswerId", required = false) Integer qnaAnswerId,
                       @RequestParam(required = false) String title,
                       @RequestParam(value = "answer", required = false) String answer,
                       HttpSession session) {
        Manager manager = (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
        qnaIndividualAdminService.updateAnswerOnly(qnaAnswerId, manager.getUserId(), title, answer);
        return flashRedirect.to("/admin/inquiries", "수정되었습니다.");
    }

    /** AS-IS GET qna/delete/{qnaId} - 답변화면의 [문의글 삭제](ajax, JSON). 답변이 있어도 지운다. */
    @GetMapping("/admin/inquiries/delete/{qnaId}")
    @ResponseBody
    public Map<String, Object> deleteQna(@PathVariable Integer qnaId) {
        try {
            qnaOpenAdminService.deleteQna(qnaId);
        } catch (QnaException e) {
            return Map.of("isSuccess", false, "errorMessage", e.getMessage());
        }
        return Map.of("isSuccess", true);
    }

    /** AS-IS GET qna/delete/{qnaId}/answer/{qnaAnswerId} - 답변만 삭제(ajax). */
    @GetMapping("/admin/inquiries/delete/{qnaId}/answer/{qnaAnswerId}")
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

    /** AS-IS POST qna/delete-item-image - 첨부파일 삭제(ajax, 파라미터명 itemId). */
    @PostMapping("/admin/inquiries/delete-item-image")
    @ResponseBody
    public Map<String, Object> deleteItemImage(@RequestParam("itemId") Integer itemId) {
        qnaOpenAdminService.deleteFile(itemId);
        return Map.of("isSuccess", true);
    }

    /**
     * AS-IS GET qna/file-download/{qnaFileId}/{qnaDetailType} - 첨부 다운로드.
     * 질문첨부를 뜻하는 {@code 'Q'}만 유효하다(그 외는 답변첨부 표를 보는데 행이 생기는 경로가 없다).
     */
    @GetMapping("/admin/inquiries/file-download/{qnaFileId}/{qnaDetailType}")
    public ResponseEntity<?> fileDownload(@PathVariable Integer qnaFileId,
                                          @PathVariable String qnaDetailType) throws IOException {
        if (!"Q".equals(qnaDetailType)) {
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

    /** 문구는 코드로 조회한다 - 없으면 코드 자체가 돌아오므로 AS-IS 주석의 문구로 대신한다. */
    private String msg(String code, String fallback) {
        String message = commonMessageService.get(code);
        return message == null || message.equals(code) ? fallback : message;
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
