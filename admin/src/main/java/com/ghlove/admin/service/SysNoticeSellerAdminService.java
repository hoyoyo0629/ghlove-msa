package com.ghlove.admin.service;

import com.ghlove.admin.domain.SysNoticeSeller;
import com.ghlove.admin.domain.SysNoticeSellerFile;
import com.ghlove.admin.repository.SysNoticeSellerAdminRepository;
import com.ghlove.admin.repository.SysNoticeSellerFileRepository;
import com.ghlove.admin.repository.SysNoticeSellerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 답례품제공자 공지사항 (메뉴 5114) - AS-IS {@code SysNoticeSellerController} +
 * {@code SysNoticeSellerServiceImpl} 이식.
 *
 * <p><b>AS-IS 규칙</b>: 등록·수정·삭제는 ROLE_ADMIN_1·2만 할 수 있다(컨트롤러에서 막는다) ·
 * 삭제는 <b>소프트 삭제</b>({@code USE_YN='N'})이고 첨부도 마찬가지다 ·
 * 첨부는 확장자 18종/50MB, 저장명 {@code yyyyMMddHHmmssSSSS_원본명}이며
 * 수정은 기존 첨부를 지우지 않고 더한다(지우려면 첨부 옆 [x]).
 */
@Service
@RequiredArgsConstructor
public class SysNoticeSellerAdminService {

    public static final String SUBDIR = "seller-notice";

    /** AS-IS {@code AVAILABLE_EXTENSION} 18종 - 운영유지관리 SR게시판과 같다. */
    private static final java.util.Set<String> ALLOWED_EXTENSIONS = java.util.Set.of(
            "jpg", "jpeg", "gif", "bmp", "png", "doc", "docx", "xls", "xlsx",
            "ppt", "pptx", "pdf", "tif", "tiff", "hwp", "hwpx", "zip", "7z");
    private static final long MAX_SIZE = 50L * 1024 * 1024;

    private final SysNoticeSellerAdminRepository sellerNoticeAdminRepository;
    private final SysNoticeSellerRepository sellerNoticeRepository;
    private final SysNoticeSellerFileRepository fileRepository;
    private final AdminFileStorageService fileStorageService;

    public int count(String where, String query, String startDt, String endDt) {
        return sellerNoticeAdminRepository.count(where, query, startDt, endDt);
    }

    public List<SysNoticeSellerAdminRepository.Row> list(String where, String query, String startDt, String endDt,
                                                          int offset, int limit) {
        return sellerNoticeAdminRepository.list(where, query, startDt, endDt, offset, limit);
    }

    /** AS-IS는 없는 글이면 "존재하지 않는 게시글입니다."로 목록에 되돌린다. */
    public SysNoticeSeller notice(Long noticeId) {
        return sellerNoticeRepository.findById(noticeId)
                .filter(n -> !"N".equals(n.getUseYn()))
                .orElseThrow(() -> new ContentException("존재하지 않는 게시글입니다."));
    }

    /** 첨부도 소프트 삭제라 살아 있는 것만 보여준다. */
    public List<SysNoticeSellerFile> filesOf(Long noticeId) {
        return fileRepository.findByNoticeIdOrderByAtchFileSeq(noticeId).stream()
                .filter(f -> !"N".equals(f.getUseYn()))
                .toList();
    }

    @Transactional
    public SysNoticeSeller create(String subject, String content, String noticeFlag, String displayFlag,
                                  MultipartFile[] files, Long managerId) {
        if (subject == null || subject.isBlank()) {
            throw new ContentException("제목을 입력해 주세요.");
        }
        SysNoticeSeller entity = new SysNoticeSeller();
        entity.setSubject(subject);
        entity.setContent(content);
        entity.setNoticeFlag("Y".equals(noticeFlag) ? "Y" : "N");
        entity.setDisplayFlag(displayFlag);
        entity.setHits(0L);
        entity.setUseYn("Y");
        entity.setFrstCrtId(managerId);
        entity.setFrstCrtDt(LocalDateTime.now());
        SysNoticeSeller saved = sellerNoticeRepository.save(entity);

        attachAll(saved.getNoticeId(), files, managerId);
        return saved;
    }

    @Transactional
    public SysNoticeSeller update(Long noticeId, String subject, String content, String noticeFlag,
                                  String displayFlag, MultipartFile[] files, Long managerId) {
        if (subject == null || subject.isBlank()) {
            throw new ContentException("제목을 입력해 주세요.");
        }
        SysNoticeSeller entity = notice(noticeId);
        entity.setSubject(subject);
        entity.setContent(content);
        entity.setNoticeFlag("Y".equals(noticeFlag) ? "Y" : "N");
        entity.setDisplayFlag(displayFlag);
        entity.setLastMdfcnId(managerId);
        entity.setLastMdfcnDt(LocalDateTime.now());
        sellerNoticeRepository.save(entity);

        attachAll(noticeId, files, managerId);
        return entity;
    }

    /** AS-IS {@code deleteNotice} - 하드 삭제가 아니라 {@code USE_YN='N'}이다. */
    @Transactional
    public void delete(Long noticeId, Long managerId) {
        SysNoticeSeller entity = notice(noticeId);
        entity.setUseYn("N");
        entity.setLastMdfcnId(managerId);
        entity.setLastMdfcnDt(LocalDateTime.now());
    }

    /**
     * 목록의 선택삭제용. <b>AS-IS는 화면 JS가 {@code /opmanager/sellerNotice/deleteNotice}로
     * 보내는데 그 엔드포인트가 컨트롤러에 없어 404로 실패한다</b>(버튼이 아무 일도 못 한다).
     * 단건 삭제와 같은 규칙으로 동작하게 보정했다.
     */
    @Transactional
    public String deleteList(List<Long> noticeIds, Long managerId) {
        if (noticeIds == null || noticeIds.isEmpty()) {
            return "FAIL";
        }
        int deleted = 0;
        for (Long id : noticeIds) {
            try {
                delete(id, managerId);
                deleted++;
            } catch (ContentException ignored) {
                // 이미 지워졌거나 없는 글은 건너뛴다
            }
        }
        return deleted == noticeIds.size() ? "SUCC" : "FAIL";
    }

    /** AS-IS {@code deleteNoticeFileByFileId} - 파일도 소프트 삭제다(디스크 파일은 남는다). */
    @Transactional
    public void deleteFile(Long fileId, Long managerId) {
        fileRepository.findById(fileId).ifPresent(f -> {
            f.setUseYn("N");
            f.setLastMdfcnId(managerId);
            f.setLastMdfcnDt(LocalDateTime.now());
        });
    }

    public SysNoticeSellerFile file(Long fileId) {
        return fileRepository.findById(fileId)
                .orElseThrow(() -> new ContentException("첨부파일이 존재하지 않습니다."));
    }

    private void attachAll(Long noticeId, MultipartFile[] files, Long managerId) {
        if (files == null) {
            return;
        }
        for (MultipartFile file : files) {
            if (file == null || file.getOriginalFilename() == null || file.getOriginalFilename().isEmpty()) {
                continue;
            }
            String storedName = fileStorageService.store(file, SUBDIR, ALLOWED_EXTENSIONS, MAX_SIZE, true);

            SysNoticeSellerFile entity = new SysNoticeSellerFile();
            entity.setFileId(sellerNoticeAdminRepository.nextFileId());
            entity.setNoticeId(noticeId);
            entity.setOrgnlAtchFileNm(file.getOriginalFilename());
            entity.setAtchFileNm(storedName);
            entity.setAtchFileExtnNm(extensionOf(file.getOriginalFilename()));
            entity.setAtchFileSz(file.getSize());
            entity.setAtchFileSeq(nextSeq(noticeId));
            entity.setAtchFilePathNm(SUBDIR);
            entity.setUseYn("Y");
            entity.setFrstCrtId(managerId);
            entity.setFrstCrtDt(LocalDateTime.now());
            fileRepository.save(entity);
        }
    }

    private int nextSeq(Long noticeId) {
        return fileRepository.findByNoticeIdOrderByAtchFileSeq(noticeId).stream()
                .map(SysNoticeSellerFile::getAtchFileSeq)
                .filter(java.util.Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0) + 1;
    }

    private static String extensionOf(String originalFilename) {
        int dot = originalFilename.lastIndexOf('.');
        return dot < 0 ? "" : originalFilename.substring(dot + 1).toLowerCase();
    }
}
