package com.ghlove.admin.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ghlove.admin.domain.Manager;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URI;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 서버 업로드용 파일관리 (AS-IS {@code saleson.common.file.FileUploadController}, {@code /opmanager/file/**},
 * 화면 opmanager/i18n/file/index.jsp, layout base). 메뉴에 없는 운영자 유틸리티로 서버 디스크를 열람·폴더생성·
 * 업로드·다운로드하고, EMS 메일발송을 시험한다.
 *
 * <p>AS-IS {@code OpmanagerHandlerInterceptor.isManagerPage}가 로그인·메뉴권한 검사에서 뺀 경로라 TO-BE도
 * 게이트(WebConfig addPathPatterns)에 넣지 않고, AS-IS처럼 핸들러가 직접 "시스템 관리자"인지 본다
 * ({@link #isAdmin}: 로그인한 운영자이고 권한이 AS-IS UserAdminRole의 LOC(5·6)·MOIS(3·4)·OFF(7·8)가 아님).
 *
 * <p><b>AS-IS와 다른 점(2026-10-13 사용자 결정 "옮기되 다운로드도 관리자만")</b>: AS-IS는 downloadFile·
 * sendMail·sendMailFile에 이 검사가 없어 로그인 없이 서버의 임의 파일을 내려받고 시험메일을 보낼 수 있었다.
 * TO-BE는 downloadFile에만 같은 검사를 건다(404). sendMail·sendMailFile은 2026-10-14 사용자 결정으로
 * AS-IS대로 검사 없이 두고, 추가했던 검사는 주석으로 남겼다.
 * 또 AS-IS의 "로그인 사용자"는 프론트 회원 세션도 포함돼(롤에 ROLE_ADMIN_이 없으면 SYS로 판정) 일반 회원도
 * 이 화면을 쓸 수 있었는데, TO-BE admin은 운영자 세션만 있어 그 구멍은 구조적으로 없다.
 *
 * <p>메일 시험 두 개는 AS-IS가 try 머리에서 {@code getInputStream()}을 본문 쓰기보다 먼저 열어, 요청이 빈
 * 본문으로 나가고 뒤의 쓰기는 버려진다(HttpURLConnection은 getInputStream 시점에 요청을 보낸다). 시험 기능이
 * 시험이 되게 본문을 다 쓴 뒤 응답을 읽는 순서로 옮겼다 - 보내는 값·주소·결과 처리는 AS-IS 그대로다.
 */
@Controller
@Slf4j
public class FileUploadController {

    private static final String CHARSET = "UTF-8";
    private static final String CRLF = "\r\n";

    /** AS-IS UserAdminRole LOC·MOIS·OFF - 이 권한이면 시스템 관리자가 아니다. */
    private static final List<String> NON_SYSTEM_ROLES = List.of(
            "ROLE_ADMIN_3", "ROLE_ADMIN_4", "ROLE_ADMIN_5", "ROLE_ADMIN_6", "ROLE_ADMIN_7", "ROLE_ADMIN_8");

    private final String emsHost;
    private final String emsSendUserName;
    private final String emsSendEmail;

    public FileUploadController(@Value("${ghlove.integrations.ems.host}") String emsHost,
                                @Value("${ghlove.integrations.ems.send-username}") String emsSendUserName,
                                @Value("${ghlove.integrations.ems.send-email}") String emsSendEmail) {
        this.emsHost = emsHost;
        this.emsSendUserName = emsSendUserName;
        this.emsSendEmail = emsSendEmail;
    }

    /** 화면 한 줄 - AS-IS JSP가 Path로 그리던 이름·디렉터리 여부·크기(MB, {@code op:getFileSize}). */
    public record FileRow(String name, boolean directory, double size) {
    }

    /** AS-IS index + index.jsp 스크립틀릿(OS별 루트, location 파라미터, 한 단계 목록). */
    @GetMapping("/file/index")
    public String index(@RequestParam(required = false) String location, HttpSession session, Model model) {
        model.addAttribute("serverIp", getServerIp());

        if (!isAdmin(session)) {
            return "redirect:/error/404";
        }

        String rootFilePath;
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            rootFilePath = "c:/";
        } else {
            rootFilePath = "/";
        }
        String filePath = location == null ? rootFilePath : location;
        Path dirPath = Paths.get(filePath);

        // AS-IS: 디렉터리 여부로 정렬(파일 먼저) 뒤 뒤집어 폴더가 위로 온다.
        List<FileRow> pathList = null;
        try (Stream<Path> walk = Files.walk(dirPath, 1)) {
            List<Path> paths = walk.filter(p -> !p.equals(dirPath))
                    .sorted(Comparator.comparing(Files::isDirectory))
                    .collect(Collectors.toList());
            Collections.reverse(paths);
            pathList = new ArrayList<>();
            for (Path p : paths) {
                pathList.add(new FileRow(String.valueOf(p.getFileName()), Files.isDirectory(p), getFileSize(p)));
            }
        } catch (IOException | RuntimeException e) {
            log.error("file index walk error: {}", filePath, e);
        }
        model.addAttribute("pathList", pathList);
        model.addAttribute("filePath", filePath);
        return "file/index";
    }

    @PostMapping("/file/makeDir")
    @ResponseBody
    public Map<String, Object> makeDir(@RequestBody Map<String, String> paramMap, HttpSession session) {
        if (!isAdmin(session)) {
            return failure("");
        }

        String dirName = paramMap.get("dirName");
        String pathName = paramMap.get("path");

        try {
            String msg;
            Path newFolderPath = Paths.get(pathName + "/" + dirName);
            if (Files.exists(newFolderPath) && Files.isDirectory(newFolderPath)) {
                msg = "폴더가 존재합니다.";
            } else {
                Files.createDirectory(newFolderPath);
                msg = "폴더가 생성되었습니다.";
            }
            return success(msg);
        } catch (Exception e) {
            return failure(e.getMessage());
        }
    }

    @PostMapping("/file/uploadFile")
    @ResponseBody
    public Map<String, Object> uploadFile(@RequestParam(value = "path") String filePath,
                                          @RequestParam(value = "files", required = false) MultipartFile[] files,
                                          HttpSession session) {
        if (!isAdmin(session)) {
            return failure("");
        }

        Path path = Paths.get(filePath).toAbsolutePath().normalize();

        for (MultipartFile file : files) {
            try (InputStream is = file.getInputStream()) {

                Files.createDirectories(path);
                if (file.isEmpty()) {
                    continue;
                }

                Path target = Paths.get(filePath + File.separator + file.getOriginalFilename());
                Files.copy(is, target, StandardCopyOption.REPLACE_EXISTING);

            } catch (RuntimeException e) {
                log.error("{} uploadFile RuntimeException ================== fileName :: {}",
                        getClass().getName(), file.getName(), e);
                return failure("업로드 실패 - RuntimeException :: " + file.getName());
            } catch (Exception e) {
                log.error("{} uploadFile Exception ================== fileName :: {}",
                        getClass().getName(), file.getName(), e);
                return failure("업로드 실패 - Exception :: " + file.getName());
            }
        }

        return success("업로드 성공");
    }

    /** AS-IS downloadFile - 시스템 관리자 검사는 TO-BE에서 더했다(클래스 주석). */
    @PostMapping("/file/downloadFile")
    public ResponseEntity<Resource> downloadFile(@RequestParam Map<String, String> data, HttpSession session) {
        if (!isAdmin(session)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        String fullPath = data.get("path") + File.separator + data.get("fileName");

        Resource resource = new FileSystemResource(fullPath);

        if (!resource.exists()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        try {
            String contentType = Files.probeContentType(Paths.get(fullPath));
            if (contentType == null || contentType.isEmpty()) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=" + URLEncoder.encode(data.get("fileName"), CHARSET))
                    .body(resource);

        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /** AS-IS isAdmin - 로그인 사용자이고 UserAdminRole이 SYS(LOC·MOIS·OFF가 아님). */
    private static boolean isAdmin(HttpSession session) {
        Manager manager = (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
        if (manager == null) {
            return false;
        }
        String authority = manager.getAuthority();
        return authority == null || !NON_SYSTEM_ROLES.contains(authority);
    }

    private static String getServerIp() {
        InetAddress local = null;
        try {
            local = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            log.error("■■■ERROR■■■ getServerIp Exception {}", e.getStackTrace()[0]);
        }

        if (local == null) {
            return "";
        } else {
            return local.getHostAddress();
        }
    }

    /** AS-IS UserUtils.getFileSize - MB, 소수 셋째 자리 반올림. */
    private static double getFileSize(Path path) {
        long bytes = path.toFile().length();
        double kilobyte = bytes / 1024.0;
        double megabyte = kilobyte / 1024.0;

        return Math.round(megabyte * 1000) / 1000.0;
    }

    /** AS-IS sendMail(메일발송 테스트) - EMS(ems.host)에 시험메일 한 통. */
    @PostMapping("/file/sendMail")
    @ResponseBody
    public Map<String, Object> sendMailTest(@RequestParam(value = "email") String email, HttpSession session)
            throws Exception {
        // AS-IS sendMail에는 시스템 관리자 검사가 없다(2026-10-14 사용자 결정: AS-IS대로 주석처리).
        // if (!isAdmin(session)) {
        //     return failure("");
        // }

        // 4. 메일 전송 데이터 셋팅
        String categoryNm = "포인트만료";
        String linkNm = "포인트만료";
        String uuid = UUID.randomUUID().toString();
        Map<String, Object> mailDataMap = new HashMap<>();
        mailDataMap.put("title", "메일 테스트");
        mailDataMap.put("content", "메일 내용");
        mailDataMap.put("sendInfo", emsSendEmail + " " + emsSendUserName);
        mailDataMap.put("rcvInfo", email + " 테스터");
        mailDataMap.put("sendDate", "");
        mailDataMap.put("sendType", "");
        mailDataMap.put("categoryNm", categoryNm);
        mailDataMap.put("linkNm", linkNm);
        mailDataMap.put("memo", uuid);

        Map<String, Object> mailMap = new HashMap<>();
        mailMap.put("data", mailDataMap);

        // 5. 메일 전송 데이터 문자로 변환
        ObjectMapper mapper = new ObjectMapper();
        String jsonValue = mapper.writeValueAsString(mailMap);

        log.info("**********************************************");
        log.info("ems host: " + emsHost);
        log.info("**********************************************");

        // 6. 메일 발송 셋팅
        HttpURLConnection conn = (HttpURLConnection) URI.create(emsHost).toURL().openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept-Charset", "UTF-8");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonValue.getBytes(StandardCharsets.UTF_8));
            os.flush();

            // 7.리턴된 결과 읽기
            try (InputStream is = conn.getInputStream();
                 InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
                 BufferedReader in = new BufferedReader(isr)) {
                String inputLine;
                StringBuilder outResult = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    outResult.append(inputLine);
                }

                log.info("*****************************************");
                log.info(outResult.toString());
                log.info("*****************************************");

                @SuppressWarnings("unchecked")
                Map<String, Object> result = new ObjectMapper().readValue(outResult.toString(), Map.class);

                return success(result);
            }
        } catch (Exception e) {
            log.error("■■■ERROR■■■ sendMailTest Exception {}", e.getStackTrace()[0]);
            return failure(e.getMessage());
        } finally {
            conn.disconnect();
        }
    }

    /** AS-IS sendMailFile - 고정 첨부파일·고정 주소(AS-IS 소스 그대로)로 파일첨부 메일을 시험한다. */
    @PostMapping("/file/sendMailFile")
    @ResponseBody
    public Map<String, Object> sendMailFileTest(@RequestParam(value = "email") String email, HttpSession session)
            throws Exception {
        // AS-IS sendMailFile에는 시스템 관리자 검사가 없다(2026-10-14 사용자 결정: AS-IS대로 주석처리).
        // if (!isAdmin(session)) {
        //     return failure("");
        // }

        // 4. 메일 전송 데이터 셋팅
        String categoryNm = "포인트만료";
        String linkNm = "포인트만료";

        log.info("**********************************************");
        log.info("ems host: " + emsHost);
        log.info("**********************************************");

        // 6. 메일 발송 셋팅
        HttpURLConnection conn = (HttpURLConnection) URI.create("http://192.168.100.250:8380/api/sendFile.do")
                .toURL().openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        conn.setDoInput(true);
        conn.setUseCaches(false);

        String boundary = setBoundary();

        try (OutputStream os = conn.getOutputStream();
             OutputStreamWriter osw = new OutputStreamWriter(os, CHARSET);
             PrintWriter writer = new PrintWriter(osw, true)) {

            File file = new File("C:/ghlove/workspace/ghlove/ghlove-web/storage/upload/email/202307061715380194_59RLZ4QV.zip");

            //전송
            addString(writer, boundary, "title", "메일 테스트");
            addString(writer, boundary, "content", "메일 내용");
            addString(writer, boundary, "sendInfo", emsSendEmail + " " + emsSendUserName);
            addString(writer, boundary, "rcvInfo", email + " 테스터");
            addString(writer, boundary, "sendDate", "");
            addString(writer, boundary, "sendType", "");
            addString(writer, boundary, "categoryNm", categoryNm);
            addString(writer, boundary, "linkNm", linkNm);
            addString(writer, boundary, "memo", "");
            addFile(writer, os, boundary, "file", file);
            addEnd(writer, boundary);

            // 7.리턴된 결과 읽기
            try (InputStream is = conn.getInputStream();
                 InputStreamReader isr = new InputStreamReader(is, CHARSET);
                 BufferedReader in = new BufferedReader(isr)) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    sb.append(line);
                }
                log.info("Response: " + sb);
            }

            return success("succ");
        } catch (Exception e) {
            log.error("■■■ERROR■■■ sendMailFileTest Exception {}", e.getStackTrace()[0]);
            return failure(e.getMessage());
        } finally {
            conn.disconnect();
        }
    }

    //바운더리 셋팅
    private static String setBoundary() {
        String boundaryTime = Long.toHexString(System.currentTimeMillis());
        return "aaboo" + boundaryTime;
    }

    //스트링 추가
    private static void addString(PrintWriter writer, String boundary, String key, String value) {
        StringBuilder sb = new StringBuilder();
        sb.append("--").append(boundary).append(CRLF);
        sb.append("Content-Disposition: form-data; name=\"").append(key).append("\"").append(CRLF);
        sb.append("Content-Type: text/plain; charset=").append(CHARSET).append(CRLF);
        sb.append(CRLF).append(value).append(CRLF);

        writer.append(sb).flush();
    }

    //파일 추가
    private static void addFile(PrintWriter writer, OutputStream os, String boundary, String key, File file)
            throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("--").append(boundary).append(CRLF);
        sb.append("Content-Disposition: form-data; name=\"").append(key).append("\"; filename=\"")
                .append(file.getName()).append("\"").append(CRLF);
        sb.append("Content-Type: ").append(URLConnection.guessContentTypeFromName(file.getName())).append(CRLF);
        sb.append("Content-Transfer-Encoding: binary").append(CRLF);
        sb.append(CRLF);
        writer.append(sb).flush();

        try (FileInputStream inputStream = new FileInputStream(file)) {
            byte[] buffer = new byte[(int) file.length()];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.flush();
        }

        writer.append(CRLF).flush();
    }

    //전송처리 끝
    private static void addEnd(PrintWriter writer, String boundary) {
        writer.append("--").append(boundary).append("--").append(CRLF).flush();
    }

    /** AS-IS JsonViewUtils.success(data). */
    private static Map<String, Object> success(Object data) {
        Map<String, Object> map = new HashMap<>();
        map.put("isSuccess", true);
        map.put("data", data);
        return map;
    }

    /** AS-IS JsonViewUtils.failure(message). */
    private static Map<String, Object> failure(String message) {
        Map<String, Object> map = new HashMap<>();
        map.put("isSuccess", false);
        map.put("errorMessage", message == null ? "" : message);
        return map;
    }
}
