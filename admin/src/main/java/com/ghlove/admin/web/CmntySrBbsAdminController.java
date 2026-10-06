package com.ghlove.admin.web;

import com.ghlove.admin.domain.CmntySrBbsCmntFile;
import com.ghlove.admin.domain.CmntySrBbsFile;
import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.service.CmntyException;
import com.ghlove.admin.service.CmntyFileStorageService;
import com.ghlove.admin.service.CmntySrBbsAdminService;
import com.ghlove.admin.service.CmntyText;
import com.ghlove.admin.service.CommonCodeService;
import com.ghlove.admin.service.LocgovClient;
import com.ghlove.admin.web.support.CmntyBbsSearchParam;
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
 * SR 게시판 (메뉴 11404) - AS-IS {@code CommunityManagerController}의 {@code srBbs/*} 15개
 * 엔드포인트 재현({@code /opmanager/community/srBbs/list} 외).
 *
 * <p><b>AS-IS 그대로인 점</b>:
 * <ul>
 *   <li>목록은 {@code @RequestMapping}이라 GET·POST를 한 메서드가 받고 <b>진입에서도 조회</b>한다.</li>
 *   <li>등록·수정은 ajax가 아니라 <b>multipart 폼 전송</b>이고, 끝나면 목록으로 리다이렉트하며
 *       안내문구를 띄운다("등록되었습니다." / "수정에 성공하였습니다.").</li>
 *   <li>삭제·댓글·첨부삭제는 ajax(JSON)다.</li>
 *   <li>댓글 첨부 등록만 <b>숨은 multipart 폼</b>이라 상세로 리다이렉트한다("파일이 등록되었습니다.").</li>
 *   <li>파일이 디스크에 없으면 AS-IS는 <b>alert + history.back()을 담은 HTML 조각</b>을 돌려준다 -
 *       같은 방식을 유지했다(문구도 그대로).</li>
 *   <li>비밀글에 막히면 목록으로: 상세는 "비밀글입니다.", 수정은 "비밀글은 본인만 수정 가능 합니다."</li>
 * </ul>
 */
@Controller
@RequestMapping("/community/sr-bbs")
@RequiredArgsConstructor
public class CmntySrBbsAdminController {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyyMMdd");

    private static final String LIST_URL = "/community/sr-bbs/list";

    /** AS-IS가 파일을 못 찾았을 때 돌려주는 HTML 조각(문구 그대로). */
    private static final String FILE_MISSING_SCRIPT =
            "<script>alert('파일이 존재하지 않습니다. 시스템관리자에 문의하여 주십시오.'); history.back();</script>";

    private final CmntySrBbsAdminService srBbsService;
    private final com.ghlove.admin.web.support.FlashRedirect flashRedirect;
    private final CmntyFileStorageService fileStorageService;
    private final CommonCodeService commonCodeService;
    private final LocgovClient locgovClient;

    /** op_menu 11404의 등록 URL이 {@code /community/sr-bbs}인데 AS-IS는 목록이 {@code srBbs/list}다. */
    @GetMapping
    public String root() {
        return "redirect:" + LIST_URL;
    }

    /** AS-IS GET/POST srBbs/list - 한 메서드가 둘 다 받는다. */
    @RequestMapping("/list")
    public String list(@ModelAttribute("searchParam") CmntyBbsSearchParam searchParam,
                       HttpServletRequest request, Model model) {
        searchParam.applyDefaults();

        int count = srBbsService.count(searchParam.getLocgovCode(), searchParam.getStartDt(),
                searchParam.getEndDt(), searchParam.getSearchRole(), searchParam.getWhere(),
                searchParam.getQuery());
        Pagination pagination = Pagination.of(count, searchParam.getPage(), searchParam.getItemsPerPage())
                .withLinkFrom(request);
        List<CmntySrBbsAdminService.BbsRow> list = srBbsService.list(searchParam.getLocgovCode(),
                searchParam.getStartDt(), searchParam.getEndDt(), searchParam.getSearchRole(),
                searchParam.getWhere(), searchParam.getQuery(), pagination.getStartRow(),
                pagination.getItemsPerPage());

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
        return "community/srBbs/list";
    }

    /** AS-IS GET srBbs/form. */
    @GetMapping("/form")
    public String createForm(HttpSession session, Model model) {
        model.addAttribute("role", srBbsService.screenRole(viewer(session)));
        return "community/srBbs/form";
    }

    /** AS-IS POST srBbs/form - multipart 폼 전송. */
    @PostMapping("/form")
    public String create(@RequestParam String bbsTtl,
                         @RequestParam String bbsCn,
                         @RequestParam(defaultValue = "N") String noticeYn,
                         @RequestParam(defaultValue = "N") String isSecret,
                         @RequestParam(value = "detailImageFiles", required = false) MultipartFile[] detailImageFiles,
                         HttpSession session) {
        try {
            srBbsService.insert(bbsTtl, bbsCn, noticeYn, isSecret, detailImageFiles, viewer(session));
        } catch (CmntyException e) {
            return redirectToList(e.getMessage());
        }
        return redirectToList("등록되었습니다.");
    }

    /** AS-IS GET srBbs/detail/{bbsId} - 조회수를 먼저 올리고 댓글·첨부를 같이 내려준다. */
    @GetMapping("/detail/{bbsId}")
    public String detail(@PathVariable long bbsId, HttpSession session, Model model) {
        Manager viewer = viewer(session);
        srBbsService.increaseInqCnt(bbsId);

        CmntySrBbsAdminService.BbsDetail detail = srBbsService.detail(bbsId, viewer);
        if (detail == null) {
            return redirectToList("해당 글은 존재하지 않습니다.");
        }
        if (detail.secretBlocked()) {
            return redirectToList("비밀글입니다.");
        }

        List<CmntySrBbsAdminService.CmntRow> comments = srBbsService.cmntList(bbsId);
        model.addAttribute("detail", detail);
        model.addAttribute("bbsCnHtml", CmntyText.nl2br(detail.bbsCn()));
        model.addAttribute("fileList", srBbsService.fileList(bbsId));
        model.addAttribute("cmntList", comments);
        model.addAttribute("srBbsCmntListCnt", comments.size());
        model.addAttribute("role", srBbsService.screenRole(viewer));
        model.addAttribute("userId", viewer == null ? null : viewer.getUserId());
        return "community/srBbs/detail";
    }

    /** AS-IS GET srBbs/edit/{bbsId} - 상세와 달리 <b>조회수를 올리지 않는다</b>. */
    @GetMapping("/edit/{bbsId}")
    public String editForm(@PathVariable long bbsId, HttpSession session, Model model) {
        Manager viewer = viewer(session);
        CmntySrBbsAdminService.BbsDetail detail = srBbsService.detail(bbsId, viewer);
        if (detail == null) {
            return redirectToList("해당 글은 존재하지 않습니다.");
        }
        if (detail.secretBlocked()) {
            return redirectToList("비밀글은 본인만 수정 가능 합니다.");
        }

        model.addAttribute("detail", detail);
        model.addAttribute("fileList", srBbsService.fileList(bbsId));
        model.addAttribute("role", srBbsService.screenRole(viewer));
        return "community/srBbs/edit";
    }

    /** AS-IS POST srBbs/edit/{bbsId} - multipart 폼 전송. */
    @PostMapping("/edit/{bbsId}")
    public String update(@PathVariable long bbsId,
                         @RequestParam String bbsTtl,
                         @RequestParam String bbsCn,
                         @RequestParam(defaultValue = "N") String noticeYn,
                         @RequestParam(defaultValue = "N") String isSecret,
                         @RequestParam(value = "detailImageFiles", required = false) MultipartFile[] detailImageFiles,
                         HttpSession session) {
        try {
            srBbsService.update(bbsId, bbsTtl, bbsCn, noticeYn, isSecret, detailImageFiles, viewer(session));
        } catch (CmntyException e) {
            return redirectToList(e.getMessage());
        }
        return redirectToList("수정에 성공하였습니다.");
    }

    /** AS-IS POST srBbs/deleteSrBbs/{bbsId}. */
    @PostMapping("/deleteSrBbs/{bbsId}")
    @ResponseBody
    public Map<String, Object> delete(@PathVariable long bbsId, HttpSession session) {
        try {
            srBbsService.delete(bbsId, viewer(session));
        } catch (CmntyException e) {
            return failure(e.getMessage());
        }
        return success("게시글이 삭제 되었습니다.");
    }

    /** AS-IS GET srBbs/file-download/{fileId} - 본문 첨부 다운로드. */
    @GetMapping("/file-download/{fileId}")
    public ResponseEntity<?> fileDownload(@PathVariable long fileId) throws IOException {
        CmntySrBbsFile file = srBbsService.bbsFile(fileId);
        return download(file.getAtchFileNm(), file.getOrgnlAtchFileNm(),
                CmntySrBbsAdminService.SUBDIR_BBS);
    }

    /** AS-IS POST srBbs/delete-item-image - 본문 첨부 삭제(수정화면). */
    @PostMapping("/delete-item-image")
    @ResponseBody
    public Map<String, Object> deleteBbsFile(@RequestParam long fileId, HttpSession session) {
        try {
            srBbsService.deleteBbsFile(fileId, viewer(session));
        } catch (CmntyException e) {
            return failure(e.getMessage());
        }
        return success("파일이 삭제 되었습니다.");
    }

    /** AS-IS POST srBbs/cmnt/add. */
    @PostMapping("/cmnt/add")
    @ResponseBody
    public Map<String, Object> addCmnt(@RequestParam long bbsId, @RequestParam String cmntCn,
                                       HttpSession session) {
        try {
            srBbsService.addCmnt(bbsId, cmntCn, viewer(session));
        } catch (CmntyException e) {
            return failure(e.getMessage());
        }
        return success("등록에 성공하였습니다.");
    }

    /** AS-IS POST srBbs/cmnt/update. */
    @PostMapping("/cmnt/update")
    @ResponseBody
    public Map<String, Object> updateCmnt(@RequestParam long cmntId, @RequestParam String cmntCn,
                                          HttpSession session) {
        try {
            srBbsService.updateCmnt(cmntId, cmntCn, viewer(session));
        } catch (CmntyException e) {
            return failure(e.getMessage());
        }
        return success("수정에 성공하였습니다.");
    }

    /** AS-IS POST srBbs/cmnt/delete/{cmntId}. */
    @PostMapping("/cmnt/delete/{cmntId}")
    @ResponseBody
    public Map<String, Object> deleteCmnt(@PathVariable long cmntId, HttpSession session) {
        try {
            srBbsService.deleteCmnt(cmntId, viewer(session));
        } catch (CmntyException e) {
            return failure(e.getMessage());
        }
        return success("삭제에 성공하였습니다.");
    }

    /** AS-IS POST srBbs/cmnt/upload - 숨은 multipart 폼. 끝나면 상세로 돌아간다. */
    @PostMapping("/cmnt/upload")
    public String uploadCmntFile(@RequestParam long cmntId,
                                 @RequestParam long bbsId,
                                 @RequestParam(value = "detailImageFile", required = false) MultipartFile[] detailImageFile,
                                 HttpSession session) {
        try {
            srBbsService.addCmntFiles(cmntId, detailImageFile, viewer(session));
        } catch (CmntyException e) {
            return redirectToDetail(bbsId, e.getMessage());
        }
        return redirectToDetail(bbsId, "파일이 등록되었습니다.");
    }

    /** AS-IS POST srBbs/cmnt/deleteFile. */
    @PostMapping("/cmnt/deleteFile")
    @ResponseBody
    public Map<String, Object> deleteCmntFile(@RequestParam long fileId, HttpSession session) {
        try {
            srBbsService.deleteCmntFile(fileId, viewer(session));
        } catch (CmntyException e) {
            return failure(e.getMessage());
        }
        return success("파일이 삭제 되었습니다.");
    }

    /** AS-IS GET srBbs/cmnt/file-download/{fileId} - 댓글 첨부 다운로드. */
    @GetMapping("/cmnt/file-download/{fileId}")
    public ResponseEntity<?> cmntFileDownload(@PathVariable long fileId) throws IOException {
        CmntySrBbsCmntFile file = srBbsService.cmntFile(fileId);
        return download(file.getAtchFileNm(), file.getOrgnlAtchFileNm(),
                CmntySrBbsAdminService.SUBDIR_CMNT);
    }

    /** AS-IS는 파일이 없으면 alert+history.back() 스크립트를 내려준다. */
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

    /** 공통코드 WDR을 AS-IS JSP가 쓰는 모양(id·label)으로 내려준다. */
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
        return flashRedirect.to("/community/sr-bbs/detail/" + bbsId, message);
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
