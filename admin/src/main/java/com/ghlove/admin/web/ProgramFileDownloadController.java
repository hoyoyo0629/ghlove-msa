package com.ghlove.admin.web;

import com.ghlove.admin.service.DesignatedProjectClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * 서버 저장 파일명이 아닌 원본 파일명으로 내려받기 (AS-IS {@code saleson.common.file.ProgramFileDownloadController},
 * {@code GET /opmanager/fileDownload/downloadByProgramData}). 대민 특정사업 상세 "공지사항" 탭의 첨부 링크가 부른다
 * (AS-IS designated-donation/details.html fileDownload). AS-IS isManagerPage 예외라 로그인 없이 열리고,
 * TO-BE도 게이트(WebConfig addPathPatterns) 대상이 아니다.
 *
 * <p>AS-IS 처리 그대로: programName은 {@code ProgramName.valueOf}(없는 이름이면 500), PRJ_NOTICE만 다룬다.
 * fileName은 대민 API가 암호화해 준 토큰이고(AS-IS pCrypto), 복호화와 {@code "../"} 제거·파일 읽기는 키와 파일을
 * 가진 donation이 한다. 응답 헤더는 AS-IS {@code FileDownloadCustom.fileDownloadCustom} 그대로(octet-stream,
 * 길이, {@code form-data; name="attachment"; filename="URL인코딩된 원본명"}). 파일이 없으면 AS-IS
 * PageNotFoundException처럼 404 페이지(AS-IS public/error/404.html 사본)를 404로 보여준다.
 */
@Controller
@RequiredArgsConstructor
public class ProgramFileDownloadController {

    /** AS-IS saleson.common.enumeration.ProgramName - HNR_USER_MNG는 AS-IS에서도 주석처리돼 있다. */
    enum ProgramName {
        PRJ_NOTICE("prjNotice", "특정사업기부 공지사항", true);

        private final String title;
        private final String description;
        private final Boolean enabled;

        ProgramName(String title, String description, Boolean enabled) {
            this.title = title;
            this.description = description;
            this.enabled = enabled;
        }

        String getProgramPath() {
            return title;
        }
    }

    private final DesignatedProjectClient designatedProjectClient;

    @GetMapping("/fileDownload/downloadByProgramData")
    public Object downloadByProgramData(@RequestParam String programName,
                                        @RequestParam(required = false, defaultValue = "0") long programId,
                                        @RequestParam(required = false, defaultValue = "0") int fileSeq,
                                        @RequestParam(required = false, defaultValue = "") String fileName,
                                        @RequestParam(required = false, defaultValue = "") String orgFileName)
            throws UnsupportedEncodingException {

        ProgramName programNameEnum = ProgramName.valueOf(programName);
        if (fileName != null && orgFileName != null) {
            // fileName의 복호화·"../" 제거는 donation이 한다(키가 거기 있다).
            orgFileName = orgFileName.replaceAll("../", "").replaceAll("..\\\\", "");
        }
        switch (programNameEnum) {
            case PRJ_NOTICE:
                byte[] bytes = designatedProjectClient.noticeFileContent(programId, fileName);
                if (bytes == null) {
                    return new ModelAndView("bix5-access/index", HttpStatus.NOT_FOUND);
                }
                HttpHeaders httpHeaders = new HttpHeaders();
                httpHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                httpHeaders.setContentLength(bytes.length);
                String setFileName = URLEncoder.encode(orgFileName, "UTF-8");        // 한글 파일명 조치
                httpHeaders.setContentDispositionFormData("attachment", setFileName);
                return new ResponseEntity<>(bytes, httpHeaders, HttpStatus.OK);
            default:
                return null;
        }
    }
}
