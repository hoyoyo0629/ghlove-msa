package com.ghlove.admin.web;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.domain.SysNoticeSellerFile;
import com.ghlove.admin.repository.SysNoticeSellerAdminRepository;
import com.ghlove.admin.service.AdminFileStorageService;
import com.ghlove.admin.service.ContentException;
import com.ghlove.admin.service.SysNoticeSellerAdminService;
import com.ghlove.admin.web.support.FlashRedirect;
import com.ghlove.admin.web.support.Pagination;
import com.ghlove.admin.web.support.SellerNoticeSearchParam;
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
 * 답례품제공자 공지사항 (메뉴 5114) - AS-IS {@code SysNoticeSellerController}
 * ({@code /opmanager/sellerNotice/**}) 이식.
 *
 * <p>기존 TO-BE는 페이징·건수·출력수·날짜검색·검색구분이 없고 첨부도 한 건만 받는 축소
 * 구현이었다. AS-IS 기준으로 다시 맞췄다.
 *
 * <p><b>AS-IS 권한 규칙</b>: 목록은 권한 검사가 <b>주석처리</b>되어 있어 누구나 열람하고,
 * 등록·수정·삭제만 <b>ROLE_ADMIN_1·2</b>로 막는다(권한이 없으면 안내와 함께 되돌린다).
 * <p><b>AS-IS 그대로</b>: 목록은 GET 진입에서도 조회 · 삭제는 소프트 삭제 ·
 * 등록과 수정이 같은 화면(form.jsp) · 첨부는 1건 · 게시여부는 '게시'/'중지'로 보여준다.
 */
@Controller
@RequiredArgsConstructor
public class SysNoticeSellerAdminController {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String BASE_URL = "/admin/seller-notices";

    private final SysNoticeSellerAdminService sellerNoticeAdminService;
    private final AdminFileStorageService fileStorageService;
    private final FlashRedirect flashRedirect;

    /** AS-IS GET list - 이 화면도 진입에서 바로 조회한다. */
    @GetMapping(BASE_URL)
    public String list(@ModelAttribute("searchParam") SellerNoticeSearchParam searchParam,
                       HttpServletRequest request, Model model) {
        fillList(searchParam, request, model);
        return "seller-notice-admin/list";
    }

    /** AS-IS POST list - 검색(GET과 같은 동작). */
    @PostMapping(BASE_URL)
    public String searchList(@ModelAttribute("searchParam") SellerNoticeSearchParam searchParam,
                             HttpServletRequest request, Model model) {
        fillList(searchParam, request, model);
        return "seller-notice-admin/list";
    }

    private void fillList(SellerNoticeSearchParam searchParam, HttpServletRequest request, Model model) {
        int count = sellerNoticeAdminService.count(searchParam.getWhere(), searchParam.getQuery(),
                searchParam.getStartDt(), searchParam.getEndDt());
        Pagination pagination = Pagination.of(count, searchParam.getPage(), searchParam.getItemsPerPage())
                .withLinkFrom(request);
        List<SysNoticeSellerAdminRepository.Row> list = sellerNoticeAdminService.list(
                searchParam.getWhere(), searchParam.getQuery(), searchParam.getStartDt(), searchParam.getEndDt(),
                pagination.getStartRow(), pagination.getItemsPerPage());

        model.addAttribute("list", list);
        model.addAttribute("pagination", pagination);
        model.addAttribute("count", count);
        String today = LocalDate.now().format(DAY);
        model.addAttribute("today", today);
        model.addAttribute("week", LocalDate.now().minusDays(7).format(DAY));
        model.addAttribute("month1", LocalDate.now().minusMonths(1).format(DAY));
        model.addAttribute("month2", LocalDate.now().minusMonths(2).format(DAY));
    }

    /** AS-IS GET create - 권한이 없으면 "게시글 작성 권한이 없습니다."로 되돌린다. */
    @GetMapping(BASE_URL + "/create")
    public String createForm(HttpSession session, Model model) {
        if (!canWrite(session)) {
            return flashRedirect.to("/admin", "게시글 작성 권한이 없습니다.");
        }
        model.addAttribute("files", List.of());
        return "seller-notice-admin/form";
    }

    /** AS-IS POST create. */
    @PostMapping(BASE_URL + "/create")
    public String create(@RequestParam String subject, @RequestParam(required = false) String content,
                         @RequestParam(required = false) String noticeFlag,
                         @RequestParam(required = false) String displayFlag,
                         @RequestParam(value = "detailImageFiles", required = false) MultipartFile[] detailImageFiles,
                         HttpSession session, Model model) {
        if (!canWrite(session)) {
            return flashRedirect.to("/admin", "게시글 작성 권한이 없습니다.");
        }
        try {
            sellerNoticeAdminService.create(subject, content, noticeFlag, displayFlag,
                    detailImageFiles, managerId(session));
        } catch (ContentException | IllegalArgumentException | IllegalStateException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("files", List.of());
            return "seller-notice-admin/form";
        }
        return flashRedirect.to(BASE_URL, "등록되었습니다.");
    }

    /** AS-IS GET edit/{noticeId} - 등록과 같은 화면을 값이 채워진 상태로 그린다. */
    @GetMapping(BASE_URL + "/edit/{noticeId}")
    public String editForm(@PathVariable Long noticeId, HttpSession session, Model model) {
        if (!canWrite(session)) {
            return flashRedirect.to("/admin", "게시글 작성 권한이 없습니다.");
        }
        try {
            model.addAttribute("sysNoticeSellerDto", sellerNoticeAdminService.notice(noticeId));
        } catch (ContentException e) {
            return flashRedirect.to(BASE_URL, e.getMessage());
        }
        model.addAttribute("files", sellerNoticeAdminService.filesOf(noticeId));
        return "seller-notice-admin/form";
    }

    /** AS-IS POST edit/{noticeId}. */
    @PostMapping(BASE_URL + "/edit/{noticeId}")
    public String edit(@PathVariable Long noticeId, @RequestParam String subject,
                       @RequestParam(required = false) String content,
                       @RequestParam(required = false) String noticeFlag,
                       @RequestParam(required = false) String displayFlag,
                       @RequestParam(value = "detailImageFiles", required = false) MultipartFile[] detailImageFiles,
                       HttpSession session, Model model) {
        if (!canWrite(session)) {
            return flashRedirect.to(BASE_URL, "답례품제공자 공지사항 수정 권한이 없습니다.");
        }
        try {
            sellerNoticeAdminService.update(noticeId, subject, content, noticeFlag, displayFlag,
                    detailImageFiles, managerId(session));
        } catch (ContentException | IllegalArgumentException | IllegalStateException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("sysNoticeSellerDto", sellerNoticeAdminService.notice(noticeId));
            model.addAttribute("files", sellerNoticeAdminService.filesOf(noticeId));
            return "seller-notice-admin/form";
        }
        return flashRedirect.to(BASE_URL, "수정에 성공하였습니다.");
    }

    /** AS-IS POST delete/{noticeId}(ajax) - 수정화면의 [삭제]. 소프트 삭제다. */
    @PostMapping(BASE_URL + "/delete/{noticeId}")
    @ResponseBody
    public Map<String, Object> delete(@PathVariable Long noticeId, HttpSession session) {
        if (!canWrite(session)) {
            return Map.of("isSuccess", false, "errorMessage", "게시글 삭제 권한이 없습니다.");
        }
        try {
            sellerNoticeAdminService.delete(noticeId, managerId(session));
        } catch (RuntimeException e) {
            return Map.of("isSuccess", false, "errorMessage", "삭제에 실패했습니다.");
        }
        return Map.of("isSuccess", true, "data", "게시글이 삭제 되었습니다.");
    }

    /**
     * 목록 선택삭제(ajax). <b>AS-IS는 화면 JS가 이 주소로 보내는데 컨트롤러에 엔드포인트가 없어
     * 404로 실패한다</b> - 버튼이 아무 일도 못 하는 결함이라 단건 삭제와 같은 규칙으로 만들었다.
     */
    @PostMapping(BASE_URL + "/deleteNotice")
    @ResponseBody
    public Map<String, Object> deleteList(
            @RequestParam(value = "noticeList", required = false) List<Long> noticeList,
            HttpSession session) {
        if (!canWrite(session)) {
            return Map.of("isSuccess", false, "errorMessage", "게시글 삭제 권한이 없습니다.");
        }
        return Map.of("isSuccess", true, "data",
                sellerNoticeAdminService.deleteList(noticeList, managerId(session)));
    }

    /** AS-IS POST delete-item-image(ajax) - 첨부 소프트 삭제. */
    @PostMapping(BASE_URL + "/delete-item-image")
    @ResponseBody
    public Map<String, Object> deleteItemImage(@RequestParam("fileId") Long fileId, HttpSession session) {
        sellerNoticeAdminService.deleteFile(fileId, managerId(session));
        return Map.of("isSuccess", true, "data", "파일이 삭제 되었습니다.");
    }

    /** AS-IS GET file-download/{fileId}. 파일이 없으면 AS-IS와 같은 alert 스크립트를 내려준다. */
    @GetMapping(BASE_URL + "/file-download/{fileId}")
    public ResponseEntity<?> fileDownload(@PathVariable Long fileId) throws IOException {
        SysNoticeSellerFile file = sellerNoticeAdminService.file(fileId);
        Resource resource = new UrlResource(
                fileStorageService.resolve(file.getAtchFileNm(), SysNoticeSellerAdminService.SUBDIR).toUri());
        if (!resource.exists()) {
            return ResponseEntity.ok()
                    .contentType(new MediaType("text", "html", StandardCharsets.UTF_8))
                    .body("<script>alert('파일이 존재하지 않습니다. 시스템관리자에 문의하여 주십시오.');"
                            + " history.back();</script>");
        }
        String downloadName = file.getOrgnlAtchFileNm() != null ? file.getOrgnlAtchFileNm() : file.getAtchFileNm();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(downloadName, StandardCharsets.UTF_8)
                                .build().toString())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    /** AS-IS: 등록·수정·삭제는 ROLE_ADMIN_1·2만. 목록은 막지 않는다(권한 검사가 주석처리). */
    private static boolean canWrite(HttpSession session) {
        Manager manager = manager(session);
        String authority = manager == null ? "" : manager.getAuthority();
        return "ROLE_ADMIN_1".equals(authority) || "ROLE_ADMIN_2".equals(authority);
    }

    private static Manager manager(HttpSession session) {
        return (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
    }

    private static Long managerId(HttpSession session) {
        Manager manager = manager(session);
        return manager == null ? null : manager.getUserId();
    }
}
