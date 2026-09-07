package com.ghlove.gift.web;

import com.ghlove.gift.domain.OpMobileCategoryEdit;
import com.ghlove.gift.repository.OpMobileCategoryEditRepository;
import com.ghlove.gift.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** 모바일 카테고리/메인 화면 레이아웃 편집 (AS-IS opmanager/mobile-category-edit -
 *  MobileCategoriesEditManagerController) cross-service API - admin 콘솔
 *  (/admin/mobile-category-edit)이 사용한다. */
@RestController
@RequestMapping("/api/admin/mobile-category-edit")
@RequiredArgsConstructor
public class MobileCategoryEditAdminApiController {

    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final OpMobileCategoryEditRepository repository;
    private final FileStorageService fileStorageService;

    @GetMapping
    public List<OpMobileCategoryEdit> list(@RequestParam(required = false) String code) {
        if (code != null && !code.isBlank()) {
            return repository.findByCodeOrderByEditPosition(code);
        }
        return repository.findAllByOrderByCodeAscEditPositionAsc();
    }

    @GetMapping("/{id}")
    public OpMobileCategoryEdit get(@PathVariable Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "편집 항목을 찾을 수 없습니다."));
    }

    @PostMapping
    public OpMobileCategoryEdit create(@RequestParam String code, @RequestParam String editKind,
                                        @RequestParam String editPosition,
                                        @RequestParam(required = false) String editContent,
                                        @RequestParam(required = false) String editUrl,
                                        @RequestParam(required = false) MultipartFile editImage) {
        OpMobileCategoryEdit row = new OpMobileCategoryEdit();
        row.setCode(code);
        row.setEditKind(editKind);
        row.setEditPosition(editPosition);
        row.setEditContent(editContent);
        row.setEditUrl(editUrl);
        if (editImage != null && !editImage.isEmpty()) {
            row.setEditImage(fileStorageService.store(editImage));
        }
        String now = LocalDateTime.now().format(TS);
        row.setCreatedDate(now);
        row.setUpdatedDate(now);
        return repository.save(row);
    }

    @PutMapping("/{id}")
    public OpMobileCategoryEdit update(@PathVariable Integer id, @RequestParam String code, @RequestParam String editKind,
                                        @RequestParam String editPosition,
                                        @RequestParam(required = false) String editContent,
                                        @RequestParam(required = false) String editUrl,
                                        @RequestParam(required = false) MultipartFile editImage) {
        OpMobileCategoryEdit row = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "편집 항목을 찾을 수 없습니다."));
        row.setCode(code);
        row.setEditKind(editKind);
        row.setEditPosition(editPosition);
        row.setEditContent(editContent);
        row.setEditUrl(editUrl);
        if (editImage != null && !editImage.isEmpty()) {
            row.setEditImage(fileStorageService.store(editImage));
        }
        row.setUpdatedDate(LocalDateTime.now().format(TS));
        return repository.save(row);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        repository.deleteById(id);
    }
}
