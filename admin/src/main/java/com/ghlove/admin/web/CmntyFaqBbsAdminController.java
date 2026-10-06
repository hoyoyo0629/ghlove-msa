package com.ghlove.admin.web;

import com.ghlove.admin.domain.CmntyFaqBbsCmntFile;
import com.ghlove.admin.domain.CmntyFaqBbsFile;
import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.service.CmntyException;
import com.ghlove.admin.service.CmntyFaqBbsAdminService;
import com.ghlove.admin.service.CmntyFileStorageService;
import com.ghlove.admin.service.CmntyText;
import com.ghlove.admin.service.CommonCodeService;
import com.ghlove.admin.service.LocgovClient;
import com.ghlove.admin.web.support.CmntyFaqBbsSearchParam;
import com.ghlove.admin.web.support.Pagination;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 담당자용 FAQ (메뉴 11405) - AS-IS {@code CommunityManagerController}의 {@code faqBbs/*}
 * 15개 엔드포인트 재현({@code /opmanager/community/faqBbs/list} 외).
 *
 * <p>엔드포인트 구성은 SR 게시판({@link CmntySrBbsAdminController})과 같고 질문유형이 추가된다.
 * 삭제 경로만 AS-IS가 {@code deleteFaqBbs/{bbsId}}로 다르다.
 *
 * <p><b>AS-IS 화면이 쓰지 않는 기능 - 그대로 비활성 유지</b>: 댓글 첨부(등록/삭제/다운로드)
 * 엔드포인트와 표는 AS-IS에 있는데, <b>FAQ 상세화면에만 '파일등록' 버튼과 댓글 첨부 목록이
 * 빠져 있다</b>(SR·오프라인SR 화면에는 있다). 숨은 업로드 폼과 스크립트는 AS-IS에 그대로 있어
 * 같이 옮겼고, 버튼과 목록은 AS-IS처럼 렌더링하지 않는다 - 빼지도 켜지도 않는다는 원칙대로다.
 * 그래서 이 게시판은 <b>본문 첨부만 실제로 쓰인다</b>.
 *
 * <p>AS-IS 화면의 또 다른 차이: FAQ 등록·수정 화면은 <b>상단공지 체크박스를 권한 조건 없이</b>
 * 보여주고(SR은 1~4만), 첨부 파일칸이 보안 점검 모달 없이 <b>그냥 file input</b>이다
 * (SR·오프라인SR은 모달을 거친다). 둘 다 AS-IS 그대로 두었다.
 *
 * <p><b>메뉴 URL 충돌(기록)</b>: TO-BE op_menu에서 {@code 5104 FAQ}(고객센터)와
 * {@code 11405 담당자용 FAQ}가 <b>둘 다 {@code /community/faq-bbs}</b>를 가리킨다. AS-IS에서는
 * 각각 {@code /opmanager/faq/list}와 {@code /opmanager/community/faqBbs/list}로 서로 다른 화면이다.
 * 5104는 고객센터 영역 소속이라 그 라운드에서 자기 URL을 받아야 한다 - 지금은 5104를 눌러도
 * 이 화면이 열린다(기존과 동일, 새로 생긴 문제가 아니다).
 */
@Controller
@RequestMapping("/community/faq-bbs")
@RequiredArgsConstructor
public class CmntyFaqBbsAdminController {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyyMMdd");

    private static final String LIST_URL = "/community/faq-bbs/list";

    private static final String FILE_MISSING_SCRIPT =
            "<script>alert('파일이 존재하지 않습니다. 시스템관리자에 문의하여 주십시오.'); history.back();</script>";

    private final CmntyFaqBbsAdminService faqBbsService;
    private final com.ghlove.admin.web.support.FlashRedirect flashRedirect;
    private final CmntyFileStorageService fileStorageService;
    private final CommonCodeService commonCodeService;
    private final LocgovClient locgovClient;

    @GetMapping
    public String root() {
        return "redirect:" + LIST_URL;
    }

    /** AS-IS GET/POST faqBbs/list - 한 메서드가 둘 다 받고 진입에서도 조회한다. */
    @RequestMapping("/list")
    public String list(@ModelAttribute("searchParam") CmntyFaqBbsSearchParam searchParam,
                       HttpServletRequest request, Model model) {
        searchParam.applyDefaults();

        int count = faqBbsService.count(searchParam.getLocgovCode(), searchParam.getStartDt(),
                searchParam.getEndDt(), searchParam.getSearchRole(), searchParam.getWhere(),
                searchParam.getQuery(), searchParam.getShFaqType());
        Pagination pagination = Pagination.of(count, searchParam.getPage(), searchParam.getItemsPerPage())
                .withLinkFrom(request);
        List<CmntyFaqBbsAdminService.BbsRow> list = faqBbsService.list(searchParam.getLocgovCode(),
                searchParam.getStartDt(), searchParam.getEndDt(), searchParam.getSearchRole(),
                searchParam.getWhere(), searchParam.getQuery(), searchParam.getShFaqType(),
                pagination.getStartRow(), pagination.getItemsPerPage());

        model.addAttribute("list", list);
        model.addAttribute("count", count);
        model.addAttribute("pagination", pagination);
        String today = LocalDate.now().format(DAY);
        model.addAttribute("today", today);
        model.addAttribute("week", LocalDate.now().minusDays(7).format(DAY));
        model.addAttribute("month1", LocalDate.now().minusMonths(1).format(DAY));
        model.addAttribute("month2", LocalDate.now().minusMonths(2).format(DAY));
        model.addAttribute("wdr", wdrOptions());
        model.addAttribute("locgovs", locgovClient.allLocgovs());
        model.addAttribute("faqTypes", faqBbsService.faqTypes());
        model.addAttribute("faqTypeTabRows", faqBbsService.faqTypeTabRows());
        return "community/faqBbs/list";
    }

    @GetMapping("/form")
    public String createForm(HttpSession session, Model model) {
        model.addAttribute("role", faqBbsService.screenRole(viewer(session)));
        model.addAttribute("faqTypes", faqBbsService.faqTypes());
        return "community/faqBbs/form";
    }

    @PostMapping("/form")
    public String create(@RequestParam String bbsTtl,
                         @RequestParam String bbsCn,
                         @RequestParam(required = false) String faqType,
                         @RequestParam(defaultValue = "N") String noticeYn,
                         @RequestParam(defaultValue = "N") String isSecret,
                         @RequestParam(value = "detailImageFiles", required = false) MultipartFile[] detailImageFiles,
                         HttpSession session) {
        try {
            faqBbsService.insert(bbsTtl, bbsCn, faqType, noticeYn, isSecret, detailImageFiles,
                    viewer(session));
        } catch (CmntyException e) {
            return redirectToList(e.getMessage());
        }
        return redirectToList("등록되었습니다.");
    }

    @GetMapping("/detail/{bbsId}")
    public String detail(@PathVariable long bbsId, HttpSession session, Model model) {
        Manager viewer = viewer(session);
        faqBbsService.increaseInqCnt(bbsId);

        CmntyFaqBbsAdminService.BbsDetail detail = faqBbsService.detail(bbsId, viewer);
        if (detail == null) {
            return redirectToList("해당 글은 존재하지 않습니다.");
        }
        if (detail.secretBlocked()) {
            return redirectToList("비밀글입니다.");
        }

        List<CmntyFaqBbsAdminService.CmntRow> comments = faqBbsService.cmntList(bbsId);
        model.addAttribute("detail", detail);
        model.addAttribute("bbsCnHtml", CmntyText.nl2br(detail.bbsCn()));
        model.addAttribute("fileList", faqBbsService.fileList(bbsId));
        model.addAttribute("cmntList", comments);
        model.addAttribute("faqBbsCmntListCnt", comments.size());
        model.addAttribute("role", faqBbsService.screenRole(viewer));
        model.addAttribute("userId", viewer == null ? null : viewer.getUserId());
        return "community/faqBbs/detail";
    }

    @GetMapping("/edit/{bbsId}")
    public String editForm(@PathVariable long bbsId, HttpSession session, Model model) {
        Manager viewer = viewer(session);
        CmntyFaqBbsAdminService.BbsDetail detail = faqBbsService.detail(bbsId, viewer);
        if (detail == null) {
            return redirectToList("해당 글은 존재하지 않습니다.");
        }
        if (detail.secretBlocked()) {
            return redirectToList("비밀글은 본인만 수정 가능 합니다.");
        }

        model.addAttribute("detail", detail);
        model.addAttribute("fileList", faqBbsService.fileList(bbsId));
        model.addAttribute("role", faqBbsService.screenRole(viewer));
        model.addAttribute("faqTypes", faqBbsService.faqTypes());
        return "community/faqBbs/edit";
    }

    @PostMapping("/edit/{bbsId}")
    public String update(@PathVariable long bbsId,
                         @RequestParam String bbsTtl,
                         @RequestParam String bbsCn,
                         @RequestParam(required = false) String faqType,
                         @RequestParam(defaultValue = "N") String noticeYn,
                         @RequestParam(defaultValue = "N") String isSecret,
                         @RequestParam(value = "detailImageFiles", required = false) MultipartFile[] detailImageFiles,
                         HttpSession session) {
        try {
            faqBbsService.update(bbsId, bbsTtl, bbsCn, faqType, noticeYn, isSecret, detailImageFiles,
                    viewer(session));
        } catch (CmntyException e) {
            return redirectToList(e.getMessage());
        }
        return redirectToList("수정에 성공하였습니다.");
    }

    /** AS-IS POST faqBbs/deleteFaqBbs/{bbsId}. */
    @PostMapping("/deleteFaqBbs/{bbsId}")
    @ResponseBody
    public Map<String, Object> delete(@PathVariable long bbsId, HttpSession session) {
        try {
            faqBbsService.delete(bbsId, viewer(session));
        } catch (CmntyException e) {
            return failure(e.getMessage());
        }
        return success("게시글이 삭제 되었습니다.");
    }

    @GetMapping("/file-download/{fileId}")
    public ResponseEntity<?> fileDownload(@PathVariable long fileId) throws IOException {
        CmntyFaqBbsFile file = faqBbsService.bbsFile(fileId);
        return download(file.getAtchFileNm(), file.getOrgnlAtchFileNm(),
                CmntyFaqBbsAdminService.SUBDIR_BBS);
    }

    @PostMapping("/delete-item-image")
    @ResponseBody
    public Map<String, Object> deleteBbsFile(@RequestParam long fileId, HttpSession session) {
        try {
            faqBbsService.deleteBbsFile(fileId, viewer(session));
        } catch (CmntyException e) {
            return failure(e.getMessage());
        }
        return success("파일이 삭제 되었습니다.");
    }

    @PostMapping("/cmnt/add")
    @ResponseBody
    public Map<String, Object> addCmnt(@RequestParam long bbsId, @RequestParam String cmntCn,
                                       HttpSession session) {
        try {
            faqBbsService.addCmnt(bbsId, cmntCn, viewer(session));
        } catch (CmntyException e) {
            return failure(e.getMessage());
        }
        return success("등록에 성공하였습니다.");
    }

    @PostMapping("/cmnt/update")
    @ResponseBody
    public Map<String, Object> updateCmnt(@RequestParam long cmntId, @RequestParam String cmntCn,
                                          HttpSession session) {
        try {
            faqBbsService.updateCmnt(cmntId, cmntCn, viewer(session));
        } catch (CmntyException e) {
            return failure(e.getMessage());
        }
        return success("수정에 성공하였습니다.");
    }

    @PostMapping("/cmnt/delete/{cmntId}")
    @ResponseBody
    public Map<String, Object> deleteCmnt(@PathVariable long cmntId, HttpSession session) {
        try {
            faqBbsService.deleteCmnt(cmntId, viewer(session));
        } catch (CmntyException e) {
            return failure(e.getMessage());
        }
        return success("삭제에 성공하였습니다.");
    }

    /** AS-IS POST faqBbs/cmnt/upload - 화면에 버튼이 없어 실제로는 호출되지 않는다(클래스 주석 참고). */
    @PostMapping("/cmnt/upload")
    public String uploadCmntFile(@RequestParam long cmntId,
                                 @RequestParam long bbsId,
                                 @RequestParam(value = "detailImageFile", required = false) MultipartFile[] detailImageFile,
                                 HttpSession session) {
        try {
            faqBbsService.addCmntFiles(cmntId, detailImageFile, viewer(session));
        } catch (CmntyException e) {
            return redirectToDetail(bbsId, e.getMessage());
        }
        return redirectToDetail(bbsId, "파일이 등록되었습니다.");
    }

    @PostMapping("/cmnt/deleteFile")
    @ResponseBody
    public Map<String, Object> deleteCmntFile(@RequestParam long fileId, HttpSession session) {
        try {
            faqBbsService.deleteCmntFile(fileId, viewer(session));
        } catch (CmntyException e) {
            return failure(e.getMessage());
        }
        return success("파일이 삭제 되었습니다.");
    }

    @GetMapping("/cmnt/file-download/{fileId}")
    public ResponseEntity<?> cmntFileDownload(@PathVariable long fileId) throws IOException {
        CmntyFaqBbsCmntFile file = faqBbsService.cmntFile(fileId);
        return download(file.getAtchFileNm(), file.getOrgnlAtchFileNm(),
                CmntyFaqBbsAdminService.SUBDIR_CMNT);
    }

    private ResponseEntity<?> download(String storedName, String originalName, String subdir)
            throws IOException {
        Resource resource = new UrlResource(fileStorageService.resolve(storedName, subdir).toUri());
        if (!resource.exists()) {
            return ResponseEntity.ok()
                    .contentType(new MediaType("text", "html", StandardCharsets.UTF_8))
                    .body(FILE_MISSING_SCRIPT);
        }
        String downloadName = originalName != null ? originalName : storedName;
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(downloadName, StandardCharsets.UTF_8)
                                .build().toString())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    private List<Map<String, String>> wdrOptions() {
        List<Map<String, String>> options = new ArrayList<>();
        commonCodeService.labelsOf("WDR").forEach((id, label) -> {
            Map<String, String> option = new LinkedHashMap<>();
            option.put("id", id);
            option.put("label", label);
            options.add(option);
        });
        return options;
    }

    private String redirectToList(String message) {
        return flashRedirect.to(LIST_URL, message);
    }

    private String redirectToDetail(long bbsId, String message) {
        return flashRedirect.to("/community/faq-bbs/detail/" + bbsId, message);
    }

    private static Manager viewer(HttpSession session) {
        return (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
    }

    private static Map<String, Object> success(String message) {
        return Map.of("isSuccess", true, "data", message);
    }

    private static Map<String, Object> failure(String message) {
        return Map.of("isSuccess", false, "errorMessage", message == null ? "" : message);
    }
}
