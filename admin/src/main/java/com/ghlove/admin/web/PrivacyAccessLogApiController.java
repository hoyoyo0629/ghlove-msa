package com.ghlove.admin.web;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.service.ManagerException;
import com.ghlove.admin.service.PrivacyAccessLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 개인정보 엑셀 다운로드 사유 기록 - AS-IS saleson.common.controller.CommonController의
 * {@code POST /common/{pageType}/privacy-access-log} / {@code privacy-access-log-update} 재현.
 *
 * 다운로드 사유 모달(fragments/privacy-access.html)이 실제 다운로드 직전에 이 엔드포인트로
 * 사유를 보내고, 성공 응답을 받은 다음에야 다운로드 URL로 이동한다(AS-IS와 동일한 2단계).
 * 경로의 {@code opmanager}는 AS-IS 그대로다(판매자 페이지용 {@code seller}는 TO-BE 미해당).
 */
@RestController
@RequiredArgsConstructor
public class PrivacyAccessLogApiController {

    private final PrivacyAccessLogService privacyAccessLogService;

    @PostMapping("/common/opmanager/privacy-access-log")
    public Map<String, Object> privacyAccessLog(@RequestParam String url,
                                                @RequestParam(required = false) String reason,
                                                @RequestParam(required = false) String reasonType,
                                                HttpSession session, HttpServletRequest request) {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            Manager manager = (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
            privacyAccessLogService.record(url, reason, reasonType, manager, request);
            result.put("isSuccess", true);
        } catch (ManagerException e) {
            result.put("isSuccess", false);
            result.put("errorMessage", e.getMessage());
        }
        return result;
    }

    @PostMapping("/common/opmanager/privacy-access-log-update")
    public Map<String, Object> privacyAccessLogUpdate(@RequestParam Long privacyAccessLogId,
                                                      @RequestParam(required = false) String reason,
                                                      @RequestParam(required = false) String reasonType,
                                                      HttpSession session) {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            Manager manager = (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
            privacyAccessLogService.updateReason(privacyAccessLogId, reason, reasonType, manager);
            result.put("isSuccess", true);
        } catch (ManagerException e) {
            result.put("isSuccess", false);
            result.put("errorMessage", e.getMessage());
        }
        return result;
    }
}
