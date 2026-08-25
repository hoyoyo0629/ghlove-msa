package com.ghlove.admin.web;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.service.CommonCodeService;
import com.ghlove.admin.service.CtbnyOpratnClient;
import com.ghlove.admin.service.LocgovClient;
import com.ghlove.admin.service.ManagerException;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 기부금 지출내역 (AS-IS opmanager/give/give-operation, "기부금 운용정보"). 실제 데이터는
 *  donation 서비스에 있고(CtbnyOpratnClient), 이 컨트롤러는 admin 콘솔의 로그인/RBAC과
 *  화면만 담당한다. 기부금액(잔액 계산용)은 이미 만든 GiveStateService의 ReadModel을 재사용. */
@Controller
@RequiredArgsConstructor
public class GiveOperationController {

    private final CtbnyOpratnClient ctbnyOpratnClient;
    private final LocgovClient locgovClient;
    private final CommonCodeService commonCodeService;
    private final com.ghlove.admin.service.GiveStateService giveStateService;

    /** 지자체별 집계(기부금액/사용금액/잔액) - AS-IS give-operation/list.jsp. */
    @GetMapping("/give-operation")
    public String list(Model model) {
        Map<String, LocgovClient.LocgovInfo> locgovsByCode = new LinkedHashMap<>();
        locgovClient.allLocgovs().forEach(l -> locgovsByCode.put(l.locgovCode(), l));

        Map<String, BigDecimal> donatedByLocgov = new LinkedHashMap<>();
        for (var row : giveStateService.search(null, null, null)) {
            donatedByLocgov.merge(row.locgovCode(), row.cntrAmt(), BigDecimal::add);
        }

        Map<String, BigDecimal> spentByLocgov = new LinkedHashMap<>();
        for (CtbnyOpratnClient.Row r : ctbnyOpratnClient.listAll()) {
            spentByLocgov.merge(r.locgovCode(), r.expndtrAmt() != null ? r.expndtrAmt() : BigDecimal.ZERO, BigDecimal::add);
        }

        List<Row> rows = new java.util.ArrayList<>();
        BigDecimal totalDonated = BigDecimal.ZERO, totalSpent = BigDecimal.ZERO;
        for (String locgovCode : donatedByLocgov.keySet()) {
            LocgovClient.LocgovInfo info = locgovsByCode.get(locgovCode);
            BigDecimal donated = donatedByLocgov.get(locgovCode);
            BigDecimal spent = spentByLocgov.getOrDefault(locgovCode, BigDecimal.ZERO);
            rows.add(new Row(locgovCode, info != null ? info.upperLocgovNm() : locgovCode,
                    info != null ? info.locgovNm() : locgovCode, donated, spent, donated.subtract(spent)));
            totalDonated = totalDonated.add(donated);
            totalSpent = totalSpent.add(spent);
        }
        model.addAttribute("rows", rows);
        model.addAttribute("totalDonated", totalDonated);
        model.addAttribute("totalSpent", totalSpent);
        model.addAttribute("totalBalance", totalDonated.subtract(totalSpent));
        return "give/give-operation-list";
    }

    /** 특정 지자체의 지출내역 목록+등록/수정/삭제 - AS-IS give-operation/list/{locgovCode}
     *  + popup/form.jsp를 하나의 화면으로 합쳤다(팝업 대신 같은 화면 안 인라인 폼). */
    @GetMapping("/give-operation/{locgovCode}")
    public String detail(@PathVariable String locgovCode, Model model) {
        LocgovClient.LocgovInfo info = locgovClient.allLocgovs().stream()
                .filter(l -> locgovCode.equals(l.locgovCode())).findFirst().orElse(null);
        List<CtbnyOpratnClient.Row> list = ctbnyOpratnClient.listByLocgov(locgovCode);

        model.addAttribute("locgovCode", locgovCode);
        model.addAttribute("locgovFullNm", info != null ? info.upperLocgovNm() + " " + info.locgovNm() : locgovCode);
        model.addAttribute("list", list);
        model.addAttribute("bsnsPurpsCodeList", commonCodeService.labelsOf("BSNS_PURPS_CODE"));
        return "give/give-operation-detail";
    }

    @PostMapping("/give-operation/{locgovCode}")
    public String create(@PathVariable String locgovCode, @RequestParam(required = false) String bsnsPurpsCode,
                          @RequestParam String bsnsNm, @RequestParam String bsnsCn,
                          @RequestParam String expndtrDe, @RequestParam BigDecimal expndtrAmt,
                          @RequestParam(required = false) String rm,
                          @RequestParam(required = false) List<MultipartFile> files,
                          HttpSession session, Model model) {
        Manager manager = (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
        try {
            ctbnyOpratnClient.create(locgovCode, bsnsPurpsCode, bsnsNm, bsnsCn, expndtrDe, expndtrAmt, rm,
                    manager.getUserId(), files);
            return "redirect:/give-operation/" + locgovCode;
        } catch (ManagerException e) {
            return errorBack(locgovCode, e.getMessage(), model);
        }
    }

    @PostMapping("/give-operation/{locgovCode}/{registSn}/edit")
    public String update(@PathVariable String locgovCode, @PathVariable Long registSn,
                          @RequestParam(required = false) String bsnsPurpsCode,
                          @RequestParam String bsnsNm, @RequestParam String bsnsCn,
                          @RequestParam String expndtrDe, @RequestParam BigDecimal expndtrAmt,
                          @RequestParam(required = false) String rm,
                          @RequestParam(required = false) List<MultipartFile> files,
                          HttpSession session, Model model) {
        Manager manager = (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
        try {
            ctbnyOpratnClient.update(registSn, bsnsPurpsCode, bsnsNm, bsnsCn, expndtrDe, expndtrAmt, rm,
                    manager.getUserId(), files);
            return "redirect:/give-operation/" + locgovCode;
        } catch (ManagerException e) {
            return errorBack(locgovCode, e.getMessage(), model);
        }
    }

    @PostMapping("/give-operation/{locgovCode}/{registSn}/delete")
    public String delete(@PathVariable String locgovCode, @PathVariable Long registSn) {
        ctbnyOpratnClient.delete(registSn);
        return "redirect:/give-operation/" + locgovCode;
    }

    @PostMapping("/give-operation/{locgovCode}/files/{fileId}/delete")
    public String deleteFile(@PathVariable String locgovCode, @PathVariable Long fileId) {
        ctbnyOpratnClient.deleteFile(fileId);
        return "redirect:/give-operation/" + locgovCode;
    }

    private String errorBack(String locgovCode, String message, Model model) {
        model.addAttribute("errorMessage", message);
        model.addAttribute("locgovCode", locgovCode);
        model.addAttribute("list", ctbnyOpratnClient.listByLocgov(locgovCode));
        model.addAttribute("bsnsPurpsCodeList", commonCodeService.labelsOf("BSNS_PURPS_CODE"));
        LocgovClient.LocgovInfo info = locgovClient.allLocgovs().stream()
                .filter(l -> locgovCode.equals(l.locgovCode())).findFirst().orElse(null);
        model.addAttribute("locgovFullNm", info != null ? info.upperLocgovNm() + " " + info.locgovNm() : locgovCode);
        return "give/give-operation-detail";
    }

    public record Row(String locgovCode, String upperLocgovNm, String locgovNm,
                       BigDecimal donated, BigDecimal spent, BigDecimal balance) {
    }
}
