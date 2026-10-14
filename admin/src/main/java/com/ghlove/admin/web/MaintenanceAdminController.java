package com.ghlove.admin.web;

import com.ghlove.admin.domain.GSrMaintenance;
import com.ghlove.admin.domain.GSrMaintenanceFile;
import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.repository.MaintenanceAdminRepository;
import com.ghlove.admin.service.AdminFileStorageService;
import com.ghlove.admin.service.CommonCodeService;
import com.ghlove.admin.service.ContentException;
import com.ghlove.admin.service.MaintenanceAdminService;
import com.ghlove.admin.web.support.FlashRedirect;
import com.ghlove.admin.web.support.MaintenanceSearchParam;
import com.ghlove.admin.web.support.Pagination;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 운영관리 SR게시판 (메뉴 5113) - AS-IS {@code MaintenanceController}
 * ({@code /opmanager/maintenance/**}) 이식.
 *
 * <p>기존 TO-BE는 페이징·건수·출력수·완료구분 다중선택·SR번호 채번이 전부 없고 첨부도 한 건만
 * 받는 축소 구현이었다. AS-IS 기준으로 다시 맞췄다.
 *
 * <p><b>AS-IS 그대로</b>: 목록은 <b>GET 진입에서도 조회한다</b>(공지·자료실과 다르다) ·
 * 등록일자를 오늘로 채운다 · 정렬은 긴급 우선 + 최신순 · 삭제는 소프트 삭제 ·
 * 목록에서 제목을 누르면 상세가 아니라 <b>수정화면</b>으로 간다(AS-IS JS가 detail 호출을 주석처리) ·
 * 등록/수정 폼은 <b>ROLE_ADMIN_1·2에게만</b> 보인다(그 외 권한은 빈 표가 뜬다).
 */
@Controller
@RequiredArgsConstructor
public class MaintenanceAdminController {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter FILE_STAMP = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final String BASE_URL = "/admin/maintenance";

    private final MaintenanceAdminService maintenanceAdminService;
    private final AdminFileStorageService fileStorageService;
    private final CommonCodeService commonCodeService;
    private final FlashRedirect flashRedirect;

    /** AS-IS GET list - 이 화면은 <b>진입에서도 조회한다</b>. */
    @GetMapping(BASE_URL)
    public String list(@ModelAttribute("searchParam") MaintenanceSearchParam searchParam,
                       HttpServletRequest request, Model model) {
        fillList(searchParam, request, model);
        return "maintenance-admin/list";
    }

    /** AS-IS POST list - 검색(GET과 같은 동작). */
    @PostMapping(BASE_URL)
    public String searchList(@ModelAttribute("searchParam") MaintenanceSearchParam searchParam,
                             HttpServletRequest request, Model model) {
        fillList(searchParam, request, model);
        return "maintenance-admin/list";
    }

    private void fillList(MaintenanceSearchParam searchParam, HttpServletRequest request, Model model) {
        searchParam.applyDefaults();
        List<String> states = searchParam.selectedProcessStates();

        int count = maintenanceAdminService.count(searchParam.getWhere(), searchParam.getQuery(),
                searchParam.getStartDt(), searchParam.getEndDt(), states);
        Pagination pagination = Pagination.of(count, searchParam.getPage(), searchParam.getItemsPerPage())
                .withLinkFrom(request);
        List<MaintenanceAdminRepository.Row> list = maintenanceAdminService.list(
                searchParam.getWhere(), searchParam.getQuery(), searchParam.getStartDt(), searchParam.getEndDt(),
                states, pagination.getStartRow(), pagination.getItemsPerPage());

        model.addAttribute("list", list);
        model.addAttribute("pagination", pagination);
        model.addAttribute("count", count);
        String today = LocalDate.now().format(DAY);
        model.addAttribute("today", today);
        model.addAttribute("week", LocalDate.now().minusDays(7).format(DAY));
        model.addAttribute("month1", LocalDate.now().minusMonths(1).format(DAY));
        model.addAttribute("month2", LocalDate.now().minusMonths(2).format(DAY));
        // AS-IS가 완료구분 체크박스를 그리는 데 쓰는 공통코드
        model.addAttribute("procsStateCds", commonCodeService.labelsOf("MAINTEN_STATE_CODE"));
    }

    /** AS-IS GET form - 등록화면. */
    @GetMapping(BASE_URL + "/form")
    public String createForm(HttpSession session, Model model) {
        fillFormCodes(model, session);
        model.addAttribute("files", List.of());
        return "maintenance-admin/form";
    }

    /** AS-IS POST form - 등록. SR번호는 서버가 채번한다. */
    @PostMapping(BASE_URL + "/form")
    public String create(@ModelAttribute GSrMaintenance form,
                         @RequestParam(value = "detailImageFiles", required = false) MultipartFile[] detailImageFiles,
                         HttpSession session, Model model) {
        try {
            maintenanceAdminService.create(form, detailImageFiles, managerId(session));
        } catch (ContentException | IllegalArgumentException | IllegalStateException e) {
            model.addAttribute("errorMessage", e.getMessage());
            fillFormCodes(model, session);
            model.addAttribute("files", List.of());
            return "maintenance-admin/form";
        }
        return flashRedirect.to(BASE_URL, "등록되었습니다.");
    }

    /** AS-IS GET edit/{bbsId} - 수정화면(목록의 제목 링크가 여기로 온다). 조회수를 올린다. */
    @GetMapping(BASE_URL + "/edit/{bbsId}")
    public String editForm(@PathVariable Long bbsId, HttpSession session, Model model) {
        maintenanceAdminService.addInquiryCount(bbsId);
        fillFormCodes(model, session);
        model.addAttribute("maintenanceDto", maintenanceAdminService.maintenance(bbsId));
        model.addAttribute("files", maintenanceAdminService.filesOf(bbsId));
        return "maintenance-admin/edit";
    }

    /** AS-IS POST edit/{bbsId}. */
    @PostMapping(BASE_URL + "/edit/{bbsId}")
    public String edit(@PathVariable Long bbsId, @ModelAttribute GSrMaintenance form,
                       @RequestParam(value = "detailImageFiles", required = false) MultipartFile[] detailImageFiles,
                       HttpSession session, Model model) {
        try {
            maintenanceAdminService.update(bbsId, form, detailImageFiles, managerId(session));
        } catch (ContentException | IllegalArgumentException | IllegalStateException e) {
            model.addAttribute("errorMessage", e.getMessage());
            fillFormCodes(model, session);
            model.addAttribute("maintenanceDto", maintenanceAdminService.maintenance(bbsId));
            model.addAttribute("files", maintenanceAdminService.filesOf(bbsId));
            return "maintenance-admin/edit";
        }
        return flashRedirect.to(BASE_URL, "수정에 성공하였습니다.");
    }

    /** AS-IS POST delete-item-image(ajax) - 첨부 소프트 삭제. */
    @PostMapping(BASE_URL + "/delete-item-image")
    @ResponseBody
    public Map<String, Object> deleteItemImage(@RequestParam("fileId") Long fileId, HttpSession session) {
        maintenanceAdminService.deleteFile(fileId, managerId(session));
        return Map.of("isSuccess", true, "data", "파일이 삭제 되었습니다.");
    }

    /** AS-IS POST deleteMaintenance/{bbsId}(ajax) - 게시글 소프트 삭제. */
    @PostMapping(BASE_URL + "/deleteMaintenance/{bbsId}")
    @ResponseBody
    public Map<String, Object> deleteMaintenance(@PathVariable Long bbsId, HttpSession session) {
        try {
            maintenanceAdminService.delete(bbsId, managerId(session));
        } catch (RuntimeException e) {
            return Map.of("isSuccess", false, "errorMessage", "삭제에 실패했습니다.");
        }
        return Map.of("isSuccess", true, "data", "게시글이 삭제 되었습니다.");
    }

    /** AS-IS GET file-download/{fileId}. 파일이 없으면 AS-IS와 같은 alert 스크립트를 내려준다. */
    @GetMapping(BASE_URL + "/file-download/{fileId}")
    public ResponseEntity<?> fileDownload(@PathVariable Long fileId) throws IOException {
        GSrMaintenanceFile file = maintenanceAdminService.file(fileId);
        Resource resource = new UrlResource(
                fileStorageService.resolve(file.getAtchFileNm(), MaintenanceAdminService.SUBDIR).toUri());
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

    /**
     * AS-IS GET list/mainten-excel-download - 목록 엑셀 받기.
     * AS-IS는 Apache POI로 .xlsx를 만드는데 admin 모듈에 POI 의존성이 없어 CSV(UTF-8 BOM)로 내려준다.
     * 검색조건·컬럼 구성은 화면 목록과 같게 맞췄다.
     */
    @GetMapping(BASE_URL + "/list/mainten-excel-download")
    public void excelDownload(@ModelAttribute("searchParam") MaintenanceSearchParam searchParam,
                              HttpServletResponse response) throws IOException {
        searchParam.applyDefaults();
        List<String> states = searchParam.selectedProcessStates();
        int count = maintenanceAdminService.count(searchParam.getWhere(), searchParam.getQuery(),
                searchParam.getStartDt(), searchParam.getEndDt(), states);
        List<MaintenanceAdminRepository.Row> rows = maintenanceAdminService.list(
                searchParam.getWhere(), searchParam.getQuery(), searchParam.getStartDt(), searchParam.getEndDt(),
                states, 0, Math.max(count, 1));

        StringBuilder csv = new StringBuilder("﻿");
        csv.append("No.,작성자,업무구분,CSR번호,처리구분,제목,내용,요청경로,담당자,답변,완료구분,")
                .append("등록일시,수정일시,처리시작일,처리종료일\n");
        int no = rows.size();
        for (MaintenanceAdminRepository.Row r : rows) {
            csv.append(no--).append(',')
                    .append(csvEscape(r.reqManagerNm())).append(',')
                    .append(csvEscape(r.reqTypeNm())).append(',')
                    .append(csvEscape(r.srNo())).append(',')
                    .append(csvEscape(r.processTypeNm())).append(',')
                    .append(csvEscape(r.bbsTtl())).append(',')
                    .append(csvEscape(r.bbsCn())).append(',')
                    .append(csvEscape(r.reqChannelNm())).append(',')
                    .append(csvEscape(r.processManagerNm())).append(',')
                    .append(csvEscape(r.processCn())).append(',')
                    .append(csvEscape(r.processStateNm())).append(',')
                    .append(csvEscape(r.frstCrtDt())).append(',')
                    .append(csvEscape(r.lastMdfcnDt())).append(',')
                    .append(csvEscape(r.processStartDate())).append(',')
                    .append(csvEscape(r.processEndDate())).append('\n');
        }

        // AS-IS 파일명: 운영관리_SR리스트_{yyyyMMddHHmmss}.xlsx
        String fileName = "운영관리_SR리스트_" + LocalDateTime.now().format(FILE_STAMP) + ".csv";
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename*=UTF-8''" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));
        response.getWriter().write(csv.toString());
    }

    private static String csvEscape(String value) {
        if (value == null) {
            return "";
        }
        return "\"" + value.replace("\"", "\"\"").replace("\r", " ").replace("\n", " ") + "\"";
    }

    /**
     * AS-IS는 로그인 운영자의 권한이 ROLE_ADMIN_1·2일 때만 {@code role}을 채우고, 폼 화면은
     * 그 값이 있을 때만 입력 표를 그린다. 담당자·조치결과는 <b>ROLE_ADMIN_1만</b> 입력할 수 있다.
     */
    private void fillFormCodes(Model model, HttpSession session) {
        Manager manager = manager(session);
        String authority = manager == null ? "" : manager.getAuthority();
        String role = ("ROLE_ADMIN_1".equals(authority) || "ROLE_ADMIN_2".equals(authority)) ? authority : "";
        model.addAttribute("role", role);
        model.addAttribute("maintenKiList", commonCodeService.labelsOf("MAINTEN_KI_CODE"));
        model.addAttribute("maintenPathList", commonCodeService.labelsOf("MAINTEN_PATH_CODE"));
        model.addAttribute("maintenCateList", commonCodeService.labelsOf("MAINTEN_CATE_CODE"));
        model.addAttribute("maintenProcList", commonCodeService.labelsOf("MAINTEN_PROC_CODE"));
        model.addAttribute("maintenStateList", commonCodeService.labelsOf("MAINTEN_STATE_CODE"));
        model.addAttribute("phoneCodeList", commonCodeService.labelsOf("PHONE"));
        model.addAttribute("telCodeList", commonCodeService.labelsOf("TEL"));
    }

    private static Manager manager(HttpSession session) {
        return (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
    }

    private static Long managerId(HttpSession session) {
        Manager manager = manager(session);
        return manager == null ? null : manager.getUserId();
    }
}
