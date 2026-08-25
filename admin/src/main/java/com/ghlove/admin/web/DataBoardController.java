package com.ghlove.admin.web;

import com.ghlove.admin.domain.DataBoard;
import com.ghlove.admin.domain.DataBoardFile;
import com.ghlove.admin.service.DataBoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 고객센터 자료실 (AS-IS data-board/list.html, detail.html) - 로그인 불필요, 공개 화면. */
@Controller
@RequiredArgsConstructor
public class DataBoardController {

    private final DataBoardService dataBoardService;

    @Value("${admin.upload.dir}")
    private String uploadDir;

    private static final int PAGE_SIZE_DEFAULT = 10;

    @GetMapping("/data-board")
    public String list(@RequestParam(required = false, defaultValue = "SUBJECT") String where,
                        @RequestParam(required = false) String q,
                        @RequestParam(required = false, defaultValue = "CREATED_DATE__DESC") String sort,
                        @RequestParam(required = false, defaultValue = "10") int size,
                        @RequestParam(required = false, defaultValue = "1") int page,
                        Model model) {
        var all = dataBoardService.search(where, q, sort);
        int pageSize = size > 0 ? size : PAGE_SIZE_DEFAULT;
        int totalPages = (int) Math.ceil(all.size() / (double) pageSize);
        int currentPage = Math.max(1, Math.min(page, Math.max(totalPages, 1)));
        int from = Math.min((currentPage - 1) * pageSize, all.size());
        int to = Math.min(from + pageSize, all.size());
        List<DataBoard> pageItems = all.subList(from, to);

        Map<Integer, String> fileTypes = new HashMap<>();
        Map<Integer, String> displayDates = new HashMap<>();
        for (DataBoard b : pageItems) {
            dataBoardService.filesOf(b.getDataId()).stream().findFirst()
                    .ifPresent(f -> fileTypes.put(b.getDataId(), f.getFileTy()));
            displayDates.put(b.getDataId(), formatDate(b.getCreatedDate()));
        }

        model.addAttribute("boards", pageItems);
        model.addAttribute("fileTypes", fileTypes);
        model.addAttribute("displayDates", displayDates);
        model.addAttribute("totalCount", all.size());
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("where", where);
        model.addAttribute("q", q);
        model.addAttribute("sort", sort);
        model.addAttribute("size", pageSize);
        return "data-board/list";
    }

    @GetMapping("/data-board/{id}")
    public String detail(@PathVariable Integer id, Model model) {
        DataBoard board = dataBoardService.findOrThrow(id);
        dataBoardService.addHit(id);
        model.addAttribute("board", board);
        model.addAttribute("displayDate", formatDate(board.getCreatedDate()));
        model.addAttribute("files", dataBoardService.filesOf(id));
        return "data-board/detail";
    }

    /** CREATED_DATE는 yyyyMMddHHmmss로 저장되므로 AS-IS 목록/상세와 동일하게 yyyy-MM-dd로 표시한다. */
    private static String formatDate(String createdDate) {
        if (createdDate == null || createdDate.length() < 8) {
            return "";
        }
        return createdDate.substring(0, 4) + "-" + createdDate.substring(4, 6) + "-" + createdDate.substring(6, 8);
    }

    @GetMapping("/data-board/file-download/{fileId}")
    public ResponseEntity<Resource> download(@PathVariable String fileId) throws IOException {
        DataBoardFile file = dataBoardService.fileOrThrow(fileId);
        Path target = Paths.get(uploadDir, "data-board", file.getFileName());
        Resource resource = new UrlResource(target.toUri());
        String downloadName = file.getOrgFileName() != null ? file.getOrgFileName() : file.getFileName();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(downloadName, StandardCharsets.UTF_8)
                                .build().toString())
                .body(resource);
    }
}
