package com.ghlove.admin.web;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.service.CntrReqmngClient;
import com.ghlove.admin.service.LocgovClient;
import com.ghlove.admin.service.ManagerException;
import com.ghlove.admin.service.MenuService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.LinkedHashMap;
import java.util.Map;

/** 기부금 변경신청 관리 (AS-IS opmanager/give/give-reqmng) - 실제 데이터/도메인효과는
 *  donation 서비스에 있고(CntrReqmngClient), 여기는 admin 콘솔의 로그인/RBAC과 화면만
 *  담당한다. give-state/give-operation/give-point와 동일하게 지자체담당자(ROLE_ADMIN_5/6)는
 *  MenuService.effectiveLocgovCode()로 조회범위가 소속 지자체로 강제된다.
 *
 *  검색조건(지자체/요청분류/승인여부/기간)+페이지네이션은 AS-IS list.jsp를 재현해 donation
 *  서비스의 /api/cntr-reqmng/search로 위임한다(CntrReqmngRepository.search 참고). */
@Controller
@RequiredArgsConstructor
public class GiveReqmngController {

    private final CntrReqmngClient cntrReqmngClient;
    private final LocgovClient locgovClient;

    @GetMapping("/give-reqmng")
    public String list(@RequestParam(required = false) String locgovCode,
                        @RequestParam(required = false) String cntrReqmngCode,
                        @RequestParam(required = false) String reqStatusCode,
                        @RequestParam(required = false) String startDate,
                        @RequestParam(required = false) String endDate,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        HttpSession session, Model model) {
        Manager manager = (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
        locgovCode = MenuService.effectiveLocgovCode(manager, locgovCode);
        CntrReqmngClient.SearchResult result = cntrReqmngClient.search(
                locgovCode, cntrReqmngCode, reqStatusCode, startDate, endDate, page, size);
        model.addAttribute("list", result.content());
        model.addAttribute("totalCount", result.totalElements());
        model.addAttribute("totalPages", result.totalPages());
        model.addAttribute("currentPage", page);
        model.addAttribute("size", size);
        model.addAttribute("locgovCode", locgovCode);
        model.addAttribute("cntrReqmngCode", cntrReqmngCode);
        model.addAttribute("reqStatusCode", reqStatusCode);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("provinces", provinces());
        model.addAttribute("allLocgovs", locgovClient.allLocgovs());
        model.addAttribute("locgovScoped", MenuService.isLocgovScoped(manager));
        return "give/give-reqmng-list";
    }

    private Map<String, String> provinces() {
        Map<String, String> map = new LinkedHashMap<>();
        for (LocgovClient.LocgovInfo l : locgovClient.allLocgovs()) {
            map.putIfAbsent(l.upperLocgovCode(), l.upperLocgovNm());
        }
        return map;
    }

    @GetMapping("/give-reqmng/new")
    public String newForm(@RequestParam String cntrSn, Model model) {
        try {
            model.addAttribute("donation", cntrReqmngClient.donationInfo(cntrSn));
            model.addAttribute("cntrSn", cntrSn);
            return "give/give-reqmng-form";
        } catch (ManagerException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "give/give-reqmng-form";
        }
    }

    @PostMapping("/give-reqmng")
    public String submit(@RequestParam String cntrSn, @RequestParam String cntrReqmngCode,
                          @RequestParam String discription,
                          @RequestParam(required = false) String taxSysCancelDe,
                          @RequestParam(required = false) String relatedDocDptNm,
                          @RequestParam(required = false) String relatedDocNum,
                          @RequestParam(required = false) String relatedDocDe,
                          HttpSession session, Model model) {
        Manager manager = (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
        try {
            cntrReqmngClient.submit(cntrSn, cntrReqmngCode, discription, taxSysCancelDe, relatedDocDptNm,
                    relatedDocNum, relatedDocDe, manager.getUserId());
            return "redirect:/give-reqmng";
        } catch (ManagerException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("donation", cntrReqmngClient.donationInfo(cntrSn));
            model.addAttribute("cntrSn", cntrSn);
            return "give/give-reqmng-form";
        }
    }

    @PostMapping("/give-reqmng/{reqId}/approve")
    public String approve(@PathVariable Long reqId, HttpSession session, RedirectAttributes redirectAttributes) {
        Manager manager = (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
        try {
            cntrReqmngClient.approve(reqId, manager.getUserId());
        } catch (ManagerException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/give-reqmng";
    }

    @PostMapping("/give-reqmng/{reqId}/cancel")
    public String cancel(@PathVariable Long reqId, HttpSession session, RedirectAttributes redirectAttributes) {
        Manager manager = (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
        try {
            cntrReqmngClient.cancel(reqId, manager.getUserId());
        } catch (ManagerException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/give-reqmng";
    }
}
