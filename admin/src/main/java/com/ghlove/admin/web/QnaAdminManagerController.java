package com.ghlove.admin.web;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.domain.QnaAdminAnswerFile;
import com.ghlove.admin.domain.QnaAdminFile;
import com.ghlove.admin.service.CommonCodeService;
import com.ghlove.admin.service.CommonMessageService;
import com.ghlove.admin.service.MenuService;
import com.ghlove.admin.service.QnaAdminManagerService;
import com.ghlove.admin.web.support.FlashRedirect;
import com.ghlove.admin.web.support.Pagination;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 관리자 > 답례품관리 > 관리자 문의 > 관리자 문의(AS-IS opmanager/qna-admin, QnaAdminManagerController, 메뉴 16651).
 *
 * <p>답례품 제공자가 판매자 포털 "지자체 문의"로 남긴 문의를 지자체 담당자가 답한다. TO-BE는 {@code /qna-admin}을 이미 Q&A(5112)가 쓰고 있어
 * 메뉴 경로 {@code /admin/internal-inquiry} 아래에 AS-IS 하위 경로를 그대로 둔다.
 *
 * <p>AS-IS 동작(그대로):
 * <ul>
 *   <li>처음 열면(GET) 조회하지 않고 빈 목록(총 0건), [검색](POST)할 때만 조회. 등록일 기본값은 오늘~오늘. 출력 수는 10/20/50/100(그 밖은 10).</li>
 *   <li>시스템·행안부(ROLE_ADMIN_1~4)는 전체를 보고 지자체 검색·지자체 칸이 있다. 그 밖은 소속 지자체 문의만 보고, 소속 지자체가 없으면 404.</li>
 *   <li>답변·문의 삭제 버튼은 시스템·행안부에게 안 보인다. 답변 등록은 문의 지자체 = 내 지자체일 때, 답변 수정·삭제·첨부 삭제는 답변 쓴 사람만.
 *       처리 중 오류는 화면에 "실패했습니다."(문의 삭제는 "실패했습니다..")로만 나온다(AS-IS가 원인 문구를 버린다).</li>
 *   <li>페이지 링크는 {@code list?page=N} - op.manager.js가 검색 폼에 page를 실어 POST로 바꾼다.</li>
 * </ul>
 */
@Slf4j
@Controller
@RequestMapping("/admin/internal-inquiry")
@RequiredArgsConstructor
public class QnaAdminManagerController {

    private static final String LIST_URL = "/admin/internal-inquiry/list";
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final QnaAdminManagerService service;
    private final CommonCodeService commonCodeService;
    private final CommonMessageService messages;
    private final FlashRedirect flashRedirect;

    /** AS-IS QnaAdminParam 중 검색 폼이 보내는 것. */
    @Getter
    @Setter
    public static class QnaAdminParam {
        private String upperLocgovCode = "";
        private String locgovCode = "";
        private String where = "";
        private String query = "";
        private String searchStartDate;
        private String searchEndDate;
        private int page = 1;
    }

    // ---- 목록 ----

    /** AS-IS GET list - 조회하지 않고 검색 폼만(총 0건). */
    @GetMapping({"", "/list"})
    public String list(@ModelAttribute("qnaAdminParam") QnaAdminParam param, @RequestParam(value = "itemsPerPage", required = false, defaultValue = "") String itemsPerPageStr,
                       HttpServletRequest request, HttpSession session, Model model) {
        Manager manager = manager(session);
        int itemsPerPage = itemsPerPage(itemsPerPageStr);
        applyScope(param, manager);
        fill(model, param, List.of(), 0, Pagination.of(0, 1, itemsPerPage).withLink(LIST_URL + "?page=[page]"), itemsPerPage, manager);
        requestParams(model, request);
        return "internal-inquiry/list";
    }

    /** AS-IS POST list - 실제 검색. */
    @PostMapping("/list")
    public String search(@ModelAttribute("qnaAdminParam") QnaAdminParam param, @RequestParam(value = "itemsPerPage", required = false, defaultValue = "") String itemsPerPageStr,
                         HttpServletRequest request, HttpSession session, Model model) {
        Manager manager = manager(session);
        int itemsPerPage = itemsPerPage(itemsPerPageStr);
        applyScope(param, manager);
        QnaAdminManagerService.Search search = toSearch(param);
        int total = service.count(search);
        Pagination pagination = Pagination.of(total, param.getPage(), itemsPerPage).withLink(LIST_URL + "?page=[page]");
        List<QnaAdminManagerService.Row> rows = service.list(search, pagination.getStartRow(), itemsPerPage, needMasking(manager));
        fill(model, param, rows, total, pagination, itemsPerPage, manager);
        requestParams(model, request);
        return "internal-inquiry/list";
    }

    /** AS-IS JSP의 ${param.where}·${param.upperLocgovCode}·${param.locgovCode} - 모델 값이 아니라 요청에 실려 온 값 그대로. */
    private static void requestParams(Model model, HttpServletRequest request) {
        model.addAttribute("paramWhere", nz(request.getParameter("where")));
        model.addAttribute("paramUpperLocgovCode", nz(request.getParameter("upperLocgovCode")));
        model.addAttribute("paramLocgovCode", nz(request.getParameter("locgovCode")));
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }

    /** 등록일 기본값(오늘)과 지자체 범위 - 시스템·행안부가 아니면 소속 지자체로 고정, 소속이 없으면 404(AS-IS PageNotFoundException). */
    private void applyScope(QnaAdminParam param, Manager manager) {
        String today = LocalDate.now().format(DAY);
        if (isBlank(param.getSearchStartDate())) {
            param.setSearchStartDate(today);
        }
        if (isBlank(param.getSearchEndDate())) {
            param.setSearchEndDate(today);
        }
        if (!isUpperAdmin(manager)) {
            if (isBlank(manager.getLocgovCode())) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            }
            param.setLocgovCode(manager.getLocgovCode());
        }
    }

    private void fill(Model model, QnaAdminParam param, List<QnaAdminManagerService.Row> rows, int total, Pagination pagination,
                      int itemsPerPage, Manager manager) {
        Map<String, String> groupLabels = qnaGroupLabels();
        List<Map<String, Object>> list = new ArrayList<>();
        for (QnaAdminManagerService.Row r : rows) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("qnaAdminId", r.qnaAdminId());
            m.put("locgovName", r.locgovName());
            m.put("qnaGroupName", r.qnaGroup() == null ? null : groupLabels.get(r.qnaGroup()));
            m.put("subject", r.subject());
            m.put("userName", r.userName());
            m.put("loginId", r.loginId());
            m.put("answered", r.answered());
            m.put("createdDateStr", r.createdDateStr());
            list.add(m);
        }
        model.addAttribute("qnaAdminList", list);
        model.addAttribute("qnaCount", total);
        model.addAttribute("pagination", pagination);
        model.addAttribute("itemsPerPage", String.valueOf(itemsPerPage));
        model.addAttribute("qnaGroups", locgovQnaGroups());
        model.addAttribute("sido", sido());
        model.addAttribute("upperAdmin", isUpperAdmin(manager));
    }

    private QnaAdminManagerService.Search toSearch(QnaAdminParam p) {
        return new QnaAdminManagerService.Search(p.getUpperLocgovCode(), p.getLocgovCode(), p.getWhere(), p.getQuery(),
                p.getSearchStartDate(), p.getSearchEndDate());
    }

    /** AS-IS: 10/20/50/100(테스트용 1 포함)만, 그 밖은 10. */
    private static int itemsPerPage(String s) {
        return switch (s == null ? "" : s) {
            case "1", "10", "20", "50", "100" -> Integer.parseInt(s);
            default -> 10;
        };
    }

    // ---- 상세(답변) ----

    /** AS-IS GET edit/{번호} - 문의 + 답변(없으면 새 답변) + 첨부. 지자체를 보지 않는다(AS-IS 그대로). */
    @GetMapping("/edit/{qnaAdminId}")
    public String edit(@PathVariable long qnaAdminId, HttpSession session, Model model) {
        QnaAdminManagerService.Detail d = service.detail(qnaAdminId);
        if (d == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        Map<String, String> groupLabels = qnaGroupLabels();
        model.addAttribute("qnaAdmin", d.qnaAdmin());
        model.addAttribute("qnaGroupName", d.qnaAdmin().getQnaGroup() == null ? null : groupLabels.get(d.qnaAdmin().getQnaGroup()));
        model.addAttribute("createdDateStr", d.createdDateStr());
        // AS-IS op:nl2br 자리 - 판매자가 쓴 글이라 이스케이프한 뒤 줄바꿈만 <br/>로
        model.addAttribute("questionHtml", com.ghlove.admin.service.CmntyText.nl2br(
                org.springframework.web.util.HtmlUtils.htmlEscape(d.qnaAdmin().getQuestion() == null ? "" : d.qnaAdmin().getQuestion())));
        model.addAttribute("answer", d.answer());
        model.addAttribute("answerId", d.answer() == null ? 0L : d.answer().getQnaAdminAnswerId());
        model.addAttribute("answerUserName", d.answerUserName());
        model.addAttribute("answerDateStr", d.answerDateStr());
        model.addAttribute("qnaAdminFiles", d.files());
        model.addAttribute("qnaAdminAnswerFiles", d.answerFiles());
        model.addAttribute("upperAdmin", isUpperAdmin(manager(session)));
        return "internal-inquiry/form";
    }

    /** AS-IS POST edit/{번호} - 답변번호가 있으면 수정, 없으면 등록. */
    @PostMapping("/edit/{qnaAdminId}")
    public String editAction(@PathVariable long qnaAdminId,
                             @RequestParam(value = "qnaAdminAnswerId", required = false, defaultValue = "0") long qnaAdminAnswerId,
                             @RequestParam(value = "title", required = false) String title,
                             @RequestParam(value = "answer", required = false) String answer,
                             @RequestParam(value = "addFile", required = false) List<MultipartFile> addFile,
                             HttpSession session) {
        Manager manager = manager(session);
        String locgovCode = "";
        if (!isUpperAdmin(manager)) {
            locgovCode = manager.getLocgovCode();
            if (isBlank(locgovCode)) {
                return flashRedirect.to(LIST_URL, "권한이 없습니다.");
            }
        }
        try {
            if (qnaAdminAnswerId > 0) {
                service.updateAnswer(qnaAdminId, qnaAdminAnswerId, title, answer, addFile, manager);
                return flashRedirect.to(LIST_URL, messages.get("M00289"));
            }
            service.insertAnswer(qnaAdminId, title, answer, addFile, locgovCode, manager);
            return flashRedirect.to(LIST_URL, messages.get("M00288"));
        } catch (QnaAdminManagerService.QnaAdminException e) {
            log.warn("관리자 문의 답변 저장 실패 qnaAdminId={} : {}", qnaAdminId, e.getMessage());
            return flashRedirect.to(LIST_URL, "실패했습니다.");
        }
    }

    /** AS-IS POST deleteQnaAdminAnswerFile - 답변 첨부 한 개 삭제, 남은 첨부 목록을 돌려준다. */
    @PostMapping("/deleteQnaAdminAnswerFile")
    @ResponseBody
    public Map<String, Object> deleteAnswerFile(@RequestParam String qnaAdminAnswerId, @RequestParam String qnaAdminAnswerFileId, HttpSession session) {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            List<Map<String, Object>> data = new ArrayList<>();
            for (QnaAdminAnswerFile f : service.deleteAnswerFile(Long.parseLong(qnaAdminAnswerId), Long.parseLong(qnaAdminAnswerFileId), manager(session))) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("qnaAdminAnswerFileId", f.getQnaAdminAnswerFileId());
                m.put("qnaAdminAnswerId", f.getQnaAdminAnswerId());
                m.put("orgFileName", f.getOrgFileName());
                data.add(m);
            }
            result.put("isSuccess", true);
            result.put("data", data);
        } catch (RuntimeException e) {
            log.warn("관리자 문의 답변 첨부 삭제 실패 : {}", e.getMessage());
            result.put("isSuccess", false);
            result.put("errorMessage", "실패했습니다.");
        }
        return result;
    }

    /** AS-IS GET deleteQnaAdminAnswer/{답변번호} - 답변 삭제(쓴 사람만). */
    @GetMapping("/deleteQnaAdminAnswer/{qnaAdminAnswerId}")
    public String deleteAnswer(@PathVariable long qnaAdminAnswerId, HttpSession session) {
        try {
            service.deleteAnswer(qnaAdminAnswerId, manager(session));
            return flashRedirect.to(LIST_URL, messages.get("M00205"));
        } catch (QnaAdminManagerService.QnaAdminException e) {
            log.warn("관리자 문의 답변 삭제 실패 qnaAdminAnswerId={} : {}", qnaAdminAnswerId, e.getMessage());
            return flashRedirect.to(LIST_URL, "실패했습니다.");
        }
    }

    // ---- 문의 삭제 ----

    /** AS-IS GET deleteQnaAdmin/{번호} - 같은 지자체 담당자만. 답변이 있으면 실패. */
    @GetMapping("/deleteQnaAdmin/{qnaAdminId}")
    public String deleteQuestion(@PathVariable long qnaAdminId, HttpSession session) {
        Manager manager = manager(session);
        String locgovCode = "";
        if (!isUpperAdmin(manager)) {
            locgovCode = manager.getLocgovCode();
            if (isBlank(locgovCode)) {
                return flashRedirect.to(LIST_URL, "권한이 없습니다.");
            }
        }
        try {
            QnaAdminManagerService.Detail d = service.detail(qnaAdminId);
            if (d != null && locgovCode.equals(d.qnaAdmin().getLocgovCode())) {
                service.deleteQuestion(qnaAdminId, d.qnaAdmin().getLocgovCode(), manager);
                return flashRedirect.to(LIST_URL, messages.get("M00205"));
            }
            return flashRedirect.to(LIST_URL, "권한이 없습니다.");
        } catch (QnaAdminManagerService.QnaAdminException e) {
            log.warn("관리자 문의 삭제 실패 qnaAdminId={} : {}", qnaAdminId, e.getMessage());
            return flashRedirect.to(LIST_URL, "실패했습니다..");
        }
    }

    /** AS-IS POST qnaAdmin/delete - 목록에서 고른 문의 삭제. 실패하면 그 이유 문구를 돌려준다(이 경로만 원인을 보여준다). */
    @PostMapping("/qnaAdmin/delete")
    @ResponseBody
    public Map<String, Object> deleteQuestions(@RequestParam(value = "id", required = false) List<Long> ids, HttpSession session) {
        Manager manager = manager(session);
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            service.deleteQuestions(ids, manager.getLocgovCode(), manager);
            result.put("isSuccess", true);
            result.put("data", messages.get("M00205"));
        } catch (QnaAdminManagerService.QnaAdminException e) {
            result.put("isSuccess", false);
            result.put("errorMessage", e.getMessage());
        }
        return result;
    }

    // ---- 내려받기 ----

    /** AS-IS GET downloadQnaAdminFile/{문의번호}/{파일번호} - 판매자가 올린 문의 첨부. */
    @GetMapping("/downloadQnaAdminFile/{qnaAdminId}/{qnaAdminFileId}")
    @ResponseBody
    public ResponseEntity<Resource> downloadQuestionFile(@PathVariable long qnaAdminId, @PathVariable long qnaAdminFileId) {
        QnaAdminFile f = service.questionFile(qnaAdminFileId);
        if (f == null) {
            return ResponseEntity.ok().build();
        }
        return download(service.questionFilePath(qnaAdminId, f), f.getOrgFileName());
    }

    /** AS-IS GET downloadQnaAdminAnswerFile/{답변번호}/{파일번호} - 답변 첨부. */
    @GetMapping("/downloadQnaAdminAnswerFile/{qnaAdminAnswerId}/{qnaAdminAnswerFileId}")
    @ResponseBody
    public ResponseEntity<Resource> downloadAnswerFile(@PathVariable long qnaAdminAnswerId, @PathVariable long qnaAdminAnswerFileId) {
        Map.Entry<Long, QnaAdminAnswerFile> e = service.answerFile(qnaAdminAnswerId, qnaAdminAnswerFileId);
        if (e == null) {
            return ResponseEntity.ok().build();
        }
        return download(service.answerFilePath(e.getKey(), e.getValue()), e.getValue().getOrgFileName());
    }

    /** AS-IS FileDownloadCustom - 파일이 없으면 빈 응답(AS-IS는 IOException 때 null을 돌려준다). */
    private static ResponseEntity<Resource> download(Path path, String orgFileName) {
        if (!Files.isRegularFile(path)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(orgFileName == null ? path.getFileName().toString() : orgFileName, StandardCharsets.UTF_8).build().toString())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new FileSystemResource(path));
    }

    // ---- 공통 ----

    /** 공통코드 QNA_GROUPS 전체(문의유형 이름 찾기용). */
    private Map<String, String> qnaGroupLabels() {
        return commonCodeService.labelsOf("QNA_GROUPS");
    }

    /** AS-IS: 문의유형 중 확장코드에 "locgov"가 들어간 것만 검색 select에. */
    private List<Map<String, String>> locgovQnaGroups() {
        List<Map<String, String>> groups = new ArrayList<>();
        commonCodeService.listByType("QNA_GROUPS").forEach(c -> {
            if ("Y".equals(c.getUseYn()) && c.getExtensionCode() != null && c.getExtensionCode().contains("locgov")) {
                Map<String, String> m = new LinkedHashMap<>();
                m.put("id", c.getId());
                m.put("label", c.getLabel());
                groups.add(m);
            }
        });
        return groups;
    }

    /** AS-IS CodeUtils.getCodeList("WDR") - 시도 select. */
    private List<Map<String, String>> sido() {
        List<Map<String, String>> options = new ArrayList<>();
        commonCodeService.labelsOf("WDR").forEach((id, label) -> {
            Map<String, String> m = new LinkedHashMap<>();
            m.put("value", id);
            m.put("label", label);
            options.add(m);
        });
        return options;
    }

    /** AS-IS isUpperAdmin - ROLE_ADMIN_1~4. */
    private static boolean isUpperAdmin(Manager manager) {
        return manager != null && MenuService.UNRESTRICTED_ROLES.contains(manager.getAuthority());
    }

    /** AS-IS ShopUtils.needMasking - 지자체 담당자(ROLE_ADMIN_5/6)가 아니면 이름을 가린다. */
    private static boolean needMasking(Manager manager) {
        return !MenuService.isLocgovScoped(manager);
    }

    private static Manager manager(HttpSession session) {
        return (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
