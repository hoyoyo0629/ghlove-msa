package com.ghlove.admin.web;

import com.ghlove.admin.domain.Manual;
import com.ghlove.admin.repository.ManualRepository;
import com.ghlove.admin.service.AdminFileStorageService;
import jakarta.servlet.http.HttpServletResponse;
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
import java.util.List;

/** 사용자매뉴얼 관리 (AS-IS opmanager/manual/user/* - ManualManagerController).
 * AS-IS 원본 DB에 대응 테이블이 없어(신규 설계) OP_MANUAL을 이 라운드에서 새로 만들었다.
 * 목록/등록/수정/삭제 + 첨부파일 1건 업로드/다운로드. */
@Controller
@RequestMapping("/admin/manuals")
@RequiredArgsConstructor
public class ManualAdminController {

    private static final String SUBDIR = "manual";

    private final ManualRepository manualRepository;
    private final AdminFileStorageService fileStorageService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("manuals", manualRepository.findAllByOrderByMnlSnDesc());
        return "manual-admin/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("manual", new Manual());
        return "manual-admin/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("manual", manualRepository.findById(id).orElseThrow());
        return "manual-admin/form";
    }

    @PostMapping
    public String create(@ModelAttribute Manual form, @RequestParam(required = false) MultipartFile file) {
        form.setMnlSn(null);
        form.setUseYn("Y");
        form.setCreatedDate(LocalDateTime.now());
        attachIfPresent(form, file);
        manualRepository.save(form);
        return "redirect:/admin/manuals";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @ModelAttribute Manual form,
                          @RequestParam(required = false) MultipartFile file) {
        Manual manual = manualRepository.findById(id).orElseThrow();
        manual.setTitle(form.getTitle());
        manual.setContent(form.getContent());
        manual.setMenuUrlCode(form.getMenuUrlCode());
        attachIfPresent(manual, file);
        manualRepository.save(manual);
        return "redirect:/admin/manuals/" + id + "/edit";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        manualRepository.findById(id).ifPresent(m -> {
            if (m.getFileSrc() != null && !m.getFileSrc().isBlank()) {
                fileStorageService.delete(m.getFileSrc(), SUBDIR);
            }
        });
        manualRepository.deleteById(id);
        return "redirect:/admin/manuals";
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id) throws IOException {
        Manual manual = manualRepository.findById(id).orElseThrow();
        if (manual.getFileSrc() == null || manual.getFileSrc().isBlank()) {
            return ResponseEntity.notFound().build();
        }
        Resource resource = new UrlResource(fileStorageService.resolve(manual.getFileSrc(), SUBDIR).toUri());
        String downloadName = manual.getOrgnlFileNm() != null ? manual.getOrgnlFileNm() : manual.getFileSrc();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(downloadName, StandardCharsets.UTF_8).build().toString())
                .body(resource);
    }

    private void attachIfPresent(Manual manual, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return;
        }
        String storedName = fileStorageService.store(file, SUBDIR);
        manual.setFileSrc(storedName);
        manual.setOrgnlFileNm(file.getOriginalFilename());
    }
}
