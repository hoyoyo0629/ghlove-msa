package com.ghlove.donation.web;

import com.ghlove.donation.domain.CtbnyOpratnFile;
import com.ghlove.donation.service.CtbnyOpratnService;
import com.ghlove.donation.service.DonationException;
import com.ghlove.donation.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/** 기부금 지출내역 증빙서류 다운로드 - 브라우저(admin 콘솔)에서 직접 링크로 접근한다
 *  (admin의 DataBoardController 다운로드 패턴과 동일). */
@Controller
@RequiredArgsConstructor
public class CtbnyOpratnFileController {

    private final CtbnyOpratnService ctbnyOpratnService;
    private final FileStorageService fileStorageService;

    @GetMapping("/ctbny-opratn/file-download/{fileId}")
    public ResponseEntity<Resource> download(@PathVariable Long fileId) throws IOException {
        CtbnyOpratnFile file = ctbnyOpratnService.file(fileId)
                .orElseThrow(() -> new DonationException("파일을 찾을 수 없습니다."));
        Resource resource = new UrlResource(fileStorageService.resolve(file.getFileNm()).toUri());
        String downloadName = file.getOrginlFileNm() != null ? file.getOrginlFileNm() : file.getFileNm();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(downloadName, StandardCharsets.UTF_8)
                                .build().toString())
                .body(resource);
    }
}
