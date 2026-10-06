package com.ghlove.admin.web;

import com.ghlove.admin.domain.CmntyOffSrBbsCmntFile;
import com.ghlove.admin.domain.CmntyOffSrBbsFile;
import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.service.CmntyException;
import com.ghlove.admin.service.CmntyFileStorageService;
import com.ghlove.admin.service.CmntyOffSrBbsAdminService;
import com.ghlove.admin.service.CmntyText;
import com.ghlove.admin.service.CommonCodeService;
import com.ghlove.admin.web.support.CmntyOffSrBbsSearchParam;
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
 * 오프라인 담당자 SR 게시판 (메뉴 11406) - AS-IS {@code CommunityManagerController}의
 * {@code offSrBbs/*} 15개 엔드포인트 재현({@code /opmanager/community/offSrBbs/list} 외).
 *
 * <p>엔드포인트 구성·동작은 SR 게시판({@link CmntySrBbsAdminController})과 같다 -
 * 다른 것은 검색칸(은행·소속지점)과 소속 표기([은행명] 지점명)뿐이다. 삭제 경로만 AS-IS가
 * {@code deleteOffSrBbs/{bbsId}}로 다르게 두었다.
 *
 * <p>이 메뉴는 op_menu_right 실측이 <b>ROLE_ADMIN_1·2·7·8</b>이다 - 지자체 담당자(5·6)는
 * 메뉴로 들어올 수 없지만 <b>댓글 작성자로는 나타날 수 있어</b>(과거 데이터) 화면이 지자체 소속
 * 표기 분기를 갖고 있다.
 */
@Controller
@RequestMapping("/community/off-sr-bbs")
@RequiredArgsConstructor
public class CmntyOffSrBbsAdminController {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyyMMdd");

    private static final String LIST_URL = "/community/off-sr-bbs/list";

    /** AS-IS가 파일을 못 찾았을 때 돌려주는 HTML 조각(문구 그대로). */
    private static final String FILE_MISSING_SCRIPT =
            "<script>alert('파일이 존재하지 않습니다. 시스템관리자에 문의하여 주십시오.'); history.back();</script>";

    /** 검색칸의 은행 목록 - AS-IS CodeUtils.getCodeList("OFF_BANK_LIST"). */
    private static final String BANK_CODE_TYPE = "OFF_BANK_LIST";

    private final CmntyOffSrBbsAdminService offSrBbsService;
    private final com.ghlove.admin.web.support.FlashRedirect flashRedirect;
    private final CmntyFileStorageService fileStorageService;
    private final CommonCodeService commonCodeService;

    @GetMapping
    public String root() {
        return "redirect:" + LIST_URL;
    }

    /** AS-IS GET/POST offSrBbs/list - 한 메서드가 둘 다 받고 진입에서도 조회한다. */
    @RequestMapping("/list")
    public String list(@ModelAttribute("searchParam") CmntyOffSrBbsSearchParam searchParam,
                       HttpServletRequest request, Model model) {
        searchParam.applyDefaults();

        int count = offSrBbsService.count(searchParam.getStartDt(), searchParam.getEndDt(),
                searchParam.getSearchRole(), searchParam.getShBank(), searchParam.getWhere(),
                searchParam.getQuery());
        Pagination pagination = Pagination.of(count, searchParam.getPage(), searchParam.getItemsPerPage())
                .withLinkFrom(request);
        List<CmntyOffSrBbsAdminService.BbsRow> list = offSrBbsService.list(searchParam.getStartDt(),
                searchParam.getEndDt(), searchParam.getSearchRole(), searchParam.getShBank(),
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
        model.addAttribute("offBanks", bankOptions());
        return "community/offSrBbs/list";
    }

    @GetMapping("/form")
    public String createForm(HttpSession session, Model model) {
        model.addAttribute("role", offSrBbsService.screenRole(viewer(session)));
        return "community/offSrBbs/form";
    }

    @PostMapping("/form")
    public String create(@RequestParam String bbsTtl,
                         @RequestParam String bbsCn,
                         @RequestParam(defaultValue = "N") String noticeYn,
                         @RequestParam(defaultValue = "N") String isSecret,
                         @RequestParam(value = "detailImageFiles", required = false) MultipartFile[] detailImageFiles,
                         HttpSession session) {
        try {
            offSrBbsService.insert(bbsTtl, bbsCn, noticeYn, isSecret, detailImageFiles, viewer(session));
        } catch (CmntyException e) {
            return redirectToList(e.getMessage());
        }
        return redirectToList("등록되었습니다.");
    }

    @GetMapping("/detail/{bbsId}")
    public String detail(@PathVariable long bbsId, HttpSession session, Model model) {
        Manager viewer = viewer(session);
        offSrBbsService.increaseInqCnt(bbsId);

        CmntyOffSrBbsAdminService.BbsDetail detail = offSrBbsService.detail(bbsId, viewer);
        if (detail == null) {
            return redirectToList("해당 글은 존재하지 않습니다.");
        }
        if (detail.secretBlocked()) {
            return redirectToList("비밀글입니다.");
        }

        List<CmntyOffSrBbsAdminService.CmntRow> comments = offSrBbsService.cmntList(bbsId);
        model.addAttribute("detail", detail);
        model.addAttribute("bbsCnHtml", CmntyText.nl2br(detail.bbsCn()));
        model.addAttribute("fileList", offSrBbsService.fileList(bbsId));
        model.addAttribute("cmntList", comments);
        model.addAttribute("offSrBbsCmntListCnt", comments.size());
        model.addAttribute("role", offSrBbsService.screenRole(viewer));
        model.addAttribute("userId", viewer == null ? null : viewer.getUserId());
        return "community/offSrBbs/detail";
    }

    @GetMapping("/edit/{bbsId}")
    public String editForm(@PathVariable long bbsId, HttpSession session, Model model) {
        Manager viewer = viewer(session);
        CmntyOffSrBbsAdminService.BbsDetail detail = offSrBbsService.detail(bbsId, viewer);
        if (detail == null) {
            return redirectToList("해당 글은 존재하지 않습니다.");
        }
        if (detail.secretBlocked()) {
            return redirectToList("비밀글은 본인만 수정 가능 합니다.");
        }

        model.addAttribute("detail", detail);
        model.addAttribute("fileList", offSrBbsService.fileList(bbsId));
        model.addAttribute("role", offSrBbsService.screenRole(viewer));
        return "community/offSrBbs/edit";
    }

    @PostMapping("/edit/{bbsId}")
    public String update(@PathVariable long bbsId,
                         @RequestParam String bbsTtl,
                         @RequestParam String bbsCn,
                         @RequestParam(defaultValue = "N") String noticeYn,
                         @RequestParam(defaultValue = "N") String isSecret,
                         @RequestParam(value = "detailImageFiles", required = false) MultipartFile[] detailImageFiles,
                         HttpSession session) {
        try {
            offSrBbsService.update(bbsId, bbsTtl, bbsCn, noticeYn, isSecret, detailImageFiles,
                    viewer(session));
        } catch (CmntyException e) {
            return redirectToList(e.getMessage());
        }
        return redirectToList("수정에 성공하였습니다.");
    }

    /** AS-IS POST offSrBbs/deleteOffSrBbs/{bbsId} - 경로 이름이 SR과 다르다. */
    @PostMapping("/deleteOffSrBbs/{bbsId}")
    @ResponseBody
    public Map<String, Object> delete(@PathVariable long bbsId, HttpSession session) {
        try {
            offSrBbsService.delete(bbsId, viewer(session));
        } catch (CmntyException e) {
            return failure(e.getMessage());
        }
        return success("게시글이 삭제 되었습니다.");
    }

    @GetMapping("/file-download/{fileId}")
    public ResponseEntity<?> fileDownload(@PathVariable long fileId) throws IOException {
        CmntyOffSrBbsFile file = offSrBbsService.bbsFile(fileId);
        return download(file.getAtchFileNm(), file.getOrgnlAtchFileNm(),
                CmntyOffSrBbsAdminService.SUBDIR_BBS);
    }

    @PostMapping("/delete-item-image")
    @ResponseBody
    public Map<String, Object> deleteBbsFile(@RequestParam long fileId, HttpSession session) {
        try {
            offSrBbsService.deleteBbsFile(fileId, viewer(session));
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
            offSrBbsService.addCmnt(bbsId, cmntCn, viewer(session));
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
            offSrBbsService.updateCmnt(cmntId, cmntCn, viewer(session));
        } catch (CmntyException e) {
            return failure(e.getMessage());
        }
        return success("수정에 성공하였습니다.");
    }

    @PostMapping("/cmnt/delete/{cmntId}")
    @ResponseBody
    public Map<String, Object> deleteCmnt(@PathVariable long cmntId, HttpSession session) {
        try {
            offSrBbsService.deleteCmnt(cmntId, viewer(session));
        } catch (CmntyException e) {
            return failure(e.getMessage());
        }
        return success("삭제에 성공하였습니다.");
    }

    @PostMapping("/cmnt/upload")
    public String uploadCmntFile(@RequestParam long cmntId,
                                 @RequestParam long bbsId,
                                 @RequestParam(value = "detailImageFile", required = false) MultipartFile[] detailImageFile,
                                 HttpSession session) {
        try {
            offSrBbsService.addCmntFiles(cmntId, detailImageFile, viewer(session));
        } catch (CmntyException e) {
            return redirectToDetail(bbsId, e.getMessage());
        }
        return redirectToDetail(bbsId, "파일이 등록되었습니다.");
    }

    @PostMapping("/cmnt/deleteFile")
    @ResponseBody
    public Map<String, Object> deleteCmntFile(@RequestParam long fileId, HttpSession session) {
        try {
            offSrBbsService.deleteCmntFile(fileId, viewer(session));
        } catch (CmntyException e) {
            return failure(e.getMessage());
        }
        return success("파일이 삭제 되었습니다.");
    }

    @GetMapping("/cmnt/file-download/{fileId}")
    public ResponseEntity<?> cmntFileDownload(@PathVariable long fileId) throws IOException {
        CmntyOffSrBbsCmntFile file = offSrBbsService.cmntFile(fileId);
        return download(file.getAtchFileNm(), file.getOrgnlAtchFileNm(),
                CmntyOffSrBbsAdminService.SUBDIR_CMNT);
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

    /** 공통코드 OFF_BANK_LIST를 AS-IS JSP가 쓰는 모양(id·label)으로 내려준다. */
    private List<Map<String, String>> bankOptions() {
        List<Map<String, String>> options = new ArrayList<>();
        commonCodeService.labelsOf(BANK_CODE_TYPE).forEach((id, label) -> {
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
        return flashRedirect.to("/community/off-sr-bbs/detail/" + bbsId, message);
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
