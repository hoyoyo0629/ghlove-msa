package com.ghlove.admin.web;

import com.ghlove.admin.service.AdminFileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AS-IS {@code saleson.common.module.smarteditor.SmartEditorController} 이식 -
 * 스마트에디터 툴바의 <b>사진 / 동영상 / CTP(템플릿)</b> 버튼이 여는 팝업 3종이다.
 * 스킨(SmartEditor2Skin_ko.html)이 노출하는 플러그인이 정확히 이 셋이라
 * ({@code hp_SE2M_AttachQuickPhoto} / {@code hp_SE2M_MOVIE} / {@code hp_SE2M_CTP})
 * 세 경로가 다 있어야 버튼이 동작한다. TO-BE에는 아예 없어서 전부 오류였다.
 *
 * <p>AS-IS 규칙(컨트롤러 본문 그대로):
 * <ul>
 *   <li>이미지: 파라미터는 {@code file[]}(다건). <b>jpg·jpeg·png·gif만</b> 통과시키고
 *       (확장자가 아닌 파일은 조용히 버린다) <b>건당 5MB</b>를 넘으면 파일명을 모아
 *       "... 파일은 용량 제한을 넘어 업로드에 실패했습니다."로 알린다.
 *       결과는 {@code <p><img src="..." alt="" /></p>}를 이어붙인 HTML이다.</li>
 *   <li>동영상: <b>업로드가 아니다</b> - GET 뿐이고 화면 JS가 youtu.be 주소를
 *       youtube.com/embed로 바꿔 iframe을 끼워 넣는다(서버 저장 없음).</li>
 *   <li>CTP: html·ctp·css·jpg·jpeg·png·gif, 5MB, <b>html(ctp) 파일이 반드시 1개 이상</b>.
 *       html 본문의 {@code <body>}~{@code </body>}만 취하고 같이 올린 이미지의 상대경로를
 *       저장경로로 치환한다. 오류는 ERROR_01/02/03 코드로 화면이 문구를 만든다.</li>
 * </ul>
 */
@Controller
@RequestMapping("/smarteditor")
@RequiredArgsConstructor
@Slf4j
public class SmartEditorController {

    /** AS-IS IMG_SIZE_LIMIT - 이미지 1건당 제한. */
    private static final long IMG_SIZE_LIMIT = 5 * 1024 * 1024;
    /** AS-IS isImageFile - 이 확장자가 아니면 업로드 대상에서 제외한다. */
    private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif");
    /** AS-IS uploadPosibleExtensions(CTP). */
    private static final Set<String> CTP_EXTENSIONS = Set.of("html", "ctp", "css", "jpg", "jpeg", "png", "gif");
    /** AS-IS uploadSizeLimit = "5" (MB). */
    private static final String CTP_SIZE_LIMIT_MB = "5";
    private static final Set<String> CTP_DOCUMENT_EXTENSIONS = Set.of("html", "ctp");
    private static final DateTimeFormatter FOLDER_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final Pattern BODY_PATTERN =
            Pattern.compile("<body>(.*?)</body>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private final AdminFileStorageService adminFileStorageService;

    /** AS-IS uploadImage - 팝업 화면. token은 AS-IS StringUtils.getToken()이 만드는 1회용 값이다. */
    @GetMapping("/upload-image")
    public String uploadImage(Model model) {
        model.addAttribute("token", UUID.randomUUID().toString().replace("-", ""));
        return "smarteditor/upload-image";
    }

    /** AS-IS uploadImageProcess - 결과 화면이 부모 창 에디터에 HTML을 끼워 넣는다. */
    @PostMapping("/upload-image")
    public String uploadImageProcess(@RequestParam(value = "file[]", required = false) MultipartFile[] files,
                                      Model model) {
        StringBuilder imageContent = new StringBuilder();
        StringBuilder oversized = new StringBuilder();

        if (files != null) {
            for (MultipartFile file : files) {
                if (file == null || file.isEmpty() || !isImageFile(file)) {
                    continue;   // AS-IS: 이미지가 아니면 조용히 건너뛴다
                }
                if (file.getSize() > IMG_SIZE_LIMIT) {
                    if (oversized.length() > 0) {
                        oversized.append(", ");
                    }
                    oversized.append(file.getOriginalFilename());
                    continue;
                }
                String stored = adminFileStorageService.store(file, "editor", IMAGE_EXTENSIONS, IMG_SIZE_LIMIT);
                // ★ 접두사 /admin이 붙어야 한다 - 이 HTML은 본문에 박혀 storefront에서 렌더되고,
                //   거기서는 /admin/uploads/...만 admin으로 라우팅된다(WebConfig 주석 참고).
                //   AS-IS는 같은 자리에 절대 CDN URL을 심었다.
                imageContent.append("<p>")
                        .append("<img src=\"/admin/uploads/editor/").append(stored).append("\" alt=\"\" />")
                        .append("</p>");
            }
        }

        model.addAttribute("imageContent", imageContent.toString());
        if (oversized.length() > 0) {
            model.addAttribute("errMsg", oversized + " 파일은 용량 제한을 넘어 업로드에 실패했습니다.");
        }
        return "smarteditor/upload-image-result";
    }

    /** AS-IS uploadMovie - 서버 저장이 없는 URL 입력 팝업이다. */
    @GetMapping("/upload-movie")
    public String uploadMovie() {
        return "smarteditor/upload-movie";
    }

    /** AS-IS uploadCtp - CTP(콘텐츠 템플릿) 업로드 팝업. */
    @GetMapping("/upload-ctp")
    public String uploadCtp() {
        return "smarteditor/upload-ctp";
    }

    /** AS-IS uploadCtpProcess - 검사 → 저장 → 상대경로 치환 → body만 추출. */
    @PostMapping("/upload-ctp")
    public String uploadCtpProcess(@RequestParam(value = "file[]", required = false) MultipartFile[] files,
                                    Model model) {
        if (files == null || files.length == 0) {
            return ctpError(model, "ERROR_03", "");
        }

        // 1. 검사 - 확장자 → 용량 → html(ctp) 포함 여부 (AS-IS 순서)
        long maxUploadSize = Long.parseLong(CTP_SIZE_LIMIT_MB) * 1000 * 1000;
        int documentCount = 0;
        String documentName = "";
        for (MultipartFile file : files) {
            if (file == null || file.getSize() <= 0) {
                continue;
            }
            String extension = extensionOf(file.getOriginalFilename());
            if (!CTP_EXTENSIONS.contains(extension)) {
                return ctpError(model, "ERROR_01", extension);
            }
            if (file.getSize() > maxUploadSize) {
                return ctpError(model, "ERROR_02", CTP_SIZE_LIMIT_MB);
            }
            if (CTP_DOCUMENT_EXTENSIONS.contains(extension)) {
                documentName = fileNameWithoutExtension(file.getOriginalFilename());
                documentCount++;
            }
        }
        if (documentCount == 0) {
            return ctpError(model, "ERROR_03", "");
        }

        // 2. 저장 - AS-IS는 ctp/{문서명}_{yyyyMMddHHmmss} 폴더에 모아 둔다
        String folder = "ctp/" + documentName + "_" + LocalDateTime.now().format(FOLDER_FORMAT);
        String ctpContent = "";
        Map<String, String> storedImages = new LinkedHashMap<>();   // 원본파일명 → 저장파일명
        for (MultipartFile file : files) {
            if (file == null || file.getSize() <= 0) {
                continue;
            }
            String extension = extensionOf(file.getOriginalFilename());
            if (CTP_DOCUMENT_EXTENSIONS.contains(extension)) {
                ctpContent = readAll(file);
            }
            String stored = adminFileStorageService.store(file, folder, CTP_EXTENSIONS, maxUploadSize);
            if (!CTP_DOCUMENT_EXTENSIONS.contains(extension)) {
                storedImages.put(file.getOriginalFilename(), stored);
            }
        }

        // 3. 컨텐츠의 상대경로 이미지를 저장경로로 치환 (AS-IS는 "./파일명"을 바꾼다)
        for (Map.Entry<String, String> image : storedImages.entrySet()) {
            // 이미지와 같은 이유로 /admin 접두사를 포함한다(본문이 storefront에서 렌더된다).
            ctpContent = ctpContent.replace("./" + image.getKey(),
                    "/admin/uploads/" + folder + "/" + image.getValue());
        }

        // 4. <body> 안쪽만 취한다
        Matcher bodyMatcher = BODY_PATTERN.matcher(ctpContent);
        while (bodyMatcher.find()) {
            ctpContent = bodyMatcher.group(1);
        }

        model.addAttribute("ctpContent", ctpContent);
        return "smarteditor/upload-ctp-result";
    }

    private String ctpError(Model model, String code, String message) {
        model.addAttribute("editorErrorCode", code);
        model.addAttribute("editorErrorMessage", message);
        return "smarteditor/upload-ctp-result";
    }

    /** AS-IS isImageFile - 대소문자 무시 비교다. */
    private boolean isImageFile(MultipartFile file) {
        return IMAGE_EXTENSIONS.contains(extensionOf(file.getOriginalFilename()));
    }

    /** AS-IS FileUtils.getExtension - 소문자 확장자. */
    private String extensionOf(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }

    /** AS-IS FileUtils.getFileNameWithoutExtension. 경로 성분은 떼어낸다(업로드 파일명 공통 방침). */
    private String fileNameWithoutExtension(String fileName) {
        if (fileName == null) {
            return "";
        }
        String bare = fileName.replace('\\', '/');
        bare = bare.substring(bare.lastIndexOf('/') + 1);
        int dot = bare.lastIndexOf('.');
        return dot < 0 ? bare : bare.substring(0, dot);
    }

    private String readAll(MultipartFile file) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String row;
            while ((row = reader.readLine()) != null) {
                lines.add(row);
            }
        } catch (IOException e) {
            log.warn("SmartEditor CTP 읽기 실패: {}", file.getOriginalFilename(), e);
        }
        return String.join(System.lineSeparator(), lines);
    }
}
