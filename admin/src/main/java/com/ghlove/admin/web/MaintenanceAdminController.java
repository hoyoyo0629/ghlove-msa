package com.ghlove.admin.web;

import com.ghlove.admin.domain.GSrMaintenance;
import com.ghlove.admin.domain.GSrMaintenanceFile;
import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.repository.GSrMaintenanceFileRepository;
import com.ghlove.admin.repository.GSrMaintenanceRepository;
import com.ghlove.admin.service.AdminFileStorageService;
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
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** 운영유지관리 게시판 (AS-IS opmanager/maintenance - MaintenanceController, G_SR_MAINTENANCE).
 * AS-IS는 지자체 필터(WDR 코드)도 모델에 실었지만 이관된 G_SR_MAINTENANCE 스키마에는
 * locgov_code 컬럼 자체가 없어(원본 덤프에도 없었음) 이 화면은 날짜range+완료구분+
 * 제목검색만 지원한다 - 실제 스키마 대비 축소된 부분. */
@Controller
@RequestMapping("/admin/maintenance")
@RequiredArgsConstructor
public class MaintenanceAdminController {

    private static final DateTimeFormatter YYYYMMDD = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String SUBDIR = "maintenance";

    private final GSrMaintenanceRepository maintenanceRepository;
    private final GSrMaintenanceFileRepository fileRepository;
    private final AdminFileStorageService fileStorageService;

    @GetMapping
    public String list(@RequestParam(required = false) String startDate,
                        @RequestParam(required = false) String endDate,
                        @RequestParam(required = false) String processState,
                        @RequestParam(required = false) String keyword,
                        Model model) {
        List<GSrMaintenance> rows = maintenanceRepository.findAllByOrderByBbsIdDesc().stream()
                .filter(m -> startDate == null || startDate.isBlank()
                        || (m.getReqCrtDate() != null && m.getReqCrtDate().compareTo(startDate) >= 0))
                .filter(m -> endDate == null || endDate.isBlank()
                        || (m.getReqCrtDate() != null && m.getReqCrtDate().compareTo(endDate) <= 0))
                .filter(m -> processState == null || processState.isBlank() || processState.equals(m.getProcessState()))
                .filter(m -> keyword == null || keyword.isBlank()
                        || (m.getBbsTtl() != null && m.getBbsTtl().contains(keyword)))
                .toList();
        model.addAttribute("rows", rows);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("processState", processState);
        model.addAttribute("keyword", keyword);
        return "maintenance-admin/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        GSrMaintenance entity = new GSrMaintenance();
        entity.setReqCrtDate(LocalDateTime.now().format(YYYYMMDD));
        model.addAttribute("row", entity);
        model.addAttribute("files", List.of());
        return "maintenance-admin/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        GSrMaintenance entity = maintenanceRepository.findById(id).orElseThrow();
        entity.setInqCnt(entity.getInqCnt() == null ? 1L : entity.getInqCnt() + 1);
        maintenanceRepository.save(entity);
        model.addAttribute("row", entity);
        model.addAttribute("files", fileRepository.findByBbsIdOrderByAtchFileSeq(id));
        return "maintenance-admin/form";
    }

    @PostMapping
    public String create(@ModelAttribute GSrMaintenance form,
                          @RequestParam(required = false) MultipartFile file,
                          HttpSession session) {
        Manager manager = manager(session);
        form.setBbsId(null);
        form.setUseYn("Y");
        form.setUrgentYn("on".equals(form.getUrgentYn()) || "Y".equals(form.getUrgentYn()) ? "Y" : "N");
        form.setInqCnt(0L);
        form.setReqDt(LocalDateTime.now());
        form.setFrstCrtDt(LocalDateTime.now());
        form.setFrstCrtId(manager.getUserId());
        GSrMaintenance saved = maintenanceRepository.save(form);
        attachIfPresent(saved.getBbsId(), file, manager);
        return "redirect:/admin/maintenance";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @ModelAttribute GSrMaintenance form,
                          @RequestParam(required = false) MultipartFile file,
                          HttpSession session) {
        Manager manager = manager(session);
        GSrMaintenance entity = maintenanceRepository.findById(id).orElseThrow();
        entity.setKiType(form.getKiType());
        entity.setReqCrtDate(form.getReqCrtDate());
        entity.setReqUserPhoneNumber(form.getReqUserPhoneNumber());
        entity.setReqChannel(form.getReqChannel());
        entity.setReqType(form.getReqType());
        entity.setProcessType(form.getProcessType());
        entity.setBbsTtl(form.getBbsTtl());
        entity.setBbsCn(form.getBbsCn());
        entity.setReqUserInfo(form.getReqUserInfo());
        entity.setUrgentYn("on".equals(form.getUrgentYn()) || "Y".equals(form.getUrgentYn()) ? "Y" : "N");
        entity.setReqSource(form.getReqSource());
        entity.setProcessManagerNm(form.getProcessManagerNm());
        entity.setProcessCn(form.getProcessCn());
        entity.setProcessReceiptDate(form.getProcessReceiptDate());
        entity.setProcessTargetEndDate(form.getProcessTargetEndDate());
        entity.setProcessStartDate(form.getProcessStartDate());
        entity.setProcessEndDate(form.getProcessEndDate());
        entity.setProcessState(form.getProcessState());
        entity.setDeployDate(form.getDeployDate());
        entity.setRm(form.getRm());
        entity.setLastMdfcnId(manager.getUserId());
        entity.setLastMdfcnDt(LocalDateTime.now());
        maintenanceRepository.save(entity);
        attachIfPresent(id, file, manager);
        return "redirect:/admin/maintenance/" + id + "/edit";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        for (GSrMaintenanceFile f : fileRepository.findByBbsIdOrderByAtchFileSeq(id)) {
            fileStorageService.delete(f.getAtchFileNm(), SUBDIR);
        }
        fileRepository.deleteAll(fileRepository.findByBbsIdOrderByAtchFileSeq(id));
        maintenanceRepository.deleteById(id);
        return "redirect:/admin/maintenance";
    }

    @PostMapping("/{id}/files/{fileId}/delete")
    public String deleteFile(@PathVariable Long id, @PathVariable Long fileId) {
        fileRepository.findById(fileId).ifPresent(f -> {
            fileStorageService.delete(f.getAtchFileNm(), SUBDIR);
            fileRepository.deleteById(fileId);
        });
        return "redirect:/admin/maintenance/" + id + "/edit";
    }

    @GetMapping("/files/{fileId}/download")
    public ResponseEntity<Resource> download(@PathVariable Long fileId) throws IOException {
        GSrMaintenanceFile file = fileRepository.findById(fileId).orElseThrow();
        Resource resource = new UrlResource(fileStorageService.resolve(file.getAtchFileNm(), SUBDIR).toUri());
        String downloadName = file.getOrgnlAtchFileNm() != null ? file.getOrgnlAtchFileNm() : file.getAtchFileNm();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(downloadName, StandardCharsets.UTF_8).build().toString())
                .body(resource);
    }

    @GetMapping("/export")
    public void export(@RequestParam(required = false) String startDate,
                        @RequestParam(required = false) String endDate,
                        @RequestParam(required = false) String processState,
                        @RequestParam(required = false) String keyword,
                        HttpServletResponse response) throws IOException {
        List<GSrMaintenance> rows = maintenanceRepository.findAllByOrderByBbsIdDesc().stream()
                .filter(m -> startDate == null || startDate.isBlank()
                        || (m.getReqCrtDate() != null && m.getReqCrtDate().compareTo(startDate) >= 0))
                .filter(m -> endDate == null || endDate.isBlank()
                        || (m.getReqCrtDate() != null && m.getReqCrtDate().compareTo(endDate) <= 0))
                .filter(m -> processState == null || processState.isBlank() || processState.equals(m.getProcessState()))
                .filter(m -> keyword == null || keyword.isBlank()
                        || (m.getBbsTtl() != null && m.getBbsTtl().contains(keyword)))
                .toList();

        StringBuilder csv = new StringBuilder("﻿");
        csv.append("접수일,제목,요청경로,업무구분,완료구분,담당자,처리내용\n");
        for (GSrMaintenance m : rows) {
            csv.append(csvEscape(m.getReqCrtDate())).append(',')
                    .append(csvEscape(m.getBbsTtl())).append(',')
                    .append(csvEscape(m.getReqChannel())).append(',')
                    .append(csvEscape(m.getProcessType())).append(',')
                    .append(csvEscape(m.getProcessState())).append(',')
                    .append(csvEscape(m.getProcessManagerNm())).append(',')
                    .append(csvEscape(m.getProcessCn())).append('\n');
        }
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"maintenance.csv\"; filename*=UTF-8''"
                        + java.net.URLEncoder.encode("운영유지관리.csv", StandardCharsets.UTF_8));
        response.getOutputStream().write(csv.toString().getBytes(StandardCharsets.UTF_8));
    }

    private static String csvEscape(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }

    private void attachIfPresent(Long bbsId, MultipartFile file, Manager manager) {
        if (file == null || file.isEmpty()) {
            return;
        }
        String storedName = fileStorageService.store(file, SUBDIR);
        GSrMaintenanceFile f = new GSrMaintenanceFile();
        f.setBbsId(bbsId);
        f.setOrgnlAtchFileNm(file.getOriginalFilename());
        f.setAtchFileNm(storedName);
        f.setAtchFileExtnNm(storedName.contains(".") ? storedName.substring(storedName.lastIndexOf('.') + 1) : "");
        f.setAtchFileSz(file.getSize());
        int nextSeq = fileRepository.findByBbsIdOrderByAtchFileSeq(bbsId).size() + 1;
        f.setAtchFileSeq(nextSeq);
        f.setAtchFilePathNm(SUBDIR);
        f.setUseYn("Y");
        f.setFrstCrtId(manager.getUserId());
        f.setFrstCrtDt(LocalDateTime.now());
        fileRepository.save(f);
    }

    private static Manager manager(HttpSession session) {
        return (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
    }
}
