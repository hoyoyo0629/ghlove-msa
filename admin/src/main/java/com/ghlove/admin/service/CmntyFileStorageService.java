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
import java.util.Locale;

/**
 * 커뮤니티 게시판 첨부파일 저장 - AS-IS {@code CmntyServiceImpl.fileUploadHandler*} 5곳이
 * 똑같이 반복하던 규칙을 한 곳에 모은 것이다(SR·담당자FAQ·오프라인SR의 본문/댓글 첨부, 자료실).
 *
 * <p><b>AS-IS 규칙 그대로</b>:
 * <ul>
 *   <li>최대 용량 <b>50MB</b>. 넘으면 "업로드 가능한 최대 용량 : 50MB 입니다"</li>
 *   <li>확장자 화이트리스트 18종. 아니면 "유효하지 않은 파일입니다."
 *       (AS-IS는 확장자 일치 검사와 소문자 {@code endsWith} 검사를 두 번 하는데 같은 목록이다)</li>
 *   <li>저장 파일명은 <b>{@code yyyyMMddHHmmssSSS_원본파일명}</b> - 원본 이름을 그대로 뒤에 붙인다</li>
 * </ul>
 *
 * <p>기존 {@link AdminFileStorageService}(20MB·UUID 파일명·화이트리스트 없음)를 쓰지 않는 이유는
 * 이 세 가지가 AS-IS 화면의 안내문구·동작과 직접 묶여 있어서다(화면에 "50MB 이하 / jp(e)g, png,
 * ppt(x), ..." 가 그대로 적혀 있다).
 *
 * <p><b>AS-IS보다 조여 둔 것 1건(보안)</b>: AS-IS는 원본 파일명을 저장명에 그대로 이어 붙이므로
 * 파일명에 경로 구분자({@code ../})가 들어오면 업로드 경로를 벗어날 수 있다. 여기서는 파일명에서
 * 경로 부분을 떼고 남은 이름만 쓴다 - 정상 파일명은 결과가 완전히 같다.
 */
@Service
@Slf4j
public class CmntyFileStorageService {

    /** AS-IS AVAILABLE_EXTENSION 18종(순서까지 그대로). */
    private static final List<String> AVAILABLE_EXTENSION = List.of(
            "jpg", "jpeg", "gif", "bmp", "png", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
            "pdf", "tif", "tiff", "hwp", "hwpx", "zip", "7z");

    /** AS-IS maxSize = 50 * 1024 * 1024. */
    private static final long MAX_SIZE = 50L * 1024 * 1024;

    private static final DateTimeFormatter SAVE_NAME_TS = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    @Value("${admin.upload.dir}")
    private String uploadDir;

    /** 저장 결과 - DB 컬럼에 그대로 들어가는 값들. */
    public record Stored(String orgnlAtchFileNm, String atchFileNm, String atchFileExtnNm,
                         long atchFileSz, String atchFilePathNm) {
    }

    /**
     * AS-IS 업로드 핸들러 한 건 분량. {@code subdir}이 AS-IS {@code dto.getUploadPath()} 자리다.
     *
     * @return 저장된 파일 정보, 빈 파일이면 {@code null}(AS-IS도 파일명이 비면 건너뛴다)
     */
    public Stored store(MultipartFile file, String subdir) {
        if (file == null || file.isEmpty()
                || file.getOriginalFilename() == null || file.getOriginalFilename().isBlank()) {
            return null;
        }

        String fileName = baseName(file.getOriginalFilename());
        String extension = extensionOf(fileName);

        // AS-IS 순서: 확장자 검사 → 용량 검사
        if (!AVAILABLE_EXTENSION.contains(extension)) {
            throw new CmntyException("유효하지 않은 파일입니다.");
        }
        if (file.getSize() >= MAX_SIZE) {
            throw new CmntyException("업로드 가능한 최대 용량 : 50MB 입니다");
        }

        String saveFileName = LocalDateTime.now().format(SAVE_NAME_TS) + "_" + fileName;
        try {
            Path dir = Paths.get(uploadDir, subdir);
            Files.createDirectories(dir);
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, dir.resolve(saveFileName), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            log.error("커뮤니티 첨부파일 저장 실패 subdir={}", subdir, e);
            throw new CmntyException("파일 저장에 실패했습니다.");
        }
        return new Stored(fileName, saveFileName, extension, file.getSize(), subdir);
    }

    public Path resolve(String atchFileNm, String subdir) {
        return Paths.get(uploadDir, subdir, atchFileNm);
    }

    /**
     * AS-IS 상세화면의 파일 크기 표기 - 1KB 미만은 B, 1MB 이하는 KB, 그 위는 MB다(정수 나눗셈).
     * AS-IS 경계 조건({@code bytes >= 1024 && bytes <= 1024*1024})을 그대로 옮겼다.
     */
    public static String formatSize(Long bytes) {
        if (bytes == null) {
            return "";
        }
        if (bytes < 1024) {
            return bytes + "B";
        }
        if (bytes <= 1024 * 1024) {
            return (bytes / 1024) + "KB";
        }
        return (bytes / 1024 / 1024) + "MB";
    }

    /** 경로 구분자를 떼고 파일명만 남긴다(윈도·유닉스 양쪽). */
    private static String baseName(String originalFilename) {
        String name = originalFilename.replace('\\', '/');
        int slash = name.lastIndexOf('/');
        return slash < 0 ? name : name.substring(slash + 1);
    }

    /** AS-IS FileUtils.getExtension - 마지막 '.' 뒤(소문자 비교를 위해 소문자로). */
    private static String extensionOf(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot < 0 ? "" : fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}
