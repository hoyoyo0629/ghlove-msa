package com.ghlove.admin.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 매뉴얼 첨부파일 저장 - AS-IS {@code ManualServiceImpl}의 업로드 규칙을 그대로 옮긴 것이다.
 * 사용자매뉴얼(5202)과 관리자매뉴얼(5201)이 같은 규칙·같은 폴더를 쓴다.
 *
 * <p><b>AS-IS 그대로</b>:
 * <ul>
 *   <li>허용 확장자 <b>16종</b>: jpg jpeg gif bmp png doc docx xls xlsx ppt pptx pdf tif tiff hwp zip
 *       - 커뮤니티 첨부(18종, hwpx·7z 포함)와 <b>다르다</b>. 각 화면의 목록을 그대로 쓴다.</li>
 *   <li>최대 <b>50MB</b>. 넘으면 "업로드 가능한 최대 용량 : 50MB 입니다".</li>
 *   <li>저장 파일명은 {@code yyyyMMddHHmmssSSSS_원본파일명}({@code Const.DATENANO_FORMAT}).</li>
 *   <li>저장 폴더는 업로드 폴더 아래 <b>{@code help}</b>({@code Manual.getUploadPath}).</li>
 *   <li>확장자 검사를 두 번 한다(확장자 목록 + 파일명 endsWith, 시큐어코딩 CWE-434 주석).
 *       둘 다 실패 문구가 "유효하지 않은 파일입니다."다.</li>
 * </ul>
 *
 * <p><b>보안 편차(이 프로젝트 고정 방침)</b>: 원본 파일명에서 경로 요소를 떼고 쓴다.
 * AS-IS는 {@code getOriginalFilename()}을 그대로 이어 붙여 경로 조작 여지가 있다.
 */
@Service
@Slf4j
public class ManualFileStorageService {

    /** AS-IS AVAILABLE_EXTENSION (매뉴얼 화면 전용 목록). */
    private static final List<String> AVAILABLE_EXTENSION = List.of(
            "jpg", "jpeg", "gif", "bmp", "png", "doc", "docx", "xls", "xlsx",
            "ppt", "pptx", "pdf", "tif", "tiff", "hwp", "zip");
    private static final long MAX_SIZE = 50L * 1024 * 1024;
    /** AS-IS Const.DATENANO_FORMAT. */
    private static final DateTimeFormatter SAVE_NAME_TS = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSSS");
    /** AS-IS Manual.getUploadPath() - 업로드 폴더 아래 help. */
    private static final String SUBDIR = "help";

    private final String uploadDir;

    public ManualFileStorageService(@Value("${admin.upload.dir}") String uploadDir) {
        this.uploadDir = uploadDir;
    }

    /** @return 저장된 파일명(DB의 {@code file_nm}에 들어가는 값) */
    public String store(MultipartFile file) {
        String original = baseName(file.getOriginalFilename());
        String extension = extensionOf(original);

        if (!AVAILABLE_EXTENSION.contains(extension)) {
            throw new ManualException("유효하지 않은 파일입니다.");
        }
        if (file.getSize() >= MAX_SIZE) {
            // AS-IS 조건이 maxSize > size라 정확히 50MB면 통과하지 못한다 - 그 경계도 그대로다
            throw new ManualException("업로드 가능한 최대 용량 : 50MB 입니다");
        }

        String saveFileName = LocalDateTime.now().format(SAVE_NAME_TS) + "_" + original;
        try {
            Path dir = Paths.get(uploadDir, SUBDIR);
            Files.createDirectories(dir);
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, dir.resolve(saveFileName), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            log.error("매뉴얼 첨부 저장 실패", e);
            throw new ManualException("파일 저장에 실패했습니다.");
        }
        return saveFileName;
    }

    public String extensionOf(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    }

    public Path resolve(String storedName) {
        return Paths.get(uploadDir, SUBDIR, storedName);
    }

    /** AS-IS deleteItemImageByItemId는 DB 행을 비우기 전에 디스크 파일을 먼저 지운다. */
    public void delete(String storedName) {
        if (storedName == null || storedName.isBlank()) {
            return;
        }
        try {
            Files.deleteIfExists(resolve(storedName));
        } catch (IOException e) {
            log.warn("매뉴얼 첨부 삭제 실패: {}", storedName, e);
        }
    }

    private static String baseName(String fileName) {
        if (fileName == null) {
            return "";
        }
        String name = fileName.replace('\\', '/');
        int slash = name.lastIndexOf('/');
        return slash < 0 ? name : name.substring(slash + 1);
    }
}
