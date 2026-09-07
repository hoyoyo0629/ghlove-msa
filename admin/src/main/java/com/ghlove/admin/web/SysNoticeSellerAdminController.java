package com.ghlove.admin.web;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.domain.SysNoticeSeller;
import com.ghlove.admin.domain.SysNoticeSellerFile;
import com.ghlove.admin.repository.SysNoticeSellerFileRepository;
import com.ghlove.admin.repository.SysNoticeSellerRepository;
import com.ghlove.admin.service.AdminFileStorageService;
import com.ghlove.admin.service.ManagerException;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/** 판매자(답례품제공자) 전용 시스템공지 관리 (AS-IS opmanager/sellerNotice - SysNoticeSellerController).
 * AS-IS 실제 권한: ROLE_ADMIN_1/2(시스템 정·부담당자)만 등록/수정/삭제 가능, 나머지는 조회만 -
 * MenuService.UNRESTRICTED_ROLES(1~4)보다 더 좁은 범위라 이 컨트롤러에서 별도로 체크한다. */
@Controller
@RequestMapping("/admin/seller-notices")
@RequiredArgsConstructor
public class SysNoticeSellerAdminController {

    private static final String SUBDIR = "seller-notice";
    private static final Set<String> WRITE_ROLES = Set.of("ROLE_ADMIN_1", "ROLE_ADMIN_2");

    private final SysNoticeSellerRepository noticeRepository;
    private final SysNoticeSellerFileRepository fileRepository;
    private final AdminFileStorageService fileStorageService;

    @GetMapping
    public String list(@RequestParam(required = false) String keyword,
                        @RequestParam(required = false) String startDate,
                        @RequestParam(required = false) String endDate,
                        Model model) {
        List<SysNoticeSeller> rows = noticeRepository.findAllByOrderByNoticeIdDesc().stream()
                .filter(n -> keyword == null || keyword.isBlank()
                        || (n.getSubject() != null && n.getSubject().contains(keyword))
                        || (n.getContent() != null && n.getContent().contains(keyword)))
                .filter(n -> startDate == null || startDate.isBlank() || n.getFrstCrtDt() == null
                        || n.getFrstCrtDt().toLocalDate().toString().replace("-", "").compareTo(startDate.replace("-", "")) >= 0)
                .filter(n -> endDate == null || endDate.isBlank() || n.getFrstCrtDt() == null
                        || n.getFrstCrtDt().toLocalDate().toString().replace("-", "").compareTo(endDate.replace("-", "")) <= 0)
                .toList();
        model.addAttribute("notices", rows);
        model.addAttribute("keyword", keyword);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        return "seller-notice-admin/list";
    }

    @GetMapping("/new")
    public String newForm(HttpSession session, Model model) {
        requireWriteAccess(session);
        model.addAttribute("notice", new SysNoticeSeller());
        model.addAttribute("files", List.of());
        return "seller-notice-admin/form";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        SysNoticeSeller notice = noticeRepository.findById(id).orElseThrow();
        notice.setHits((notice.getHits() == null ? 0 : notice.getHits()) + 1);
        noticeRepository.save(notice);
        model.addAttribute("notice", notice);
        model.addAttribute("files", fileRepository.findByNoticeIdOrderByAtchFileSeq(id));
        model.addAttribute("readonly", true);
        return "seller-notice-admin/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, HttpSession session, Model model) {
        requireWriteAccess(session);
        model.addAttribute("notice", noticeRepository.findById(id).orElseThrow());
        model.addAttribute("files", fileRepository.findByNoticeIdOrderByAtchFileSeq(id));
        return "seller-notice-admin/form";
    }

    @PostMapping
    public String create(@ModelAttribute SysNoticeSeller form, @RequestParam(required = false) MultipartFile file,
                          HttpSession session) {
        Manager manager = requireWriteAccess(session);
        form.setNoticeId(null);
        form.setUseYn("Y");
        form.setDisplayFlag("Y");
        form.setHits(0L);
        form.setNoticeFlag("Y".equals(form.getNoticeFlag()) || "on".equals(form.getNoticeFlag()) ? "Y" : "N");
        form.setFrstCrtId(manager.getUserId());
        form.setFrstCrtDt(LocalDateTime.now());
        SysNoticeSeller saved = noticeRepository.save(form);
        attachIfPresent(saved.getNoticeId(), file, manager);
        return "redirect:/admin/seller-notices";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @ModelAttribute SysNoticeSeller form,
                          @RequestParam(required = false) MultipartFile file, HttpSession session) {
        Manager manager = requireWriteAccess(session);
        SysNoticeSeller notice = noticeRepository.findById(id).orElseThrow();
        notice.setSubject(form.getSubject());
        notice.setContent(form.getContent());
        notice.setNoticeFlag("Y".equals(form.getNoticeFlag()) || "on".equals(form.getNoticeFlag()) ? "Y" : "N");
        notice.setLastMdfcnId(manager.getUserId());
        notice.setLastMdfcnDt(LocalDateTime.now());
        noticeRepository.save(notice);
        attachIfPresent(id, file, manager);
        return "redirect:/admin/seller-notices/" + id + "/edit";
    }

    @PostMapping("/{id}/delete")
    @Transactional
    public String delete(@PathVariable Long id, HttpSession session) {
        requireWriteAccess(session);
        for (SysNoticeSellerFile f : fileRepository.findByNoticeIdOrderByAtchFileSeq(id)) {
            fileStorageService.delete(f.getAtchFileNm(), SUBDIR);
        }
        fileRepository.deleteAll(fileRepository.findByNoticeIdOrderByAtchFileSeq(id));
        noticeRepository.deleteById(id);
        return "redirect:/admin/seller-notices";
    }

    /** 목록 화면의 체크박스 일괄삭제. */
    @PostMapping("/bulk-delete")
    @Transactional
    public String bulkDelete(@RequestParam(required = false) List<Long> ids, HttpSession session) {
        requireWriteAccess(session);
        if (ids != null) {
            for (Long id : ids) {
                for (SysNoticeSellerFile f : fileRepository.findByNoticeIdOrderByAtchFileSeq(id)) {
                    fileStorageService.delete(f.getAtchFileNm(), SUBDIR);
                }
                fileRepository.deleteAll(fileRepository.findByNoticeIdOrderByAtchFileSeq(id));
            }
            noticeRepository.deleteAllById(ids);
        }
        return "redirect:/admin/seller-notices";
    }

    @PostMapping("/{id}/files/{fileId}/delete")
    public String deleteFile(@PathVariable Long id, @PathVariable Long fileId, HttpSession session) {
        requireWriteAccess(session);
        fileRepository.findById(fileId).ifPresent(f -> {
            fileStorageService.delete(f.getAtchFileNm(), SUBDIR);
            fileRepository.deleteById(fileId);
        });
        return "redirect:/admin/seller-notices/" + id + "/edit";
    }

    @GetMapping("/files/{fileId}/download")
    public ResponseEntity<Resource> download(@PathVariable Long fileId) throws IOException {
        SysNoticeSellerFile file = fileRepository.findById(fileId).orElseThrow();
        Resource resource = new UrlResource(fileStorageService.resolve(file.getAtchFileNm(), SUBDIR).toUri());
        String downloadName = file.getOrgnlAtchFileNm() != null ? file.getOrgnlAtchFileNm() : file.getAtchFileNm();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(downloadName, StandardCharsets.UTF_8).build().toString())
                .body(resource);
    }

    private void attachIfPresent(Long noticeId, MultipartFile file, Manager manager) {
        if (file == null || file.isEmpty()) {
            return;
        }
        String storedName = fileStorageService.store(file, SUBDIR);
        SysNoticeSellerFile f = new SysNoticeSellerFile();
        f.setNoticeId(noticeId);
        f.setOrgnlAtchFileNm(file.getOriginalFilename());
        f.setAtchFileNm(storedName);
        f.setAtchFileExtnNm(storedName.contains(".") ? storedName.substring(storedName.lastIndexOf('.') + 1) : "");
        f.setAtchFileSz(file.getSize());
        int nextSeq = fileRepository.findByNoticeIdOrderByAtchFileSeq(noticeId).size() + 1;
        f.setAtchFileSeq(nextSeq);
        f.setAtchFilePathNm(SUBDIR);
        f.setUseYn("Y");
        f.setFrstCrtId(manager.getUserId());
        f.setFrstCrtDt(LocalDateTime.now());
        fileRepository.save(f);
    }

    /** AS-IS 실제 권한: ROLE_ADMIN_1/2(시스템 정·부담당자)만 쓰기 가능. */
    private static Manager requireWriteAccess(HttpSession session) {
        Manager manager = (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
        if (manager == null || !WRITE_ROLES.contains(manager.getAuthority())) {
            throw new ManagerException("시스템 관리자(ROLE_ADMIN_1/2)만 게시글을 작성/수정/삭제할 수 있습니다.");
        }
        return manager;
    }
}
