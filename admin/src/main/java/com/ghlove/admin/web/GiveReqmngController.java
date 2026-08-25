package com.ghlove.admin.web;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.service.CntrReqmngClient;
import com.ghlove.admin.service.ManagerException;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/** 기부금 변경신청 관리 (AS-IS opmanager/give/give-reqmng) - 실제 데이터/도메인효과는
 *  donation 서비스에 있고(CntrReqmngClient), 여기는 admin 콘솔의 로그인/RBAC과 화면만
 *  담당한다. give-state/give-operation/give-point와 동일하게 지자체별 자기서비스 화면은
 *  이번 라운드에서 생략하고 ROLE_ADMIN 전용 "전체 보기+승인" 화면만 구현한다(AS-IS의
 *  mois 권한 화면에 해당). */
@Controller
@RequiredArgsConstructor
public class GiveReqmngController {

    private final CntrReqmngClient cntrReqmngClient;

    @GetMapping("/give-reqmng")
    public String list(Model model) {
        model.addAttribute("list", cntrReqmngClient.listAll());
        return "give/give-reqmng-list";
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
    public String approve(@PathVariable Long reqId, HttpSession session, Model model) {
        Manager manager = (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
        try {
            cntrReqmngClient.approve(reqId, manager.getUserId());
        } catch (ManagerException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("list", cntrReqmngClient.listAll());
            return "give/give-reqmng-list";
        }
        return "redirect:/give-reqmng";
    }

    @PostMapping("/give-reqmng/{reqId}/cancel")
    public String cancel(@PathVariable Long reqId, HttpSession session, Model model) {
        Manager manager = (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
        try {
            cntrReqmngClient.cancel(reqId, manager.getUserId());
        } catch (ManagerException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("list", cntrReqmngClient.listAll());
            return "give/give-reqmng-list";
        }
        return "redirect:/give-reqmng";
    }
}
