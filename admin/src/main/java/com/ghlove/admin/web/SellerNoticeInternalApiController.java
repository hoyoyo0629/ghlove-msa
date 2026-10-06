package com.ghlove.admin.web;

import com.ghlove.admin.domain.SysNoticeSeller;
import com.ghlove.admin.domain.SysNoticeSellerFile;
import com.ghlove.admin.repository.SysNoticeSellerFileRepository;
import com.ghlove.admin.repository.SysNoticeSellerRepository;
import com.ghlove.admin.service.AdminFileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 판매자 공지사항 조회 내부 API - gift 서비스의 판매자 셀프포털(SellerNoticeController,
 * AS-IS /seller/sys-notice)이 호출한다. 판매자 공지 데이터(op_sys_notice_seller)는 admin이
 * 소유하므로, 운영자 관리화면(SysNoticeSellerAdminController)과 같은 원본을 판매자 조회용으로
 * 노출한다. gift→order(OrderAdminApiController)와 동일하게 공유시크릿(X-Internal-Secret)으로
 * 보호한다. AS-IS getFrontNoticeList처럼 노출(useYn/displayFlag != 'N')된 공지만 내려준다.
 */
@RestController
@RequestMapping("/api/seller-notices")
@RequiredArgsConstructor
public class SellerNoticeInternalApiController {

    private static final String SUBDIR = "seller-notice";
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final SysNoticeSellerRepository noticeRepository;
    private final SysNoticeSellerFileRepository fileRepository;
    private final AdminFileStorageService fileStorageService;

    @Value("${ghlove.internal.admin-secret}")
    private String adminSecret;

    private void requireInternal(String secret) {
        if (secret == null || !secret.equals(adminSecret)) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED);
        }
    }

    /** 노출 대상 공지인가 - useYn/displayFlag가 명시적으로 'N'이 아니면 노출(AS-IS front list). */
    private static boolean visible(SysNoticeSeller n) {
        return !"N".equals(n.getUseYn()) && !"N".equals(n.getDisplayFlag());
    }

    public record NoticeRow(Long noticeId, String subject, Long hits, String noticeFlag,
                            String frstCrtDt, int attachedFileCnt) {
    }

    public record NoticeFile(Long fileId, String fileName) {
    }

    public record NoticeDetail(Long noticeId, String subject, String content, Long hits,
                               String frstCrtDt, List<NoticeFile> files) {
    }

    @GetMapping
    public List<NoticeRow> list(@RequestHeader(value = "X-Internal-Secret", required = false) String secret,
                                @RequestParam(required = false) String keyword,
                                @RequestParam(required = false) String startDate,
                                @RequestParam(required = false) String endDate) {
        requireInternal(secret);
        String start = startDate == null ? null : startDate.replace("-", "");
        String end = endDate == null ? null : endDate.replace("-", "");
        return noticeRepository.findAllByOrderByNoticeIdDesc().stream()
                .filter(SellerNoticeInternalApiController::visible)
                .filter(n -> keyword == null || keyword.isBlank()
                        || (n.getSubject() != null && n.getSubject().contains(keyword)))
                .filter(n -> start == null || start.isBlank() || n.getFrstCrtDt() == null
                        || n.getFrstCrtDt().toLocalDate().toString().replace("-", "").compareTo(start) >= 0)
                .filter(n -> end == null || end.isBlank() || n.getFrstCrtDt() == null
                        || n.getFrstCrtDt().toLocalDate().toString().replace("-", "").compareTo(end) <= 0)
                .map(n -> new NoticeRow(n.getNoticeId(), n.getSubject(), n.getHits(), n.getNoticeFlag(),
                        n.getFrstCrtDt() != null ? n.getFrstCrtDt().format(DATE) : "",
                        fileRepository.findByNoticeIdOrderByAtchFileSeq(n.getNoticeId()).size()))
                .toList();
    }

    @GetMapping("/{noticeId}")
    @Transactional
    public NoticeDetail detail(@RequestHeader(value = "X-Internal-Secret", required = false) String secret,
                               @PathVariable Long noticeId) {
        requireInternal(secret);
        SysNoticeSeller n = noticeRepository.findById(noticeId)
                .filter(SellerNoticeInternalApiController::visible)
                .orElseThrow(() -> new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND));
        // 조회수 증가 (AS-IS sysNoticeSellerService.addHitCount)
        n.setHits((n.getHits() == null ? 0 : n.getHits()) + 1);
        noticeRepository.save(n);

        List<NoticeFile> files = fileRepository.findByNoticeIdOrderByAtchFileSeq(noticeId).stream()
                .map(f -> new NoticeFile(f.getFileId(),
                        f.getOrgnlAtchFileNm() != null ? f.getOrgnlAtchFileNm() : f.getAtchFileNm()))
                .toList();
        return new NoticeDetail(n.getNoticeId(), n.getSubject(), n.getContent(), n.getHits(),
                n.getFrstCrtDt() != null ? n.getFrstCrtDt().format(DATE) : "", files);
    }

    @GetMapping("/files/{fileId}")
    public ResponseEntity<Resource> download(@RequestHeader(value = "X-Internal-Secret", required = false) String secret,
                                             @PathVariable Long fileId) throws IOException {
        requireInternal(secret);
        SysNoticeSellerFile file = fileRepository.findById(fileId)
                .orElseThrow(() -> new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND));
        Resource resource = new UrlResource(fileStorageService.resolve(file.getAtchFileNm(), SUBDIR).toUri());
        String downloadName = file.getOrgnlAtchFileNm() != null ? file.getOrgnlAtchFileNm() : file.getAtchFileNm();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(downloadName, StandardCharsets.UTF_8).build().toString())
                .body(resource);
    }
}
