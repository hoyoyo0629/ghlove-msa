package com.ghlove.admin.web;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.domain.OpEmail;
import com.ghlove.admin.domain.OpEmailFile;
import com.ghlove.admin.repository.RoleRepository;
import com.ghlove.admin.service.EmailAttachmentStorageService;
import com.ghlove.admin.service.EmailDefaultContent;
import com.ghlove.admin.service.OpEmailService;
import com.ghlove.admin.web.support.EmailDetailParam;
import com.ghlove.admin.web.support.EmailForm;
import com.ghlove.admin.web.support.EmailParam;
import com.ghlove.admin.web.support.EmailSendTarget;
import com.ghlove.admin.web.support.Pagination;
import com.ghlove.admin.web.support.SendParam;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 이메일 발송 (메뉴 1410) - AS-IS saleson.shop.email.EmailManagerController(/opmanager/email) 재현.
 *
 * 목록(검색·화면출력·페이징) → 발송 폼(발송대상 권한별/답례품/개별 · 발송시점 즉시/지정 ·
 * 제목 · 첨부파일 · 스마트에디터 내용) → 등록 후 /send로 발송 요청 → 상세(발송결과·발송인원).
 *
 * AS-IS는 등록(POST form)과 발송(POST send)이 분리돼 있고, 화면 JS가 등록 응답의 emailId로
 * 숨은 iframe에 발송 폼을 submit한다 - 그 구조를 그대로 뒀다.
 *
 * 예전 TO-BE는 이 화면을 "관리자에게 즉시 메일 한 통"으로 축소해 두고 AS-IS에 없는 발송대상
 * 코드(A:전체관리자/D:ROLE_ADMIN/O:ROLE_OPERATOR)를 지어 썼다 - AS-IS 코드(A/S/E/L)로 되돌렸다.
 */
@Controller
@RequiredArgsConstructor
public class OpEmailController {

    private final OpEmailService opEmailService;
    private final EmailAttachmentStorageService attachmentStorage;
    private final RoleRepository roleRepository;

    /** AS-IS GET/POST /opmanager/email/list. 메뉴(op_menu 1410)가 /email을 가리켜 둘 다 받는다. */
    @RequestMapping(value = { "/email", "/email/list" }, method = { RequestMethod.GET, RequestMethod.POST })
    public String list(@ModelAttribute("searchParam") EmailParam searchParam,
                       HttpServletRequest request, Model model) {
        if (searchParam.getItemsPerPage() <= 0) {
            searchParam.setItemsPerPage(Pagination.DEFAULT_ITEMS_PER_PAGE);
        }
        List<OpEmail> all = opEmailService.getEmailList(searchParam);
        Pagination pagination = Pagination.of(all.size(), searchParam.getPage(), searchParam.getItemsPerPage())
                .withLinkFrom(request);
        model.addAttribute("list", all.stream()
                .skip(pagination.getStartRow())
                .limit(pagination.getItemsPerPage())
                .toList());
        model.addAttribute("count", all.size());
        model.addAttribute("pagination", pagination);
        return "email/list";
    }

    /**
     * AS-IS GET /opmanager/email/form - 발송대상 "권한별" 체크박스용 권한그룹 목록을 함께 내린다
     * (AS-IS는 userGroupService.getUserGroupList(conditionType='ROLE_ADMIN')).
     */
    @GetMapping("/email/form")
    public String form(Model model) {
        model.addAttribute("groupList", roleRepository.findAdminRoles());
        model.addAttribute("defaultContent", EmailDefaultContent.HTML);
        return "email/form";
    }

    /**
     * AS-IS POST /opmanager/email/form - 등록만 하고 emailId를 돌려준다(발송은 /send).
     * 화면이 FormData로 보내므로 multipart 파라미터를 그대로 받는다
     * ({@code authList[i].authority} 모양까지 AS-IS와 동일).
     */
    @PostMapping("/email/form")
    @ResponseBody
    public Map<String, Object> submit(@ModelAttribute EmailForm form,
                                      @RequestParam(value = "files", required = false) MultipartFile files,
                                      HttpSession session) {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            OpEmail email = new OpEmail();
            email.setSubject(form.getSubject());
            email.setContent(form.getContent());
            email.setSendType(form.getSendType());
            email.setSendDate(form.getSendDate());
            email.setAuthTarget(form.getAuthTarget());
            email.setAuthList(OpEmailService.toAuthList(form.authorities()));
            Manager manager = (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
            email.setFrstRegisterId(manager == null ? null : manager.getUserId());

            OpEmail saved = opEmailService.insertEmail(email, files);
            result.put("isSuccess", true);
            result.put("data", saved.getEmailId());
        } catch (RuntimeException e) {
            result.put("isSuccess", false);
            result.put("errorMessage", e.getMessage());
        }
        return result;
    }

    /** AS-IS POST /opmanager/email/send - 대상을 모아 EMS로 발송 요청한다. */
    @PostMapping("/email/send")
    @ResponseBody
    public Map<String, Object> send(@ModelAttribute SendParam param) {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            opEmailService.sendEmail(param);
            result.put("isSuccess", true);
        } catch (RuntimeException e) {
            result.put("isSuccess", false);
            result.put("errorMessage", e.getMessage());
        }
        return result;
    }

    /** AS-IS POST /opmanager/email/search - 개별 발송 수신자 검색(이름 완전일치). */
    @PostMapping("/email/search")
    @ResponseBody
    public Map<String, Object> searchUser(@RequestParam(required = false) String userName) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("isSuccess", true);
        result.put("data", opEmailService.getUserList(userName));
        return result;
    }

    /** AS-IS GET /opmanager/email/{emailId}. */
    @GetMapping("/email/{emailId}")
    public String detail(@PathVariable long emailId, Model model) {
        OpEmail mail = opEmailService.getEmailDetail(emailId);
        if (mail.isEmailSend()) {
            model.addAttribute("cnt", opEmailService.getEmsTotalCnt(emailId));
        }
        model.addAttribute("mail", mail);
        return "email/detail";
    }

    /** AS-IS POST /opmanager/email/{emailId} - 상세화면의 발송인원 목록(ajax). */
    @PostMapping("/email/{emailId}")
    @ResponseBody
    public Map<String, Object> emsDetail(@PathVariable long emailId, @ModelAttribute EmailDetailParam params) {
        params.setEmailId(emailId);
        if (params.getItemsPerPage() <= 0) {
            params.setItemsPerPage(Pagination.DEFAULT_ITEMS_PER_PAGE);
        }
        int count = opEmailService.getEmsUserCnt(params);
        Pagination pagination = Pagination.of(count, params.getPage(), params.getItemsPerPage());
        List<EmailSendTarget> list = opEmailService.getEmsUserList(params);

        Map<String, Object> resultMap = new LinkedHashMap<>();
        resultMap.put("count", count);
        resultMap.put("list", list);
        resultMap.put("pagination", pagination);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("isSuccess", true);
        result.put("data", resultMap);
        return result;
    }

    /** AS-IS GET /opmanager/email/download/{emailFileId}. */
    @GetMapping("/email/download/{emailFileId}")
    public ResponseEntity<Resource> download(@PathVariable int emailFileId) {
        OpEmailFile file = opEmailService.getEmailFile(emailFileId);
        if (file == null) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        byte[] bytes = attachmentStorage.read(file.getFileName());
        if (bytes == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        String fileName = URLEncoder.encode(
                file.getOrgFileName() == null ? "attachment" : file.getOrgFileName(), StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(attachmentStorage.contentTypeOf(file.getOrgFileName())))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                .body(new ByteArrayResource(bytes));
    }
}
