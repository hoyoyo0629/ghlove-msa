package com.ghlove.admin.web;

import com.ghlove.admin.service.CaptchaService;
import com.ghlove.admin.service.CommonCodeService;
import com.ghlove.admin.service.JwtVerifier;
import com.ghlove.admin.service.QnaException;
import com.ghlove.admin.service.QnaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/** 마이페이지 "1:1 문의"(AS-IS mypage/inquiry.html, 내 글만) + 고객센터 "Q&amp;A"(AS-IS
 *  qna/qna-open.html, 전체 공개 게시판) - 둘 다 같은 OP_QNA를 공유한다. SFR-010: userId는
 *  로그인 JWT 쿠키에서만 가져온다. */
@Controller
@RequiredArgsConstructor
public class QnaController {

    private final QnaService qnaService;
    private final CommonCodeService commonCodeService;
    private final CaptchaService captchaService;
    private final JwtVerifier jwtVerifier;

    private String loginRedirect(String returnPath) {
        return "redirect:http://localhost:8081/login?target=" + encode("http://localhost:8086" + returnPath);
    }

    @GetMapping("/qna")
    public String list(@RequestParam(required = false) String searchStartDate,
                        @RequestParam(required = false) String searchEndDate,
                        @RequestParam(required = false) String errorMessage,
                        HttpServletRequest request, Model model) {
        var authUserId = jwtVerifier.currentUserId(request);
        if (authUserId.isEmpty()) {
            return loginRedirect("/qna");
        }
        model.addAttribute("searchStartDate", searchStartDate);
        model.addAttribute("searchEndDate", searchEndDate);
        model.addAttribute("errorMessage", errorMessage);
        model.addAttribute("qnaGroups", commonCodeService.labelsOf("QNA_GROUPS"));
        model.addAttribute("inquiries", qnaService.myInquiriesDetailed(authUserId.get(), searchStartDate, searchEndDate));
        return "qna/list";
    }

    @PostMapping("/qna")
    public String submit(@RequestParam(required = false) String userName,
                          @RequestParam(required = false) String email, @RequestParam String qnaGroup,
                          @RequestParam String subject, @RequestParam String question,
                          @RequestParam(required = false) boolean secretFlag,
                          @RequestParam(required = false) List<MultipartFile> files,
                          @RequestParam(required = false) String captcha,
                          HttpServletRequest request, HttpSession session) {
        var authUserId = jwtVerifier.currentUserId(request);
        if (authUserId.isEmpty()) {
            return loginRedirect("/qna");
        }
        Long userId = authUserId.get();
        String answer = (String) session.getAttribute(CaptchaService.SESSION_KEY);
        session.removeAttribute(CaptchaService.SESSION_KEY);
        if (answer == null || !answer.equals(captcha)) {
            return "redirect:/qna?errorMessage=" + encode("자동입력 방지문자가 일치하지 않습니다.");
        }
        try {
            qnaService.ask(userId, userName, email, qnaGroup, subject, question, secretFlag, files);
            return "redirect:/qna";
        } catch (QnaException e) {
            return "redirect:/qna?errorMessage=" + encode(e.getMessage());
        }
    }

    /** 고객센터 &gt; Q&amp;A (AS-IS qna/qna-open.html) - 로그인 없이도 볼 수 있는 공개 게시판. */
    @GetMapping("/qna/board")
    public String board(@RequestParam(required = false, defaultValue = "SUBJECT") String where,
                         @RequestParam(required = false) String q,
                         @RequestParam(required = false, defaultValue = "CREATED_DATE") String orderBy,
                         @RequestParam(required = false, defaultValue = "DESC") String sort,
                         @RequestParam(required = false, defaultValue = "1") int page,
                         @RequestParam(required = false, defaultValue = "10") int size,
                         HttpServletRequest request, Model model) {
        Long viewerUserId = jwtVerifier.currentUserId(request).orElse(null);
        var result = qnaService.publicBoard(where, q, orderBy, sort, page, size, viewerUserId);
        model.addAttribute("where", where);
        model.addAttribute("q", q);
        model.addAttribute("orderBy", orderBy);
        model.addAttribute("sort", sort);
        model.addAttribute("size", size);
        model.addAttribute("result", result);
        return "qna/board";
    }

    @GetMapping("/qna/board/{qnaId}")
    public String boardDetail(@PathVariable Integer qnaId, HttpServletRequest request, Model model) {
        Long viewerUserId = jwtVerifier.currentUserId(request).orElse(null);
        var detail = qnaService.publicBoardDetail(qnaId, viewerUserId);
        if (detail.isEmpty()) {
            return "redirect:/qna/board";
        }
        model.addAttribute("detail", detail.get());
        return "qna/board-detail";
    }

    /** 캡차 이미지 - 새로고침 버튼도 이 URL을 다시 불러오는 것으로 처리한다(매 요청마다 새 정답 생성). */
    @GetMapping(value = "/qna/captcha", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> captchaImage(HttpSession session) {
        String answer = captchaService.generateAnswer();
        session.setAttribute(CaptchaService.SESSION_KEY, answer);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(captchaService.renderImage(answer));
    }

    /** "음성듣기" - 현재 세션에 발급된 캡차 정답을 그대로 반환, 브라우저 TTS(Web Speech API)로 읽는다. */
    @GetMapping("/qna/captcha/audio-text")
    @ResponseBody
    public String captchaAudioText(HttpSession session) {
        String answer = (String) session.getAttribute(CaptchaService.SESSION_KEY);
        return answer != null ? answer : "";
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
