package com.ghlove.admin.web;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.domain.Mnl;
import com.ghlove.admin.repository.MnlAdminRepository;
import com.ghlove.admin.service.CommonMessageService;
import com.ghlove.admin.service.ManualAdminService;
import com.ghlove.admin.service.ManualException;
import com.ghlove.admin.service.ManualFileStorageService;
import com.ghlove.admin.web.support.ManualSearchParam;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 매뉴얼 관리 - AS-IS {@code ManualManagerController}({@code /opmanager/manual/**}) 이식.
 *
 * <p><b>★ 구조갭 해소</b>: AS-IS는 한 컨트롤러가 <b>두 화면</b>을 담당한다 -
 * {@code manual/user/*}(사용자매뉴얼, 메뉴 <b>5202</b>)와 {@code manual/manager/*}
 * (관리자매뉴얼, 메뉴 <b>5201</b>). TO-BE는 두 메뉴와 중지된 5109까지 <b>셋 다
 * {@code /admin/manuals}</b>를 가리켜 서로 구분되지 않았다. 이번에 나눴다:
 * <ul>
 *   <li>5202 사용자매뉴얼 → {@code /admin/manuals}</li>
 *   <li>5201 관리자매뉴얼 → {@code /admin/manuals/manager}</li>
 * </ul>
 * (5109 '매뉴얼관리'는 AS-IS에서 중지된 메뉴라 그대로 중지 상태로 둔다.)
 *
 * <p><b>★ 표도 바로잡았다</b>: TO-BE는 사용자매뉴얼용으로 {@code OP_MANUAL}을 새로 만들어
 * 두었는데 AS-IS 표 {@code g_mnl}이 TO-BE 스키마에 이미 있었다 - 자세한 내용은
 * {@link com.ghlove.admin.domain.Mnl} 주석.
 *
 * <p>AS-IS 동작·결함은 {@link ManualAdminService} 주석에 적어 두었다.
 */
@Controller
@RequiredArgsConstructor
public class ManualAdminController {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final ManualAdminService manualAdminService;
    private final com.ghlove.admin.web.support.FlashRedirect flashRedirect;
    private final ManualFileStorageService manualFileStorageService;
    private final CommonMessageService commonMessageService;

    // ------------------------------------------------------------------ 사용자매뉴얼 (5202)

    /** AS-IS GET manual/list - 진입에서는 조회하지 않는다(빈 목록). 등록일은 오늘~오늘로 채워진다. */
    @GetMapping("/admin/manuals")
    public String list(@ModelAttribute("searchParam") ManualSearchParam searchParam,
                       HttpServletRequest request, Model model) {
        fillList(searchParam, request, model, false);
        return "manual-admin/list";
    }

    /** AS-IS POST manual/list - 검색. */
    @PostMapping("/admin/manuals")
    public String searchList(@ModelAttribute("searchParam") ManualSearchParam searchParam,
                             HttpServletRequest request, Model model) {
        fillList(searchParam, request, model, true);
        return "manual-admin/list";
    }

    private void fillList(ManualSearchParam searchParam, HttpServletRequest request, Model model,
                          boolean doSearch) {
        // AS-IS는 GET·POST 모두 등록일 기본값을 오늘로 채운다(비어 있을 때만)
        searchParam.applyDefaults(LocalDate.now().format(DAY));

        int count = 0;
        List<MnlAdminRepository.Row> list = List.of();
        if (doSearch) {
            count = manualAdminService.count(searchParam.getWhere(), searchParam.getQuery(),
                    searchParam.getStartCreateDate(), searchParam.getEndCreateDate());
        }
        Pagination pagination = Pagination.of(count, searchParam.getPage(), searchParam.getItemsPerPage())
                .withLinkFrom(request);
        if (doSearch) {
            list = manualAdminService.list(searchParam.getWhere(), searchParam.getQuery(),
                    searchParam.getStartCreateDate(), searchParam.getEndCreateDate(),
                    pagination.getStartRow(), pagination.getItemsPerPage());
        }

        model.addAttribute("list", list);
        model.addAttribute("count", count);
        model.addAttribute("pagination", pagination);
        // AS-IS가 같이 내려주는 날짜 기준값들 - 이 화면은 month2까지 쓴다(화면 버튼은 3달·1년도 있다)
        String today = LocalDate.now().format(DAY);
        model.addAttribute("today", today);
        model.addAttribute("week", LocalDate.now().minusDays(7).format(DAY));
        model.addAttribute("month1", LocalDate.now().minusMonths(1).format(DAY));
        model.addAttribute("month2", LocalDate.now().minusMonths(2).format(DAY));
        model.addAttribute("month3", LocalDate.now().minusMonths(3).format(DAY));
        model.addAttribute("year1", LocalDate.now().minusYears(1).format(DAY));
    }

    /** AS-IS GET manual/create. */
    @GetMapping("/admin/manuals/create")
    public String createForm(@ModelAttribute("searchParam") ManualSearchParam searchParam, Model model) {
        model.addAttribute("manual", new Mnl());
        model.addAttribute("menuUrlList", manualAdminService.menuUrlList());
        return "manual-admin/form";
    }

    /** AS-IS POST manual/create - 끝나면 목록으로 가며 "등록되었습니다."를 띄운다. */
    @PostMapping("/admin/manuals/create")
    public String create(@RequestParam String menuSeCode, @RequestParam String menuSj,
                         @RequestParam(required = false) String menuCn,
                         @RequestParam(value = "detailImageFiles[]", required = false) MultipartFile file,
                         HttpSession session) {
        Manager manager = (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
        try {
            manualAdminService.create(menuSeCode, menuSj, menuCn, file, manager.getUserId());
        } catch (ManualException e) {
            return flashRedirect.to("/admin/manuals/create", e.getMessage());
        }
        return flashRedirect.to("/admin/manuals", "등록되었습니다.");
    }

    /** AS-IS GET manual/edit/{mnlSn} - 등록과 같은 화면이다. */
    @GetMapping("/admin/manuals/edit/{mnlSn}")
    public String editForm(@PathVariable Integer mnlSn,
                           @ModelAttribute("searchParam") ManualSearchParam searchParam, Model model) {
        try {
            model.addAttribute("manual", manualAdminService.find(mnlSn));
        } catch (ManualException e) {
            return flashRedirect.to("/admin/manuals", e.getMessage());
        }
        model.addAttribute("menuUrlList", manualAdminService.menuUrlList());
        return "manual-admin/form";
    }

    /** AS-IS POST manual/edit/{mnlSn} - 끝나면 목록으로 가며 M00289를 띄운다. */
    @PostMapping("/admin/manuals/edit/{mnlSn}")
    public String edit(@PathVariable Integer mnlSn, @RequestParam String menuSeCode,
                       @RequestParam String menuSj, @RequestParam(required = false) String menuCn,
                       @RequestParam(value = "detailImageFiles[]", required = false) MultipartFile file,
                       HttpSession session) {
        Manager manager = (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
        try {
            manualAdminService.update(mnlSn, menuSeCode, menuSj, menuCn, file, manager.getUserId());
        } catch (ManualException e) {
            return flashRedirect.to("/admin/manuals/edit/" + mnlSn, e.getMessage());
        }
        // AS-IS는 이 문구만 코드로 조회한다(등록은 문자열 리터럴이다)
        return flashRedirect.to("/admin/manuals", msg("M00289", "수정되었습니다."));
    }

    /** AS-IS POST manual/delete/{mnlSn} - 등록화면의 [삭제](ajax, JSON). */
    @PostMapping("/admin/manuals/delete/{mnlSn}")
    @ResponseBody
    public Map<String, Object> delete(@PathVariable Integer mnlSn) {
        try {
            manualAdminService.delete(mnlSn);
        } catch (RuntimeException e) {
            // AS-IS JsonViewUtils.exception("실패했습니다.")
            return Map.of("isSuccess", false, "errorMessage", "실패했습니다.");
        }
        return Map.of("isSuccess", true);
    }

    /**
     * AS-IS POST manual/deleteManual - 목록에서 체크한 건 일괄삭제(ajax).
     * AS-IS는 성공·실패를 <b>응답 본문의 결과코드</b>로 알린다({@code SUCC}/{@code FAIL}) -
     * 화면이 {@code response.data}를 보고 문구를 고른다.
     */
    @PostMapping("/admin/manuals/deleteManual")
    @ResponseBody
    public Map<String, Object> deleteList(
            @RequestParam(value = "manualList", required = false) List<Integer> manualList) {
        return Map.of("isSuccess", true, "data", manualAdminService.deleteList(manualList));
    }

    /**
     * AS-IS POST manual/delete-item-image - 첨부파일 삭제(ajax).
     * 파라미터 이름이 {@code itemId}인데 실제로는 매뉴얼 번호다(다른 화면에서 복사한 흔적).
     */
    @PostMapping("/admin/manuals/delete-item-image")
    @ResponseBody
    public Map<String, Object> deleteItemImage(@RequestParam("itemId") Integer itemId,
                                               HttpSession session) {
        Manager manager = (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
        try {
            manualAdminService.deleteFile(itemId, manager.getUserId());
        } catch (ManualException e) {
            return Map.of("isSuccess", false, "errorMessage", e.getMessage());
        }
        return Map.of("isSuccess", true);
    }

    /** AS-IS GET manual/file-download/{mnlSn}. */
    @GetMapping("/admin/manuals/file-download/{mnlSn}")
    public ResponseEntity<?> fileDownload(@PathVariable Integer mnlSn) throws IOException {
        Mnl mnl = manualAdminService.find(mnlSn);
        return download(mnl.getFileNm(), mnl.getOrginlFileNm());
    }

    // ---------------------------------------------------------------- 관리자매뉴얼 (5201)

    /** AS-IS GET·POST manual/manager/list - 둘이 같은 동작이다(검색 수단이 없다). */
    @GetMapping("/admin/manuals/manager")
    public String managerList(HttpSession session, Model model) {
        return fillManagerList(session, model);
    }

    @PostMapping("/admin/manuals/manager")
    public String managerSearchList(HttpSession session, Model model) {
        return fillManagerList(session, model);
    }

    private String fillManagerList(HttpSession session, Model model) {
        Manager manager = (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
        String authority = manager == null ? null : manager.getAuthority();
        model.addAttribute("menuList", manualAdminService.managerTree(authority));
        model.addAttribute("role", ManualAdminService.screenRole(authority));
        return "manual-admin/manager-list";
    }

    /**
     * AS-IS GET manual/manager/create - 메뉴명을 누르면 열리는 <b>작은 팝업</b>이다
     * (AS-IS {@code @RequestProperty(layout="base")} - 공통 레이아웃 없이 뜬다).
     */
    @GetMapping("/admin/manuals/manager/create")
    public String managerCreateForm(@RequestParam Integer menuId,
                                    @RequestParam(required = false) String menuName, Model model) {
        model.addAttribute("menuId", menuId);
        model.addAttribute("menuName", menuName);
        model.addAttribute("manual", manualAdminService.managerMenu(menuId));
        return "manual-admin/manager-form";
    }

    /**
     * AS-IS POST manual/manager/create - 메뉴에 파일을 붙이고 <b>팝업을 닫으며 부모창을
     * 새로고침</b>한다(AS-IS가 redirect에 그 자바스크립트를 함께 싣는다).
     */
    @PostMapping("/admin/manuals/manager/create")
    public String managerCreate(@RequestParam Integer menuId,
                                @RequestParam(required = false) String menuNm,
                                @RequestParam(value = "detailImageFiles[]", required = false) MultipartFile file) {
        try {
            manualAdminService.attachManagerManual(menuId, file);
        } catch (ManualException e) {
            return "redirect:/admin/manuals/manager/create?menuId=" + menuId
                    + "&menuName=" + encode(menuNm == null ? "" : menuNm)
                    + "&errorMessage=" + encode(e.getMessage());
        }
        return "redirect:/admin/manuals/manager/create?menuId=" + menuId
                + "&menuName=" + encode(menuNm == null ? "" : menuNm)
                + "&errorMessage=" + encode("등록되었습니다.") + "&closePopup=Y";
    }

    /** AS-IS POST manual/manager/delete-item-image - 메뉴에 붙은 파일 삭제(ajax, 파라미터 menuId). */
    @PostMapping("/admin/manuals/manager/delete-item-image")
    @ResponseBody
    public Map<String, Object> managerDeleteItemImage(@RequestParam("menuId") Integer menuId) {
        try {
            manualAdminService.deleteManagerManualFile(menuId);
        } catch (ManualException e) {
            return Map.of("isSuccess", false, "errorMessage", e.getMessage());
        }
        return Map.of("isSuccess", true);
    }

    /** AS-IS GET manual/manager/file-download/{menuId}. */
    @GetMapping("/admin/manuals/manager/file-download/{menuId}")
    public ResponseEntity<?> managerFileDownload(@PathVariable Integer menuId) throws IOException {
        var menu = manualAdminService.managerMenu(menuId);
        return download(menu.fileNm(), menu.orginlFileNm());
    }

    private ResponseEntity<?> download(String fileNm, String orginlFileNm) throws IOException {
        if (fileNm == null || fileNm.isBlank()) {
            return ResponseEntity.notFound().build();
        }
        Resource resource = new UrlResource(manualFileStorageService.resolve(fileNm).toUri());
        if (!resource.exists()) {
            return ResponseEntity.ok()
                    .contentType(new MediaType("text", "html", StandardCharsets.UTF_8))
                    .body("<script>alert('파일이 존재하지 않습니다.'); history.back();</script>");
        }
        String downloadName = orginlFileNm != null ? orginlFileNm : fileNm;
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
