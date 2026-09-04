package com.ghlove.admin.web;

import com.ghlove.admin.domain.DataBoard;
import com.ghlove.admin.domain.DataBoardFile;
import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.repository.DataBoardFileRepository;
import com.ghlove.admin.repository.DataBoardRepository;
import com.ghlove.admin.service.DataBoardFileStorageService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/** 고객센터 자료실 관리자 CRUD (AS-IS opmanager/data-board - DataboardManagerController).
 * 공개 화면(DataBoardController, /data-board)은 조회 전용이라 이 화면이 없으면 등록된
 * 게시물을 새로 올릴 방법이 없었다. */
@Controller
@RequestMapping("/admin/data-board")
@RequiredArgsConstructor
public class DataBoardAdminController {

    private static final DateTimeFormatter CREATED_DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final DataBoardRepository dataBoardRepository;
    private final DataBoardFileRepository dataBoardFileRepository;
    private final DataBoardFileStorageService fileStorageService;

    @GetMapping
    public String list(Model model) {
        List<DataBoard> boards = dataBoardRepository.findAllByOrderByDataIdDesc();
        model.addAttribute("boards", boards);
        return "data-board-admin/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("board", new DataBoard());
        model.addAttribute("files", List.of());
        return "data-board-admin/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Integer id, Model model) {
        DataBoard board = dataBoardRepository.findById(id).orElseThrow();
        model.addAttribute("board", board);
        model.addAttribute("files", dataBoardFileRepository.findByDataIdOrderByOrdering(id));
        return "data-board-admin/form";
    }

    @PostMapping
    public String create(@ModelAttribute DataBoard form,
                          @RequestParam(required = false) MultipartFile file,
                          HttpSession session) {
        Manager manager = manager(session);
        form.setDataId(null);
        form.setUserName(manager.getUserName());
        form.setHits(0);
        form.setUseYn("Y");
        form.setNoticeFlag("Y".equals(form.getNoticeFlag()) || "on".equals(form.getNoticeFlag()) ? "Y" : "N");
        form.setCreatedDate(LocalDateTime.now().format(CREATED_DATE_FMT));
        DataBoard saved = dataBoardRepository.save(form);
        attachIfPresent(saved.getDataId(), file);
        return "redirect:/admin/data-board";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Integer id, @ModelAttribute DataBoard form,
                          @RequestParam(required = false) MultipartFile file) {
        DataBoard board = dataBoardRepository.findById(id).orElseThrow();
        board.setSubject(form.getSubject());
        board.setContent(form.getContent());
        board.setNoticeFlag(form.getNoticeFlag() != null ? "Y" : "N");
        dataBoardRepository.save(board);
        attachIfPresent(id, file);
        return "redirect:/admin/data-board/" + id + "/edit";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Integer id) {
        for (DataBoardFile f : dataBoardFileRepository.findByDataIdOrderByOrdering(id)) {
            fileStorageService.delete(f.getFileName());
        }
        dataBoardFileRepository.deleteAll(dataBoardFileRepository.findByDataIdOrderByOrdering(id));
        dataBoardRepository.deleteById(id);
        return "redirect:/admin/data-board";
    }

    @PostMapping("/{id}/files/{fileId}/delete")
    public String deleteFile(@PathVariable Integer id, @PathVariable String fileId) {
        dataBoardFileRepository.findById(fileId).ifPresent(f -> {
            fileStorageService.delete(f.getFileName());
            dataBoardFileRepository.deleteById(fileId);
        });
        return "redirect:/admin/data-board/" + id + "/edit";
    }

    private void attachIfPresent(Integer dataId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return;
        }
        String storedName = fileStorageService.store(file);
        DataBoardFile f = new DataBoardFile();
        f.setDataFileId(UUID.randomUUID().toString());
        f.setDataId(dataId);
        f.setFileName(storedName);
        f.setOrgFileName(file.getOriginalFilename());
        f.setFileTy(storedName.contains(".") ? storedName.substring(storedName.lastIndexOf('.') + 1) : "");
        int nextOrdering = dataBoardFileRepository.findByDataIdOrderByOrdering(dataId).size() + 1;
        f.setOrdering(nextOrdering);
        f.setCreatedDate(LocalDateTime.now().format(CREATED_DATE_FMT));
        dataBoardFileRepository.save(f);
    }

    private static Manager manager(HttpSession session) {
        return (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
    }
}
