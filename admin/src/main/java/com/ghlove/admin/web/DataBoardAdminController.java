package com.ghlove.admin.web;

import com.ghlove.admin.domain.DataBoard;
import com.ghlove.admin.domain.DataBoardFile;
import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.service.CommonMessageService;
import com.ghlove.admin.service.ContentException;
import com.ghlove.admin.service.DataBoardAdminService;
import com.ghlove.admin.service.DataBoardFileStorageService;
import com.ghlove.admin.web.support.DataBoardSearchParam;
import com.ghlove.admin.web.support.FlashRedirect;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 자료실 (메뉴 5107) - AS-IS {@code DataboardManagerController}
 * ({@code /opmanager/data-board/**}) 이식.
 *
 * <p>기존 TO-BE 화면은 검색·페이징·건수·출력수·선택삭제가 전부 없고 첨부도 한 건만 받는
 * 축소 구현이었다. 게다가 {@code DATA_FILE_ID}가 숫자 컬럼인데 UUID 문자열을 넣고 있어
 * <b>첨부 업로드는 실행되면 반드시 실패하는 코드</b>였다(시퀀스 채번으로 바로잡았다).
 *
 * <p><b>AS-IS 그대로인 것</b>: GET 진입은 빈 목록(POST 검색에서만 조회) · 등록일 검색조건을
 * 오늘로 채움 · 검색구분 제목/내용 · 역할 분기 없음 · 등록과 수정이 같은 화면(form.jsp,
 * {@code dataId} 유무로 버튼만 갈림) · 첨부 다중 업로드 · 수정은 기존 첨부를 지우지 않고 추가.
 */
@Controller
@RequiredArgsConstructor
public class DataBoardAdminController {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String BASE_URL = "/admin/data-board";

    private final DataBoardAdminService dataBoardAdminService;
    private final DataBoardFileStorageService fileStorageService;
    private final CommonMessageService commonMessageService;
    private final FlashRedirect flashRedirect;

    /** AS-IS GET list - 진입에서는 조회하지 않는다(빈 목록). */
    @GetMapping(BASE_URL)
    public String list(@ModelAttribute("searchParam") DataBoardSearchParam searchParam,
                       HttpServletRequest request, Model model) {
        fillList(searchParam, request, model, false);
        return "data-board-admin/list";
    }

    /** AS-IS POST list - 검색. */
    @PostMapping(BASE_URL)
    public String searchList(@ModelAttribute("searchParam") DataBoardSearchParam searchParam,
                             HttpServletRequest request, Model model) {
        fillList(searchParam, request, model, true);
        return "data-board-admin/list";
    }

    private void fillList(DataBoardSearchParam searchParam, HttpServletRequest request,
                          Model model, boolean doSearch) {
        searchParam.applyDefaults();

        int count = 0;
        List<DataBoard> list = List.of();
        if (doSearch) {
            count = dataBoardAdminService.count(searchParam.getWhere(), searchParam.getQuery(),
                    searchParam.getStartCreateDate(), searchParam.getEndCreateDate());
        }
        Pagination pagination = Pagination.of(count, searchParam.getPage(), searchParam.getItemsPerPage())
                .withLinkFrom(request);
        if (doSearch) {
            list = dataBoardAdminService.list(searchParam.getWhere(), searchParam.getQuery(),
                    searchParam.getStartCreateDate(), searchParam.getEndCreateDate(),
                    pagination.getStartRow(), pagination.getItemsPerPage());
        }

        model.addAttribute("list", list);
        model.addAttribute("pagination", pagination);
        model.addAttribute("count", count);
        String today = LocalDate.now().format(DAY);
        model.addAttribute("today", today);
        model.addAttribute("week", LocalDate.now().minusDays(7).format(DAY));
        model.addAttribute("month1", LocalDate.now().minusMonths(1).format(DAY));
        model.addAttribute("month2", LocalDate.now().minusMonths(2).format(DAY));
    }

    /** AS-IS GET create - 등록·수정이 같은 화면이라 databoard 없이 들어가면 등록 모드다. */
    @GetMapping(BASE_URL + "/create")
    public String createForm(Model model) {
        model.addAttribute("files", List.of());
        return "data-board-admin/form";
    }

    /** AS-IS POST create - 첨부는 {@code detailImageFiles[]}로 여러 건이 올라온다. */
    @PostMapping(BASE_URL + "/create")
    public String create(@RequestParam String subject, @RequestParam(required = false) String content,
                         @RequestParam(required = false) String noticeFlag,
                         @RequestParam(required = false) String useYn,
                         @RequestParam(value = "detailImageFiles[]", required = false) MultipartFile[] detailImageFiles,
                         HttpSession session, Model model) {
        try {
            dataBoardAdminService.create(subject, content, noticeFlag, useYn, managerName(session), detailImageFiles);
        } catch (ContentException | IllegalArgumentException | IllegalStateException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("files", List.of());
            return "data-board-admin/form";
        }
        // AS-IS: ViewUtils.redirect(..., "등록되었습니다.") - 메시지 코드가 아니라 하드코딩 문자열이다.
        return flashRedirect.to(BASE_URL, "등록되었습니다.");
    }

    /** AS-IS GET edit/{dataId} - 등록과 같은 화면을 dataId가 있는 상태로 그린다. */
    @GetMapping(BASE_URL + "/edit/{dataId}")
    public String editForm(@PathVariable Integer dataId, Model model) {
        model.addAttribute("databoard", dataBoardAdminService.dataBoard(dataId));
        model.addAttribute("files", dataBoardAdminService.filesOf(dataId));
        return "data-board-admin/form";
    }

    /** AS-IS POST edit/{dataId}. */
    @PostMapping(BASE_URL + "/edit/{dataId}")
    public String edit(@PathVariable Integer dataId, @RequestParam String subject,
                       @RequestParam(required = false) String content,
                       @RequestParam(required = false) String noticeFlag,
                       @RequestParam(required = false) String useYn,
                       @RequestParam(value = "detailImageFiles[]", required = false) MultipartFile[] detailImageFiles,
                       HttpSession session, Model model) {
        try {
            dataBoardAdminService.update(dataId, subject, content, noticeFlag, useYn,
                    managerName(session), detailImageFiles);
        } catch (ContentException | IllegalArgumentException | IllegalStateException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("databoard", dataBoardAdminService.dataBoard(dataId));
            model.addAttribute("files", dataBoardAdminService.filesOf(dataId));
            return "data-board-admin/form";
        }
        return flashRedirect.to(BASE_URL, commonMessageService.get("M00289"));
    }

    /** AS-IS POST delete/{dataId}(ajax) - 수정화면의 [삭제] 버튼. */
    @PostMapping(BASE_URL + "/delete/{dataId}")
    @ResponseBody
    public Map<String, Object> delete(@PathVariable Integer dataId) {
        try {
            dataBoardAdminService.delete(dataId);
        } catch (RuntimeException e) {
            return Map.of("isSuccess", false, "errorMessage", "삭제에 실패했습니다.");
        }
        return Map.of("isSuccess", true);
    }

    /** AS-IS POST deleteDataboard - 목록 체크박스 일괄삭제(파라미터명 databoardList). */
    @PostMapping(BASE_URL + "/deleteDataboard")
    @ResponseBody
    public Map<String, Object> deleteList(
            @RequestParam(value = "databoardList", required = false) List<Integer> databoardList) {
        return Map.of("isSuccess", true, "data", dataBoardAdminService.deleteList(databoardList));
    }

    /** AS-IS POST delete-item-image - 첨부 하나 삭제(파라미터명 itemId, 디스크 파일도 지운다). */
    @PostMapping(BASE_URL + "/delete-item-image")
    @ResponseBody
    public Map<String, Object> deleteItemImage(@RequestParam("itemId") Integer itemId) {
        dataBoardAdminService.deleteFile(itemId);
        return Map.of("isSuccess", true);
    }

    /** AS-IS GET file-download/{dataFileId} - 첨부 다운로드(원본 파일명으로 내려준다). */
    @GetMapping(BASE_URL + "/file-download/{dataFileId}")
    public ResponseEntity<?> fileDownload(@PathVariable Integer dataFileId) throws IOException {
        DataBoardFile file = dataBoardAdminService.file(dataFileId);
        Resource resource = new UrlResource(fileStorageService.resolve(file.getFileName()).toUri());
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

    /** AS-IS는 {@code SecurityUtils.getCurrentUser().getUserName()}을 작성자로 저장한다. */
    private static String managerName(HttpSession session) {
        Manager manager = (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
        return manager == null ? null : manager.getUserName();
    }
}
